package kcsaba.image.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

/**
 * The component that displays the image itself.
 * @author Kazó Csaba
 */
class ImageComponent extends JComponent {
	private ResizeStrategy resizeStrategy = ResizeStrategy.NO_RESIZE;
	private BufferedImage image;
	private boolean pixelatedZoom=false;
	private double zoomFactor=1;
	private final List<ImageMouseMoveListener> moveListeners = new ArrayList<ImageMouseMoveListener>(4);
	private final List<ImageMouseClickListener> clickListeners = new ArrayList<ImageMouseClickListener>(4);
	private final MouseEventTranslator mouseEventTranslator = new MouseEventTranslator();
	/**
	 * This set is shared by all synchronized image components and contains all
	 * synchronized image components. Unless there is no synchronization; then it is null.
	 */
	private Set<ImageComponent> trackSizeIfEmpty = null;
	
	private final PropertyChangeSupport propertyChangeSupport;
	private final Object eventSource;

	public ImageComponent(Object eventSource, PropertyChangeSupport propertyChangeSupport) {
		this.eventSource = eventSource;
		this.propertyChangeSupport=propertyChangeSupport;
		mouseEventTranslator.register(this);
		setOpaque(true);
	}
	
	public void addImageMouseMoveListener(ImageMouseMoveListener l) {
		moveListeners.add(l);
	}

	public void removeImageMouseMoveListener(ImageMouseMoveListener l) {
		moveListeners.remove(l);
	}

	public void addImageMouseClickListener(ImageMouseClickListener l) {
		clickListeners.add(l);
	}

	public void removeImageMouseClickListener(ImageMouseClickListener l) {
		clickListeners.remove(l);
	}
	
