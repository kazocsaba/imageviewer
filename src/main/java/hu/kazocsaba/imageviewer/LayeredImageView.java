package hu.kazocsaba.imageviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.OverlayLayout;
import javax.swing.Scrollable;

/**
 * A component showing an image as well as an arbitrary number of overlays.
 * @author Kazó Csaba
 */
class LayeredImageView  {
	private final ImageComponent theImage;
	private final JLayeredPane layeredPane;
	
	public LayeredImageView(ImageComponent theImage) {
		this.theImage = theImage;
		layeredPane=new ScrollableLayeredPane();
		layeredPane.setLayout(new OverlayLayout(layeredPane));
		layeredPane.add(theImage, Integer.valueOf(0));
	}
	/**
	 * Returns the component for this layered view.
	 * @return the Swing component for this view
	 */
	public JComponent getComponent() {return layeredPane;}
	
	/**
	 * Adds an overlay as the specified layer.
	 * @param overlay the overlay to add
	 * @param layer the layer to add the overlay to; higher layers are on top of lower layers;
	 * the image resides in layer 0
	 */
	public void addOverlay(Overlay overlay, int layer) {
		OverlayComponent c=new OverlayComponent(overlay, theImage);
		overlay.addOverlayComponent(c);
		layeredPane.add(c, Integer.valueOf(layer));
		layeredPane.revalidate();
		layeredPane.repaint();
	}
	/**
	 * Removes an overlay from the image viewer.
	 * @param overlay the overlay to remove
	 * @throws IllegalArgumentException if the overlay is not in the image viewer
	 */
	public void removeOverlay(Overlay overlay) {
		for (Component c: layeredPane.getComponents()) {
			if (c instanceof OverlayComponent && ((OverlayComponent)c).overlay==overlay) {
				overlay.removeOverlayComponent((OverlayComponent)c);
				layeredPane.remove(c);
				layeredPane.revalidate();
				layeredPane.repaint();
			}
		}
		throw new IllegalArgumentException("Overlay not part of this viewer");
	}
	private class ScrollableLayeredPane extends JLayeredPane implements Scrollable {

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}

		/*
		 * The getScrollableTracksViewportXxx functions below are used by
		 * javax.swing.ScrollPaneLayout to determine whether the scroll bars should
		 * be visible; so these need to be implemented.
		 */
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return theImage.getResizeStrategy()==ResizeStrategy.SHRINK_TO_FIT || theImage.getResizeStrategy()==ResizeStrategy.RESIZE_TO_FIT;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return theImage.getResizeStrategy()==ResizeStrategy.SHRINK_TO_FIT || theImage.getResizeStrategy()==ResizeStrategy.RESIZE_TO_FIT;
		}
		
		/*
		 * The getPreferredScrollableViewportSize does not seem to be used.
		 */
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			if (theImage.getResizeStrategy()==ResizeStrategy.NO_RESIZE)
				return getPreferredSize();
			else
				return ((JViewport)javax.swing.SwingUtilities.getAncestorOfClass(JViewport.class, this)).getSize();
		}
	}
}
