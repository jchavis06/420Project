package cmsc420.pmquadtree;


import java.util.ArrayList;

import cmsc420.geometry.City;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;
import cmsc420.pmquadtree.PMQuadtree.Node;

public class PM3Validator implements Validator {
	
	public boolean valid(final Black node) {
		return (node.getNumPoints() <= 1);
	}
	
	/*
	 * from piazza
	 * a gray node with 4 black nodes not one of which contains a vertex should collapse 
	 * into a single black node since pm3's don't split for edges, only vertexes.
	 */
	public boolean valid(final Gray node) {
		int totalCities = 0;
		ArrayList<City> citiesContained = new ArrayList<City>();
		for (int i = 0; i < 4; i++) {
			Node child = node.children[i];
			if (child.getType() == Node.BLACK) {
				Black b = ((Black) child);
				//totalCities += b.getNumPoints();
				if (b.getNumPoints() > 0) {
					//have to make sure we are not double counting cities in the gray node 
					//in case a city is on a quadrant border.
					City c = b.getCity();
					if (!citiesContained.contains(c)) {
						//no other quadrand contains this city.
						citiesContained.add(c);
						totalCities ++;
					}
				}
			} else if (child.getType() == Node.GRAY) {
				//for now, assume a gray with a gray inside means its a valid node.
				return true;
			}
		}
		
		return (totalCities > 1);
	}
}
