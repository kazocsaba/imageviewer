package kcsaba.image.viewer;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A default status bar implementation that displays the current mouse position (in pixel
 * coordinates) and the colour of the pixel under the cursor.
 * @author Kazó Csaba
 */
public class DefaultStatusBar extends StatusBar implements ImageMouseMoveListener {
	private final JPanel statusBar;
	private final JLabel posLabel;

	public DefaultStatusBar() {
		statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		posLabel = new JLabel("n/a");
		statusBar.add(posLabel);
	}
	
	@Override
	public void mouseMoved(ImageMouseEvent e) {
		int rgb = e.getImage().getRGB(e.getX(), e.getY());
		Color c = new Color(rgb);
		posLabel.setText(String.format("%d, %d; color %d,%d,%d", e.getX(), e.getY(),
				c.getRed(), c.getGreen(), c.getBlue()));
	}

	@Override
	public void mouseExited(ImageMouseEvent e) {
		posLabel.setText("n/a");
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
	}

	@Override
	protected void unregister(ImageViewer viewer) {
		viewer.removeImageMouseMoveListener(this);
	}
	
}
