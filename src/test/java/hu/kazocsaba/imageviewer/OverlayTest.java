package hu.kazocsaba.imageviewer;

import org.junit.Test;

/**
 *
 * @author Kazó Csaba
 */
public class OverlayTest {

    public OverlayTest() {
    }

	@Test
	public void testRemoveOverlay() {
		ImageViewer viewer=new ImageViewer();
		Overlay overlay=new PixelMarkerOverlay();
		viewer.addOverlay(overlay);
		viewer.removeOverlay(overlay);
	}

}