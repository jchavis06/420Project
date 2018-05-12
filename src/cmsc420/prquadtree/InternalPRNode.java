package cmsc420.prquadtree;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cmsc420.geometry.City;
import cmsc420.utils.*;


/**
 * Represents an internal PRNode of a PR Quadtree.
 */
public class InternalPRNode extends PRNode {
	/** children PRNodes of this PRNode */
	public PRNode[] children;

	/** rectangular quadrants of the children PRNodes */
	protected Rectangle2D.Float[] regions;

	/** origin of the rectangular bounds of this PRNode */
	public Point2D.Float origin;

	/** origins of the rectangular bounds of each child PRNode */
	protected Point2D.Float[] origins;

	/** width of the rectangular bounds of this PRNode */
	public int width;

	/** height of the rectangular bounds of this PRNode */
	public int height;

	/** half of the width of the rectangular bounds of this PRNode */
	protected int halfWidth;

	/** half of the height of the rectangular bounds of this PRNode */
	protected int halfHeight;

	/**
	 * Constructs and initializes this internal PR Quadtree PRNode.
	 * 
	 * @param origin
	 *            origin of the rectangular bounds of this PRNode
	 * @param width
	 *            width of the rectangular bounds of this PRNode
	 * @param height
	 *            height of the rectangular bounds of this PRNode
	 */
	public InternalPRNode(Point2D.Float origin, int width, int height) {
		super(PRNode.INTERNAL);

		this.origin = origin;

		children = new PRNode[4];
		for (int i = 0; i < 4; i++) {
			children[i] = EmptyPRNode.instance;
		}

		this.width = width;
		this.height = height;

		halfWidth = width >> 1;
		halfHeight = height >> 1;

		origins = new Point2D.Float[4];
		origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
		origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
				+ halfHeight);
		origins[2] = new Point2D.Float(origin.x, origin.y);
		origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

		regions = new Rectangle2D.Float[4];
		int i = 0;
		while (i < 4) {
			regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
					halfWidth, halfHeight);
			i++;
		}

		/* add a cross to the drawing panel */
		if (Canvas.instance != null) {
            //canvas.addCross(getCenterX(), getCenterY(), halfWidth, Color.d);
			int cx = getCenterX();
			int cy = getCenterY();
            Canvas.instance.addLine(cx - halfWidth, cy, cx + halfWidth, cy, Color.BLACK);
            Canvas.instance.addLine(cx, cy - halfHeight, cx, cy + halfHeight, Color.BLACK);
		}
	}

	public PRNode add(Point2D.Float metro, Point2D.Float origin, int width, int height) {
		for (int i = 0; i < 4; i++) {
			if (intersects(metro, regions[i])) {
				children[i] = children[i].add(metro, origins[i], halfWidth,
						halfHeight);
				break;
			}
		}
		return this;
	}

	public PRNode remove(Point2D.Float metro, Point2D.Float origin, int width,
			int height) {
		for (int i = 0; i < 4; i++) {
			if (intersects(metro, regions[i])) {
				children[i] = children[i].remove(metro, origins[i],
						halfWidth, halfHeight);
			}
		}

		if (getNumEmptyPRNodes() == 4) {
			/* remove cross from the drawing panel */
			if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
			}
			return EmptyPRNode.instance;

		} else if (getNumEmptyPRNodes() == 3 && getNumLeafPRNodes() == 1) {
			/* remove cross from the drawing panel */
            if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
            }
            
			for (PRNode PRNode : children) {
				if (PRNode.getType() == PRNode.LEAF) {
					return PRNode;
				}
			}
			/* should never get here */
			return null;

		} else {
			return this;
		}

	}
	
	
	/**
	 * Returns if a point lies within a given rectangular bounds according to
	 * the rules of the PR Quadtree.
	 * 
	 * @param point
	 *            point to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */	
	public static boolean intersects(Point2D point, Rectangle2D rect) {
		return (point.getX() >= rect.getMinX() && point.getX() < rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() < rect
				.getMaxY());
	}

	/**
	 * Gets the number of empty child PRNodes contained by this internal PRNode.
	 * 
	 * @return the number of empty child PRNodes
	 */
	protected int getNumEmptyPRNodes() {
		int numEmptyPRNodes = 0;
		for (PRNode PRNode : children) {
			if (PRNode == EmptyPRNode.instance) {
				numEmptyPRNodes++;
			}
		}
		return numEmptyPRNodes;
	}

	/**
	 * Gets the number of leaf child PRNodes contained by this internal PRNode.
	 * 
	 * @return the number of leaf child PRNodes
	 */
	protected int getNumLeafPRNodes() {
		int numLeafPRNodes = 0;
		for (PRNode PRNode : children) {
			if (PRNode.getType() == PRNode.LEAF) {
				numLeafPRNodes++;
			}
		}
		return numLeafPRNodes;
	}

	/**
	 * Gets the child PRNode of this PRNode according to which quadrant it falls
	 * in
	 * 
	 * @param quadrant
	 *            quadrant number (top left is 0, top right is 1, bottom
	 *            left is 2, bottom right is 3)
	 * @return child PRNode
	 */
	public PRNode getChild(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return children[quadrant];
		}
	}

	/**
	 * Gets the rectangular region for the specified child PRNode of this
	 * internal PRNode.
	 * 
	 * @param quadrant
	 *            quadrant that child lies within
	 * @return rectangular region for this child PRNode
	 */
	public Rectangle2D.Float getChildRegion(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return regions[quadrant];
		}
	}

	/**
	 * Gets the rectangular region contained by this internal PRNode.
	 * 
	 * @return rectangular region contained by this internal PRNode
	 */
	public Rectangle2D.Float getRegion() {
		return new Rectangle2D.Float(origin.x, origin.y, width, height);
	}

	/**
	 * Gets the center X coordinate of this PRNode's rectangular bounds.
	 * 
	 * @return center X coordinate of this PRNode's rectangular bounds
	 */
	public int getCenterX() {
		return (int) origin.x + halfWidth;
	}

	/**
	 * Gets the center Y coordinate of this PRNode's rectangular bounds.
	 * 
	 * @return center Y coordinate of this PRNode's rectangular bounds
	 */
	public int getCenterY() {
		return (int) origin.y + halfHeight;
	}

	/**
	 * Gets half the width of this internal PRNode.
	 * @return half the width of this internal PRNode
	 */
	public int getHalfWidth() {
		return halfWidth;
	}

	/** 
	 * Gets half the height of this internal PRNode.
	 * @return half the height of this internal PRNode
	 */
	public int getHalfHeight() {
		return halfHeight;
	}
}
