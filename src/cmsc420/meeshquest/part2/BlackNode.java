package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class BlackNode extends Node {

	private City city;
	private int x,y,height,width;
	public BlackNode(City c, int x, int y, int height, int width) {
		this.city = c;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}
	
	public Node add(City c, int x, int y, int height, int width) {
		if (c.equals(city)) {
			//this means we tried adding a city that is already in the map.
			return null;
		}
		GrayNode g = new GrayNode(this.x, this.y, this.height, this.width);
		g = (GrayNode) g.add(city, this.x, this.y, this.height, this.width);
		g = (GrayNode) g.add(c, x, y, height, width);
		return g;
	}
	
	public Node remove(City c) {
		return new WhiteNode(x, y, height, width);
	}
	
	public Rectangle2D.Float getRect() {
		return new Rectangle2D.Float((float) x,(float) y, (float) height, (float) width);
	}
	
	public City getCity() {
		return this.city;
	}
	
	public Element printNode(Document doc) {
		Element blackNode = doc.createElement("black");
		blackNode.setAttribute("name", city.getName());
		blackNode.setAttribute("x", "" + city.getX());
		blackNode.setAttribute("y", "" + city.getY());
		return blackNode;
	}
	
	public CanvasPlus drawMap(CanvasPlus cp) {
		cp.addRectangle(x, y, width, height, Color.BLACK, false);
		cp.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
		return cp;
	}
}
