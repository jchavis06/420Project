package cmsc420.meeshquest.part2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cmsc420.geom.Shape2DDistanceCalculator;

public class QuadDist {

	private Rectangle2D rect;
	private double distance;
	private Node n;
	private int pointX, pointY;
	private Line2D.Float lineSeg;
	
	public QuadDist(Rectangle2D.Float rect, int pointX, int pointY, Node n) {
		this.rect = rect;
		this.pointX = pointX;
		this.pointY = pointY;
		this.n = n;
		this.distance = Shape2DDistanceCalculator.distance(new Point2D.Float((float) pointX, (float) pointY), rect);
	}
	
	public QuadDist(Rectangle2D.Float rect, Line2D.Float lineSeg, Node n) {
		this.rect = rect;
		this.lineSeg = lineSeg;
		this.n = n; 
		this.distance = Shape2DDistanceCalculator.distance(lineSeg, rect);
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
		
		double distance;
		if (this.lineSeg != null) {
			//calc distance from point to a line.
			float x1 = (float)lineSeg.getX1();
			float y1 = (float)lineSeg.getY1();
			float x2 = (float)lineSeg.getX2();
			float y2 = (float)lineSeg.getY2();
			
			float pX = (float)c.getX();
			float pY = (float)c.getY();
			
			float numerator = Math.abs(((y2 - y1)*(pX)) - ((x2 - x1)*(pY)) + (x2*y1) - (y2*x1));
			float denominator = (float)Math.sqrt((Math.pow((y2 - y1),2) + Math.pow((x2-x1),2)));
			
			distance = numerator / denominator;
		} else {
			distance = Point2D.distance((float) pointX, (float) pointY, (float) x, (float) y);
		}
		
		return distance;
	}
}
