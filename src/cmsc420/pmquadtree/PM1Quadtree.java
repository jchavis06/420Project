package cmsc420.pmquadtree;

/**
 * A PM Quadtree of order 3 has the following rules:
 * <p>
 * 1. At most, one vertex can lie in a region represented by a quadtree leaf
 * node.
 * <p>
 * 2. Each region's quadtree leaf node is maximal.
 */
public class PM1Quadtree extends PMQuadtree {
	/**
	 * Constructs and initializes this PM Quadtree of order 1.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public PM1Quadtree(final int spatialWidth, final int spatialHeight) {
		super(new PM1Validator(), spatialWidth, spatialHeight, 3);
	}
}