	public void setImage(BufferedImage newImage) {
		BufferedImage oldImage = image;
		image = newImage;
		if (oldImage != newImage &&
				(oldImage == null || newImage == null || oldImage.getWidth() != newImage.getWidth() ||
				oldImage.getHeight() != newImage.getHeight()))
			revalidate();
		repaint();
		propertyChangeSupport.firePropertyChange("image", oldImage, newImage);
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public void setResizeStrategy(ResizeStrategy resizeStrategy) {
		if (resizeStrategy == this.resizeStrategy)
			return;
		ResizeStrategy oldResizeStrategy=this.resizeStrategy;
		this.resizeStrategy = resizeStrategy;
		revalidate();
		repaint();
		propertyChangeSupport.firePropertyChange("resizeStrategy", oldResizeStrategy, resizeStrategy);
	}
	
	public ResizeStrategy getResizeStrategy() {
		return resizeStrategy;
	}
	
	public void setPixelatedZoom(boolean pixelatedZoom) {
		if (pixelatedZoom == this.pixelatedZoom)
			return;
		this.pixelatedZoom = pixelatedZoom;
		repaint();
		propertyChangeSupport.firePropertyChange("pixelatedZoom", !pixelatedZoom, pixelatedZoom);
	}
	
	public boolean isPixelatedZoom() {
		return pixelatedZoom;
	}
	
	/** Returns the zoom factor used when resize strategy is CUSTOM_ZOOM. */
	public double getZoomFactor() {
		return zoomFactor;
	}
	
	/**
	 * Sets the zoom factor to use when the resize strategy is CUSTOM_ZOOM.
	 * <p>
	 * Note that calling this function does not change the current resize strategy.
	 * @throws IllegalArgumentException if {@code newZoomFactor} is not a positive number
	 */
	public void setZoomFactor(double newZoomFactor) {
		if (zoomFactor==newZoomFactor) return;
		if (newZoomFactor<=0 || Double.isInfinite(newZoomFactor) || Double.isNaN(newZoomFactor))
			throw new IllegalArgumentException("Invalid zoom factor: "+newZoomFactor);
		double oldZoomFactor=zoomFactor;
		zoomFactor=newZoomFactor;
		revalidate();
		repaint();
		propertyChangeSupport.firePropertyChange("zoomFactor", oldZoomFactor, newZoomFactor);
	}
	@Override
	public Dimension getPreferredSize() {
		if (image == null) {
			if (trackSizeIfEmpty!=null)
				for (ImageComponent c:trackSizeIfEmpty)
					if (c.getImage()!=null)
						return new Dimension(c.getImage().getWidth(), c.getImage().getHeight());
			return new Dimension();
		} else if (resizeStrategy==ResizeStrategy.CUSTOM_ZOOM) {
			return new Dimension((int)Math.ceil(image.getWidth()*zoomFactor), (int)Math.ceil(image.getHeight()*zoomFactor));
		} else
			return new Dimension(image.getWidth(), image.getHeight());
	}
	/**
	 * Adds a component to the trackSizeIfEmpty set. If this component has no image set
	 * but one of the tracked ones does, then the size of this component will be set to
	 * match the size of the image displayed in one of the tracked components. This
	 * method is useful if the scroll bars of image viewers are synchronized, because
	 * if a viewer has no image set, it can cause the scrolling of a viewer that has an
	 * image set not to work.
	 * @param c the component to track
	 */
	public void trackSizeIfEmpty(ImageComponent c) {
		if (trackSizeIfEmpty!=null) {
			if (c.trackSizeIfEmpty!=null) {
				trackSizeIfEmpty.addAll(c.trackSizeIfEmpty);
				c.trackSizeIfEmpty=trackSizeIfEmpty;
			} else {
				trackSizeIfEmpty.add(c);
				c.trackSizeIfEmpty=trackSizeIfEmpty;
			}
		} else {
			if (c.trackSizeIfEmpty!=null) {
				c.trackSizeIfEmpty.add(this);
				trackSizeIfEmpty=c.trackSizeIfEmpty;
			} else {
				trackSizeIfEmpty=new HashSet<ImageComponent>(4);
				trackSizeIfEmpty.add(this);
				trackSizeIfEmpty.add(c);
				c.trackSizeIfEmpty=trackSizeIfEmpty;
			}
		}
	}

	/**
	 * Returns the image pixel that is under the given point.
	 * 
	 * @param p a point in component coordinate system
	 * @return the corresponding image pixel, or <code>null</code> if the point is outside the image
	 */
	public Point pointToPixel(Point p) {
		return pointToPixel(p, true);
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
		Point2D.Double fp=new Point2D.Double(p.x+.5, p.y+.5);
		try {
			getImageTransform().inverseTransform(fp, fp);
		} catch (NoninvertibleTransformException ex) {
			throw new Error("Image transformation not invertible");
		}
		p.x=(int)Math.floor(fp.x);
		p.y=(int)Math.floor(fp.y);
		if (clipToImage && (p.x < 0 || p.y < 0 || p.x >= image.getWidth() || p.y >= image.getHeight())) {
			return null;
		}
		return p;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setColor(getBackground());
		gg.fillRect(0, 0, getWidth(), getHeight());
		if (image!=null) {
			paint(gg);
		}
		gg.dispose();
	}

	/**
	 * Returns the transformation that is applied to the image. Most commonly the transformation
	 * is the concatenation of a uniform scale and a translation.
	 * <p>
	 * The <code>AffineTransform</code>
	 * instance returned by this method should not be modified.
	 * @return the transformation applied to the image before painting
	 * @throws IllegalStateException if there is no image set
	 */
	public AffineTransform getImageTransform() {
		if (getImage()==null) throw new IllegalStateException("No image");
		double currentZoom;
		switch (resizeStrategy) {
			case NO_RESIZE:
				currentZoom=1;
				break;
			case SHRINK_TO_FIT:
				currentZoom = Math.min(getSizeRatio(), 1);
				break;
			case RESIZE_TO_FIT:
				currentZoom = getSizeRatio();
				break;
			case CUSTOM_ZOOM:
				currentZoom = zoomFactor;
				break;
			default:
				throw new Error("Unhandled resize strategy");
		}
		AffineTransform tr=new AffineTransform();
		tr.setToTranslation((getWidth()-image.getWidth()*currentZoom)/2.0, (getHeight()-image.getHeight()*currentZoom)/2.0);
		tr.scale(currentZoom, currentZoom);
		return tr;
	}

	private void paint(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		AffineTransform imageTransform = getImageTransform();
		if (pixelatedZoom && (imageTransform.getScaleX()>=1 || imageTransform.getScaleY()>=1)) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		} else {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}
		g.drawImage(image, imageTransform, this);
	}

