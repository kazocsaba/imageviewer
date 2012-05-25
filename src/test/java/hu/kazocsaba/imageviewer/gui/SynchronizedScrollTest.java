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

import hu.kazocsaba.imageviewer.ImageViewer;
import hu.kazocsaba.imageviewer.ImageViewerUtil;
import hu.kazocsaba.imageviewer.ResizeStrategy;
import static hu.kazocsaba.imageviewer.gui.GuiUtils.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.fest.assertions.Assertions;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests scrolling of synchronized viewers.
 * @author Kazó Csaba
 */
public class SynchronizedScrollTest {
	private static final Dimension windowSize=new Dimension(200, 100);
	private FrameFixture window;
	private ImageViewer viewer1, viewer2;
	
	public SynchronizedScrollTest() {
	}
	
	@Before
	public void setUp() {
		JFrame frame=GuiActionRunner.execute(new GuiQuery<JFrame>() {

			@Override
			protected JFrame executeInEDT() throws Throwable {
				JFrame frame=new JFrame("Test window");
				viewer1=new ImageViewer();
				viewer2=new ImageViewer();
				ImageViewerUtil.synchronizeViewers(viewer1, viewer2);
				frame.setLayout(new GridLayout(1, 2));
				frame.add(viewer1.getComponent());
				frame.add(viewer2.getComponent());
				return frame;
			}
		});
		window=new FrameFixture(frame);
		window.show(windowSize);
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
	
	@Test
	public void testSameSizeImages() {
		GuiActionRunner.execute(new GuiTask() {

			@Override
			protected void executeInEDT() throws Throwable {
				viewer1.setResizeStrategy(ResizeStrategy.NO_RESIZE);
				BufferedImage image=new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY);
				viewer1.setImage(image);
				viewer2.setImage(image);
				/* 
				 * Validate right now so that all the sizes are correct; otherwise layout within the viewer
				 * components will occur in the background, concurrently to our test.
				 */
				viewer1.getComponent().validate();
				viewer2.getComponent().validate();
				
				/* Verify, just to be safe. */
				Assertions.assertThat(getScrollPaneFor(viewer2).getViewport().getViewRect()).isEqualTo(getScrollPaneFor(viewer1).getViewport().getViewRect());
			}
		});
		JScrollPane scroll1=getScrollPaneFor(viewer1);
		JScrollPane scroll2=getScrollPaneFor(viewer2);
		JViewport viewport1=scroll1.getViewport();
		JViewport viewport2=scroll2.getViewport();
		BoundedRangeModel modelH1 = scroll1.getHorizontalScrollBar().getModel();
		BoundedRangeModel modelV1 = scroll1.getHorizontalScrollBar().getModel();
		BoundedRangeModel modelH2 = scroll2.getHorizontalScrollBar().getModel();
		BoundedRangeModel modelV2 = scroll2.getHorizontalScrollBar().getModel();
		
		
		resizeViewer(viewer1, 3);
		Assertions.assertThat(viewport2.getViewRect()).isEqualTo(viewport1.getViewRect());
		scrollTo(modelH1, modelH1.getMinimum());
		scrollTo(modelV1, modelV1.getMinimum());
		Assertions.assertThat(viewport2.getViewRect()).isEqualTo(viewport1.getViewRect());
		resizeViewer(viewer1, 1);
		Assertions.assertThat(viewport2.getViewRect()).isEqualTo(viewport1.getViewRect());
		scrollTo(modelH2, modelH2.getMaximum());
		scrollTo(modelV2, modelV2.getMaximum());
		Assertions.assertThat(viewport2.getViewRect()).isEqualTo(viewport1.getViewRect());
	}
	
}
