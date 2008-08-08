package kcsaba.image.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Utility methods for image viewers.
 * @author Kazó Csaba
 */
public class ImageViewerUtil {
	/** Private constructor. */
	private ImageViewerUtil() {}
	/**
	 * A property change listener that synchronizes the display-related properties of
	 * two image viewers.
	 */
	private static class PropertySynchronizer implements PropertyChangeListener {
		private final ImageViewer v1,v2;

		public PropertySynchronizer(ImageViewer v1, ImageViewer v2) {
			this.v1 = v1;
			this.v2 = v2;
			v1.addPropertyChangeListener(this);
			v2.addPropertyChangeListener(this);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			ImageViewer source=(ImageViewer)evt.getSource();
			ImageViewer target=source==v1 ? v2 : v1;
			if ("statusBarVisible".equals(evt.getPropertyName())) {
				target.setStatusBarVisible(source.isStatusBarVisible());
			} else if ("resizeStrategy".equals(evt.getPropertyName())) {
				target.setResizeStrategy(source.getResizeStrategy());
			}
		}
		
	}
	/**
	 * Synchronizes the view state of two image viewers with respect to scroll position
	 * and resize strategy, and other properties affecting display. For this to work
	 * correctly, the two viewers should always have the same size.
	 * @param v1 the first viewer
	 * @param v2 the second viewer
	 */
	public static void synchronizeViewers(ImageViewer v1, ImageViewer v2) {
		if (v1==v2) return;
		v1.getScrollPane().getHorizontalScrollBar().setModel(v2.getScrollPane().getHorizontalScrollBar().getModel());
		v1.getScrollPane().getVerticalScrollBar().setModel(v2.getScrollPane().getVerticalScrollBar().getModel());
		v1.trackSizeIfEmpty(v2);
		new PropertySynchronizer(v1, v2);
	}
}
