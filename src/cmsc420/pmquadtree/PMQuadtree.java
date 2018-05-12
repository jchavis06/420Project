package cmsc420.pmquadtree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import cmsc420.exception.AirportDoesNotExistThrowable;
import cmsc420.exception.AirportNotInSameMetropoleThrowable;
import cmsc420.exception.AirportOutOfBoundsThrowable;
import cmsc420.exception.AirportViolatesPMRulesThrowable;
import cmsc420.exception.CityAlreadyExistsThrowable;
import cmsc420.exception.ConnectingCityDoesNotExistThrowable;
import cmsc420.exception.ConnectingCityNotInSameMetropoleThrowable;
import cmsc420.exception.ConnectingCityNotMappedThrowable;
import cmsc420.exception.DuplicateTerminalCoordinatesThrowable;
import cmsc420.exception.DuplicateTerminalNameThrowable;
import cmsc420.exception.PMRuleViolation;
import cmsc420.exception.RoadIntersectsAnotherRoadThrowable;
import cmsc420.exception.TerminalOutOfBoundsThrowable;
import cmsc420.exception.TerminalViolatesPMRulesThrowable;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geometry.Airport;
import cmsc420.geometry.City;
import cmsc420.geometry.CityLocationComparator;
import cmsc420.geometry.Geometry;
import cmsc420.geometry.Road;
import cmsc420.geometry.RoadAdjacencyList;
import cmsc420.geometry.RoadNameComparator;
import cmsc420.geometry.Terminal;
import cmsc420.sortedmap.Treap;

public abstract class PMQuadtree {

	/** stores all mapped roads in the PM Quadtree */
	final protected TreeSet<Road> allRoads;
	
	/** stores how many roads are connected to each city */
	final protected HashMap<String, Integer> numRoadsForCity;
	
	
	protected Treap<String, City> citiesByName;
	
	protected int numCities;
	/** number of isolated cities */
	protected int numAirports;
	
	protected int numTerminals;
	/** root of the PM Quadtree */
	protected Node root;

	/** spatial width of the PM Quadtree */
	final protected int spatialWidth;

	/** spatial height of the PM Quadtree */
	final protected int spatialHeight;

	/** spatial origin of the PM Quadtree (i.e. (0,0)) */
	final protected Point2D.Float spatialOrigin;

	/** validator for the PM Quadtree */
	final protected Validator validator;

	/** singleton white node */
	final protected White white = new White();

	/** order of the PM Quadtree (one of: {1,2,3}) */
	final protected int order;
	
	protected TreeSet<City> citiesByLocation;

	protected RoadAdjacencyList adjacencyList;
	
	public abstract class Node {
		/** Type flag for an empty PM Quadtree leaf node */
		public static final int WHITE = 0;

		/** Type flag for a non-empty PM Quadtree leaf node */
		public static final int BLACK = 1;

		/** Type flag for a PM Quadtree internal node */
		public static final int GRAY = 2;

		/** type of PR Quadtree node (either empty, leaf, or internal) */
		protected final int type;

		/**
		 * Constructor for abstract Node class.
		 * 
		 * @param type
		 *            type of the node (either empty, leaf, or internal)
		 */
		protected Node(final int type) {
			this.type = type;
		}

		/**
		 * Gets the type of this PM Quadtree node. One of: BLACK, WHITE, GRAY.
		 * 
		 * @return type of this PM Quadtree node
		 */
		public int getType() {
			return type;
		}

		/**
		 * Adds a road to this PM Quadtree node.
		 * 
		 * @param g
		 *            road to be added
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return this node after the city has been added
		 * @throws InvalidPartitionThrowable
		 *             if the map if partitioned too deeply
		 * @throws IntersectingRoadsThrowable
		 *             if this road intersects with another road
		 */
		public Node add(final Geometry g, final Point2D.Float origin,
				final int width, final int height) throws PMRuleViolation{
			throw new UnsupportedOperationException();
		}
		
		public Node remove(final Geometry g, final Point2D.Float origin, 
				final int width, final int height) {
			throw new UnsupportedOperationException();
		}
		