	private double getSizeRatio() {
		return Math.min(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
	}
	/**
	 * Helper class that generates ImageMouseEvents by translating normal mouse events onto
	 * the image.
	 */
	private class MouseEventTranslator implements MouseInputListener, PropertyChangeListener {
		/** This flag is true if the mouse cursor is inside the bounds of the image. */
		private boolean on=false;
		
		/** Sets up this translator. */
		private void register(ImageComponent ic) {
			ic.addMouseListener(this);
			ic.addMouseMotionListener(this);
			ic.propertyChangeSupport.addPropertyChangeListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (image == null || !on) return;
			Point p = pointToPixel(e.getPoint());
			if (p != null) {
				fireMouseClickedAtPixel(p.x, p.y, e);
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			if (image != null) {
				Point p=pointToPixel(e.getPoint());
				if (p!=null) {
					on=true;
					fireMouseEnter(p.x, p.y, e);
					fireMouseAtPixel(p.x, p.y, e);
				}
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (on) {
				on = false;
				fireMouseExit();
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			if (image == null) return;
			Point p = pointToPixel(e.getPoint());
			if (p == null) {
				if (on) {
					on = false;
					fireMouseExit();
				}
			} else {
				on = true;
				fireMouseAtPixel(p.x, p.y, e);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (image==null) return;
			Point p = pointToPixel(e.getPoint(), false);
			fireMouseDrag(p.x, p.y, e);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("image".equals(evt.getPropertyName())) {
				if (image == null && on) {
					on = false;
					fireMouseExit();
				}
			} else if ("resizeStrategy".equals(evt.getPropertyName())) {
				if (image!=null) {
					Point p=ImageComponent.this.getMousePosition();
					if (p!=null) p=pointToPixel(p);
					if (p==null) {
						if (on) {
							on = false;
							fireMouseExit();
						}
					} else {
						on = true;
						fireMouseAtPixel(p.x, p.y, null);
					}
				}
			}
		}

		private void fireMouseAtPixel(int x, int y, MouseEvent ev) {
			ImageMouseEvent e = null;
			for (ImageMouseMoveListener imageMouseMoveListener: moveListeners) {
				if (e == null)
					e = new ImageMouseEvent(eventSource, image, x, y, ev);
				imageMouseMoveListener.mouseMoved(e);
			}
		}

		private void fireMouseClickedAtPixel(int x, int y, MouseEvent ev) {
			ImageMouseEvent e = null;
			for (ImageMouseClickListener imageMouseClickListener: clickListeners) {
				if (e == null)
					e = new ImageMouseEvent(eventSource, image, x, y, ev);
				imageMouseClickListener.mouseClicked(e);
			}
		}

		private void fireMouseEnter(int x, int y, MouseEvent ev) {
			ImageMouseEvent e = null;
			for (ImageMouseMoveListener imageMouseMoveListener: moveListeners) {
				if (e == null)
					e = new ImageMouseEvent(eventSource, image, x, y, ev);
				imageMouseMoveListener.mouseEntered(e);
			}
		}

		private void fireMouseExit() {
			ImageMouseEvent e = null;
			for (ImageMouseMoveListener imageMouseMoveListener: moveListeners) {
				if (e == null)
					e = new ImageMouseEvent(eventSource, image, -1, -1, null);
				imageMouseMoveListener.mouseExited(e);
			}
		}
		
		private void fireMouseDrag(int x, int y, MouseEvent ev) {
			ImageMouseEvent e = null;
			for (ImageMouseMoveListener imageMouseMoveListener: moveListeners) {
				if (e == null)
					e = new ImageMouseEvent(eventSource, image, x, y, ev);
				imageMouseMoveListener.mouseDragged(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}
}
