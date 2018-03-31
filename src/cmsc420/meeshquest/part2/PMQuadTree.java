package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.*;
import cmsc420.sortedmap.Treap;

public abstract class PMQuadTree {

	public Validator validator;
	private WhiteNode whiteNode;
	public TreeMap<String, City> cityNames;
	public TreeMap<Point2D.Float, City> cityCoordinates;
	public Treap<String, City> treap;
	public ArrayList<String> citiesMapped;
	public ArrayList<City> isolatedCities;
	public Node spatialMap; //will start off as white node.
	private int spatialHeight, spatialWidth;
	private ArrayList<Road> roads;
	private AdjacencyList neighbors;
	private Rectangle2D rectangle;
	
	public PMQuadTree(Validator v, int spatialHeight, int spatialWidth) {
		this.validator = v;
		this.neighbors = new AdjacencyList();
		this.isolatedCities = new ArrayList<City>();
		this.roads = new ArrayList<Road>();
		this.whiteNode = new WhiteNode(); //singleton
		this.spatialMap = whiteNode; //to start off the PMQuadTree, have root be a white node.
		this.spatialHeight = spatialHeight;
		this.spatialWidth = spatialWidth;
		this.treap = new Treap<String, City>(new CityNameComparator());
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.setRect(0,0, spatialHeight, spatialWidth);
		this.rectangle = rect;
	}
	
	public boolean isValid(){
		return this.validator.isValid();
	}	
	
	/*
	 * Needs to check validity on coordinates, check duplicates,
	 * and then map to both the dictionary and the treap.
	 */
	public XmlOutput createCity(String name, int x, int y, int radius, String color, Document doc) {
		if (cityNames == null) {
			cityNames = new TreeMap<String, City>(new CityNameComparator());
		}
		
		if (cityCoordinates == null) {
			cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
		}
		Point2D.Float newCoord = new Point2D.Float((float) x, (float) y);
		if (cityCoordinates.containsKey(newCoord)) {
			//dont add the city, this should be illegal.
			//duplicate cityName
			Error err = new Error(doc, "duplicateCityCoordinates", "createCity");
			err.addParam("name", name);
			err.addParam("x", "" + x);
			err.addParam("y", "" + y);
			err.addParam("radius", "" + radius);
			err.addParam("color", color);
			return err;
		} else {
			if (cityNames.containsKey(name)) {
				//duplicate coordinates.
    			Error err = new Error(doc, "duplicateCityName", "createCity");
    			err.addParam("name", name);
    			err.addParam("x", "" + x);
    			err.addParam("y", "" + y);
    			err.addParam("radius", "" + radius);
    			err.addParam("color", color);
				return err;
			} else {
				//no duplicate city name or coordinate.
				//add the city.
				City c = new City(name, x, y, radius, color, false); //we say false for now because we aren't sure if it will be isolated or not.
				addCityByName(name, c);
				addCityByCoordinates(c.getCoordinates(), c);
				addCityToTreap(name, c);
				
    			Success s = new Success(doc, "createCity");
    			s.addParams("name", name);
    			s.addParams("x", "" + x);
    			s.addParams("y", "" + y);
    			s.addParams("radius", "" + radius);
    			s.addParams("color", color);
				return s;
			}
		}
	}
	
	
	private XmlOutput mapCity(String cityName, boolean isIsolated, Document doc) {
		//need to make sure that the city exists in our dictionary.
		if (cityNames.containsKey(cityName)) {
			City c = cityNames.get(cityName);
			int x = c.getX();
			int y = c.getY();
			//out of bounds coordinates. (Different for PM Quadtree than point quadtree.
			if (x < 0 || y < 0 || x > spatialWidth || y > spatialHeight) {
				//Entering a city that is out of bounds.
				Error err = new Error(doc, "cityOutOfBounds", "mapCity");
				err.addParam("name", cityName);
				return err;
			}
			if (citiesMapped.contains(cityName)) {
				//this means we tried adding a city that was already in the map.
				Error e = new Error(doc, "cityAlreadyMapped", "mapCity");
				e.addParam("name", cityName);
				return e;
			}
			
			if (isIsolated) {
				c.makeIsolated();
				isolatedCities.add(c);
			}
			
			Node map = spatialMap.add(c, c.getX(), c.getY(), 1, 1);
			spatialMap = map;
			this.citiesMapped.add(cityName);
			Success s = new Success(doc, "mapCity");
			s.addParams("name", cityName);
			return s;
		} else {
			Error e = new Error(doc, "nameNotInDictionary", "mapCity");
			e.addParam("name", cityName);
			return e;
			//this means we tried mapping a city that is not in the names dictionary.
		}
	}

