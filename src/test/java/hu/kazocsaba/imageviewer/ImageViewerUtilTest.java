package hu.kazocsaba.imageviewer;

import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

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
	
	@Test
	public void testSynchronizationFromCollection() {
		List<ImageViewer> viewers=new ArrayList<ImageViewer>(5);
		for (int i=0; i<5; i++) {
			ImageViewer viewer=new ImageViewer();
			viewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
			viewers.add(viewer);
		}
		ImageViewerUtil.synchronizeViewers(viewers);
		
		for (int i=0; i<5; i++) {
			double expectedZoom=i+.5;
			// set the zoom on the i-th viewer
			viewers.get(i).setZoomFactor(expectedZoom);
			
			// check the others
			for (ImageViewer viewer: viewers)
				assertEquals(expectedZoom, viewer.getZoomFactor(), 0);
		}
	}
}