		public Node removeRoad(final Road r, final Point2D.Float origin,
				final int width, final int height) {
			throw new UnsupportedOperationException();
		}
		/**
		 * Returns if this node follows the rules of the PM Quadtree.
		 * 
		 * @return <code>true</code> if the node follows the rules of the PM
		 *         Quadtree; <code>false</code> otherwise
		 */
		public boolean isValid() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * White class represents an empty PM Quadtree leaf node.
	 */
	public class White extends Node {
		/**
		 * Constructs and initializes an empty PM Quadtree leaf node.
		 */
		public White() {
			super(WHITE);
		}

		/**
		 * Adds a road to this PM Quadtree node.
		 * 
		 * @param g
		 *            road to be added
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return this node after the city has been added
		 * @throws InvalidPartitionThrowable
		 *             if the map if partitioned too deeply
		 * @throws IntersectingRoadsThrowable
		 *             if this road intersects with another road
		 */
		public Node add(final Geometry g, final Point2D.Float origin,
				final int width, final int height) throws PMRuleViolation{
			final Black blackNode = new Black();
			return blackNode.add(g, origin, width, height);
		}

		/**
		 * Returns if this node follows the rules of the PM Quadtree.
		 * 
		 * @return <code>true</code> if the node follows the rules of the PM
		 *         Quadtree; <code>false</code> otherwise
		 */
		public boolean isValid() {
			return true;
		}

		public String toString() {
			return "white";
		}
	}

	/**
	 * Black class represents a non-empty PM Quadtree leaf node. Black nodes are
	 * capable of storing both cities (points) and roads (line segments).
	 * <p>
	 * Each black node stores cities and roads into its own sorted geometry
	 * list.
	 * <p>
	 * Black nodes are split into a gray node if they do not satisfy the rules
	 * of the PM Quadtree.
	 */
	public class Black extends Node {

		/** list of cities and roads contained within black node */
		final protected LinkedList<Geometry> geometry;

		/** number of cities contained within this black node */
		protected int numPoints;

		/**
		 * Constructs and initializes a non-empty PM Quadtree leaf node.
		 */
		public Black() {
			super(BLACK);
			geometry = new LinkedList<Geometry>();
			numPoints = 0;
		}

		/**
		 * Gets a linked list of the cities and roads contained by this black
		 * node.
		 * 
		 * @return list of cities and roads contained within this black node
		 */
		public LinkedList<Geometry> getGeometry() {
			return geometry;
		}

		/**
		 * Gets the index of the road in this black node's geometry list.
		 * 
		 * @param g
		 *            road to be searched for in the sorted geometry list
		 * @return index of the search key, if it is contained in the list;
		 *         otherwise, (-(insertion point) - 1)
		 */
		private int getIndex(final Geometry g) {
			return Collections.binarySearch(geometry, g);
		}

		/**
		 * Adds a road to this black node. After insertion, if the node becomes
		 * invalid, it will be split into a Gray node.
		 */
		public Node add(final Geometry g, final Point2D.Float origin,
				final int width, final int height) throws PMRuleViolation {
			boolean addedStart = false;
			boolean addedEnd = false;
			if (g.isRoad()) {
				// g is a road
				Road r = (Road)g;
				/* create region rectangle */
				final Rectangle2D.Float rect = new Rectangle2D.Float(origin.x,
						origin.y, width, height);
				
				/* check if start point intersects with region */
				if (Inclusive2DIntersectionVerifier.intersects(r.getStart().toPoint2D(), rect)) {
					addGeometryToList(r.getStart());
					addedStart = true;
				}
	
				/* check if end point intersects with region */
				if (Inclusive2DIntersectionVerifier.intersects(r.getEnd().toPoint2D(), rect)) {
					addGeometryToList(r.getEnd());
					addedEnd = true;
				}
				
			}

			/* add the road or isolated city to the geometry list */
			addGeometryToList(g);
			
			//][
			/* check if this node is valid */
			if (isValid()) {
				/* valid so return this black node */
				return this;
			} else {
				/* invalid so partition into a Gray node */
				if (width <= 1 || height <= 1) {
					//cant partition any further. whatever object has been added needs to be removed.
					geometry.remove(g);
					if (addedStart) {
						geometry.remove(((Road) g).getStart());
					}
					
					if (addedEnd) {
						geometry.remove(((Road)g ).getEnd());
					}
					throw new PMRuleViolation();
				}
				
				try {
					Node temp = partition(origin, width, height);
					return temp;
				} catch (PMRuleViolation e) {
					geometry.remove(g);
					if (addedStart) {
						geometry.remove(((Road) g).getStart());
					}
					
					if (addedEnd) {
						geometry.remove(((Road)g ).getEnd());
					}
					throw new PMRuleViolation();
				}
			}
		}
		
//		public Node removeTerminal(final Terminal t, final Point2D.Float origin, 
//				final int width, final int height) {
//			geometry.remove(t);
//			return this;
//		}
//		
//		public Node removeAirport(final Airport a, final Point2D.Float origin, 
//				final int width, final int height) {
//			geometry.remove(a);
//			return this;
//		}
		
