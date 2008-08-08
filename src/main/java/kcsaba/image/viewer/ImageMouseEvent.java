package kcsaba.image.viewer;

import java.awt.image.BufferedImage;
import java.util.EventObject;

/**
 * An event indicating that a mouse action occured over an image.
 * @author Kazó Csaba
 */
public class ImageMouseEvent extends EventObject {
	private BufferedImage image;
	private int x,y;
	public ImageMouseEvent(Object source, BufferedImage image, int x, int y) {
		super(source);
		this.image=image;
		this.x=x;
		this.y=y;
	}
	/**
	 * Returns the image on which the event occured.
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}
	/**
	 * Returns the x coordinate of the pixel related to the event.
	 * @return the x coordinate of the pixel related to the event
	 */
	public int getX() {
		return x;
	}
	/**
	 * Returns the y coordinate of the pixel related to the event.
	 * @return the y coordinate of the pixel related to the event
	 */
	public int getY() {
		return y;
	}
	
}
