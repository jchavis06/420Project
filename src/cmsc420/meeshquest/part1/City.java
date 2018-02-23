package cmsc420.meeshquest.part1;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class City {

	private String name;
	private Point2D.Float coordinates;
	private int radius;
	private String color;
	
	
	public City(String name, int x, int y, int radius, String color) {
		this.name = name;
		this.coordinates = new Point2D.Float((float) x, (float) y);
		this.radius = radius;
		this.color = color;
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
	
}
