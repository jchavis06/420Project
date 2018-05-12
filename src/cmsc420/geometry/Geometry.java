package cmsc420.geometry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Geometry2D;

public abstract class Geometry implements Geometry2D, Comparable<Geometry> {
	@Override
	public int compareTo(Geometry o) {
		if (this.isCity()) {
			if (o.isCity()) {
				// both are cities
				return ((City) o).getName().compareTo(((City) this).getName());
			} else {
				// this is a city, o is a road
				return -1;
			}
		} else if (this.isAirport()) {
			return -1;
		} else if (this.isTerminal()) {
			return -1;
		} else {
			// this is a road
			if (o.isCity()) {
				// o is a city
				return 1;
			} else if (o.isAirport()) {
				return 1;
			} else if (o.isTerminal()) {
				return 1;
			} else {
				// o is a road
				if (((Road) this).getStart().getName()
						.compareTo(((Road) o).getStart().getName()) == 0) {
					// start names are the same so compare end names
					return ((Road) o).getEnd().getName()
							.compareTo(((Road) this).getEnd().getName());
				} else {
					/* start names are different; compare start names */
					return ((Road) o).getStart().getName()
							.compareTo(((Road) this).getStart().getName());
				}
			}
		}
	}
	
	public boolean isRoad() {
		return getType() == Geometry2D.SEGMENT;
	}

	public boolean isCity() {
		return (getType() == Geometry2D.POINT) && (getCityType() == 1);
	}
	
	//used to distinguish between city, airport, terminals.
	public int getCityType() {
		return -1;
	}

	public boolean isAirport() {
		return (getType() == Geometry2D.POINT) && (getCityType() == 2);
	}
	
	public boolean isTerminal() {
		return (getType() == Geometry2D.POINT) && (getCityType() == 3);
	}

	public Element printNode(Document doc) {
		return null;
	}
}
