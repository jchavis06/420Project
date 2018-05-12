package cmsc420.prquadtree;

import java.awt.geom.Point2D;

import cmsc420.geometry.*;

/**
 * Represents an empty leaf PRNode of a PR Quadtree.
 */
public class EmptyPRNode extends PRNode {
	
	/** empty PR Quadtree PRNode */
	public static EmptyPRNode instance = new EmptyPRNode();
	
	/**
	 * Constructs and initializes an empty PRNode.
	 */
	public EmptyPRNode() {
		super(PRNode.EMPTY);
	}

	public PRNode add(Point2D.Float metro, Point2D.Float origin, int width, int height) {
		PRNode leafPRNode = new LeafPRNode();
		return leafPRNode.add(metro, origin, width, height);
	}

	public PRNode remove(Point2D.Float metro, Point2D.Float origin, int width,
			int height) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}
}