		public Node remove(final Geometry g, final Point2D.Float origin, 
				final int width, final int height) {
			geometry.remove(g);
			if (geometry.size() == 0) {
				return white;
			}
			return this;
		}
		
		

		public Node removeRoad(Road r, final Point2D.Float origin,
				final int width, final int height) {
			//check to see if road intersects this node.
			if (geometry.remove(r)) {
				//this means geometry list contained the road and deleted it. Now we check for cities to unmap.
				City start = r.getStart();
				City end = r.getEnd();
				PMQuadtree.this.removeRoadForCity(start);
				PMQuadtree.this.removeRoadForCity(end);
				if (geometry.contains(start)) {
					if (PMQuadtree.this.numRoadsForCity.get(start.getName()) ==  0) {
						geometry.remove(start);
					}
				}
				
				if (geometry.contains(end)) {
					if (PMQuadtree.this.numRoadsForCity.get(end.getName()) == 0) {
						geometry.remove(end);
					}
				}
			}
			
			if (geometry.size() == 0) {
				return white;
			}
			
			return this;
		}
		
		/**
		 * Adds a road to this node's geometry list.
		 * 
		 * @param g
		 *            road to be added
		 */
		private boolean addGeometryToList(final Geometry g) {
			/* search for the non-existent item */
			final int index = getIndex(g);

			/* add the non-existent item to the list */
			if (index < 0) {
				geometry.add(-index - 1, g);

				if (g.isCity() || g.isAirport() || g.isTerminal()) {
					// g is a city or airport or terminal
					numPoints++;
				}
				return true;
			}
			return false;
		}

		/**
		 * Returns if this node follows the rules of the PM Quadtree.
		 * 
		 * @return <code>true</code> if the node follows the rules of the PM
		 *         Quadtree; <code>false</code> otherwise
		 */
		public boolean isValid() {
			return validator.valid(this);
		}

		/**
		 * Gets the number of cities contained in this black node.
		 * 
		 * @return number of cities contained in this black node
		 */
		public int getNumPoints() {
			return numPoints;
		}

		/**
		 * Partitions an invalid back node into a gray node and adds this black
		 * node's roads to the new gray node.
		 * 
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return the new gray node
		 * @throws InvalidPartitionThrowable
		 *             if the quadtree was partitioned too deeply
		 * @throws IntersectingRoadsThrowable
		 *             if two roads intersect
		 */
		private Node partition(final Point2D.Float origin, final int width, final int height) throws PMRuleViolation{
			//][			
			/* create new gray node */
			Node gray = new Gray(origin, width, height);

			// add isolated cities only; endpoints of roads are added in recursive calls
			// to black.add()
			for (int i = 0; i < numPoints; i++) {
				final Geometry g = geometry.get(i);
				if (isAirport(g)) {
					gray = gray.add(g, origin, width, height); //will throw exception on too deep partitioning.
				}
			}			
			// add roads
			for (int i = numPoints; i < geometry.size(); i++) {
				final Geometry g = geometry.get(i);
				gray = gray.add(g, origin, width, height); //will throw exception on too deep partitioning.
			}
			return gray;
		}

