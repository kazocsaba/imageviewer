package hu.kazocsaba.imageviewer;

import java.awt.image.BufferedImage;
import org.junit.Test;

/**
 *
 * @author Kazó Csaba
 */
public class ImageViewerTest {

	@Test
	public void setParametersBeforeShown() {
		ImageViewer viewer=new ImageViewer(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB));
		viewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
		viewer.setZoomFactor(3.2);
		viewer.setResizeStrategy(ResizeStrategy.RESIZE_TO_FIT);
	}
}