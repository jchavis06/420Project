package cmsc420.meeshquest.part1;

import java.util.Comparator;

public class QuadDistComp implements Comparator<QuadDist> {

	@Override
	public int compare(QuadDist o1, QuadDist o2) {
		double dist1 = o1.getDist();
		double dist2 = o2.getDist();
		
		if (dist1 == dist2) {
			if (o1.getNode() instanceof BlackNode && o2.getNode() instanceof BlackNode) {
				//if they are both black nodes, we can compare city distances.
				return (int) (o1.getBlackNodeDistance() - o2.getBlackNodeDistance());
			}
			return 1; //if 2 quadrants are the same, just return the 1st one as better.
		} else {
			return (int) (dist1 - dist2);
		}
	}

}