		/**
		 * Returns a string representing this black node and its road list.
		 * 
		 * @return a string representing this black node and its road list
		 */
		public String toString() {
			return "black: " + geometry.toString();
		}

		/**
		 * Returns if this black node contains a city.
		 * 
		 * @return if this black node contains a city
		 */
		public boolean containsCity() {
			return (numPoints > 0);
		}

		/**
		 * @return true if this black node contains at least a road
		 */
		public boolean containsRoad() {
			return (geometry.size() - numPoints) > 0;
		}

		/**
		 * If this black node contains a city, returns the city contained within
		 * this black node. Else returns <code>null</code>.
		 * 
		 * @return the city if it exists, else <code>null</code>
		 */
		public City getCity() {
			final Geometry g = geometry.getFirst();
			return g.isCity() ? (City)g : null;
		}		
	}

	/**
	 * Gray class represents an internal PM Quadtree node.
	 */
	public class Gray extends Node {
		/** this gray node's 4 child nodes */
		final protected Node[] children;

		/** regions representing this gray node's 4 child nodes */
		final protected Rectangle2D.Float[] regions;

		/** origin of the rectangular bounds of this node */
		final protected Point2D.Float origin;

		/** the origin of rectangular bounds of each of the node's child nodes */
		final protected Point2D.Float[] origins;

		/** half the width of the rectangular bounds of this node */
		final protected int halfWidth;

		/** half the height of the rectangular bounds of this node */
		final protected int halfHeight;

		/**
		 * Constructs and initializes an internal PM Quadtree node.
		 * 
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 */
		public Gray(final Point2D.Float origin, final int width,
				final int height) {
			super(GRAY);

			/* set this node's origin */
			this.origin = origin;

			/* initialize the children as white nodes */
			children = new Node[4];
			for (int i = 0; i < 4; i++) {
				children[i] = white;
			}

			/* get half the width and half the height */
			halfWidth = width >> 1;
			halfHeight = height >> 1;

			/* initialize the child origins */
			origins = new Point2D.Float[4];
			origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
			origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
					+ halfHeight);
			origins[2] = new Point2D.Float(origin.x, origin.y);
			origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

			/* initialize the child regions */
			regions = new Rectangle2D.Float[4];
			for (int i = 0; i < 4; i++) {
				regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
						halfWidth, halfHeight);
			}
		}

		/**
		 * Adds a road to this PM Quadtree node.
		 * 
		 * @param g
		 *            road to be added
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return this node after the city has been added
		 * @throws InvalidPartitionThrowable
		 *             if the map if partitioned too deeply
		 * @throws IntersectingRoadsThrowable
		 *             if this road intersects with another road
		 */
		public Node add(final Geometry g, final Point2D.Float origin,
				final int width, final int height) throws PMRuleViolation{
			
			for (int i = 0; i < 4; i++) {
				if (g.isRoad() && Inclusive2DIntersectionVerifier.intersects(
						((Road)g).toLine2D(),regions[i]) 
						|| g.isCity() && Inclusive2DIntersectionVerifier.intersects(
								((City)g).toPoint2D(),regions[i])
						|| g.isAirport() && Inclusive2DIntersectionVerifier.intersects(
								((Airport)g).toPoint2D(),regions[i]) 
						|| g.isTerminal() && Inclusive2DIntersectionVerifier.intersects(
								((Terminal)g).toPoint2D(),regions[i])) {
					try {
						//this break up of commands allows an exception to be thrown without
						//changing the PMQuadtree structure.
						Node temp = children[i].add(g, origins[i], halfWidth,
								halfHeight);
						children[i] = temp;
					} catch (PMRuleViolation e) {
						//need to get structure back to how it was before adding geometry.
						//Note: might need to turn node back into a black node.
						if (this.isValid()) {
							//even though it is a valid node now, this means that somewhere
							//down the line, a PMRuleViolation was thrown. Continue this throwing 
							//until back to the original function call. This way we can output
							//to the user that this happened.
							throw new PMRuleViolation();
						} else {
							//reverse partition to get back to normal.
							this.reversePartition(width, height);
							throw new PMRuleViolation();
						}
					}
				}
			}
			return this;
		}
		
