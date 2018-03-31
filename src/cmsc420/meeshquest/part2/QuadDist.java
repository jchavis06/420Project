package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cmsc420.geom.Shape2DDistanceCalculator;

public class QuadDist {

	private Rectangle2D rect;
	private double distance;
	private Node n;
	private int pointX, pointY;
	
	public QuadDist(Rectangle2D.Float rect, int pointX, int pointY, Node n) {
		this.rect = rect;
		this.pointX = pointX;
		this.pointY = pointY;
		this.n = n;
		this.distance = Shape2DDistanceCalculator.distance(new Point2D.Float((float) pointX, (float) pointY), rect);
	}
	
	public double getDist() {
		return this.distance;
	}
	
	public Node getNode() {
		return this.n;
	}
	
	//NOTE THIS SHOULD ONLY BE CALLED IF CONFIRMED TO BE A BLACK NODE.
	public double getBlackNodeDistance() {
		BlackNode node = ((BlackNode) n);
		City c = node.getCity();
		int x = c.getX();
		int y = c.getY();
		double distance = Point2D.distance((float) pointX, (float) pointY, (float) x, (float) y);
		return distance;
	}
}
