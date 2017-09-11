package com.wangrupeng.model;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.wangrupeng.api.PlayCtrl;
import com.wangrupeng.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileOutputStream;

/**
 * Created by WangRupeng on 2017/8/17 0017.
 */
public class DecCBFun implements PlayCtrl.DecCBFunCallBack {

	@Override
	public void invoke(NativeLong nPort, ByteByReference pBuf, NativeLong nSize, PlayCtrl.FRAME_INFO pFrameInfo, NativeLong nReserved1, NativeLong nReserved2) {

		//byte b = pBuf.getValue();

		//System.out.println("DecCBFun");
		int width = pFrameInfo.nWidth.intValue();
		int height = pFrameInfo.nHeight.intValue();
		Pointer pointer = pBuf.getPointer();
		byte[] bytes = pointer.getByteArray(0, nSize.intValue());
		byte[] nv21Bytes = YV12toNV21(bytes, nSize.intValue(), width, height);
		System.out.println("YV12 size : " + nv21Bytes.length);
		BufferedImage bufferedImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_BGR);
		int[] array = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
		int[] pixels = yuv2rgb(nv21Bytes, width, height);
		System.out.println("RGB size : " + pixels.length);
		System.out.println("array size :" + array.length);
		System.arraycopy(pixels, 0, array, 0, array.length);

		BufferedImage bufferedImage1 = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		bufferedImage1.getGraphics().drawImage(bufferedImage, 0, 0, null);
		byte[] bytes1 = ImageUtils.decodeToPixels(bufferedImage1);

		try {
			//ImageUtils.saveToFile(bufferedImage, "E:\\HUST\\HUST-BitData\\RealTimeDetectFace\\GrabImageHCDK\\data\\", System.currentTimeMillis() + "", "jpg");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] YV12toNV21(final byte[] input, final int nSize, final int width, final int height) {

		byte[] output = new byte[nSize];
		final int size = width * height;
		final int quarter = size / 4;
		final int vPosition = size; // This is where V starts
		final int uPosition = size + quarter; // This is where U starts

		System.arraycopy(input, 0, output, 0, size); // Y is same

		for (int i = 0; i < quarter; i++) {
			output[size + i*2 ] = input[vPosition + i]; // For NV21, V first
			output[size + i*2 + 1] = input[uPosition + i]; // For Nv21, U second
		}
		return output;
	}

	public static int[] yuv2rgb(byte[] yuv, int width, int height) {
		int total = width * height;
		int[] rgb = new int[total];
		//byte[] RGB = new byte[total];
		int Y, Cb = 0, Cr = 0, index = 0;
		int R, G, B;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Y = yuv[y * width + x];
				if (Y < 0) Y += 255;

				if ((x & 1) == 0) {
					Cr = yuv[(y >> 1) * (width) + x + total];
					Cb = yuv[(y >> 1) * (width) + x + total + 1];

					if (Cb < 0) Cb += 127; else Cb -= 128;
					if (Cr < 0) Cr += 127; else Cr -= 128;
				}

				R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
				G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
				B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);

				// Approximation
//				R = (int) (Y + 1.40200 * Cr);
//			    G = (int) (Y - 0.34414 * Cb - 0.71414 * Cr);
//				B = (int) (Y + 1.77200 * Cb);

				if (R < 0) R = 0; else if (R > 255) R = 255;
				if (G < 0) G = 0; else if (G > 255) G = 255;
				if (B < 0) B = 0; else if (B > 255) B = 255;

				rgb[index++] = 0xff000000 + (B << 16) + (G << 8) + R;
			}
		}
/*
        for (int i = 0;i < total; i++) {
            RGB[i] = (byte)rgb[i];
        }*/

		return rgb;
	}
}