		public Node remove(final Geometry g, final Point2D.Float origin, 
				final int width, final int height) {
			//this command is for all geometry except roads.
			if (! g.isRoad()) {
				for (int i = 0; i < 4; i ++) {
					if (g.isCity() && Inclusive2DIntersectionVerifier.intersects(((City) g).toPoint2D(), regions[i]) ||
							g.isAirport() && Inclusive2DIntersectionVerifier.intersects(((Airport) g).toPoint2D(), regions[i]) ||
							g.isTerminal() && Inclusive2DIntersectionVerifier.intersects(((Terminal) g).toPoint2D(), regions[i])){
						children[i] = children[i].remove(g, origins[i], halfWidth, halfHeight);
					}
				}
				
				//now we need to do a validator check to see if we need to get rid of this gray node.
				if (isValid()) {
					return this;
				} else {
//					//not valid.. need to get rid of gray node.
//					Black blackNode = new Black();
//					//need to get geometry from every child.
//					for (int i = 0; i < 4; i ++) {
//						if (children[i].getType() == Node.BLACK) {
//							LinkedList<Geometry> geom = ((Black)children[i]).getGeometry();
//							for (Geometry geo: geom) {
//								//since the current gray node is invalid, this should not lead to any partitioning.
//								blackNode.add(geo, origin, width, height);
//							}
//						}
//					}
//					return blackNode;
					
					return reversePartition(width, height);
				}
			} else {
				return null;
			}
		}
		
		
		public Node removeRoad(final Road r, final Point2D.Float origin,
				final int width, final int height){
			//removal of a road is O(N). Must visit every black node.
			for (int i = 0; i < 4; i++) {
				children[i] = children[i].removeRoad(r, origins[i], halfWidth, halfHeight);
			}
			
			//need to make sure node is valid.
			if (isValid()) {
				return this;
			} else {
				//need to do a reverse partition again.
				return reversePartition(width, height);
			}
		}
		
		
		private Node reversePartition(int width, int height) {
			Black blackNode = new Black();
			//need to get geometry from every child.
			for (int i = 0; i < 4; i ++) {
				if (children[i].getType() == Node.BLACK) {
					LinkedList<Geometry> geom = ((Black)children[i]).getGeometry();
					for (Geometry geo: geom) {
						//since the current gray node is invalid, this should not lead to any partitioning.
						try {
							blackNode.add(geo, origin, width, height);
						} catch (PMRuleViolation e) {
							//if this happens idek anymore.
							return null;
						}
						
					}
				}
			}
			return blackNode;
		}
		/**
		 * Returns if this node follows the rules of the PM Quadtree.
		 * 
		 * @return <code>true</code> if the node follows the rules of the PM
		 *         Quadtree; <code>false</code> otherwise
		 */
		public boolean isValid() {
			return validator.valid(this) && children[0].isValid() && children[1].isValid()
				&& children[2].isValid() && children[3].isValid();
		}

		public String toString() {
			StringBuilder grayStringBuilder = new StringBuilder("gray:");
			for (Node child : children) {
				grayStringBuilder.append("\n\t");
				grayStringBuilder.append(child.toString());
			}
			return grayStringBuilder.toString();
		}

