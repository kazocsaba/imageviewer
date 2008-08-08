package kcsaba.image.viewer;

/**
 * Strategy for resizing an image inside a component.
 * @author Kazó Csaba
 */
/*
 * These constants are referenced in the following places:
 * - ImageComponent.getImageTransform()
 * - CustomViewportLayout.layoutComponent()
 * - ImageViewer.createPopup()
 * - LayeredImageView.ScrollableLayeredPane.getScrollableTracksViewportXxx()
 */
public enum ResizeStrategy {
	/** The image is displayed in its original size. */
	NO_RESIZE,
	/** If the image doesn't fit in the component, it is shrunk to the best fit. */
	SHRINK_TO_FIT
}
