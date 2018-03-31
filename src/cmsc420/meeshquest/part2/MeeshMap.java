package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import cmsc420.drawing.*;

public class MeeshMap {
	public TreeMap<String, City> cityNames;
	public TreeMap<Point2D.Float, City> cityCoordinates;
	public ArrayList<String> citiesMapped;
	public Node spatialMap; //will start off as white node.
	private int spatialHeight, spatialWidth;
	
	public MeeshMap(int spatialHeight, int spatialWidth) {
		this.cityNames = new TreeMap<String, City>(new CityNameComparator());
		this.cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
		this.citiesMapped = new ArrayList<String>();
		this.spatialMap = new WhiteNode(0, 0, spatialHeight, spatialWidth);
		this.spatialHeight = spatialHeight;
		this.spatialWidth = spatialWidth;
	}
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
				City c = new City(name, x, y, radius, color, false); //we say false for isIsolated because Point Quadtrees have no isolated cities (they all are, since no roads exist).
				addCityByName(name, c);
				addCityByCoordinates(c.getCoordinates(), c);
				
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
	
	public void addCityByName(String name, City c) {
		cityNames.put(name,  c);
	}
	
	public void addCityByCoordinates(Point2D.Float coordinates, City c) {
		cityCoordinates.put(coordinates, c);
	}
	
