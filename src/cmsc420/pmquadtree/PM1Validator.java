package cmsc420.pmquadtree;


import java.util.LinkedList;

import cmsc420.geometry.City;
import cmsc420.geometry.Geometry;
import cmsc420.geometry.Road;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;

public class PM1Validator implements Validator {
	
	//valid if only 1 city, as well as no roads that are in this quadrant that
	//are not endpoints to the city in this quadrant.
	public boolean valid(final Black node) {
		//return (node.getNumPoints() <= 1);
		int numV = node.getNumPoints();
		LinkedList<Geometry> geom = node.getGeometry();
		if (numV > 1) {
			return false;
		} else if (numV == 0) {
			//only 1 q-edge is allowed to be in this quadrant.
			//geometry should only be of size one....
			if (geom.size() > 1) {
				return false;
			}
			
			Geometry geo = geom.get(0);
			//should be a road.
			return geo.isRoad();
		} else {
			//1 vertex, only edges that end at this vertex can be included.
			for (Geometry g: geom) {
				//should be one city and the rest roads.
				City point = node.getCity();
				if (g.isCity() || g.isAirport() || g.isTerminal()) {
					//skip it. we already know theres only one point.
				} else if (g.isRoad()) {
					Road r = (Road) g;
					//one of the end points has to be the city.
					if (!r.contains(point)) {
						return false;
					}
				}
			}
			
			return true;
		}
	}
	
	public boolean valid(final Gray node) {
		return true;
	}
}