	public XmlOutput listCities(String sortBy, Document doc) {
		ArrayList<City> cities = new ArrayList<City>();
		switch(sortBy) {
		
		case "name":
			Set<String> names = cityNames.keySet();
			for(String s: names) {
				cities.add(cityNames.get(s));
			}
			break;
		case "coordinate":
			Set<Point2D.Float> coords = cityCoordinates.keySet();
			for(Point2D.Float c: coords) {
				cities.add(cityCoordinates.get(c));
			}
			break;
		}

		if (cities.isEmpty()) {
			//error: noCitiesToList
			Error err = new Error(doc, "noCitiesToList", "listCities");
			err.addParam("sortBy", sortBy);
			return err;
		} else {

			CityList cityList = new CityList(cities);
			Element cityListElement = cityList.getXmlElement(doc);
			Success s = new Success(doc, "listCities");
			s.addParams("sortBy", sortBy);
			s.addOutputElement(cityListElement);
			return s;
		}
	}
	
	private void addCityByName(String name, City c) {
		cityNames.put(name, c);
	}
	
	private void addCityByCoordinates(Point2D.Float coordinates, City c) {
		cityCoordinates.put(coordinates, c);
	}
	
	private void addCityToTreap(String name, City c) {
		treap.put(name, c);
	}
	
	public XmlOutput mapIsolatedCity(String name, Document doc) {
		return mapCity(name, true, doc);
	}
	
	public XmlOutput mapNonIsolatedCity(String name, Document doc) {
		return mapCity(name, false, doc);
	}
	
	public void clearAll() {
		//reset everything, effectively losing all cities created.
		this.cityNames = new TreeMap<String, City>(new CityNameComparator());
		this.cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
		this.citiesMapped = new ArrayList<String>();
		this.treap = new Treap<String, City>(new CityNameComparator());
		this.spatialMap = whiteNode;
	}
	
