package cmsc420.geometry;

import java.awt.geom.Point2D;

public class Metropole extends Geometry {

	protected Point2D.Float point;
	
	public Metropole(int x, int y) {
		this.point = new Point2D.Float(x, y);
	}
	
	public Point2D.Float getLocation() {
		return this.point;
	}
	
	public int getType() {
		return POINT;
	}
}
