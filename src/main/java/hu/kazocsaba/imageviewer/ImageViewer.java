package hu.kazocsaba.imageviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
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
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.filechooser.FileNameExtensionFilter;

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
public final class ImageViewer {
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
	private static JButton saveChooserHelpButton;
	private static JLabel saveChooserHelpLabel;

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
		view=new LayeredImageView(theImage);
		theImage.setImage(image);
		scroller=new JScrollPane(view.getComponent()) {

			@Override
			protected JViewport createViewport() {
				return new JViewport() {

					@Override
					protected LayoutManager createLayoutManager() {
						return new CustomViewportLayout(ImageViewer.this);
					}
					
					@Override
					public Dimension getMaximumSize() {
						return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
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
					saveChooserHelpLabel=new JLabel();
					saveChooserHelpLabel.setText("<html>If the file name ends<br>with '.png' or '.jpg',<br>then the appropriate<br>format is used.<br>Otherwise '.png' is<br>appended to the name.");
					saveChooserHelpLabel.setFont(saveChooserHelpLabel.getFont().deriveFont(10f));
					saveChooserHelpButton=new JButton("?");
					saveChooserHelpButton.setMargin(new Insets(0, 2, 0, 2));
					saveChooserHelpButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							saveChooser.getAccessory().removeAll();
							saveChooser.getAccessory().add(saveChooserHelpLabel);
							saveChooser.revalidate();
							saveChooser.repaint();
						}
					});
					saveChooserHelpLabel.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent e) {
							saveChooser.getAccessory().removeAll();
							saveChooser.getAccessory().add(saveChooserHelpButton);
							saveChooser.revalidate();
							saveChooser.repaint();
						}
						
					});
					saveChooser.setAccessory(new JPanel());
					saveChooser.setDialogTitle("Save image...");
					
					saveChooser.setFileFilter(new FileNameExtensionFilter("JPG and PNG images", "jpg", "png"));
				}
				// reset to show the help button with every new dialog
				saveChooser.getAccessory().removeAll();
				saveChooser.getAccessory().add(saveChooserHelpButton);
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
							JOptionPane.showMessageDialog(imageViewer.getComponent(), "<html>Cannot write image to "+f.getAbsolutePath()+":<br>"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
			if (statusBarVisible) {
				panel.add(this.statusBar.getComponent(), BorderLayout.SOUTH);
				panel.revalidate();
				panel.repaint();
			}
		}
		propertyChangeSupport.firePropertyChange("statusBar", oldStatusBar, statusBar);
	}
	/**
	 * Sets whether the status bar is visible. The status bar is hidden by default.
	 * @param statusBarVisible true, if the status bar should be visible; false otherwise
	 */
	public void setStatusBarVisible(boolean statusBarVisible) {
		if (this.statusBarVisible == statusBarVisible) return;
		if (statusBar!=null) {
			if (statusBarVisible)
				panel.add(statusBar.getComponent(), BorderLayout.SOUTH);
			else
				panel.remove(statusBar.getComponent());
			panel.revalidate();
			panel.repaint();
		}
		boolean prev = this.statusBarVisible;
		this.statusBarVisible = statusBarVisible;
		propertyChangeSupport.firePropertyChange("statusBarVisible", prev, statusBarVisible);
	}
	/**
	 * Returns whether the status bar is set to be visible. The status bar is hidden by default.
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
	 * Sets the image displayed by the viewer. If the argument is the same object as the image currently being displayed,
	 * then this method will trigger a refresh. If you modify the image shown by the viewer, use this function to notify
	 * the component and cause it to update.
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
	 * Sets the resize strategy this viewer should use. The default is {@link ResizeStrategy#SHRINK_TO_FIT}.
	 * @param resizeStrategy the new resize strategy
	 */
	public void setResizeStrategy(ResizeStrategy resizeStrategy) {
		theImage.setResizeStrategy(resizeStrategy);
	}
	/**
	 * Returns the current resize strategy. The default is {@link ResizeStrategy#SHRINK_TO_FIT}.
	 * @return the current resize strategy
	 */
	public ResizeStrategy getResizeStrategy() {
		return theImage.getResizeStrategy();
	}
	
	/**
	 * Sets whether the image should be resized with nearest neighbor interpolation when it is expanded.
	 * The default is {@code false}.
	 * @param pixelatedZoom the new value of the pixelatedZoom property
	 */
	public void setPixelatedZoom(boolean pixelatedZoom) {
		theImage.setPixelatedZoom(pixelatedZoom);
	}
	/**
	 * Returns the current pixelated zoom setting. The default is {@code false}.
	 * @return the current pixelated zoom setting
	 */
	public boolean isPixelatedZoom() {
		return theImage.isPixelatedZoom();
	}
	
	/**
	 * Returns the current interpolation type. The default is {@link java.awt.RenderingHints#VALUE_INTERPOLATION_BICUBIC}.
	 * @return the interpolation type
	 * @see #setInterpolationType(Object)
	 */
	public Object getInterpolationType() {
		return theImage.getInterpolationType();
	}
	
	/**
	 * Sets the interpolation type to use when resizing images. See {@link java.awt.RenderingHints#KEY_INTERPOLATION}
	 * for details. The default value is {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC}.
	 * <p>
	 * The allowed values are:
	 * <ul>
	 * <li>{@link java.awt.RenderingHints#VALUE_INTERPOLATION_NEAREST_NEIGHBOR}
	 * <li>{@link java.awt.RenderingHints#VALUE_INTERPOLATION_BILINEAR}
	 * <li>{@link java.awt.RenderingHints#VALUE_INTERPOLATION_BICUBIC} (default)
	 * </ul>
	 * Changing the interpolation type to bilinear or nearest neighbor improves painting performance when the image
	 * needs to be resized.
	 * <p>
	 * Note: when the {@code pixelatedZoom} property is set to true and the image is enlarged, then the nearest
	 * neighbor method is used regardless of this setting.
	 * @param type the interpolation type to use when resizing images
	 * @throws IllegalArgumentException if the parameter is not one of the allowed values
	 */
	public void setInterpolationType(Object type) {
		theImage.setInterpolationType(type);
	}
	
	/**
	 * Returns the zoom factor used when resize strategy is CUSTOM_ZOOM. The default value is 1.
	 * @return the custom zoom factor
	 */
	public double getZoomFactor() {
		return theImage.getZoomFactor();
	}
	
	/**
	 * Sets the zoom factor to use when the resize strategy is CUSTOM_ZOOM. The default value is 1.
	 * <p>
	 * Note that calling this function does not change the current resize strategy.
	 * @param newZoomFactor the new zoom factor for the CUSTOM_ZOOM strategy
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
	 * @throws IllegalStateException if there is no image set or if the size of the viewer is 0 (for example because
	 * it is not in a visible component)
	 */
	public AffineTransform getImageTransform() {
		return theImage.getImageTransform();
	}
	
	/**
	 * Adds an overlay as the specified layer.
	 * @param overlay the overlay to add
	 * @param layer the layer to add the overlay to; higher layers are on top of lower layers;
	 * the image resides in layer 0
	 */
	public void addOverlay(Overlay overlay, int layer) {
		view.addOverlay(overlay, layer);
	}
	
	/**
	 * Adds an overlay to layer 1.
	 * @param overlay the overlay to add
	 */
	public void addOverlay(Overlay overlay) {
		addOverlay(overlay, 1);
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
	/**
	 * Adds the specified image mouse motion listener to this viewer. The listener is notified as the mouse
	 * moves over pixels of the image. If listener <code>l</code>
	 * is {@code null}, no exception is thrown and no action is performed.
	 * @param l the image mouse motion listener
	 */
	public void addImageMouseMotionListener(ImageMouseMotionListener l) {
		theImage.addImageMouseMoveListener(l);
	}

	/**
	 * Removes the specified image mouse motion listener so that it no longer receives
	 * mouse motion events from the image component of this image viewer. This method
	 * performs no function, nor does it throw an exception, if the listener specified
	 * by the argument was not previously added to this component. If listener
	 * <code>l</code> is {@code null}, no exception is thrown and no action is
	 * performed.
	 * @param l the mouse motion listener
	 */
	public void removeImageMouseMotionListener(ImageMouseMotionListener l) {
		theImage.removeImageMouseMoveListener(l);
	}

	/**
	 * Adds the specified image mouse listener to this viewer. The listener is notified as mouse buttons are clicked
	 * over pixels of the image. If listener <code>l</code>
	 * is {@code null}, no exception is thrown and no action is performed.
	 * @param l the image mouse motion listener
	 */
	public void addImageMouseClickListener(ImageMouseClickListener l) {
		theImage.addImageMouseClickListener(l);
	}
	
	/**
	 * Removes the specified image mouse listener so that it no longer receives
	 * mouse click events from the image component of this image viewer. This method
	 * performs no function, nor does it throw an exception, if the listener specified
	 * by the argument was not previously added to this component. If the listener
	 * <code>l</code> is {@code null}, no exception is thrown and no action is
	 * performed.
	 * @param l the mouse motion listener
	 */
	public void removeImageMouseClickListener(ImageMouseClickListener l) {
		theImage.removeImageMouseClickListener(l);
	}
	
	/**
	 * Adds a {@code PropertyChangeListener} to the listener list.
	 * The same listener object may be added more than once, and will be
	 * called as many times as it is added. If the listener is {@code null},
	 * no exception is thrown and no action is taken.
	 * @param l the listener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	/**
	 * Remove a {@code PropertyChangeListener} from the listener list.
	 * This removes a listener that was registered for all properties.
	 * If the listener was added more than once, it will be notified
	 * one less time after being removed. If the listener is {@code null},
	 * or was never added, no exception is thrown and no action is taken.
	 * @param l the listener to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}
	
	/**
	 * Adds a {@code PropertyChangeListener} for a specific property. The listener
	 * will be invoked only when a call on firePropertyChange names that specific property.
	 * The same listener object may be added more than once. For each property,
	 * the listener will be invoked the number of times it was added for that property.
	 * If the property name or the listener is null, no exception is thrown and no action is taken.
	 * @param name the name of the property to listen on
	 * @param l the listener to add
	 */
	public void addPropertyChangeListener(String name, PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(name, l);
	}
	
	/**
	 * Remove a {@code PropertyChangeListener} from the listener list.
	 * This removes a PropertyChangeListener that was registered for all properties.
	 * If the listener was added more than once,
	 * it will be notified one less time after being removed. If the listener is {@code null},
	 * or was never added, no exception is thrown and no action is taken.
	 * @param name the name of the property that was listened on
	 * @param l the listener to remove
	 */
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
	 * @throws IllegalStateException if there is no image set or if the size of the viewer is 0 (for example because
	 * it is not in a visible component)
	 */
	public Point pointToPixel(Point p, boolean clipToImage) {
		return theImage.pointToPixel(p, clipToImage);
	}
}
