package cmsc420.part2;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class City {

	private String name;
	private Point2D.Float coordinates;
	private int radius;
	private String color;
	private boolean isIsolated;
	
	
	public City(String name, int x, int y, int radius, String color, boolean isolated) {
		this.name = name;
		this.coordinates = new Point2D.Float((float) x, (float) y);
		this.radius = radius;
		this.color = color;
		this.isIsolated = isolated;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getRadius() {
		return this.radius;
	}
	
	public Point2D.Float getCoordinates() {
		return this.coordinates;
	}
	
	public int getX() {
		return (int) this.coordinates.getX();
	}
	
	public int getY() {
		return (int) this.coordinates.getY();
	}
	
	public String getColor() {
		return this.color;
	}
	
	public boolean isIsolated() {
		return this.isIsolated;
	}
	
	public void makeIsolated() {
		this.isIsolated = true;
	}
	
	public String toString() {
		return "(" + this.getX() + "," + this.getY() + ")";
	}
	
}