	public XmlOutput mapCity(String cityName, Document doc) {
		//need to make sure that the city exists in our dictionary.
		if (cityNames.containsKey(cityName)) {
			City c = cityNames.get(cityName);
			int x = c.getX();
			int y = c.getY();
			//out of bounds coordinates. 
			if (x < 0 || y < 0 || x >= spatialWidth || y >= spatialHeight) {
				//special case of being bottom right of the rectangle;
				if (!((y == 0 && x == spatialWidth) || (x == 0 && y == spatialHeight))) {
					//Entering a city that is out of bounds.
					Error err = new Error(doc, "cityOutOfBounds", "mapCity");
					err.addParam("name", cityName);
					return err;
				}
			}
			if (citiesMapped.contains(cityName)) {
				//this means we tried adding a city that was already in the map.
				Error e = new Error(doc, "cityAlreadyMapped", "mapCity");
				e.addParam("name", cityName);
				return e;
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
	
	public XmlOutput unmapCity(String cityName, Document doc) {
		if (cityNames.containsKey(cityName)) {
			City c = cityNames.get(cityName);
			if (citiesMapped.contains(cityName)) {
				spatialMap = spatialMap.remove(c);
				Success s = new Success(doc, "unmapCity");
				s.addParams("name", cityName);
				return s;
			} else {
				//this means we tried removing a city that wasn't in the map.
				Error e = new Error(doc, "cityNotMapped", "unmapCity");
				e.addParam("name", cityName);
				return e;
			}
			
		} else {
			//this means we tried removing a city that isn't in the names dictionary.
			Error e = new Error(doc, "nameNotInDictionary", "unmapCity");
			e.addParam("name", cityName);
			return e;
		}
	}
	
	public XmlOutput deleteCity(String cityName, Document doc) {
		if (cityNames.containsKey(cityName)) {
			XmlOutput output = unmapCity(cityName, doc);
			City c = cityNames.get(cityName);
			cityNames.remove(cityName);
			int x = c.getX();
			int y = c.getY();
			Point2D.Float f = new Point2D.Float((float) x, (float) y);
			cityCoordinates.remove(f);
			Success s = new Success(doc, "deleteCity");
			
			if (output instanceof Success) {
				citiesMapped.remove(cityName);
				Element unmappedCity = doc.createElement("cityUnmapped");
				unmappedCity.setAttribute("name", cityName);
				unmappedCity.setAttribute("x", "" + c.getX());
				unmappedCity.setAttribute("y", "" + c.getY());
				unmappedCity.setAttribute("color", c.getColor());
				unmappedCity.setAttribute("radius", "" + c.getRadius());
				
				s.addOutputElement(unmappedCity);
			}
			s.addParams("name", cityName);
			return s;
		} else {
			//this means we tried removing a city that wasn't added before.
			Error e = new Error(doc, "cityDoesNotExist", "deleteCity");
			e.addParam("name", cityName);
			return e;
		}
	}
	
	public XmlOutput printPRQuadTree(Document doc) {
		if (spatialMap instanceof WhiteNode) {
			Error e = new Error(doc, "mapIsEmpty", "printPRQuadtree");
			return e;
		} else {
			Success s = new Success(doc, "printPRQuadtree");
			Element prQuadTree = doc.createElement("quadtree");
			Element tree = spatialMap.printNode(doc);
			prQuadTree.appendChild(tree);
			s.addOutputElement(prQuadTree);
			return s;
		}
	}

	
	//output should be in descending alphabetical order.
	public XmlOutput rangeCities(int x, int y, int radius, String fileName, Document doc) {
		
		PriorityQueue<QuadDist> pq = new PriorityQueue<QuadDist>(new QuadDistComp());
		
		Node root = this.spatialMap;
		if (root instanceof WhiteNode) {
			Error e = new Error(doc, "noCitiesExistInRange", "rangeCities");
			e.addParam("x", "" + x);
			e.addParam("y", "" + y);
			e.addParam("radius", "" + radius);
			if (! fileName.equals("")) {
				e.addParam("saveMap", fileName);
			}
			return e;
		}
		
		Rectangle2D.Float rect = root.getRect();
		QuadDist quadDist = new QuadDist(rect, x, y, root);
		pq.add(quadDist);
		ArrayList<QuadDist> possibleSolutions = new ArrayList<QuadDist>();
		
		while (! pq.isEmpty()) {
			QuadDist qd = pq.poll();
			Node n = qd.getNode();
			if (n instanceof WhiteNode) {
				//theres no cities within the range.
				Error e = new Error(doc, "noCitiesExistInRange", "rangeCities");
				e.addParam("x", ""+x);
				e.addParam("y", ""+y);
				e.addParam("radius", ""+radius);
				if (! fileName.equals("")) {
					e.addParam("saveMap", fileName);
				}
				return e;
			} else if (n instanceof GrayNode) {
				for (Node child: ((GrayNode) n).getChildren()) {
					if (child instanceof WhiteNode) {
						//we dont want to add a white node into the priority queue.
					} else {
						Rectangle2D.Float r = child.getRect();
						QuadDist qd1 = new QuadDist(r, x, y, child);
						pq.add(qd1);
					}
				}
			} else if (n instanceof BlackNode) {
				//check to make sure city is within radius. If it is, add to possible solution
				//if not within radius, we break the loop and check the next in queue for final check.
				double dist = qd.getBlackNodeDistance();
				if (dist > radius) {
					//break the loop.
					break;
				} else {
					possibleSolutions.add(qd);
				}
			} else {
				//cry
			}
		}
		
		
		QuadDist otherSolution = pq.poll();
		if (otherSolution != null) {
			PriorityQueue<QuadDist> pq1 = new PriorityQueue<QuadDist>(new QuadDistComp());
			pq1.add(otherSolution);
			
			while (! pq1.isEmpty()) {
				QuadDist qd = pq1.poll();
				Node n = qd.getNode();
				if (n instanceof WhiteNode) {
					//break from this
					break;
				} else if (n instanceof GrayNode) {
					for (Node child: ((GrayNode) n).getChildren()) {
						Rectangle2D.Float r = child.getRect();
						QuadDist qd1 = new QuadDist(r, x, y, child);
						pq1.add(qd1);
					}
				} else if (n instanceof BlackNode) {
					double distance = qd.getDist();
					if (radius >= distance) {
						//add to possible solutions
						possibleSolutions.add(qd);
					} else {
						//we have looked at all of the possible solutions within radius.
						break;
					}
					
				} else {
					//weep hard
				}
			}
		}
		
		
		//now we have all of the solutions that are within the radius.
		//we have them in the form of ArrayList<QuadDist> and we need ArrayList<City>
		ArrayList<City> cities = new ArrayList<City>();
		for (QuadDist qd: possibleSolutions) {
			cities.add(((BlackNode)qd.getNode()).getCity());
		}
		
		//sort by city name.
		cities.sort(new CityComparator());
		
		
		if (cities.isEmpty()) {
			Error e = new Error(doc, "noCitiesExistInRange", "rangeCities");
			e.addParam("x", ""+x);
			e.addParam("y", ""+y);
			e.addParam("radius", ""+radius);
			if (! fileName.equals("")) {
				e.addParam("saveMap", fileName);
			}
			return e;
		} else {
			if (! fileName.equals("")) {
				saveMap(fileName, x, y, radius);
			}
			CityList cityList = new CityList(cities);
			Element cityListElement = cityList.getXmlElement(doc);
			Success s = new Success(doc, "rangeCities");
			s.addParams("x",""+x);
			s.addParams("y",""+y);
			s.addParams("radius", ""+radius);
			if (! fileName.equals("")) {
				s.addParams("saveMap", fileName);
			}
			s.addOutputElement(cityListElement);
			return s;
		}
	}
	
	
	public XmlOutput nearestCity(int x, int y, Document doc) {
		
		PriorityQueue<QuadDist> pq = new PriorityQueue<QuadDist>(new QuadDistComp());
		//add root of the spatial map.
		Node root = this.spatialMap;
		if (root instanceof WhiteNode) {
			//white root means no cities have been added.
			Error e = new Error(doc, "mapIsEmpty", "nearestCity");
			e.addParam("x", ""+x);
			e.addParam("y", ""+y);
			return e;
		}
		
		Rectangle2D.Float rect = root.getRect();
		QuadDist quadDist = new QuadDist(rect, x, y, root);
		pq.add(quadDist);
		QuadDist possibleSolution = null;
		City solution = null;
		while(! pq.isEmpty()) {
			
			QuadDist qd = pq.poll();
			Node n = qd.getNode();
			if (n instanceof WhiteNode) {
				//do nothing
				//break;
			} else if (n instanceof GrayNode) {
				for (Node child: ((GrayNode) n).getChildren()) {
					Rectangle2D.Float r = child.getRect();
					QuadDist qd1 = new QuadDist(r, x, y, child);
					pq.add(qd1);
				}
			} else if (n instanceof BlackNode) {
				possibleSolution = qd;
				break;
				//stop adding to the queue.
			} else {
				//something went wrong.
			}
		}
		
		if (possibleSolution != null) {
			//need to only check the top of the PriorityQueue to make sure we have right answer.
			QuadDist otherSolution = pq.poll();
			if (otherSolution == null) {
				solution = ((BlackNode) (possibleSolution.getNode())).getCity();
			} else {
				PriorityQueue<QuadDist> pq1 = new PriorityQueue<QuadDist>(new QuadDistComp());
				pq1.add(otherSolution);
				
				while (! pq1.isEmpty()) {
					QuadDist qd = pq1.poll();
					Node n = qd.getNode();
					if (n instanceof WhiteNode) {
						
						solution = ((BlackNode) (possibleSolution.getNode())).getCity();
						break;
					} else if (n instanceof BlackNode) {
						
						double dist = qd.getBlackNodeDistance();
						double psDist = possibleSolution.getBlackNodeDistance();
						if (psDist == dist) {
							//dead tie on closest city.
							solution = ((BlackNode) (possibleSolution.getNode())).getCity();
						} else {
							if (psDist > dist) {
								solution = ((BlackNode) (qd.getNode())).getCity();
							} else {
								solution = ((BlackNode) (possibleSolution.getNode())).getCity();
							}
						}
						break;
						
					} else if (n instanceof GrayNode) {
						
						for (Node child: ((GrayNode) n).getChildren()) {
							Rectangle2D.Float r = child.getRect();
							QuadDist qd1 = new QuadDist(r, x, y, child);
							pq1.add(qd1);
						}
						
					}
				}
			}
			
			if (solution != null) {
				//now that we have the solution as a city object, just need to return XmlOutput object
				Success s = new Success(doc, "nearestCity");
				s.addParams("x", "" + x);
				s.addParams("y", "" + y);
				Element city = doc.createElement("city");
				city.setAttribute("name", solution.getName());
				city.setAttribute("x", "" + solution.getX());
				city.setAttribute("y", "" + solution.getY());
				city.setAttribute("color", solution.getColor());
				city.setAttribute("radius", "" + solution.getRadius());
				
				s.addOutputElement(city);
				return s;
			} else {
				Error e = new Error(doc, "mapIsEmpty", "nearestCity");
				e.addParam("x", ""+x);
				e.addParam("y", ""+y);
				return e;
			}
		} else {
			//something must have really gone wrong.
			FatalError fe = new FatalError(doc);
			return fe;
		}
	}
	
	
	public ArrayList<City> listCities(String sortBy) {
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
		return cities;
	}
	
	public void clearAll() {
		//reset everything, effectively losing all cities created.
		this.cityNames = new TreeMap<String, City>(new CityNameComparator());
		this.cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
		this.citiesMapped = new ArrayList<String>();
		this.spatialMap = new WhiteNode(0, 0, spatialHeight, spatialWidth);
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
	
	public void saveMap(String fileName, int x, int y, int radius) {
		try {
			CanvasPlus cp = new CanvasPlus();
			cp.setFrameSize(spatialWidth, spatialHeight);
			//cp.setFrameSize(100,100);
			cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.WHITE, true);
			//cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			//cp.addPoint("baltimore", 20, 20, Color.BLACK);
			cp = spatialMap.drawMap(cp);
			cp.addCircle(x, y, radius, Color.BLUE, false);
			cp.save(fileName);
		
		} catch (Exception e) {
			System.out.println("error: " + e.getMessage());
		}
	}
	
	
}
