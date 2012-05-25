package hu.kazocsaba.imageviewer.gui;

import hu.kazocsaba.imageviewer.ImageViewer;
import hu.kazocsaba.imageviewer.ResizeStrategy;
import java.awt.Component;
import java.awt.Container;
import javax.swing.BoundedRangeModel;
import javax.swing.JScrollPane;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.junit.Assert;

/**
 * Utility functions for GUI tests.
 * @author Kazó Csaba
 */
class GuiUtils {

	protected static void scrollTo(final BoundedRangeModel model, final int value) {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				model.setValue(value);
			}
		});
	}

	protected static void resizeViewer(final ImageViewer viewer, final ResizeStrategy strategy) {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				viewer.setResizeStrategy(strategy);
			}
		});
	}

	protected static void resizeViewer(final ImageViewer viewer, final double zoomFactor) {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				viewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
				viewer.setZoomFactor(zoomFactor);
			}
		});
	}
	
	protected static void setStatusBarVisible(final ImageViewer viewer, final boolean visible) {
		GuiActionRunner.execute(new GuiTask() {

			@Override
			protected void executeInEDT() throws Throwable {
				viewer.setStatusBarVisible(visible);
			}
		});
	}

	protected static JScrollPane getScrollPaneFor(ImageViewer viewer) {
		JScrollPane scrollPane=findScrollPane(viewer.getComponent());
		Assert.assertNotNull("Cannot find image viewer scroll pane", scrollPane);
		return scrollPane;
	}

	private static JScrollPane findScrollPane(Component component) {
		if (component instanceof JScrollPane)
			return (JScrollPane)component;
		
		if (component instanceof Container) { 
			Container c=(Container)component;
			for (int i=0; i<c.getComponentCount(); i++) {
				JScrollPane result=findScrollPane(c.getComponent(i));
				if (result!=null) return result;
			}
		}
		return null;
	}
}
