package cmsc420.geometry;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class MetropoleComparator implements Comparator<Metropole>{

	public int compare(final Metropole one, final Metropole two) {
		Point2D.Float onePT = one.getLocation();
		Point2D.Float twoPT = two.getLocation();
		
		if (onePT.getY() < twoPT.getY()) {
			return -1;
		} else if (onePT.getY() > twoPT.getY()) {
			return 1;
		} else {
			/* onePT.getY() == twoPT.getY() */
			if (onePT.getX() < twoPT.getX()) {
				return -1;
			} else if (onePT.getX() > twoPT.getX()) {
				return 1;
			} else {
				/* onePT.getX() == twoPT.getX() */
				return 0;
			}
		}
	}
}
