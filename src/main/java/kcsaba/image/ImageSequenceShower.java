package kcsaba.image;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * 
 * @author Kazó Csaba
 */
public abstract class ImageSequenceShower extends JPanel {

	private final AncestorListener ancestorListener = new AncestorListener() {

		public void ancestorAdded(AncestorEvent event) {
			ImageSequenceShower.this.removeAncestorListener(this);
			updateImage();
		}

		public void ancestorRemoved(AncestorEvent event) {}

		public void ancestorMoved(AncestorEvent event) {}
	};
	private final ImageShowerComponent imageShower;
	private int number, position;
	private JButton forwardButton, backwardButton;
	private JLabel locationLabel;

	public ImageSequenceShower() {
		this(0);
	}

	public ImageSequenceShower(int number) {
		this(null, number, 0);
	}

	public ImageSequenceShower(ImageShowerSettings showerSettings, int number, int startPos) {
		super(new BorderLayout());
		if (number < 0 || startPos < 0 || startPos >= number) throw new IllegalArgumentException();
		imageShower = new ImageShowerComponent(showerSettings);
		this.number = number;
		this.position = startPos;
		add(imageShower, BorderLayout.CENTER);

		forwardButton = new JButton(">");
		backwardButton = new JButton("<");
		JPanel locationPanel = new JPanel(new FlowLayout());
		locationPanel.add(backwardButton);
		locationPanel.add(createLocationDefinition());
		locationPanel.add(forwardButton);

		forwardButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setPosition(position + 1);
			}
		});
		backwardButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setPosition(position - 1);
			}
		});

		add(locationPanel, BorderLayout.NORTH);

		setPosition(position);
		addAncestorListener(ancestorListener);
	}

	public ImageShowerComponent getImageShower() {
		return imageShower;
	}
	
	public void setPosition(int pos) {
		if (pos < 0 || pos >= number) throw new IllegalArgumentException("Position " + pos + " out of range");
		position = pos;
		updateLocationDefinition(position);
		forwardButton.setEnabled(position < number - 1);
		backwardButton.setEnabled(position > 0);
		if (isShowing()) updateImage();
	}

	/**
	 * Returns the current position of this image sequence shower.
	 * @param pos the current position, at least zero, less than the number of images
	 */
	public int getPosition() {
		return position;
	}

	protected JComponent createLocationDefinition() {
		locationLabel = new JLabel();
		return locationLabel;
	}

	protected void updateLocationDefinition(int pos) {
		locationLabel.setText(String.format("%d/%d", pos + 1, number));
	}
	/**
	 * Called to (re)render the image corresponding to the current position and apply it to the
	 * image shower.
	 */
	public abstract void updateImage();
}
