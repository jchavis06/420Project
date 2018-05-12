package cmsc420.prquadtree;

import java.awt.geom.Point2D;

import cmsc420.geometry.City;
import cmsc420.geometry.Metropole;

/**
 * PRNode abstract class for a PR Quadtree. A PRNode can either be an empty
 * PRNode, a leaf PRNode, or an internal PRNode.
 */
public abstract class PRNode {
	/** Type flag for an empty PR Quadtree PRNode */
	public static final int EMPTY = 0;

	/** Type flag for a PR Quadtree leaf PRNode */
	public static final int LEAF = 1;

	/** Type flag for a PR Quadtree internal PRNode */
	public static final int INTERNAL = 2;

	/** type of PR Quadtree PRNode (either empty, leaf, or internal) */
	protected final int type;

	/**
	 * Constructor for abstract PRNode class.
	 * 
	 * @param type
	 *            type of the PRNode (either empty, leaf, or internal)
	 */
	protected PRNode(final int type) {
		this.type = type;
	}

	/**
	 * Adds a city to the PRNode. If an empty PRNode, the PRNode becomes a leaf
	 * PRNode. If a leaf PRNode already, the leaf PRNode becomes an internal PRNode
	 * and both cities are added to it. If an internal PRNode, the city is
	 * added to the child whose quadrant the city is located within.
	 * 
	 * @param metropole
	 *            metropole to be added to the PR Quadtree
	 * @param origin
	 *            origin of the rectangular bounds of this PRNode
	 * @param width
	 *            width of the rectangular bounds of this PRNode
	 * @param height
	 *            height of the rectangular bounds of this PRNode
	 * @return this PRNode after the city has been added
	 */
	public abstract PRNode add(Point2D.Float metropole, Point2D.Float origin, int width,
			int height);

	/**
	 * Removes a city from the PRNode. If this is a leaf PRNode and the city is
	 * contained in it, the city is removed and the PRNode becomes a leaf
	 * PRNode. If this is an internal PRNode, then the removal command is passed
	 * down to the child PRNode whose quadrant the city falls in.
	 * 
	 * @param city
	 *            city to be removed
	 * @param origin
	 *            origin of the rectangular bounds of this PRNode
	 * @param width
	 *            width of the rectangular bounds of this PRNode
	 * @param height
	 *            height of the rectangular bounds of this PRNode
	 * @return this PRNode after the city has been removed
	 */
	public abstract PRNode remove(Point2D.Float metropole, Point2D.Float origin, int width,
			int height);

	/**
	 * Gets the type of the PRNode (either empty, leaf, or internal).
	 * 
	 * @return type of the PRNode
	 */
	public int getType() {
		return type;
	}
}

