package hu.kazocsaba.imageviewer.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import hu.kazocsaba.imageviewer.ImageViewer;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kazó Csaba
 */
public class PopupTest {
	private final Dimension windowSize=new Dimension(100, 100);
	private final Dimension imageSize=new Dimension(100, 100);
	private FrameFixture window;
	private ImageViewer viewer;
	
	@After
	public void tearDown() {
		if (window!=null) {
			window.cleanUp();
			window=null;
		}
	}
	
	@Test
	public void testDefault() {
		JFrame frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {

			@Override
			protected JFrame executeInEDT() throws Throwable {
				JFrame frame = new JFrame("Test window");
				viewer = new ImageViewer(new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_BYTE_GRAY));
				frame.add(viewer.getComponent());
				return frame;
			}
		});
		window = new FrameFixture(frame);
		window.show(windowSize);
		
		JViewport viewport=GuiUtils.getScrollPaneFor(viewer).getViewport();
		window.showPopupMenuAt(SwingUtilities.convertPoint(viewport, viewport.getLocation(), window.component()));
	}
	@Test
	public void testDisable() {
		JFrame frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {

			@Override
			protected JFrame executeInEDT() throws Throwable {
				JFrame frame = new JFrame("Test window");
				viewer = new ImageViewer(new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_BYTE_GRAY), false);
				frame.add(viewer.getComponent());
				return frame;
			}
		});
		window = new FrameFixture(frame);
		window.show(windowSize);
		
		JViewport viewport=GuiUtils.getScrollPaneFor(viewer).getViewport();
		try {
			window.showPopupMenuAt(SwingUtilities.convertPoint(viewport, viewport.getLocation(), window.component()));
			fail("Popup shown");
		} catch (ComponentLookupException e) {
			
		}
	}
}