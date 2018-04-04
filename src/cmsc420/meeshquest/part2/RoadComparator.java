package cmsc420.meeshquest.part2;

import java.util.Comparator;

public class RoadComparator implements Comparator<Road>{

	@Override
	public int compare(Road o1, Road o2) {
		String o1Start = o1.getStart();
		String o2Start = o2.getStart();
		if (o1Start.compareTo(o2Start) == 0) {
			String o1End = o1.getEnd();
			String o2End = o2.getEnd();
			if (o1End.compareTo(o2End) > 0) {
				return -1;
			} else {
				return 1;
			}
		} else if (o1Start.compareTo(o2Start) > 0) {
			return -1;
		} else {
			return 1;
		}
	} 

}
