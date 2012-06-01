package hu.kazocsaba.imageviewer;

/**
 * Utility methods for image viewers.
 * @author Kazó Csaba
 */
public final class ImageViewerUtil {
	/** Private constructor. */
	private ImageViewerUtil() {}
	/**
	 * Synchronizes the view state of multiple image viewers with respect to scroll position
	 * and resize strategy, and other properties affecting display.
	 * @param first the first viewer
	 * @param others the other viewers
	 */
	public static void synchronizeViewers(ImageViewer first, ImageViewer... others) {
		Synchronizer mainSynchronizer=first.getSynchronizer();
		for (ImageViewer other: others) {
			mainSynchronizer.add(other);
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