		/**
		 * Gets the child node of this node according to which quadrant it falls
		 * in.
		 * 
		 * @param quadrant
		 *            quadrant number (top left is 0, top right is 1, bottom
		 *            left is 2, bottom right is 3)
		 * @return child node
		 */
		public Node getChild(final int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return children[quadrant];
			}
		}

		/**
		 * Gets the rectangular region for the specified child node of this
		 * internal node.
		 * 
		 * @param quadrant
		 *            quadrant that child lies within
		 * @return rectangular region for this child node
		 */
		public Rectangle2D.Float getChildRegion(int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return regions[quadrant];
			}
		}

		/**
		 * Gets the center X coordinate of this node's rectangular bounds.
		 * 
		 * @return center X coordinate of this node's rectangular bounds
		 */
		public int getCenterX() {
			return (int) origin.x + halfWidth;
		}

		/**
		 * Gets the center Y coordinate of this node's rectangular bounds.
		 * 
		 * @return center Y coordinate of this node's rectangular bounds
		 */
		public int getCenterY() {
			return (int) origin.y + halfHeight;
		}

		/**
		 * Gets half the width of this internal node.
		 * 
		 * @return half the width of this internal node
		 */
		public int getHalfWidth() {
			return halfWidth;
		}

		/**
		 * Gets half the height of this internal node.
		 * 
		 * @return half the height of this internal node
		 */
		public int getHalfHeight() {
			return halfHeight;
		}
	}

	public PMQuadtree(final Validator validator, final int spatialWidth,
			final int spatialHeight, final int order) {
		if (order != 1 && order != 3) {
			throw new IllegalArgumentException("order must be one of: {1,3}");
		}

		root = white;
		this.validator = validator;
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
		spatialOrigin = new Point2D.Float(0.0f, 0.0f);
		allRoads = new TreeSet<Road>(new RoadNameComparator());
		citiesByLocation = new TreeSet<City>(new CityLocationComparator());
		numRoadsForCity = new HashMap<String, Integer>();
		this.order = order;
		this.citiesByName = new Treap<String, City>();
		this.adjacencyList = new RoadAdjacencyList();
	}

	public Node getRoot() {
		return root;
	}
	
	public void addRoad(final Road g) 
			throws RoadAlreadyExistsThrowable, OutOfBoundsThrowable,
			RoadIntersectsAnotherRoadThrowable, PMRuleViolation {
//		if (isIsolatedCity(g.getStart()) || isIsolatedCity(g.getEnd())) {
//			throw new AirportAlreadyExistsThrowable();
//		}

		final Road g2 = new Road(g.getEnd(), g.getStart());

		
		Rectangle2D.Float world = new Rectangle2D.Float(spatialOrigin.x, spatialOrigin.y, 
				spatialWidth, spatialHeight);
		if (!Inclusive2DIntersectionVerifier.intersects(g.toLine2D(), world)) {
			throw new OutOfBoundsThrowable();
		}

		for (Road r: allRoads) {
			if (r.equals(g) || r.equals(g2)) {
				throw new RoadAlreadyExistsThrowable();
			}
			
			if (Inclusive2DIntersectionVerifier.intersects(g.toLine2D(), r.toLine2D())) {
				throw new RoadIntersectsAnotherRoadThrowable();
			}
		}
//		if (allRoads.contains(g) || allRoads.contains(g2)) {
//			throw new RoadAlreadyExistsThrowable();
//		}
	
		root = root.add(g, spatialOrigin, spatialWidth, spatialHeight);
		allRoads.add(g);
		adjacencyList.addRoad(g);
		if (Inclusive2DIntersectionVerifier.intersects(g.getStart().toPoint2D(), world)) {
			increaseNumRoadsMap(g.getStart().getName());
		}
		if (Inclusive2DIntersectionVerifier.intersects(g.getEnd().toPoint2D(), world)) {
			increaseNumRoadsMap(g.getEnd().getName());
		}

	}
	
	
	public void addCity(final City c) throws PMRuleViolation {
		root = root.add(c, spatialOrigin, spatialWidth, spatialHeight);
		//if an exception is thrown here, none of the structures will be added to.
		numCities++;
		numRoadsForCity.put(c.getName(), 0);		
		citiesByName.put(c.getName(), c);
		citiesByLocation.add(c);
	}
	
	
	public void addAirport(final Airport a) throws AirportViolatesPMRulesThrowable,
		ConnectingCityNotMappedThrowable, TerminalViolatesPMRulesThrowable, RoadIntersectsAnotherRoadThrowable {
		try {
			this.addCity(a);
			numAirports++;
		} catch (PMRuleViolation e) {
			throw new AirportViolatesPMRulesThrowable();
		}
		
		Terminal t = a.getFirstTerminal();
		String connectingCity = t.getTerminalCity();
		if (! this.containsCity(connectingCity)) {
			throw new ConnectingCityNotMappedThrowable();
		}
	
		City c = citiesByName.get(t.getTerminalCity());
		Road r = new Road(t, c);
		
		try {
			this.addRoad(r);
			numTerminals++;	
		} catch (RoadAlreadyExistsThrowable e) {
			//not a possible thing to happen. 
			//Would have already seen both terminal and terminal city already exist.
		} catch (OutOfBoundsThrowable e) {
			//Not possible.
			//Would have seen terminal or terminal city out of bounds.
		} catch (PMRuleViolation e) {
			this.unmapAirport(a);
			throw new TerminalViolatesPMRulesThrowable();	
		} catch (RoadIntersectsAnotherRoadThrowable e) {
			//this is at the bottom of priority, so if we catch this, we still need
			//to see if this violates PMRules.
			try {
				root.add(r, spatialOrigin, spatialWidth, spatialHeight);
				//if it gets to this point, we know it was just the road intersection that was the error.
				//this also doesn't change anything in the structure. (since we didnt do root = root.add .....)
				this.unmapAirport(a);
				throw new RoadIntersectsAnotherRoadThrowable();
			} catch (PMRuleViolation e1) {
				this.unmapAirport(a);
				throw new TerminalViolatesPMRulesThrowable();
			}
		}
	}
	
	public void addTerminal(final Terminal terminal) throws ConnectingCityNotMappedThrowable, 
					TerminalViolatesPMRulesThrowable, RoadIntersectsAnotherRoadThrowable {
		
		//for now, assume that the airportName given will NOT be just a regular city name.....
		Airport a = (Airport) this.citiesByName.get(terminal.getAirportName());
		
		String connectingCity = terminal.getTerminalCity();
		if (! this.containsCity(connectingCity)) {
			//note that ConnectingCityDoesNotExist is in higher priority...
			//getting this exception should require the code to test and see if the city exists at all.
			throw new ConnectingCityNotMappedThrowable();
		}
		
		City c = citiesByName.get(terminal.getTerminalCity());
		Road r = new Road(terminal, c);
		try {
			//this.addCity(terminal);
			this.addRoad(r);
			numTerminals ++;
			a.addTerminal(terminal);
		} catch (RoadAlreadyExistsThrowable e) {
			// not going to happen. already checked.
		} catch (OutOfBoundsThrowable e) {
			//not going to happen. already checked.
		} catch (PMRuleViolation e) {
			throw new TerminalViolatesPMRulesThrowable();
		} catch (RoadIntersectsAnotherRoadThrowable e) {
			//this is at the bottom of priority, so if we catch this, we still need
			//to see if this violates PMRules.
			try {
				root.add(r, spatialOrigin, spatialWidth, spatialHeight);
				//if it gets to this point, we know it was just the road intersection that was the error.
				//this also doesn't change anything in the structure. (since we didnt do root = root.add .....)
				throw new RoadIntersectsAnotherRoadThrowable();
			} catch (PMRuleViolation e1) {
				throw new TerminalViolatesPMRulesThrowable();
			}
		}
	}

	private void increaseNumRoadsMap(final String name) {
		Integer numRoads = numRoadsForCity.get(name);
		if (numRoads != null) {
			numRoads++;
			numRoadsForCity.put(name, numRoads);
		} else {
			numRoadsForCity.put(name, 1);
		}
	}

	public void unmapRoad(final Road r) {
		//method is only called after checking to see if road is contained in local map.
		//this.root = unmapRoadHelper(this.getRoot(), r);	
		root = root.removeRoad(r, spatialOrigin, spatialWidth, spatialHeight);
		allRoads.remove(r);
		adjacencyList.removeRoad(r);
	}

	
	public void removeRoadForCity(City c) {
		int currentNum = this.numRoadsForCity.get(c.getName());
		this.numRoadsForCity.put(c.getName(), (currentNum - 1));
	}
	
	public ArrayList<Geometry> unmapAirport(final Airport a) {
		//should only be called if we know this pmquadtree contains the airport.
		ArrayList<Geometry> deletedItems = new ArrayList<Geometry>();
		ArrayList<Terminal> terminals = a.getTerminals();
		for (Terminal t: terminals) {
			this.unmapTerminal(t);
			deletedItems.add(t);
			//deletedItems.add(r);
		}
		
		
		root = root.remove(a, spatialOrigin, spatialWidth, spatialHeight);
		deletedItems.add(a);
		return deletedItems;
	}
	
	public ArrayList<Geometry> unmapTerminal(final Terminal t) {
		ArrayList<Geometry> deletedItems = new ArrayList<Geometry>();
		String connectingCity = t.getTerminalCity();
		City city = this.citiesByName.get(connectingCity);
		//city should never be null....
		Road r = new Road(t, city);
		this.unmapRoad(r);
		root = root.remove(t, spatialOrigin, spatialWidth, spatialHeight);
		deletedItems.add(t);
		Airport a = (Airport) this.citiesByName.get(t.getAirportName());
		if (a.getNumTerminals() == 0) {
			//now have to unmap airport...
			root = root.remove(a, spatialOrigin, spatialWidth, spatialHeight);
			deletedItems.add(a);
		}
		
		return deletedItems;
	}
	
	/*
	 * This function is for the deleteCity command. This will unmap the 
	 * city as well as all roads that are attached to the city.
	 * 
	 * Per the spec, we do not need to handle the case of a city
	 * being deleted leading to a terminal unmapping.
	 */
	public ArrayList<Geometry> unmapCity(final City c) {
		ArrayList<Geometry> deletedItems = new ArrayList<Geometry>();
		//this can be done by removing all of the roads that this city is apart of.
		int numRoads = this.numRoadsForCity.get(c.getName());
		if (numRoads == 0) {
			//this shouldn't happen since if there are no roads attached it should've been removed.
			return null;
		}
		
		TreeSet<Road> connectedCities = adjacencyList.getRoadSet(c);
		//remove all roads for the city.
		//once the last road for the city is removed, the city should be unmapped as well from the unmapRoad code.
		for (Road r: connectedCities) {
			this.unmapRoad(r);
			deletedItems.add(r);
		}
		
		unmapCityHelper(c);
		
		return deletedItems;
	}
	
	/*
	 * This function deletes the cities from the dictionaries in this map.
	 */
	private void unmapCityHelper(final City c) {
		//do all the deletes in one place.
		this.citiesByName.remove(c.getName());
		this.citiesByLocation.remove(c);
	}
	
	public void clear() {
		root = white;
		allRoads.clear();
		numRoadsForCity.clear();
		numAirports = 0;
		citiesByLocation.clear();
		citiesByName.clear();
		adjacencyList.clear();
	}

	public boolean isEmpty() {
		return (root == white);
	}

	public boolean containsCity(final String name) {
		final Integer numRoads = numRoadsForCity.get(name);
		return (numRoads != null);
	}
	
	public boolean containsRoad(final Road road) {
		return allRoads.contains(road);
	}

	public int getOrder() {
		return order;
	}
	
	public int getNumCities() {
		return numRoadsForCity.keySet().size();
	}

	public int getNumAirports() {
		return numAirports;
	}
	
	public int getNumTerminals() {
		return this.numTerminals;
	}
	
	public int getNumRoads() {
		return allRoads.size();
	}
	
	public boolean isIsolatedCity(Geometry g) {
		if (!g.isCity()) {
			return false;
		}
		City c = (City)g;
		Integer n = numRoadsForCity.get(c.getName());
		if (n == null || n > 0) {
			return false;
		}
		return true;
	}
	
	public boolean isAirport(Geometry g) {
		return g.isAirport();
	}
	
	public boolean isTerminal(Geometry g) {
		return g.isTerminal();
	}


	public Rectangle2D.Float getPMRectangle() {
		return new Rectangle2D.Float(0, 0, spatialWidth, spatialHeight);
	}
	
	public TreeSet<City> getCitiesInMap() {
		return this.citiesByLocation;
	}
	
	//takes any Point2D.Float and will return whether it would be 
	//in bounds of this pmquadtree
	public boolean isPointInBounds(Point2D.Float point) {
		return Inclusive2DIntersectionVerifier.intersects(point, this.getPMRectangle());
	}
}
