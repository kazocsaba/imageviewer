/*
 * The MIT License
 *
 * Copyright 2012 Kazó Csaba.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hu.kazocsaba.imageviewer.gui;

import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionListener;
import hu.kazocsaba.imageviewer.ImageViewer;
import hu.kazocsaba.imageviewer.ResizeStrategy;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import static org.fest.assertions.Assertions.assertThat;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static hu.kazocsaba.imageviewer.gui.GuiUtils.*;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Tests that image mouse events get fired when something other than mouse movement causes
 * the cursor to be over a different pixel.
 * @author Kazó Csaba
 */
public class SyntheticMouseEventTest {
	private static final Dimension imageSize=new Dimension(100, 100);
	private static final Dimension windowSize=new Dimension(100, 140);
	
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
				viewer=new ImageViewer(new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_BYTE_GRAY));
				Graphics2D g=viewer.getImage().createGraphics();
				g.setColor(Color.WHITE);
				g.drawLine(0, imageSize.height/2, imageSize.width, imageSize.height/2);
				g.dispose();
				viewer.setResizeStrategy(ResizeStrategy.NO_RESIZE);
				frame.add(viewer.getComponent());
				return frame;
			}
		});
		window=new FrameFixture(frame);
		window.show(windowSize);
		scroll=GuiUtils.getScrollPaneFor(viewer);
		viewport=scroll.getViewport();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
	
	final AtomicReference<Point> lastEventPixelPosition=new AtomicReference<Point>();
	
	@Test
	public void testMouseMotion() {
		
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
		
		// First, position the cursor over the viewport.
		
		Point pointInViewport=new Point(40, 30);
		
		window.robot.moveMouse(viewport, pointInViewport);
		checkMousePosition();
		
		// Now do a bunch of resizes and see if we get appropriate position updates
		resizeViewer(viewer, 3);
		checkMousePosition();
		
		resizeViewer(viewer, ResizeStrategy.RESIZE_TO_FIT);
		checkMousePosition();
		
		// Turning the status bar on will resize/reposition the image when it is set to fit.
		setStatusBarVisible(viewer, true);
		checkMousePosition();
		
		// Resize the window.
		window.component().setSize(windowSize.width+10, windowSize.height+10);
		checkMousePosition();
		
		resizeViewer(viewer, 2.2);
		checkMousePosition();
		
		// Finally, scroll programatically.
		scrollTo(scroll.getHorizontalScrollBar().getModel(), scroll.getHorizontalScrollBar().getMaximum());
		checkMousePosition();
		scrollTo(scroll.getVerticalScrollBar().getModel(), scroll.getVerticalScrollBar().getMaximum());
		checkMousePosition();
		
		viewer.removeImageMouseMotionListener(moveListener);
	}
	private void checkMousePosition() {
		// wait for all the current events to run their course
		window.robot.waitForIdle();
		
		Point pointOnViewport=viewport.getMousePosition();
		Point pointOnCanvas=SwingUtilities.convertPoint(viewport, pointOnViewport, viewport.getView());
		Point imagePixel=viewer.pointToPixel(pointOnCanvas, true);
		
		assertThat(lastEventPixelPosition.get()).overridingErrorMessage("No synthetic move event arrived").isNotNull();
		assertThat(lastEventPixelPosition.get()).isEqualTo(imagePixel);
		lastEventPixelPosition.set(null);
	}
}
