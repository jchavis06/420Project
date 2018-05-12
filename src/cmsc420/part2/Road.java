package cmsc420.part2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;

public class Road {

	private City cityA, cityB;
	private String start, end; //based on alphabetical order.
	private float length;
	private Line2D.Float line;
	//private float angle;
	
	public Road(City cityA, City cityB) {
		this.cityA = cityA;
		this.cityB = cityB;
		this.line = new Line2D.Float((float)cityA.getX(), (float)cityA.getY(), (float)cityB.getX(), (float)cityB.getY());
		
		if (cityA.getName().compareTo(cityB.getName()) < 0) {
			this.start = cityA.getName();
			this.end = cityB.getName();
		} else {
			this.start = cityB.getName();
			this.end = cityA.getName();
		}
	}
	
	/**
	 * Not sure if I will need this method, but it will return true if 
	 * the parameter matches one of the two cities that are endpoints of this road.
	 * (Maybe in the future we will need to add if it intersects roads that are not endpoints.
	 */
	public boolean intersectsCity(City city) {
		return (city.equals(this.cityA) || city.equals(this.cityB));
	}
	
	public boolean intersectsOrInsideCircle(Circle2D.Float circle) {
		//take the center of your circle as a point. Find distance from that point to our line.
		//If the distance from point to line is > radius, line doesn't intersect circle.
		
		//first we need to make sure the circle is in between the two points. 
		float cX = (float)circle.getCenterX();
		float cY = (float)circle.getCenterY();
		
		//if either of the two points are inside the circle, we know the road intersects.
		if (Inclusive2DIntersectionVerifier.intersects(line.getP1(), circle) || Inclusive2DIntersectionVerifier.intersects(line.getP2(), circle)) {
			return true;
		}
		
		//now we know that neither of the two points are inside the circle.
		if (line.getX1() > cX) {
			//this means 1 point is to the right of the center of the circle. 
			//so 1 point should also be to the left of the circle.
			if (line.getX2() <= cX) {
				//may continue, the second point is to the left. Now all we check is if it is between the Y bounds.
				if (line.getY1() > cY) {
					//1 point is above the center, 1 point SHOULD be below the center
					if (line.getY2() > cY) {
						return false;
					}
				} else {
					//we know 1 point is below, so now one point should be above
					if (line.getY2() < cY){
						return false;
					}
				}
			} else {
				//both points are to the right of the center of the circle.
				return false;
			}
		} else {
			//this means 1 point is to the left of the center of the circle.
			//so 1 point should also be to the right of the circle.
			if (line.getX2() >= cX) {
				//may continue by checking the Y bounds.
				if (line.getY1() > cY) {
					//1 point is above the center, 1 point SHOULD be below the center
					if (line.getY2() > cY) {
						return false;
					}
				} else {
					//we know 1 point is below, so now one point should be above
					if (line.getY2() < cY){
						return false;
					}
				}
			} else {
				return false;
			}
		}
		float distance = this.getDistanceToPoint(new Point2D.Float(cX, cY));
		float radius = (float)circle.getRadius();
		if (distance <= radius) {
			return true;
		}
		
		return false;
	}
	
	public boolean intersectsLine(Line2D.Float line) {
		return Inclusive2DIntersectionVerifier.intersects(this.line, line);
	}
	
	public boolean intersectsRectangle(Rectangle2D.Float rect) {
		return Inclusive2DIntersectionVerifier.intersects(this.line, rect);
	}
	
	public float getDistanceToPoint(Point2D.Float point) {
//		float x1 = (float)cityA.getX();
//		float y1 = (float)cityA.getY();
//		float x2 = (float)cityB.getX();
//		float y2 = (float)cityB.getY();
//		
//		float pX = (float)point.getX();
//		float pY = (float)point.getY();
//		
//		float numerator = Math.abs(((y2 - y1)*(pX)) - ((x2 - x1)*(pY)) + (x2*y1) - (y2*x1));
//		float denominator = (float)Math.sqrt((Math.pow((y2 - y1),2) + Math.pow((x2-x1),2)));
//		
//		float distance = numerator / denominator;
		float distance = (float) this.line.ptSegDist(point);
		//float distance = (float) Shape2DDistanceCalculator.distance(this.line, new Rectangle2D.Float((float)point.getX(), (float)point.getY(), 0, 0));
		return distance;
	}
	public float getLength() {
		return this.length;
	}
	
	public Line2D.Float getLineSegment() {
		return this.line;
	}
	
	public String getStart() {
		return this.start;
	}
	
	public String getEnd() {
		return this.end;
	}
	
	public Element printRoad(Document doc) {
		Element road = doc.createElement("road");
		road.setAttribute("end", this.end);
		road.setAttribute("start", this.start);
		return road;
	}
}
