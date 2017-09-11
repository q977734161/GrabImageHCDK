package com.wangrupeng.api;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.wangrupeng.model.DecCBFun;

/**
 * Created by WangRupeng on 2017/8/17 0017.
 */
public interface PlayCtrl extends StdCallLibrary {
	PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("PlayCtrl",
			PlayCtrl.class);

	public static final int STREAME_REALTIME = 0;
	public static final int STREAME_FILE = 1;

	boolean PlayM4_GetPort(NativeLongByReference nPort);
	boolean PlayM4_OpenStream(NativeLong nPort, ByteByReference pFileHeadBuf, int nSize, int nBufPoolSize);
	boolean PlayM4_InputData(NativeLong nPort, ByteByReference pBuf, int nSize);
	boolean PlayM4_CloseStream(NativeLong nPort);
	boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);
	boolean PlayM4_Play(NativeLong nPort, W32API.HWND hWnd);
	boolean PlayM4_Stop(NativeLong nPort);
	boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);

	boolean  PlayM4_SetDecCallBack(NativeLong nPort ,DecCBFun decCBFunCallBack);

	public static interface DecCBFunCallBack extends StdCallCallback {
		public void invoke(NativeLong nPort, ByteByReference pBuf, NativeLong nSize, FRAME_INFO pFrameInfo, NativeLong nReserved1, NativeLong nReserved2);
	}

	public static class FRAME_INFO extends Structure {
		public NativeLong nWidth;
		public NativeLong nHeight;
		public NativeLong nStamp;
		public NativeLong nType;
		public NativeLong nFrameRate;
		public int dwFrameNum;
	}
}
