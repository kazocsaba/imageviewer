package hu.kazocsaba.imageviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Utility methods for image viewers.
 * @author Kazó Csaba
 */
public final class ImageViewerUtil {
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
				other.setStatusBarVisible(first.isStatusBarVisible());
				other.setResizeStrategy(first.getResizeStrategy());
				other.setZoomFactor(first.getZoomFactor());
				other.setPixelatedZoom(first.isPixelatedZoom());
				other.setInterpolationType(first.getInterpolationType());
				new PropertySynchronizer(first, other);
			}
		}
	}
	
	/**
	 * Synchronizes the {@link PixelInfoStatusBar}s associated with the viewers. Viewers with a different or {@code null}
	 * status bar are ignored. When this function returns, the {@code PixelInfoStatusBar}s among the viewer status bars
	 * will share the same {@code PixelModel}, and thus display the same pixel.
	 * <p>
	 * The default status bar of ImageViewer is a {@code PixelInfoStatusBar}, so this function can be used to
	 * synchronize the default status bars of viewers.
	 * @param viewers the viewers
	 */
	public static void synchronizePixelInfoStatusBars(ImageViewer... viewers) {
		PixelModel model=null;
		for (ImageViewer viewer: viewers) {
			StatusBar bar=viewer.getStatusBar();
			if (bar instanceof PixelInfoStatusBar) {
				if (model==null)
					model=((PixelInfoStatusBar)bar).getModel();
				else
					((PixelInfoStatusBar)bar).setModel(model);
			}
		}
	}
}
