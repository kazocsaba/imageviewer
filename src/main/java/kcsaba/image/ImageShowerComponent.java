package kcsaba.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A bean for visualizing an image.
 * 
 * @author Kazó Csaba
 */
public class ImageShowerComponent extends JPanel {

	public enum ResizeStrategy {

		NO_RESIZE, SHRINK_TO_FIT
	}

	private final TheImage theImage = new TheImage();
	private final JScrollPane theImageScroller;
	private BufferedImage image = null;
	private Collection<? extends Point> points;
	private Collection<? extends Shape> shapes;
	private ImageShowerSettings settings;
	private ResizeStrategy resizeStrategy = ResizeStrategy.NO_RESIZE;
	private JPopupMenu popup;
	private JRadioButtonMenuItem[] zoomButtons;
	private boolean statusBarVisible = false;
	private final JPanel statusBar;
	private final List<ImageMouseMoveListener> moveListeners = new ArrayList<ImageMouseMoveListener>(4);
	private final List<ImageMouseClickListener> clickListeners = new ArrayList<ImageMouseClickListener>(4);

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

	private MouseListener contextMenuListener = new MouseAdapter() {
		private void showPopup(MouseEvent e) {
			Point p = getPopupLocation(e);
			if (p == null) {
				p = e.getPoint();
			}
			popup.show(e.getComponent(), p.x, p.y);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				e.consume();
				showPopup(e);
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				e.consume();
				showPopup(e);
			}
		}
	};
	private JFileChooser saveImageChooser;
	private Action saveDecoratedImage_action = new AbstractAction("Save image (original size)") {

		public void actionPerformed(ActionEvent e) {
			if (image == null) {
				return;
			}
			if (saveImageChooser.showSaveDialog(ImageShowerComponent.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			BufferedImage decoratedImage = new BufferedImage(image.getWidth(), image.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics g = decoratedImage.createGraphics();
			theImage.paint(g, false, image.getWidth(), image.getHeight());
			g.dispose();
			File f = saveImageChooser.getSelectedFile();
			try {
				ImageIO.write(decoratedImage, "png", f);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(ImageShowerComponent.this, "Error saving image: " + ex,
						"I/O error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	private Action showSettingsDialog_action = new AbstractAction("Settings...") {

		public void actionPerformed(ActionEvent e) {
			new SettingsDialog(ImageShowerComponent.this).setVisible(true);
		}
	};

	public ImageShowerComponent() {
		this(null, null);
	}

	public ImageShowerComponent(ImageShowerSettings settings) {
		this(settings, null);
	}

	public ImageShowerComponent(BufferedImage image) {
		this(null, image);
	}

	public ImageShowerComponent(ImageShowerSettings settings, BufferedImage image) {
		super(new BorderLayout());
		theImageScroller = new JScrollPane(theImage) {

			@Override
			public boolean isValidateRoot() {
				return false;
			}
			
		};
		add(theImageScroller, BorderLayout.CENTER);
		if (settings == null) {
			settings = new ImageShowerSettings();
		}
		this.settings = settings;
		theImage.addMouseListener(contextMenuListener);
		saveImageChooser = new JFileChooser();
		saveImageChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));
		popup = new JPopupMenu();
		popup.add(showSettingsDialog_action);
		popup.add(saveDecoratedImage_action);
		final JCheckBoxMenuItem toggleStatusBarItem = new JCheckBoxMenuItem("Status bar");
		toggleStatusBarItem.setState(statusBarVisible);
		addPropertyChangeListener("statusBarVisible", new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				toggleStatusBarItem.setState(statusBarVisible);
			}
		});
		toggleStatusBarItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setStatusBarVisible(!statusBarVisible);
			}
		});
		popup.add(toggleStatusBarItem);
		JMenu zoomMenu = new JMenu("Zoom");
		zoomButtons = new JRadioButtonMenuItem[2];
		zoomButtons[0] = new JRadioButtonMenuItem("Original size", true);
		zoomButtons[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setResizeStrategy(ResizeStrategy.NO_RESIZE);
			}
		});
		zoomButtons[1] = new JRadioButtonMenuItem("Shrink to fit", false);
		zoomButtons[1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setResizeStrategy(ResizeStrategy.SHRINK_TO_FIT);
			}
		});
		ButtonGroup group = new ButtonGroup();
		for (JRadioButtonMenuItem i : zoomButtons) {
			zoomMenu.add(i);
			group.add(i);
		}
		popup.add(zoomMenu);

		saveDecoratedImage_action.setEnabled(false);
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {
				ImageShowerComponent.this.settings.components.add(theImage);
			}

			public void ancestorRemoved(AncestorEvent event) {
				ImageShowerComponent.this.settings.components.remove(theImage);
			}

			public void ancestorMoved(AncestorEvent event) {}
		});

		// **********
		// Status bar
		// **********
		statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		final JLabel posLabel = new JLabel("n/a");
		statusBar.add(posLabel);
		addImageMouseMoveListener(new ImageMouseMoveListener() {

			public void mouseAtPixel(int x, int y) {
				int rgb = getImage().getRGB(x, y);
				Color c = new Color(rgb);
				posLabel.setText(String.format("%d, %d; color %d,%d,%d", x, y, c.getRed(), c.getGreen(), c
						.getBlue()));
			}

			public void mouseLeft() {
				posLabel.setText("n/a");
			}
		});
		MouseImageEventTranslator miet = new MouseImageEventTranslator();
		theImage.addMouseListener(miet);
		theImage.addMouseMotionListener(miet);
		addPropertyChangeListener("image", miet);
		if (image != null) setImage(image);
	}

	private void fireMouseExit() {
		for (ImageMouseMoveListener l : moveListeners)
			l.mouseLeft();
	}

	private void fireMouseClickedAtPixel(int x, int y, int button) {
		for (ImageMouseClickListener l : clickListeners)
			l.mouseClickedAtPixel(x, y, button);
	}

	private void fireMouseAtPixel(int x, int y) {
		for (ImageMouseMoveListener l : moveListeners)
			l.mouseAtPixel(x, y);
	}

	private class MouseImageEventTranslator implements MouseListener, MouseMotionListener,
			PropertyChangeListener {

		private boolean on = false;

		public void mouseClicked(MouseEvent e) {
			if (getImage() == null || !on) return;
			Point p = pointToPixel(e.getPoint());
			if (p != null) {
				fireMouseClickedAtPixel(p.x, p.y, e.getButton());
			}
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {
			if (on) {
				on = false;
				fireMouseExit();
			}
		}

		public void mouseDragged(MouseEvent e) {}

		public void mouseMoved(MouseEvent e) {
			if (getImage() == null) return;
			Point p = pointToPixel(e.getPoint());
			if (p == null) {
				if (on) {
					on = false;
					fireMouseExit();
				}
			} else {
				on = true;
				fireMouseAtPixel(p.x, p.y);
			}
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (getImage() == null && on) {
				on = false;
				fireMouseExit();
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
		switch (resizeStrategy) {
		case NO_RESIZE:
			p = new Point(p);
			break;
		case SHRINK_TO_FIT:
			double shrink = Math.min(theImage.getSizeRatio(), 1);
			p = new Point((int) Math.round(p.x / shrink), (int) Math.round(p.y / shrink));
			break;
		default:
			throw new Error("Unimplemented resize strategy");
		}
		if (p.x < 0 || p.y < 0 || p.x >= image.getWidth() || p.y >= image.getHeight()) {
			return null;
		}
		return p;
	}

	/**
	 * Sets the image to display in this component.
	 * 
	 * @param image the image to display, if <code>null</code>, then no image will be displayed
	 */
	public void setImage(BufferedImage image) {
		if (image==null && this.image==null) return;
		BufferedImage prev = this.image;
		this.image = image;
		saveDecoratedImage_action.setEnabled(image != null);
		firePropertyChange("image", prev, image);
		if (prev!=image && (prev==null || image==null || prev.getWidth()!=image.getWidth() || prev.getHeight()!=image.getHeight())) {
			theImage.revalidate();
		}
		theImage.repaint();
	}

	/**
	 * Retrieves the image set by {@link #setImage(BufferedImage)}.
	 * 
	 * @return the currently set image, or <code>null</code> if no image is set
	 */
	public BufferedImage getImage() {
		return image;
	}

	public void setResizeStrategy(ResizeStrategy resizeStrategy) {
		if (this.resizeStrategy == resizeStrategy) {
			return;
		}
		ResizeStrategy prev = this.resizeStrategy;
		this.resizeStrategy = resizeStrategy;
		zoomButtons[resizeStrategy.ordinal()].setSelected(true);
		firePropertyChange("resizeStrategy", prev, resizeStrategy);
		theImage.revalidate();
		theImage.repaint();
	}

	public ResizeStrategy getResizeStrategy() {
		return resizeStrategy;
	}

	public void setStatusBarVisible(boolean statusBarVisible) {
		if (this.statusBarVisible == statusBarVisible) return;
		if (statusBarVisible)
			add(statusBar, BorderLayout.SOUTH);
		else
			remove(statusBar);
		boolean prev = this.statusBarVisible;
		this.statusBarVisible = statusBarVisible;
		firePropertyChange("statusBarVisible", prev, statusBarVisible);
	}

	/**
	 * Sets the list of pixels to visualize. The specified points will be marked as circles on the image.
	 * 
	 * @param points a list of the points to visualize; if <code>null</code>, then no points will be shown;
	 *            this list is stored by reference, no further modification on it is permissible
	 */
	public void setPoints(Collection<? extends Point> points) {
		Collection<? extends Point> prev = this.points;
		this.points = points;
		firePropertyChange("points", prev, points);
		theImage.repaint();
	}

	/**
	 * Sets the list of shapes to visualize. The specified shapes will be drawn on the image.
	 * 
	 * @param shapes a list of the shapes to visualize; if <code>null</code>, then no shapes will be shown;
	 *            this list is stored by reference, no further modification on it is permissible
	 */
	public void setShapes(Collection<? extends Shape> shapes) {
		Collection<? extends Shape> prev = this.shapes;
		this.shapes = shapes;
		firePropertyChange("shapes", prev, shapes);
		theImage.repaint();
	}

	public ImageShowerSettings getSettings() {
		return settings;
	}

	public void setSettings(ImageShowerSettings settings) {
		if (settings == null) throw new NullPointerException();
		this.settings.components.remove(theImage);
		this.settings = settings;
		this.settings.components.add(theImage);
		theImage.repaint();
	}

	public void setScrollBarModelFrom(ImageShowerComponent otherComp) {
		theImageScroller.getHorizontalScrollBar().setModel(
				otherComp.theImageScroller.getHorizontalScrollBar().getModel());
		theImageScroller.getVerticalScrollBar().setModel(
				otherComp.theImageScroller.getVerticalScrollBar().getModel());
	}

	public void setScrollBarModel(BoundedRangeModel horizontal, BoundedRangeModel vertical) {
		theImageScroller.getHorizontalScrollBar().setModel(horizontal);
		theImageScroller.getVerticalScrollBar().setModel(vertical);
	}

	public BoundedRangeModel getHorizontalScrollBarModel() {
		return theImageScroller.getHorizontalScrollBar().getModel();
	}

	public BoundedRangeModel getVerticalScrollBarModel() {
		return theImageScroller.getVerticalScrollBar().getModel();
	}

	private class TheImage extends JComponent implements Scrollable {

		public double getSizeRatio() {
			return Math.min(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
		}

		@Override
		public void paint(Graphics g) {
			paint(g, true, getWidth(), getHeight());
		}

		public void paint(Graphics gr, boolean withResize, int windowWidth, int windowHeight) {
			if (image == null) {
				gr.setColor(Color.GRAY);
				gr.fillRect(0, 0, windowWidth, windowHeight);
			} else {
				Graphics2D g = (Graphics2D) gr.create();
				int imageWidth = image.getWidth();
				int imageHeight = image.getHeight();
				if (withResize) {
					switch (resizeStrategy) {
					case NO_RESIZE:
						break;
					case SHRINK_TO_FIT:
						double shrink = Math.min(getSizeRatio(), 1);
						g.scale(shrink, shrink);
						break;
					}
				}
				g.drawImage(image, 0, 0, null);
				if (points != null) {
					int radius = settings.getPointRadius();
					if (settings.getPointStrokeColor() != null) {
						g.setColor(settings.getPointStrokeColor());
						for (Point p : points)
							g.drawOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
					}
					if (settings.getPointFillColor() != null) {
						g.setColor(settings.getPointFillColor());
						for (Point p : points)
							g.fillOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
					}
				}
				if (shapes != null) {
					g.setColor(Color.red);
					for (Shape s : shapes)
						g.draw(s);
				}
				g.dispose();
			}
		}

		@Override
		public Dimension getPreferredSize() {
			if (image == null) return super.getPreferredSize();
			return new Dimension(image.getWidth(), image.getHeight());
		}

		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}

		public boolean getScrollableTracksViewportWidth() {
			return resizeStrategy == ResizeStrategy.SHRINK_TO_FIT;
		}

		public boolean getScrollableTracksViewportHeight() {
			return resizeStrategy == ResizeStrategy.SHRINK_TO_FIT;
		}
	}
}