//import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.awt.SequenceEncoder;
import org.jcodec.common.model.Picture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangRupeng on 2017/8/19 0019.
 */
public class SequenceEncoderTest {
	List<BufferedImage> images = new ArrayList<BufferedImage>();
	//File file = new File();

	public SequenceEncoderTest() {
		String path = "E:\\HUST\\HUST-BitData\\RealTimeDetectFace\\GrabImageHCDK\\data\\img\\";
		File file = new File(path);
		try {
			for (File f : file.listFiles()) {
				BufferedImage image = ImageIO.read(f);
				this.images.add(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void encodeToVideoFile() {
		try {
			SequenceEncoder enc = new SequenceEncoder(new File("E:\\HUST\\HUST-BitData\\RealTimeDetectFace\\GrabImageHCDK\\data\\temp.h264"));
			// GOP size will be supported in 0.2
			// enc.getEncoder().setKeyInterval(25);
			for (BufferedImage image : images) {
				//BufferedImage image = ... // Obtain an image to encode
				enc.encodeImage(image);
				//Picture picture = new Picture();
				//enc.encodeNativeFrame();
			}
			enc.finish();
		}catch (Exception e) {

		}
	}
}
