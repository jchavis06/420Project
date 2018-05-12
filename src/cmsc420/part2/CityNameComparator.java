package cmsc420.part2;

import java.util.Comparator;

public class CityNameComparator implements Comparator<String> {
	public int compare (String city1, String city2) {
		return -1 * city1.compareTo(city2); //may have to reverse this.
	}
}
