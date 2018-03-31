package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class CityCoordinateComparator implements Comparator<Point2D.Float> {

	public int compare (Point2D.Float city1, Point2D.Float city2) {
		//sort on the y coordinate first; if two cities
		//have the same y coordinate, compare their x coordinates
		//to determine their final ordering.
		int city1x = (int) city1.getX();
		int city1y = (int) city1.getY();
		int city2x = (int) city2.getX();
		int city2y = (int) city2.getY();
		
		if (city1y == city2y) {
			return city1x - city2x; //might need to flip
		} else {
			return city1y - city2y; //might need to flip
		}
	}
}
