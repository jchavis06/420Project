package cmsc420.meeshquest.part1;

import java.util.Comparator;

public class CityComparator implements Comparator<City> {
	public int compare (City c1, City c2) {
		String city1 = c1.getName();
		String city2 = c2.getName();
		return -1 * city1.compareTo(city2);
	}
}
