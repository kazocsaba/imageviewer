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
 * A default status bar implementation that displays the current mouse position (in pixel
 * coordinates) and the colour of the pixel under the cursor.
 * @author Kazó Csaba
 */
public final class DefaultStatusBar extends StatusBar implements ImageMouseMotionListener, PropertyChangeListener {
	private final JPanel statusBar;
	private final JLabel posLabel;
	
	private int currentX=-1, currentY=-1;
	
	private final Insets statusBarInsets;

	public DefaultStatusBar() {
		statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		posLabel = new JLabel("n/a");
		statusBar.add(posLabel);
		statusBarInsets=statusBar.getInsets();
	}
	
	@Override
	public void mouseMoved(ImageMouseEvent e) {
		currentX=e.getX();
		currentY=e.getY();
		update(e.getImage());
	}

	@Override
	public void mouseExited(ImageMouseEvent e) {
		currentX=-1; currentY=-1;
		update(e.getImage());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		update(getImageViewer().getImage());
	}
	
	private void update(BufferedImage image) {
		if (currentX==-1 || image==null)
			posLabel.setText("n/a");
		else {
			if (image.getRaster().getNumBands()==1) {
				posLabel.setText(String.format("%d, %d; intensity %d", currentX, currentY,
						image.getRaster().getSample(currentX, currentY, 0)));
			} else if (image.getRaster().getNumBands()==4) {
				int rgb = image.getRGB(currentX, currentY);
				Color c = new Color(rgb, true);
				posLabel.setText(String.format("%d, %d; color %d,%d,%d, alpha %d", currentX, currentY,
						c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
			} else {
				int rgb = image.getRGB(currentX, currentY);
				Color c = new Color(rgb);
				posLabel.setText(String.format("%d, %d; color %d,%d,%d", currentX, currentY,
						c.getRed(), c.getGreen(), c.getBlue()));
			}
			
			if (statusBar.getWidth()-statusBarInsets.left-statusBarInsets.right<posLabel.getPreferredSize().width) {
				// not enough space, shorter version is required
				if (image.getRaster().getNumBands()==1) {
					posLabel.setText(String.format("%d, %d; %d", currentX, currentY,
							image.getRaster().getSample(currentX, currentY, 0)));
				} else if (image.getRaster().getNumBands()==4) {
					int rgb = image.getRGB(currentX, currentY);
					Color c = new Color(rgb, true);
					posLabel.setText(String.format("%d, %d; (%d,%d,%d,%d)", currentX, currentY,
							c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
				} else {
					int rgb = image.getRGB(currentX, currentY);
					Color c = new Color(rgb);
					posLabel.setText(String.format("%d, %d; (%d,%d,%d)", currentX, currentY,
							c.getRed(), c.getGreen(), c.getBlue()));
				}
			}
		}
	}

	@Override
	public void mouseEntered(ImageMouseEvent e) {}

	@Override
	public void mouseDragged(ImageMouseEvent e) {}
	
	@Override
	public JComponent getComponent() {
		return statusBar;
	}

	@Override
	protected void register(ImageViewer viewer) {
		viewer.addImageMouseMotionListener(this);
		viewer.addPropertyChangeListener("image", this);
	}

	@Override
	protected void unregister(ImageViewer viewer) {
		viewer.removeImageMouseMotionListener(this);
		viewer.removePropertyChangeListener("image", this);
	}
	
}
