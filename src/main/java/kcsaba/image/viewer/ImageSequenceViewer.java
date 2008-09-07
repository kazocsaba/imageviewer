package kcsaba.image.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A component for displaying a series of images. Supports paging through GUI as well as setting the current
 * position via function {@link #setPosition(int)}. When the position is changed, the {@link #positionChanged()}
 * method is called. Subclasses should override this method to update the image according to the new position.
 * @author Kazó Csaba
 */
public class ImageSequenceViewer extends JPanel {

	private final ImageViewer imageViewer;
	private int number,  position;
	private JButton forwardButton, backwardButton;
	private JLabel locationLabel;

	/**
	 * Creates a new sequence viewer that can display the specified number of images.
	 * @param number the number of images
	 * @throws IllegalArgumentException if the number is negative
	 */
	public ImageSequenceViewer(int number) {
		this(number, 0);
	}

	/**
	 * Creates a new sequence viewer that can display the specified number of images.
	 * @param number the number of images
	 * @param startPos the initial position of the viewer
	 * @throws IllegalArgumentException if the number is negative or the starting position is not valid
	 */
	public ImageSequenceViewer(int number, int startPos) {
		super(new BorderLayout());
		if (number <= 0 || startPos < 0 || startPos >= number)
			throw new IllegalArgumentException();
		imageViewer = new ImageViewer();
		this.number = number;
		this.position = startPos;
		add(imageViewer.getComponent(), BorderLayout.CENTER);

		forwardButton = new JButton(">");
		backwardButton = new JButton("<");
		JPanel locationPanel = new JPanel(new FlowLayout());
		locationPanel.add(backwardButton);
		locationPanel.add(createLocationDefinition());
		locationPanel.add(forwardButton);

		forwardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(position + 1);
			}
		});
		backwardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(position - 1);
			}
		});

		add(locationPanel, BorderLayout.NORTH);

		setPosition(position);
	}

	/**
	 * Called when the current position of the viewer has changed. The default implementation does nothing.
	 * Subclasses should override this method to update the image.
	 */
	protected void positionChanged() {
	}

	public ImageViewer getImageViewer() {
		return imageViewer;
	}

	/**
	 * Sets the position of the viewer.
	 * @param pos the new position of the viewer
	 * @throws IllegalArgumentException if the position is not valid
	 */
	public void setPosition(int pos) {
		if (pos < 0 || pos >= number)
			throw new IllegalArgumentException("Position " + pos + " out of range");
		position = pos;
		updateLocationDefinition(position);
		forwardButton.setEnabled(position < number - 1);
		backwardButton.setEnabled(position > 0);
		positionChanged();
	}

	/**
	 * Returns the current position of this image sequence shower.
	 * @param pos the current position, at least zero, less than the number of images
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Creates and returns the component that displays the current position to the user. The default implementation
	 * creates a <code>JLabel</code>.
	 * @return the location component
	 */
	protected JComponent createLocationDefinition() {
		locationLabel = new JLabel();
		return locationLabel;
	}

	/**
	 * Called when the current position changes to update the location component.
	 * @param pos the current position
	 */
	protected void updateLocationDefinition(int pos) {
		locationLabel.setText(String.format("%d/%d", pos + 1, number));
	}
}