	public XmlOutput mapRoad(String start, String end, Document doc) {
		City s = cityNames.get(start);
		City e = cityNames.get(end);
		if (s == null) {
			//error: startPointDoesNotExist
			Error err = new Error(doc, "startPointDoesNotExist", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		} else if (e == null) {
			//error: endPointDoesNotExist
			Error err = new Error(doc, "endPointDoesNotExist", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
		}
		
		if (start.equals(end)) {
			//error: startEqualsEnd
			Error err = new Error(doc, "startEqualsEnd", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
		}
		
		if (s.isIsolated() || e.isIsolated()) {
			//error: startOrEndIsIsolated
			Error err = new Error(doc, "startOrEndIsIsolated", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
		}
		
		if (roadAlreadyMapped(start, end)) {
			//error: roadAlreaadyMapped
			Error err = new Error(doc, "roadAlreadyMapped", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
		}
		
		if (roadOutOfBounds(s, e)) {
			//error: roadOutOfBounds
			Error err = new Error(doc, "roadOutOfBounds", "mapRoad");
			err.addParam("start", start);
			err.addParam("end", end);
		}
		
		Road road = new Road(s, e);
		roads.add(road);
		neighbors.mapNeighbors(start, end); //adds neighbor mapping to our adjacency list, both start ==> end and end==>start
		
		Success suc = new Success(doc, "mapRoad");
		suc.addParams("start", start);
		suc.addParams("end", end);
		Element roadCreated = doc.createElement("roadCreated");
		roadCreated.setAttribute("start", start);
		roadCreated.setAttribute("end", end);
		suc.addOutputElement(roadCreated);
		return suc;
	}
	
	private boolean roadAlreadyMapped(String city1, String city2) {
		return neighbors.containsRoad(city1, city2);
	}
	
	private boolean roadOutOfBounds(City city1, City city2) {
		//the endpoints can be out of bounds
		//if the actual road itself never touches within the spatial map that is when it is out of bounds.
		Line2D.Float line = new Line2D.Float((float)city1.getX(), (float)city1.getY(), (float)city2.getX(), (float)city2.getY());
		return (!Inclusive2DIntersectionVerifier.intersects(line, rectangle));
	}
	
	public XmlOutput unmapCity(String cityName, Document doc) {
		return null;
	}
	
	public XmlOutput printPMQuadtree(Document doc) {
		if (spatialMap instanceof WhiteNode) {
			Error e = new Error(doc, "mapIsEmpty", "printPMQuadtree");
			return e;
		} else {
			Success s = new Success(doc, "printPMQuadtree");
			Element pmQuadTree = doc.createElement("quadtree");
			Element tree = spatialMap.printNode(doc);
			pmQuadTree.appendChild(tree);
			s.addOutputElement(pmQuadTree);
			return s;
		}
	}
	
	public XmlOutput printTreap(Document doc) {
		return null;
	}
	
	public XmlOutput deleteCity(String cityName, Document doc) {
		return null;
	}
	
	public XmlOutput rangeCities(int x, int y, int radius, String fileName, Document doc) {
		return null;
	}
	
	public XmlOutput rangeRoads(int x, int y, int radius, String fileName, Document doc) {
		return null;
	}
	
	public XmlOutput nearestCity(int x, int y, Document doc) {
		return null;
	}
	
	public XmlOutput nearestIsolatedCity(int x, int y, Document doc) {
		/*
		 * This will be efficient not because the search method is efficient but because we are 
		 * only looking at those cities that are already isolated. It is most likely the case that most 
		 * of the cities will NOT be isolated, which means that the isolatedCities list will be small.
		 * If it is the case however that most cities are isolated, then this will be slower than 
		 * what would be expected.
		 */
		
		double dist = -1;
		City nearest = null;
		for (City c: isolatedCities) {
			Rectangle2D.Float rect = new Rectangle2D.Float((float)c.getX(), (float)c.getY(), (float)c.getRadius(), (float)c.getRadius());
			Point2D.Float point = new Point2D.Float((float)x, (float)y);
			double temp = Shape2DDistanceCalculator.distance(point, rect);
			if (dist == -1) {
				dist = temp;
				nearest = c;
			} else if (temp < dist) {
				dist = temp;
				nearest = c;
			}
		}
		
		if (nearest == null) {
			Error e = new Error(doc, "cityNotFound", "nearestIsolatedCity");
			e.addParam("x", ""+x);
			e.addParam("y", ""+y);
			return e;
		} else {
			Element nearestIso = doc.createElement("isolatedCity");
			nearestIso.setAttribute("name", nearest.getName());
			nearestIso.setAttribute("x",""+ nearest.getX());
			nearestIso.setAttribute("y","" + nearest.getY());
			nearestIso.setAttribute("color", nearest.getColor());
			nearestIso.setAttribute("radius", ""+ nearest.getRadius());
			Success s = new Success(doc, "nearestIsolatedCity");
			s.addParams("x", "" + x);
			s.addParams("y", "" + y);
			s.addOutputElement(nearestIso);
			return s;
		}
	}
	
	public XmlOutput nearestRoad(int x, int y, Document doc) {
		return null;
	}
	
	public XmlOutput nearestCityToRoad(String start, String end, Document doc) {
		return null;
	}
	
	public XmlOutput shortestPath(String start, String end, String saveMap, String saveHTML) {
		return null;
	}
	
	public void saveMap(String fileName) {
		try {
			CanvasPlus cp = new CanvasPlus();
			cp.setFrameSize(spatialWidth, spatialHeight);
			//cp.setFrameSize(100,100);
			cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.WHITE, true);
			//cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			//cp.addPoint("baltimore", 20, 20, Color.BLACK);
			cp = spatialMap.drawMap(cp);
			cp.save(fileName);
		
		} catch (Exception e) {
			System.out.println("error: " + e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*****************************************************************************************************
	 * 
	 * BELOW THIS ARE ALL OF THE INNER CLASSES REPRESENTING THE STRUCTURE OF THE NODES IN THE PMQUADTREE.
	 *
	 *****************************************************************************************************/
	public class WhiteNode extends Node {
		public Node add(City c, int x, int y, int height, int width) {
			return new BlackNode(c, x, y, height, width);
		}
		
		//pray this doesn't get called like ever.
		public Node remove(City c) {
			return null;
		}
		
		public Element printNode(Document doc) {
			Element whiteNode = doc.createElement("white");
			return whiteNode;
		}
		
		public CanvasPlus drawNode(CanvasPlus cp) {
			cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			return cp;
		}
	}
	
	public class BlackNode extends Node {
		private City city;
		private int x,y,height,width;
		public BlackNode(City c, int x, int y, int height, int width) {
			this.city = c;
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}
		
		public Node add(City c, int x, int y, int height, int width) {
			if (c.equals(city)) {
				//this means we tried adding a city that is already in the map.
				return null;
			}
			GrayNode g = new GrayNode(this.x, this.y, this.height, this.width);
			g = (GrayNode) g.add(city, this.x, this.y, this.height, this.width);
			g = (GrayNode) g.add(c, x, y, height, width);
			if(PMQuadTree.this.isValid()) {
				return g;
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		public Node remove(City c) {
			if (c.equals(this.city)) {
				return whiteNode;
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		public Rectangle2D.Float getRect() {
			return new Rectangle2D.Float((float) x,(float) y, (float) height, (float) width);
		}
		
		public City getCity() {
			return this.city;
		}
		
		public Element printNode(Document doc) {
			TreeSet<String> list = neighbors.getNeighbors(city.getName());
			int cardinality = 1 + list.size();
			Element blackNode = doc.createElement("black");
			Element c;
			if (city.isIsolated()) {
				c = doc.createElement("isolatedCity");
			} else {
				c = doc.createElement("city");
			}
			
			blackNode.setAttribute("cardinality", "" + cardinality);
			c.setAttribute("color", city.getColor());
			c.setAttribute("name", city.getName());
			c.setAttribute("x", "" + city.getX());
			c.setAttribute("y", "" + city.getY());
			c.setAttribute("radius", ""+city.getRadius());
			blackNode.appendChild(c);
			
			for (String neighbor: list) {
				Element road = doc.createElement("road");
				if (neighbor.compareTo(city.getName()) < 0) {
					road.setAttribute("end", neighbor);
					road.setAttribute("start", city.getName());
				} else {
					road.setAttribute("end", city.getName());
					road.setAttribute("start", neighbor);
				}
				blackNode.appendChild(road);
			}
			return blackNode;
		}
		
		public CanvasPlus drawMap(CanvasPlus cp) {
			cp.addRectangle(x, y, width, height, Color.BLACK, false);
			cp.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
			return cp;
		}
	}
	
	public class GrayNode extends Node {
		private Node[] nodes; //represents the 4 quadrants
		private int x,y,height,width;
		public GrayNode(int x, int y, int height, int width) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
			this.nodes = new Node[4];
			int mid = width / 2;
			this.nodes[0] = whiteNode;
			this.nodes[1] = whiteNode;
			this.nodes[2] = whiteNode;
			this.nodes[3] = whiteNode;
		}
		public Node add(City c, int x, int y, int height, int width) {
			//need to get the quadrant the city is to be added to.
			//can be multiple quadrants. Need to check each quadrant to see if they intersect.
			int cityX = c.getX();
			int cityY = c.getY();
			
			int midX = this.x + (this.width / 2);
			int midY = this.y + (this.height / 2);
			ArrayList<Integer> desiredQuadrants = getDesiredQuadrants(cityX, cityY, midX, midY);
			
			
//			int desiredX = getX(x, y, height, width, desiredQuadrant);
//			int desiredY = getY(x, y, height, width, desiredQuadrant);
			//need to add city to the node in that quadrant.
					
			for (Integer i: desiredQuadrants) {
				Node n = this.nodes[i - 1].add(c, x, y, height, width);
				if (n == null) {
					return null;
				}
				this.nodes[i - 1] = n;
			}
//			Node n = this.nodes[desiredQuadrant - 1].add(c, x, y, height, width);
//			if (n == null) {
//				return null;
//			}
//			this.nodes[desiredQuadrant - 1] = n; 
			return this;
		}
		
		public Node remove(City c) {
			//need to get the quadrant the city is supposed to be removed from.
			int cityX = c.getX();
			int cityY = c.getY();
			
			int midX = x + (width / 2);
			int midY = y + (height / 2);
			ArrayList<Integer> desiredQuadrants = getDesiredQuadrants(cityX, cityY, midX, midY);
			
//			int desiredX = getX(x, y, height, width, desiredQuadrant);
//			int desiredY = getY(x, y, height, width, desiredQuadrant);
			
			for (Integer i: desiredQuadrants) {
				if (! (this.nodes[i - 1] instanceof WhiteNode) ) {
					this.nodes[i - 1] = this.nodes[i - 1].remove(c);
				}
			}
			
			int numBlackNodes = getNumBlackNodes();
			int numWhiteNodes = getNumWhiteNodes();
			
			if (numBlackNodes == 1 && numWhiteNodes == 3) {
				BlackNode onlyOne = getOnlyBlackNodeLeft();
				return onlyOne;
			} else {
				return this;
			} 
		}
		
		public int getNumBlackNodes() {
			//loop through list of nodes, just return num black ones left.
			int numBlack = 0;
			for (Node n: this.nodes) {
				if (n instanceof BlackNode) {
					numBlack ++;
				}
			}
			
			return numBlack;
		}
		
		public int getNumWhiteNodes() {
			//loop through list of nodes, just return num white ones left.
			int numWhite = 0;
			for (Node n: this.nodes) {
				if (n instanceof WhiteNode) {
					numWhite ++;
				}
			}
			
			return numWhite;
		}
		
		public BlackNode getOnlyBlackNodeLeft() {
			//assumed to only be called when we know that there is only one left.
			for (Node n: this.nodes) {
				if (n instanceof BlackNode) {
					return (BlackNode) n;
				}
			}
			
			//should never get to this point.
			return null;
		}
		
		//for PMQuadTrees a black node can be in multiple quadrants.
		public ArrayList<Integer> getDesiredQuadrants(int x, int y, int midX, int midY) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			if (x == midX && y == midY) {
				//all four quadrants;
				list.add(1);
				list.add(2);
				list.add(3);
				list.add(4);
				return list;
			}
			
			if (x == midX) {
				//y != midY
				if (y < midY) {
					//Q3,Q4
					list.add(3);
					list.add(4);
					return list;
				} else {
					//y > midY==> Q1, Q2
					list.add(1);
					list.add(2);
					return list;
				}
			}
			
			if (y == midY) {
				//x != midX
				if (x < midX) {
					//Q1, Q3
					list.add(1);
					list.add(3);
					return list;
				} else {
					//x > midX ==> Q2,Q4
					list.add(2);
					list.add(4);
					return list;
				}
			}
			
			if (x < midX && y < midY) {
				//Q3
				list.add(3);
			} else if (x < midX) {
				//y > midY ==> Q1
				list.add(1);
			} else if (y < midY) {
				//x > midX ==> Q4
				 list.add(4);
			} else {
				//x < midX ==> Q2
				list.add(2);
			}
			
			return list;
		}

		public int getX(int x, int y, int height, int width, int quadrant) {
			//quadrant 1 ==> x
			//quadrant 2 ==> x + width / 2;
			//quadrant 3 ==> x
			//quadrant 4 ==> x + width / 2;
			
			int desiredX = 0;
			
			switch (quadrant) {
			case 1: desiredX = x;
			break;
			case 2: desiredX = (x + width) / 2;
			break;
			case 3: desiredX = x;
			break; 
			case 4: desiredX = (x + width) / 2;
			break;
			}
			
			return desiredX;
		}
		
		public int getY(int x, int y, int height, int width, int quadrant) {
			//quadrant 1 ==> y + height / 2;
			//quadrant 2 ==> y + height / 2;
			//quadrant 3 ==> y;
			//quadrant 4 ==> y;
			
			int desiredY = 0;
			
			switch (quadrant) {
			case 1: desiredY = (y + height) / 2;
			break;
			case 2: desiredY = (y + height) / 2;
			break;
			case 3: desiredY = y;
			break; 
			case 4: desiredY = y;
			break;
			}
			
			return desiredY;
		}
		
		public Rectangle2D.Float getRect() {
			return new Rectangle2D.Float((float) x, (float) y, (float) height, (float) width);
		}
		
		public Node[] getChildren() {
			return this.nodes;
		}
		
		public Element printNode (Document doc) {
			//need to go through all children nodes and recursively print those.
			Element grayNode = doc.createElement("gray");
			grayNode.setAttribute("x", "" + (x + (height / 2)));
			grayNode.setAttribute("y", "" + (y + (width / 2)));
			
			
			Element child1 = nodes[0].printNode(doc);
			Element child2 = nodes[1].printNode(doc);
			Element child3 = nodes[2].printNode(doc);
			Element child4 = nodes[3].printNode(doc);
			
			grayNode.appendChild(child1);
			grayNode.appendChild(child2);
			grayNode.appendChild(child3);
			grayNode.appendChild(child4);

			return grayNode;
		}
		
		public CanvasPlus drawMap(CanvasPlus cp) {
			for (Node n: nodes) {
				cp.addRectangle(x, y, width, height, Color.BLACK, false);
				if (!(n instanceof WhiteNode)) {
					cp = n.drawMap(cp);
				}
			}
			return cp;
		}
	}
}
