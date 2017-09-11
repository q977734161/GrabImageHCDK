package com.wangrupeng.main;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.wangrupeng.api.HCNetSDK;
import com.wangrupeng.api.PlayCtrl;
import com.wangrupeng.model.DecCBFun;
import com.wangrupeng.util.Time;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by WangRupeng on 2017/8/17 0017.
 */
public class Client{
	static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	static PlayCtrl playControl = PlayCtrl.INSTANCE;

	private HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息
	private HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg;//IP参数
	private HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;//用户参数

	private boolean bRealPlay = false;//是否在预览.

	private String m_sDeviceIP = "192.168.1.9";//已登录设备的IP地址
	private int port = 37777;
	private String userName = "admin";
	private String password = "iec123456";

	private NativeLong lUserID;//用户句柄
	private NativeLong lPreviewHandle;//预览句柄
	private NativeLongByReference m_lPort;//回调预览时播放库端口指针

	private FRealDataCallBack fRealDataCallBack;//预览回调函数实现
	private DecCBFun decCBFunCallBack;

	private int m_iTreeNodeNum;//通道树节点数目
	private DefaultMutableTreeNode m_DeviceRoot;//通道树根节点
	private int channel = -1;

	private boolean running = false;

	/*************************************************
	 函数:      主类构造函数
	 函数描述:	初始化成员
	 *************************************************/
	public Client() {
		lUserID = new NativeLong(-1);
		lPreviewHandle = new NativeLong(-1);
		m_lPort = new NativeLongByReference(new NativeLong(-1));
		fRealDataCallBack = new FRealDataCallBack();
		decCBFunCallBack = new DecCBFun();
		m_iTreeNodeNum = 0;
	}

	public boolean login() {
		if (lUserID.longValue() > -1) {
			//先注销
			hCNetSDK.NET_DVR_Logout_V30(lUserID);
			lUserID = new NativeLong(-1);
			//m_iTreeNodeNum = 0;
			//m_DeviceRoot.removeAllChildren();
		}
		m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
		lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP, (short) this.port, this.userName, this.password, m_strDeviceInfo);
		long userID = lUserID.longValue();
		if (userID == -1) {
			m_sDeviceIP = "";//登录未成功,IP置为空
			System.out.println("注册失败");
			return false;
		} else {
			//this.lUserID = userId;
			getChannelNumber();
			return true;
		}
	}

	public void getChannelNumber() {
		IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
		boolean bRet = false;
		m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
		m_strIpparaCfg.write();

		Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
		bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_IPPARACFG, new NativeLong(0), lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
		m_strIpparaCfg.read();

		if (!bRet) {
			//设备不支持,则表示没有IP通道
			channel = m_strDeviceInfo.byStartChan;
		} else {

		}
	}


	public void realPlay() {
		if (this.lUserID.intValue() == -1) {
			System.out.println("请先注册");
			return;
		}

		if (bRealPlay == false) {
			//获取通道号
			//int iChannelNum = getChannelNumber();//通道号
			int iChannelNum = this.channel;
			if (iChannelNum == -1) {
				System.out.println("请选择要预览的通道");
				return;
			}
			m_strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
			m_strClientInfo.lChannel = new NativeLong(iChannelNum);
			m_strClientInfo.hPlayWnd = null;
			lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID, m_strClientInfo, fRealDataCallBack, null, true);

			long previewSucValue = lPreviewHandle.longValue();
			//预览失败时:
			if (previewSucValue == -1) {
				System.out.println("预览失败");
				return;
			} else {
				try {
					Time.sleep(1000 * 1200);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {
		//预览回调
		public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
			//W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(panelRealplay));
			//System.out.println("FRealDataCallBack");
			switch (dwDataType) {
				case HCNetSDK.NET_DVR_SYSHEAD: //系统头
					//DecCBFun decCBFun = new DecCBFun();
					if (!playControl.PlayM4_GetPort(m_lPort)) {//获取播放库未使用的通道号
						break;
					}

					if (dwBufSize > 0) {
						//设置实时流播放模式
						if (!playControl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_REALTIME)) {
							break;
						}
						//打开流接口
						if (!playControl.PlayM4_OpenStream(m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) {
							break;
						}

						NativeLong v = m_lPort.getValue();
						long n = v.longValue();
						if (!playControl.PlayM4_SetDecCallBack(m_lPort.getValue(), decCBFunCallBack)) {
							break;
						}
						if (!playControl.PlayM4_Play(m_lPort.getValue(), null)) //播放开始
						{
							break;
						}
					}
				case HCNetSDK.NET_DVR_STREAMDATA:
					//码流数据
					if ((dwBufSize > 0) && (m_lPort.getValue().intValue() != -1)) {
						//System.out.println(dwBufSize);
						//byte b = pBuffer.getValue();
						//输入流数据
						if (!playControl.PlayM4_InputData(m_lPort.getValue(), pBuffer, dwBufSize)) {
							break;
						}
					}
			}
		}
	}


	public static void main(String[] args) {
		boolean initSuc = hCNetSDK.NET_DVR_Init();
		if (initSuc != true) {
			System.out.println("初始化失败");
			return;
		}
		Client client = new Client();
		client.login();
		client.realPlay();

	}
}
