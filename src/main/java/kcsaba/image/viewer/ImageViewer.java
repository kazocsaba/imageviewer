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
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
	/*
	 * This will only be accessed from the event dispatch thread so using a static instance to share
	 * the current directory across components is fine.
	 */
	private static JFileChooser saveChooser;

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
		/** Status bar toggle **/
		
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
		
		/** Zoom menu **/
		
		JMenu zoomMenu = new JMenu("Zoom");
		final JRadioButtonMenuItem zoomOriginalSize = new JRadioButtonMenuItem("Original size", imageViewer.getResizeStrategy()==ResizeStrategy.NO_RESIZE);
		zoomOriginalSize.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.NO_RESIZE);
			}
		});
		final JRadioButtonMenuItem zoomShrinkToFit = new JRadioButtonMenuItem("Shrink to fit", imageViewer.getResizeStrategy()==ResizeStrategy.SHRINK_TO_FIT);
		zoomShrinkToFit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.SHRINK_TO_FIT);
			}
		});
		final JRadioButtonMenuItem zoomResizeToFit = new JRadioButtonMenuItem("Resize to fit", imageViewer.getResizeStrategy()==ResizeStrategy.RESIZE_TO_FIT);
		zoomResizeToFit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setResizeStrategy(ResizeStrategy.RESIZE_TO_FIT);
			}
		});
		
		class CustomZoomEntry {
			String label;
			double value;
			JRadioButtonMenuItem menuItem;

			private CustomZoomEntry(String label, double value) {
				this.label = label;
				this.value = value;
				menuItem=new JRadioButtonMenuItem(label, imageViewer.getResizeStrategy()==ResizeStrategy.CUSTOM_ZOOM && imageViewer.getZoomFactor()==value);
				menuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						imageViewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
						imageViewer.setZoomFactor(CustomZoomEntry.this.value);
					}
				});
			}
			
		}
		final CustomZoomEntry[] customZoomEntries={
			new CustomZoomEntry("25%", .25),
			new CustomZoomEntry("50%", .50),
			new CustomZoomEntry("75%", .75),
			new CustomZoomEntry("100%", 1),
			new CustomZoomEntry("150%", 1.5),
			new CustomZoomEntry("200%", 2),
			new CustomZoomEntry("300%", 3),
			new CustomZoomEntry("500%", 5),
			new CustomZoomEntry("1000%", 10),
			new CustomZoomEntry("2000%", 20),
			new CustomZoomEntry("5000%", 50)
		};
		final ButtonGroup group = new ButtonGroup();
		group.add(zoomOriginalSize);
		group.add(zoomShrinkToFit);
		group.add(zoomResizeToFit);
		
		zoomMenu.add(zoomOriginalSize);
		zoomMenu.add(zoomShrinkToFit);
		zoomMenu.add(zoomResizeToFit);
		zoomMenu.add(new JSeparator());
		for (CustomZoomEntry cze: customZoomEntries) {
			zoomMenu.add(cze.menuItem);
			group.add(cze.menuItem);
		}
		
		imageViewer.addPropertyChangeListener("resizeStrategy", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				switch ((ResizeStrategy)evt.getNewValue()) {
					case NO_RESIZE:
						zoomOriginalSize.setSelected(true);
						break;
					case RESIZE_TO_FIT:
						zoomResizeToFit.setSelected(true);
						break;
					case SHRINK_TO_FIT:
						zoomShrinkToFit.setSelected(true);
						break;
					case CUSTOM_ZOOM:
						group.clearSelection();
						for (CustomZoomEntry cze: customZoomEntries) {
							if (cze.value==imageViewer.getZoomFactor()) {
								cze.menuItem.setSelected(true);
								break;
							}
						}
						break;
					default:
						throw new AssertionError("Unknown resize strategy: "+evt.getNewValue());
				}
			}
		});
		imageViewer.addPropertyChangeListener("zoomFactor", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (imageViewer.getResizeStrategy()==ResizeStrategy.CUSTOM_ZOOM) {
					group.clearSelection();
					for (CustomZoomEntry cze: customZoomEntries) {
						if (cze.value==imageViewer.getZoomFactor()) {
							cze.menuItem.setSelected(true);
							break;
						}
					}
				}
			}
		});
		
		/** Save command **/
		
		JMenuItem saveImageMenuItem=new JMenuItem("Save image...");
		saveImageMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (saveChooser==null) {
					saveChooser=new JFileChooser();
					saveChooser.setDialogTitle("Save image...");
				}
				if (JFileChooser.APPROVE_OPTION==saveChooser.showSaveDialog(imageViewer.getComponent())) {
					File f=saveChooser.getSelectedFile();
					BufferedImage image=imageViewer.getImage();
					if (image==null) {
						JOptionPane.showMessageDialog(imageViewer.getComponent(), "No image", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						String name=f.getName().toLowerCase();
						try {
							if (name.endsWith(".jpg")) {
								ImageIO.write(image, "jpg", f);
							} else if (name.endsWith(".png")) {
								ImageIO.write(image, "png", f);
							} else {
								f=new File(f.getPath()+".png");
								ImageIO.write(image, "png", f);
							}
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(imageViewer.getComponent(), "Cannot write image to "+f.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		
		/** Pixelated zoom toggle **/
		final JCheckBoxMenuItem togglePixelatedZoomItem = new JCheckBoxMenuItem("Pixelated zoom");
		togglePixelatedZoomItem.setState(imageViewer.isPixelatedZoom());
		imageViewer.addPropertyChangeListener("pixelatedZoom", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				togglePixelatedZoomItem.setState(imageViewer.isPixelatedZoom());
			}
		});
		togglePixelatedZoomItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imageViewer.setPixelatedZoom(!imageViewer.isPixelatedZoom());
			}
		});
		
		JPopupMenu popup = new JPopupMenu();
		popup.add(toggleStatusBarItem);
		popup.add(zoomMenu);
		popup.add(togglePixelatedZoomItem);
		popup.add(saveImageMenuItem);
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
	 * Sets whether the image should be resized with nearest neighbor interpolation when it is expanded.
	 * @param pixelatedZoom the new value of the pixelatedZoom property
	 */
	public void setPixelatedZoom(boolean pixelatedZoom) {
		theImage.setPixelatedZoom(pixelatedZoom);
	}
	/**
	 * Returns the current pixelated zoom setting.
	 * @return the current pixelated zoom setting
	 */
	public boolean isPixelatedZoom() {
		return theImage.isPixelatedZoom();
	}
	
	/**
	 * Returns the zoom factor used when resize strategy is CUSTOM_ZOOM.
	 * @return the custom zoom factor
	 */
	public double getZoomFactor() {
		return theImage.getZoomFactor();
	}
	
	/**
	 * Sets the zoom factor to use when the resize strategy is CUSTOM_ZOOM.
	 * <p>
	 * Note that calling this function does not change the current resize strategy.
	 * @throws IllegalArgumentException if {@code newZoomFactor} is not a positive number
	 */
	public void setZoomFactor(double newZoomFactor) {
		theImage.setZoomFactor(newZoomFactor);
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
	
	/**
	 * Returns the image pixel corresponding to the given point. If the <code>clipToImage</code>
	 * parameter is <code>false</code>, then the function will return an appropriately positioned
	 * pixel on an infinite plane, even if the point is outside the image bounds. If
	 * <code>clipToImage</code> is <code>true</code> then the function will return <code>null</code>
	 * for such positions, and any non-null return value will be a valid image pixel.
	 * @param p a point in component coordinate system
	 * @param clipToImage whether the function should return <code>null</code> for positions outside
	 * the image bounds
	 * @return the corresponding image pixel
	 */
	public Point pointToPixel(Point p, boolean clipToImage) {
		return theImage.pointToPixel(p, clipToImage);
	}
}
