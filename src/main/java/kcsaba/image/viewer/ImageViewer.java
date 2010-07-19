package kcsaba.image.viewer;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * A general purpose image viewer component.
 * <p>
 * The Swing component that can be added to the GUI is obtained by calling
 * {@link #getComponent()}.
 * <p>
 * ImageViewer supports {@link #setStatusBar(StatusBar) status bars}: arbitrary
 * components that can be added to the viewer and are displayed below the image.
 * <p>
 * {@link #addOverlay Overlays} can also be added to the viewer; for details, see
 * the the documentation of the {@link Overlay} class.
 * @see StatusBar
 * @see Overlay
 * @author Kazó Csaba
 */
public class ImageViewer {
	private final LayeredImageView view;
	private ImageComponent theImage;
	private final JScrollPane scroller;
	private JPanel panel;
	private StatusBar statusBar;
	private boolean statusBarVisible=false;
	private PropertyChangeSupport propertyChangeSupport;
	private JPopupMenu popup;

	private MouseListener contextMenuListener = new MouseAdapter() {
		private void showPopup(MouseEvent e) {
			e.consume();
			Point p = panel.getPopupLocation(e);
			if (p == null) {
				p = e.getPoint();
			}
			popup.show(e.getComponent(), p.x, p.y);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}
	};
	/**
	 * Creates a new image viewer.
	 */
	public ImageViewer() {
		this(null);
	}
	/**
	 * Creates a new image viewer displaying the specified image.
	 * @param image the image to display; if <code>null</code> then no image is displayed
	 * @see #setImage(BufferedImage)
	 */
	public ImageViewer(BufferedImage image) {
		propertyChangeSupport=new PropertyChangeSupport(this);
		panel=new JPanel(new BorderLayout());
		theImage=new ImageComponent(this, propertyChangeSupport);
		view=new LayeredImageView(theImage, propertyChangeSupport);
		theImage.setImage(image);
		scroller=new JScrollPane(view.getComponent()) {

			@Override
			protected JViewport createViewport() {
				return new JViewport() {

					@Override
					protected LayoutManager createLayoutManager() {
						return new CustomViewportLayout(ImageViewer.this);
					}
					
				};
			}
			
			@Override
			public boolean isValidateRoot() {
				return false;
			}
			
		};
		
		panel.add(scroller, BorderLayout.CENTER);
		
		setStatusBar(new DefaultStatusBar());
		
		popup=createPopup(this);
		theImage.addMouseListener(contextMenuListener);
	}
	private static JPopupMenu createPopup(final ImageViewer imageViewer) {
		JPopupMenu popup = new JPopupMenu();
		final JCheckBoxMenuItem toggleStatusBarItem = new JCheckBoxMenuItem("Status bar");
		toggleStatusBarItem.setState(imageViewer.isStatusBarVisible());
		imageViewer.addPropertyChangeListener("statusBarVisible", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				toggleStatusBarItem.setState(imageViewer.isStatusBarVisible());
			}
		});
		toggleStatusBarItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setStatusBarVisible(!imageViewer.isStatusBarVisible());
			}
		});
		popup.add(toggleStatusBarItem);
		
		JMenu zoomMenu = new JMenu("Zoom");
		final JRadioButtonMenuItem[] zoomButtons = new JRadioButtonMenuItem[3];
		zoomButtons[0] = new JRadioButtonMenuItem("Original size", imageViewer.getResizeStrategy()==ResizeStrategy.NO_RESIZE);
		zoomButtons[0].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.NO_RESIZE);
			}
		});
		zoomButtons[1] = new JRadioButtonMenuItem("Shrink to fit", imageViewer.getResizeStrategy()==ResizeStrategy.SHRINK_TO_FIT);
		zoomButtons[1].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.SHRINK_TO_FIT);
			}
		});
		zoomButtons[2] = new JRadioButtonMenuItem("Resize to fit", imageViewer.getResizeStrategy()==ResizeStrategy.RESIZE_TO_FIT);
		zoomButtons[2].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.RESIZE_TO_FIT);
			}
		});
		ButtonGroup group = new ButtonGroup();
		for (JRadioButtonMenuItem i : zoomButtons) {
			zoomMenu.add(i);
			group.add(i);
		}
		imageViewer.addPropertyChangeListener("resizeStrategy", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				zoomButtons[((ResizeStrategy)evt.getNewValue()).ordinal()].setSelected(true);
			}
		});
		popup.add(zoomMenu);
		return popup;
	}
	/**
	 * Sets the status bar component for this image viewer. The new status bar is made
	 * visible only if the statusBarVisible property is true. If <code>statusBar</code> is
	 * <code>null</code> this method removes any existing status bar.
	 * @param statusBar the new status bar component to set
	 * @throws IllegalArgumentException if the status bar has already been added to a
	 * different image viewer
	 */
	public void setStatusBar(StatusBar statusBar) {
		if (statusBar==this.statusBar) return;
		if (statusBar.getImageViewer()!=null)
			throw new IllegalArgumentException("Status bar already added to an image viewer");
		StatusBar oldStatusBar=this.statusBar;
		if (oldStatusBar!=null) {
			panel.remove(oldStatusBar.getComponent());
			oldStatusBar.setImageViewer(null);
		}
		this.statusBar=statusBar;
		if (this.statusBar!=null) {
			this.statusBar.setImageViewer(this);
			panel.add(this.statusBar.getComponent(), BorderLayout.SOUTH);
		}
		this.statusBar.getComponent().setVisible(statusBarVisible);
		panel.revalidate();
		panel.repaint();
		propertyChangeSupport.firePropertyChange("statusBar", oldStatusBar, statusBar);
	}
	/**
	 * Sets whether the status bar is visible.
	 * @param statusBarVisible true, if the status bar should be visible; false otherwise
	 */
	public void setStatusBarVisible(boolean statusBarVisible) {
		if (this.statusBarVisible == statusBarVisible) return;
		if (statusBar!=null)
			statusBar.getComponent().setVisible(statusBarVisible);
		boolean prev = this.statusBarVisible;
		this.statusBarVisible = statusBarVisible;
		propertyChangeSupport.firePropertyChange("statusBarVisible", prev, statusBarVisible);
	}
	/**
	 * Returns whether the status bar is set to be visible.
	 * @return the statusBarVisible property
	 */
	public boolean isStatusBarVisible() {
		return statusBarVisible;
	}
	/**
	 * Returns the image viewer component that can be displayed.
	 * @return the image viewer component
	 */
	public JComponent getComponent() {
		return panel;
	}
	/**
	 * Sets the image displayed by the viewer.
	 * @param image the new image to display; if <code>null</code> then no image is displayed
	 */
	public void setImage(BufferedImage image) {
		theImage.setImage(image);
	}
	/**
	 * Returns the currently displayed image.
	 * @return the current image, or <code>null</code> if no image is displayed
	 */
	public BufferedImage getImage() {
		return theImage.getImage();
	}
	/**
	 * Sets the resize strategy this viewer should use.
	 * @param resizeStrategy the new resize strategy
	 */
	public void setResizeStrategy(ResizeStrategy resizeStrategy) {
		theImage.setResizeStrategy(resizeStrategy);
	}
	/**
	 * Returns the current resize strategy.
	 * @return the current resize strategy
	 */
	public ResizeStrategy getResizeStrategy() {
		return theImage.getResizeStrategy();
	}
	
	/**
	 * Returns the transformation that is applied to the image. Most commonly the
	 * transformation is the concatenation of a uniform scale and a translation.
	 * <p>
	 * The <code>AffineTransform</code>
	 * instance returned by this method should not be modified.
	 * @return the transformation applied to the image before painting
	 */
	public AffineTransform getImageTransform() {
		return theImage.getImageTransform();
	}
	
	/**
	 * Adds an overlay as the specified layer.
	 * @param overlay the overlay to add
	 * @param layer the layer to add the overlay to; higher layers are on top of lower layers;
	 * the image resides in layer 0
	 * @throws IllegalArgumentException if the overlay is already added to a viewer.
	 */
	public void addOverlay(Overlay overlay, int layer) {
		view.addOverlay(overlay, layer);
	}
	/**
	 * Removes an overlay from the image viewer.
	 * @param overlay the overlay to remove
	 * @throws IllegalArgumentException if the overlay is not in the image viewer
	 */
	public void removeOverlay(Overlay overlay) {
		view.removeOverlay(overlay);
	}
	/**
	 * Adds the specified mouse listener to receive mouse events from
	 * the image component of this image viewer. If listener <code>l</code>
	 * is <code>null</code>, no exception is thrown and no action is performed.
	 * @param l the mouse listener
	 */
	public void addMouseListener(MouseListener l) {
		theImage.addMouseListener(l);
	}
	/**
	 * Removes the specified mouse listener so that it no longer receives
	 * mouse motion events from the image component of this image viewer. This method
	 * performs no function, nor does it throw an exception, if the listener specified
	 * by the argument was not previously added to this component. If listener
	 * <code>l</code> is <code>null</code>, no exception is thrown and no action is
	 * performed.
	 * @param l the mouse motion listener
	 */
	public void removeMouseListener(MouseListener l) {
		theImage.removeMouseListener(l);
	}
	/**
	 * Adds the specified mouse motion listener to receive mouse events from
	 * the image component of this image viewer. If listener <code>l</code>
	 * is <code>null</code>, no exception is thrown and no action is performed.
	 * @param l the mouse listener
	 */
	public void addMouseMotionListener(MouseMotionListener l) {
		theImage.addMouseMotionListener(l);
	}
	/**
	 * Removes the specified mouse motion listener so that it no longer receives
	 * mouse motion events from the image component of this image viewer. This method
	 * performs no function, nor does it throw an exception, if the listener specified
	 * by the argument was not previously added to this component. If listener
	 * <code>l</code> is <code>null</code>, no exception is thrown and no action is
	 * performed.
	 * @param l the mouse motion listener
	 */
	public void removeMouseMotionListener(MouseMotionListener l) {
		theImage.removeMouseMotionListener(l);
	}
	
	public void addImageMouseMoveListener(ImageMouseMoveListener l) {
		theImage.addImageMouseMoveListener(l);
	}

	public void removeImageMouseMoveListener(ImageMouseMoveListener l) {
		theImage.removeImageMouseMoveListener(l);
	}

	public void addImageMouseClickListener(ImageMouseClickListener l) {
		theImage.addImageMouseClickListener(l);
	}
	public void removeImageMouseClickListener(ImageMouseClickListener l) {
		theImage.removeImageMouseClickListener(l);
	}
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}
	public void addPropertyChangeListener(String name, PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(name, l);
	}
	public void removePropertyChangeListener(String name, PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(name, l);
	}

	/**
	 * Returns the scroll pane of the image viewer.
	 * @return the scroll pane
	 */
	JScrollPane getScrollPane() {
		return scroller;
	}
	/**
	 * Adds a component to the trackSizeIfEmpty set. If this component has no image set
	 * but one of the tracked ones does, then the size of this component will be set to
	 * match the size of the image displayed in one of the tracked components. This
	 * method is useful if the scroll bars of image viewers are synchronized, because
	 * if a viewer has no image set, it can cause the scrolling of a viewer that has an
	 * image set not to work.
	 * <p>
	 * Tracking is symmetrical and transitive.
	 * @param c the component to track
	 */
	void trackSizeIfEmpty(ImageViewer c) {
		theImage.trackSizeIfEmpty(c.theImage);
	}
}
