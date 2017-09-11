package com.wangrupeng.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 * Created by WangRupeng on 2017/8/16 0016.
 */
public class ImageUtils {
	public static boolean saveToFile(BufferedImage bufferedImage, String path, String fileName, String imageType) throws IOException {
		if (bufferedImage == null || path == null) {
			return false;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		File file = new File(path + fileName + "." + imageType);
		ImageIO.write(bufferedImage, imageType, file);
		return true;
	}

	/**
	 *
	 * @param bufferedImage
	 * @return
	 * @author WangRupeng
	 */
	public static byte[] decodeToPixels(BufferedImage bufferedImage)
	{
		if(bufferedImage == null)
			return null;
		return ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
	}
}
