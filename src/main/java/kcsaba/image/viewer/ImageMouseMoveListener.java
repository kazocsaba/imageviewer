package kcsaba.image.viewer;

import java.util.EventListener;

/**
 * Interface for receiving mouse motion events on an image.
 * @author Kazó Csaba
 */
public interface ImageMouseMoveListener extends EventListener {
	/**
	 * Invoked when the mouse has entered a pixel of an image.
	 * @param e the event object containing attributes of the event
	 */
	public void mouseMoved(ImageMouseEvent e);
	/**
	 * Invoked when the mouse has entered the area of an image.
	 * @param e the event object containing attributes of the event
	 */
	public void mouseEntered(ImageMouseEvent e);
	/**
	 * Invoked when the mouse has left the area of an image.
	 * @param e the event object containing attributes of the event
	 */
	public void mouseExited(ImageMouseEvent e);
}
