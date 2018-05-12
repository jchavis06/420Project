package cmsc420.geometry ;


import java.util.Comparator;

/**
 * Compares two cities based on location of x and y coordinates. First compares
 * the x values of each {@link City}. If the x values are the same, then the y values of
 * each City are compared.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 23 Jan 2007
 */
public class CityLocationComparator implements Comparator<City> {

	public int compare(final City one, final City two) {
		
		//first make sure they are in the same remote coordinates
		if (one.getRemoteY() < two.getRemoteY()) {
			return -1;
		} else if (one.getRemoteY() > two.getRemoteY()) {
			return 1;
		} else {
			//check remoteX coordinate
			if (one.getRemoteX() < two.getRemoteX()) {
				return -1;
			} else if (one.getRemoteX() > two.getRemoteX()) {
				return 1;
			} else {
				//remote city coordinates are equal. now check local coordinates.
				if (one.getLocalY() < two.getLocalY()) {
					return -1;
				} else if (one.getLocalY() > two.getLocalY()) {
					return 1;
				} else {
					/* one.getY() == two.getY() */
					if (one.getLocalX() < two.getLocalX()) {
						return -1;
					} else if (one.getLocalX() > two.getLocalX()) {
						return 1;
					} else {
						/* one.getX() == two.getX() */
						return 0;
					}
				}
			}
		}
	}
}