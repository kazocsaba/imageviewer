package hu.kazocsaba.imageviewer;

import java.awt.Color;
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
public class DefaultStatusBar extends StatusBar implements ImageMouseMoveListener, PropertyChangeListener {
	private final JPanel statusBar;
	private final JLabel posLabel;
	
	private int currentX=-1, currentY=-1;

	public DefaultStatusBar() {
		statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		posLabel = new JLabel("n/a");
		statusBar.add(posLabel);
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
		if ("image".equals(evt.getPropertyName()))
			update((BufferedImage)evt.getNewValue());
	}
	
	private void update(BufferedImage image) {
		if (currentX==-1 || image==null)
			posLabel.setText("n/a");
		else {
			if (image.getRaster().getNumBands()==1) {
				posLabel.setText(String.format("%d, %d; intensity %d", currentX, currentY,
						image.getRaster().getSample(currentX, currentY, 0)));
				
			} else {
				int rgb = image.getRGB(currentX, currentY);
				Color c = new Color(rgb);
				posLabel.setText(String.format("%d, %d; color %d,%d,%d", currentX, currentY,
						c.getRed(), c.getGreen(), c.getBlue()));
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
		viewer.addImageMouseMoveListener(this);
		viewer.addPropertyChangeListener("image", this);
	}

	@Override
	protected void unregister(ImageViewer viewer) {
		viewer.removeImageMouseMoveListener(this);
		viewer.removePropertyChangeListener("image", this);
	}
	
}
