package hu.kazocsaba.imageviewer;

import java.awt.Color;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A status bar implementation that display information based on a pixel of the image. Call the
 * {@link #setPixel(int, int)} function to specify which pixel it should display. The current implementation shows
 * the position and color of the pixel; override {@link #updateLabel(java.awt.image.BufferedImage, int, int, int)} to
 * customize this information.
 * @author Kazó Csaba
 */
public class PixelInfoStatusBar extends StatusBar {
	
	private final JPanel statusBar;
	/** The label displaying the info text. */
	protected final JLabel label;

	private final Insets statusBarInsets;
	
	/** The x coordinate of the pixel currently displayed; -1 if none. */
	private int currentX=-1;
	/** The y coordinate of the pixel currently displayed; -1 if none. */
	private int currentY=-1;
	
	private PropertyChangeListener propertyChangeListener=new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			update();
		}
	};
	
	/**
	 * Creates a new instance.
	 */
	public PixelInfoStatusBar() {
		statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		label = new JLabel("n/a");
		statusBar.add(label);
		statusBarInsets=statusBar.getInsets();
	}

	@Override
	public final JComponent getComponent() {
		return statusBar;
	}

	/**
	 * Sets the pixel this status bar is to display information on. The values may be arbitrary; you can use negative
	 * values to indicate that there is nothing to display.
	 * @param x the x coordinate of the pixel
	 * @param y the y coordinate of the pixel
	 */
	public void setPixel(int x, int y) {
		// (-1,-1) indicates 'no data'; change all other representations to match
		if (x<0) {
			x=-1;
			y=-1;
		} else if (y<0) {
			x=-1;
			y=-1;
		}
		if (currentX!=x || currentY!=y) {
			currentX=x;
			currentY=y;
			update();
		}
	}
	/**
	 * Updates the info label. This function is called when either the selected pixel is changed via {@code setPixel}
	 * or the image shown in the viewer is changed via {@code ImageViewer.setImage}. You can call this method to indicate
	 * that the message should be updated for some different reason.
	 */
	protected final void update() {
		BufferedImage image=getImageViewer().getImage();
		if (image==null || currentX<0 || currentY<0 || currentX>=image.getWidth() || currentY>=image.getHeight())
			label.setText("n/a");
		else
			updateLabel(image, currentX, currentY, statusBar.getWidth()-statusBarInsets.left-statusBarInsets.right);
	}
	/**
	 * This function updates the contents of the {@link #label} to indicate that no data can be shown. By default,
	 * this function is called by {@code update} when the image is {@code null} or the pixel to be shown is not within
	 * bounds.
	 */
	protected void updateLabelNoData() {
		label.setText("n/a");
	}
	
	/**
	 * This function updates the contents of the {@link #label}. It is called when the highlighted pixel or the image changes,
	 * and only when the image is not {@code null} and the pixel is within the bounds of the image. If either of these
	 * conditions aren't true, the {@code update} function calls {@link #updateLabelNoData()} instead.
	 * Override it to provide a custom message.
	 * @param image the current image displayed in the viewer
	 * @param x the x coordinate of the pixel that should be displayed
	 * @param y the y coordinate of the pixel that should be displayed
	 * @param availableWidth the maximum label width that can be displayed; you can use this parameter to specify a shorter
	 * message if there is not enough room
	 */
	protected void updateLabel(BufferedImage image, int x, int y, int availableWidth) {
		if (image.getRaster().getNumBands()==1) {
			label.setText(String.format("%d, %d; intensity %d", x, y,
					image.getRaster().getSample(x, y, 0)));
		} else if (image.getRaster().getNumBands()==4) {
			int rgb = image.getRGB(x, y);
			Color c = new Color(rgb, true);
			label.setText(String.format("%d, %d; color %d,%d,%d, alpha %d", x, y,
					c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
		} else {
			int rgb = image.getRGB(x, y);
			Color c = new Color(rgb);
			label.setText(String.format("%d, %d; color %d,%d,%d", x, y,
					c.getRed(), c.getGreen(), c.getBlue()));
		}

		if (availableWidth<label.getPreferredSize().width) {
			// not enough space, shorter version is required
			if (image.getRaster().getNumBands()==1) {
				label.setText(String.format("%d, %d; %d", x, y,
						image.getRaster().getSample(x, y, 0)));
			} else if (image.getRaster().getNumBands()==4) {
				int rgb = image.getRGB(x, y);
				Color c = new Color(rgb, true);
				label.setText(String.format("%d, %d; (%d,%d,%d,%d)", x, y,
						c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
			} else {
				int rgb = image.getRGB(x, y);
				Color c = new Color(rgb);
				label.setText(String.format("%d, %d; (%d,%d,%d)", x, y,
						c.getRed(), c.getGreen(), c.getBlue()));
			}
		}
	}

	@Override
	protected void register(ImageViewer viewer) {
		viewer.addPropertyChangeListener("image", propertyChangeListener);
	}

	@Override
	protected void unregister(ImageViewer viewer) {
		viewer.removePropertyChangeListener("image", propertyChangeListener);
	}

}
