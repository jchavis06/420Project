package cmsc420.prquadtree;

import java.awt.geom.Point2D;

import cmsc420.geometry.City;
import cmsc420.geometry.Metropole;

/**
 * Represents a leaf PRNode of a PR Quadtree.
 */
public class LeafPRNode extends PRNode {
	/** city contained within this leaf PRNode */
	protected Point2D.Float metropole;

	/**
	 * Constructs and initializes a leaf PRNode.
	 */
	public LeafPRNode() {
		super(PRNode.LEAF);
	}

	/**
	 * Gets the city contained by this PRNode.
	 * 
	 * @return city contained by this PRNode
	 */
	public Point2D.Float getMetropole() {
		return metropole;
	}

	public PRNode add(Point2D.Float newMetropole, Point2D.Float origin, int width,
			int height) {
		if (metropole == null) {
			/* PRNode is empty, add city */
			metropole = newMetropole;
			return this;
		} else {
			/* PRNode is full, partition PRNode and then add city */
			InternalPRNode internalPRNode = new InternalPRNode(origin, width,
					height);
			internalPRNode.add(metropole, origin, width, height);
			internalPRNode.add(newMetropole, origin, width, height);
			return internalPRNode;
		}
	}

	public PRNode remove(Point2D.Float metro, Point2D.Float origin, int width,
			int height) {
		if (!this.metropole.equals(metro)) {
			/* city not here */
			throw new IllegalArgumentException();
		} else {
			/* remove city, PRNode becomes empty */
			this.metropole = null;
			return EmptyPRNode.instance;
		}
	}
}