package kcsaba.image;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;

/**
 * 
 * @author csaba
 */
public class ImageShowerSettings {
	final Set<JComponent> components = new HashSet<JComponent>(4);

	private int pointRadius = 2;
	private Color pointStrokeColor = Color.RED;
	private Color pointFillColor = null;

	private void repaintComponents() {
		for (JComponent comp : components)
			comp.repaint();
	}

	public int getPointRadius() {
		return pointRadius;
	}

	public void setPointRadius(int pointRadius) {
		if (this.pointRadius == pointRadius) return;
		if (pointRadius <= 0) throw new IllegalArgumentException();
		this.pointRadius = pointRadius;
		repaintComponents();
	}

	public Color getPointStrokeColor() {
		return pointStrokeColor;
	}

	public void setPointStrokeColor(Color pointStrokeColor) {
		this.pointStrokeColor = pointStrokeColor;
		repaintComponents();
	}

	public Color getPointFillColor() {
		return pointFillColor;
	}

	public void setPointFillColor(Color pointFillColor) {
		this.pointFillColor = pointFillColor;
		repaintComponents();
	}
}
