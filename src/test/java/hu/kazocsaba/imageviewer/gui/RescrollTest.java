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
import hu.kazocsaba.imageviewer.ResizeStrategy;
import static hu.kazocsaba.imageviewer.gui.GuiUtils.*;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JViewport;
import org.fest.assertions.Assertions;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JScrollPaneFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests that the viewer properly positions the viewport after an image resize.
 * @author Kazó Csaba
 */
@RunWith(Parameterized.class)
public class RescrollTest {
	private final Dimension windowSize, imageSize;
	private FrameFixture window;
	private ImageViewer viewer;

	@Parameterized.Parameters
	public static Collection<Object[]> sizes() {
		List<Dimension> windowSizes=Arrays.asList(new Dimension(300, 300),new Dimension(301,301));
		List<Dimension> imageSizes=Arrays.asList(new Dimension(500, 200),new Dimension(501,201));
		
		Collection<Object[]> allCombinations=new ArrayList<Object[]>(windowSizes.size()*imageSizes.size());
		for (Dimension imageSize: imageSizes)
			for (Dimension windowSize: windowSizes)
				allCombinations.add(new Object[]{windowSize, imageSize});
		return allCombinations;
	}
	
	public RescrollTest(Dimension windowSize, Dimension imageSize) {
		this.windowSize = windowSize;
		this.imageSize = imageSize;
	}
	
