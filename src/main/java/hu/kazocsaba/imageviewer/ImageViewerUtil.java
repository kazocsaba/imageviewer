package hu.kazocsaba.imageviewer;

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
			} else if ("zoomFactor".equals(evt.getPropertyName())) {
				target.setZoomFactor(source.getZoomFactor());
			} else if ("pixelatedZoom".equals(evt.getPropertyName())) {
				target.setPixelatedZoom(source.isPixelatedZoom());
			} else if ("interpolationType".equals(evt.getPropertyName())) {
				target.setInterpolationType(source.getInterpolationType());
			}
		}
		
	}
	/**
	 * Synchronizes the view state of multiple image viewers with respect to scroll position
	 * and resize strategy, and other properties affecting display. For this to work
	 * correctly, the viewers should always have the same size.
	 * @param first the first viewer
	 * @param others the other viewers
	 */
	public static void synchronizeViewers(ImageViewer first, ImageViewer... others) {
		for (ImageViewer other: others) {
			if (other!=first) {
				other.getScrollPane().getHorizontalScrollBar().setModel(first.getScrollPane().getHorizontalScrollBar().getModel());
				other.getScrollPane().getVerticalScrollBar().setModel(first.getScrollPane().getVerticalScrollBar().getModel());
				other.trackSizeIfEmpty(first);
				new PropertySynchronizer(first, other);
			}
		}
	}
}
