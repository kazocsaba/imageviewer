package hu.kazocsaba.imageviewer;

import java.awt.RenderingHints;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kazó Csaba
 */
public class ImageViewerUtilTest {
	@Test
	public void testSynchronizationOfCurrentParameters() {
		ImageViewer v1=new ImageViewer();
		ImageViewer v2=new ImageViewer();
		
		v1.setStatusBarVisible(true);
		v2.setStatusBarVisible(false);
		
		v1.setPixelatedZoom(true);
		v2.setPixelatedZoom(false);
		
		v1.setInterpolationType(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		v2.setInterpolationType(RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		v1.setResizeStrategy(ResizeStrategy.NO_RESIZE);
		v2.setResizeStrategy(ResizeStrategy.RESIZE_TO_FIT);
		
		v1.setZoomFactor(4.6);
		v2.setZoomFactor(1.1);
		
		ImageViewerUtil.synchronizeViewers(v1, v2);
		
		assertTrue(v2.isStatusBarVisible());
		assertTrue(v2.isPixelatedZoom());
		assertSame(RenderingHints.VALUE_INTERPOLATION_BILINEAR, v2.getInterpolationType());
		assertSame(ResizeStrategy.NO_RESIZE, v2.getResizeStrategy());
		assertEquals(4.6, v2.getZoomFactor(), 0);
	}
}