	@Before
	public void setUp() {
		JFrame frame=GuiActionRunner.execute(new GuiQuery<JFrame>() {

			@Override
			protected JFrame executeInEDT() throws Throwable {
				JFrame frame=new JFrame("Test window");
				viewer=new ImageViewer(new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_BYTE_GRAY));
				frame.add(viewer.getComponent());
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
	/**
	 * Checks that the viewport is scrolled properly to bring the specified image position as close to the center as possible.
	 * @param expectedCenter
	 * @return the actual image point at the viewport center
	 */
	private Point2D checkCenter(Point2D expectedCenter) throws NoninvertibleTransformException {
		JViewport viewport=window.scrollPane().component().getViewport();
		AffineTransform imageTransform = viewer.getImageTransform();
		
		Point2D actualCenter=imageTransform.inverseTransform(new Point2D.Double(viewport.getViewRect().getCenterX(),viewport.getViewRect().getCenterY()), null);
		if (expectedCenter!=null) {
			BoundedRangeModel horizontalModel = window.scrollPane().horizontalScrollBar().component().getModel();
			BoundedRangeModel verticalModel = window.scrollPane().verticalScrollBar().component().getModel();
			
			/*
			 * We check if the scroll pane is scrolled ideally if the goal is to move the specified image point to the viewport center.
			 * We verify this by testing if the error of the center increases when we scroll one unit in either direction.
			 */
			double horizontalError=Math.abs(expectedCenter.getX()-actualCenter.getX());
			double verticalError=Math.abs(expectedCenter.getY()-actualCenter.getY());
			
			// check horizontal scroll
			{
				int horizontalPos=horizontalModel.getValue();
				if (horizontalPos>horizontalModel.getMinimum()) {
					scrollTo(horizontalModel, horizontalPos-1);
					Point2D alternativeCenter=imageTransform.inverseTransform(new Point2D.Double(viewport.getViewRect().getCenterX(),viewport.getViewRect().getCenterY()), null);
					Assertions.assertThat(Math.abs(expectedCenter.getX()-alternativeCenter.getX())).as("previous possible center x error")
							.overridingErrorMessage(String.format("Point (%s,%s) should be centered; (%s,%s) is at center, but scrolling %s would yield better center (%s,%s)",expectedCenter.getX(),expectedCenter.getY(),actualCenter.getX(),actualCenter.getY(),"left",alternativeCenter.getX(),alternativeCenter.getY()))
							.isGreaterThanOrEqualTo(horizontalError);
				}
				if (horizontalPos+horizontalModel.getExtent()<horizontalModel.getMaximum()) {
					scrollTo(horizontalModel, horizontalPos+1);
					Point2D alternativeCenter=imageTransform.inverseTransform(new Point2D.Double(viewport.getViewRect().getCenterX(),viewport.getViewRect().getCenterY()), null);
					Assertions.assertThat(Math.abs(expectedCenter.getX()-alternativeCenter.getX())).as("next possible center x error")
							.overridingErrorMessage(String.format("Point (%s,%s) should be centered; (%s,%s) is at center, but scrolling %s would yield better center (%s,%s)",expectedCenter.getX(),expectedCenter.getY(),actualCenter.getX(),actualCenter.getY(),"right",alternativeCenter.getX(),alternativeCenter.getY()))
							.isGreaterThanOrEqualTo(horizontalError);
				}
				// reset the scroll
				scrollTo(horizontalModel, horizontalPos);
			}
			
			// check vertical scroll
			{
				int verticalPos=verticalModel.getValue();
				if (verticalPos>verticalModel.getMinimum()) {
					scrollTo(verticalModel, verticalPos-1);
					Point2D alternativeCenter=imageTransform.inverseTransform(new Point2D.Double(viewport.getViewRect().getCenterX(),viewport.getViewRect().getCenterY()), null);
					Assertions.assertThat(Math.abs(expectedCenter.getY()-alternativeCenter.getY())).as("previous possible center y error")
							.overridingErrorMessage(String.format("Point (%s,%s) should be centered; (%s,%s) is at center, but scrolling %s would yield better center (%s,%s)",expectedCenter.getX(),expectedCenter.getY(),actualCenter.getX(),actualCenter.getY(),"up",alternativeCenter.getX(),alternativeCenter.getY()))
							.isGreaterThanOrEqualTo(verticalError);
				}
				if (verticalPos+verticalModel.getExtent()<verticalModel.getMaximum()) {
					scrollTo(verticalModel, verticalPos+1);
					Point2D alternativeCenter=imageTransform.inverseTransform(new Point2D.Double(viewport.getViewRect().getCenterX(),viewport.getViewRect().getCenterY()), null);
					Assertions.assertThat(Math.abs(expectedCenter.getY()-alternativeCenter.getY())).as("next possible center y error")
							.overridingErrorMessage(String.format("Point (%s,%s) should be centered; (%s,%s) is at center, but scrolling %s would yield better center (%s,%s)",expectedCenter.getX(),expectedCenter.getY(),actualCenter.getX(),actualCenter.getY(),"down",alternativeCenter.getX(),alternativeCenter.getY()))
							.isGreaterThanOrEqualTo(verticalError);
				}
				// reset the scroll
				scrollTo(verticalModel, verticalPos);
			}
		}
		return actualCenter;
	}
	private void resizeViewer(final ResizeStrategy strategy) {
		resizeViewer(viewer, strategy);
	}
	private void resizeViewer(final double zoomFactor) {
		resizeViewer(viewer, zoomFactor);
	}
	
	@Test
	public void testRescroll() throws NoninvertibleTransformException {
		JScrollPaneFixture scrollPane = window.scrollPane();
		BoundedRangeModel horizontalModel = scrollPane.component().getHorizontalScrollBar().getModel();
		BoundedRangeModel verticalModel = scrollPane.component().getVerticalScrollBar().getModel();
		
		Point2D imageCenter=new Point2D.Double(viewer.getImage().getWidth()/2.0, viewer.getImage().getHeight()/2.0);
		imageCenter=checkCenter(imageCenter);
		
		resizeViewer(ResizeStrategy.NO_RESIZE); imageCenter=checkCenter(imageCenter);
		
		resizeViewer(3); imageCenter=checkCenter(imageCenter);
		
		// scroll to the top corner
		scrollTo(horizontalModel,horizontalModel.getMinimum());
		scrollTo(verticalModel,verticalModel.getMinimum());
		
		imageCenter=checkCenter(null);
		
		resizeViewer(5); imageCenter=checkCenter(imageCenter);
		resizeViewer(3); imageCenter=checkCenter(imageCenter);
		resizeViewer(2); imageCenter=checkCenter(imageCenter);
		resizeViewer(1); imageCenter=checkCenter(imageCenter);
		resizeViewer(.5); imageCenter=checkCenter(imageCenter);
		
		// try the same with the bottom right corner
		
		resizeViewer(3); checkCenter(imageCenter);
		
		scrollTo(horizontalModel,horizontalModel.getMaximum());
		scrollTo(verticalModel,verticalModel.getMaximum());

		imageCenter=checkCenter(null);
		
		resizeViewer(5); imageCenter=checkCenter(imageCenter);
		resizeViewer(3); imageCenter=checkCenter(imageCenter);
		resizeViewer(2); imageCenter=checkCenter(imageCenter);
		resizeViewer(1); imageCenter=checkCenter(imageCenter);
		resizeViewer(.5); imageCenter=checkCenter(imageCenter);
	}
}
