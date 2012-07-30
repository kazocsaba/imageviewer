package hu.kazocsaba.imageviewer.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionListener;
import hu.kazocsaba.imageviewer.ImageViewer;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 *
 * @author Kazó Csaba
 */
public class AppearingViewerTest {
	private FrameFixture window;
	private ImageViewer viewer;
	private JScrollPane scroll;
	private JViewport viewport;
	
	@Before
	public void setUp() {
		JFrame frame=GuiActionRunner.execute(new GuiQuery<JFrame>() {

			@Override
			protected JFrame executeInEDT() throws Throwable {
				JFrame frame=new JFrame("Test window");
				viewer=new ImageViewer(new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY));
				return frame;
			}
		});
		window=new FrameFixture(frame);
		window.show(new Dimension(200, 200));
		scroll=GuiUtils.getScrollPaneFor(viewer);
		viewport=scroll.getViewport();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
	
	final AtomicReference<Point> lastEventPixelPosition=new AtomicReference<Point>();
	
	@Test
	public void test() {
		ImageMouseMotionListener moveListener=new ImageMouseMotionListener() {

			@Override
			public void mouseMoved(ImageMouseEvent e) {
				lastEventPixelPosition.set(new Point(e.getX(), e.getY()));
			}

			@Override
			public void mouseEntered(ImageMouseEvent e) {
			}

			@Override
			public void mouseExited(ImageMouseEvent e) {
			}

			@Override
			public void mouseDragged(ImageMouseEvent e) {
			}
		};
		viewer.addImageMouseMotionListener(moveListener);
		GuiActionRunner.execute(new GuiTask() {

			@Override
			protected void executeInEDT() throws Throwable {
				window.component().add(viewer.getComponent());
				window.component().revalidate();
			}
		});
		window.robot.waitForIdle();
		Point pointOnViewport=viewport.getMousePosition();
		Point pointOnCanvas=SwingUtilities.convertPoint(viewport, pointOnViewport, viewport.getView());
		Point imagePixel=viewer.pointToPixel(pointOnCanvas, true);
		
		assertThat(lastEventPixelPosition.get()).overridingErrorMessage("No synthetic move event arrived").isNotNull();
		assertThat(lastEventPixelPosition.get()).isEqualTo(imagePixel);
		lastEventPixelPosition.set(null);
		viewer.removeImageMouseMotionListener(moveListener);
	}
}
