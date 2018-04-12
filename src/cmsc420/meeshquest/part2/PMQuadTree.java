package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.PriorityQueue;
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
		this.citiesMapped = new ArrayList<String>();
		this.roads = new ArrayList<Road>();
		this.whiteNode = new WhiteNode(); //singleton
		this.spatialMap = whiteNode; //to start off the PMQuadTree, have root be a white node.
		this.spatialHeight = spatialHeight;
		this.spatialWidth = spatialWidth;
		this.treap = new Treap<String, City>(new CityNameComparator());
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.setRect(0,0, spatialHeight, spatialWidth);
		this.rectangle = rect;
		this.cityNames = new TreeMap<String, City>(new CityNameComparator());
		this.cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
	}
	
	public boolean isValid(){
		return this.validator.isValid();
	}	
	
	/*
	 * Needs to check validity on coordinates, check duplicates,
	 * and then map to both the dictionary and the treap.
	 */
	public XmlOutput createCity(String name, int x, int y, int radius, String color, Document doc, Integer id) {
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
			Error err = new Error(doc, "duplicateCityCoordinates", "createCity", id);
			err.addParam("name", name);
			err.addParam("x", "" + x);
			err.addParam("y", "" + y);
			err.addParam("radius", "" + radius);
			err.addParam("color", color);
			return err;
		} else {
			if (cityNames.containsKey(name)) {
				//duplicate coordinates.
    			Error err = new Error(doc, "duplicateCityName", "createCity", id);
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
				
    			Success s = new Success(doc, "createCity", id);
    			s.addParams("name", name);
    			s.addParams("x", "" + x);
    			s.addParams("y", "" + y);
    			s.addParams("radius", "" + radius);
    			s.addParams("color", color);
				return s;
			}
		}
	}
	
	
	private XmlOutput mapCity(String cityName, boolean isIsolated, Document doc, Integer id) {
		//need to make sure that the city exists in our dictionary.
		if (cityNames.containsKey(cityName)) {
			City c = cityNames.get(cityName);
			int x = c.getX();
			int y = c.getY();
			//out of bounds coordinates. (Different for PM Quadtree than point quadtree.
			if (x < 0 || y < 0 || x > spatialWidth || y > spatialHeight) {
				//Entering a city that is out of bounds.
				Error err = new Error(doc, "cityOutOfBounds", "mapCity", id);
				err.addParam("name", cityName);
				return err;
			}
			if (citiesMapped.contains(cityName)) {
				//this means we tried adding a city that was already in the map.
				Error e = new Error(doc, "cityAlreadyMapped", "mapCity", id);
				e.addParam("name", cityName);
				return e;
			}
			
			if (isIsolated) {
				c.makeIsolated();
				isolatedCities.add(c);
			}
			Node map = spatialMap.addCity(c, 0, 0, c.getX(), c.getY(), spatialHeight, spatialWidth);
			//Node map = spatialMap.add(c, c.getX(), c.getY(), spatialHeight, spatialWidth);
			spatialMap = map;
			this.citiesMapped.add(cityName);
			Success s = new Success(doc, "mapCity", id);
			s.addParams("name", cityName);
			return s;
		} else {
			Error e = new Error(doc, "nameNotInDictionary", "mapCity", id);
			e.addParam("name", cityName);
			return e;
			//this means we tried mapping a city that is not in the names dictionary.
		}
	}

	public XmlOutput listCities(String sortBy, Document doc, Integer id) {
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
			Error err = new Error(doc, "noCitiesToList", "listCities", id);
			err.addParam("sortBy", sortBy);
			return err;
		} else {

			CityList cityList = new CityList(cities);
			Element cityListElement = cityList.getXmlElement(doc);
			Success s = new Success(doc, "listCities", id);
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
	
	public XmlOutput mapIsolatedCity(String name, Document doc, Integer id) {
		return mapCity(name, true, doc, id);
	}
	
	public XmlOutput mapNonIsolatedCity(String name, Document doc, Integer id) {
		return mapCity(name, false, doc, id);
	}
	
	public void clearAll() {
		//reset everything, effectively losing all cities created.
		this.cityNames = new TreeMap<String, City>(new CityNameComparator());
		this.cityCoordinates = new TreeMap<Point2D.Float, City>(new CityCoordinateComparator());
		this.citiesMapped = new ArrayList<String>();
		this.isolatedCities = new ArrayList<City>();
		this.neighbors = new AdjacencyList();
		this.roads = new ArrayList<Road>();
		this.treap = new Treap<String, City>(new CityNameComparator());
		this.spatialMap = whiteNode;
	}
	
	public XmlOutput mapRoad(String start, String end, Document doc, Integer id) {
		
		boolean sb = this.cityNames.containsKey(start);
		boolean eb = this.cityNames.containsKey(end);
		City s = this.cityNames.get(start);
		City e = this.cityNames.get(end);
		if (s == null) {
			//error: startPointDoesNotExist
			Error err = new Error(doc, "startPointDoesNotExist", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		} else if (e == null) {
			//error: endPointDoesNotExist
			Error err = new Error(doc, "endPointDoesNotExist", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		}
		
		if (start.equals(end)) {
			//error: startEqualsEnd
			Error err = new Error(doc, "startEqualsEnd", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		}
		
		if (s.isIsolated() || e.isIsolated()) {
			//error: startOrEndIsIsolated
			Error err = new Error(doc, "startOrEndIsIsolated", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		}
		
		if (roadAlreadyMapped(start, end)) {
			//error: roadAlreaadyMapped
			Error err = new Error(doc, "roadAlreadyMapped", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		}
		
		if (roadOutOfBounds(s, e)) {
			//error: roadOutOfBounds
			Error err = new Error(doc, "roadOutOfBounds", "mapRoad", id);
			err.addParam("start", start);
			err.addParam("end", end);
			return err;
		}
		
		this.mapCity(start, false, doc, id);
		this.mapCity(end, false, doc, id);
		Road road = new Road(s, e);
		spatialMap = spatialMap.addRoad(road);
		roads.add(road);
		neighbors.mapNeighbors(start, end); //adds neighbor mapping to our adjacency list, both start ==> end and end==>start
		
		Success suc = new Success(doc, "mapRoad", id);
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
	
	public XmlOutput unmapCity(String cityName, Document doc, Integer id) {
		return null;
	}
	
	public XmlOutput printPMQuadtree(Document doc, Integer id) {
		if (spatialMap instanceof WhiteNode) {
			Error e = new Error(doc, "mapIsEmpty", "printPMQuadtree",id);
			return e;
		} else {
			Success s = new Success(doc, "printPMQuadtree", id);
			Element pmQuadTree = doc.createElement("quadtree");
			pmQuadTree.setAttribute("order", "3");
			Element tree = spatialMap.printNode(doc);
			pmQuadTree.appendChild(tree);
			s.addOutputElement(pmQuadTree);
			return s;
		}
	}
	
	public XmlOutput printTreap(Document doc, Integer id) {
		return this.treap.printTreap(doc, id);
	}
	
	public XmlOutput deleteCity(String cityName, Document doc, Integer id) {
		return null;
	}
	
	public XmlOutput rangeCities(int x, int y, int radius, String fileName, Document doc, Integer id) {
PriorityQueue<QuadDist> pq = new PriorityQueue<QuadDist>(new QuadDistComp());
		
		Node root = this.spatialMap;
		if (root instanceof WhiteNode) {
			Error e = new Error(doc, "noCitiesExistInRange", "rangeCities", id);
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
				Error e = new Error(doc, "noCitiesExistInRange", "rangeCities", id);
				e.addParam("x", ""+x);
				e.addParam("y", ""+y);
				e.addParam("radius", ""+radius);
				if (! fileName.equals("")) {
					e.addParam("saveMap", fileName);
				}
				return e;
			} else if (n instanceof GrayNode) {
				for (Node child: ((GrayNode) n).getChildren()) {
					if (!(child instanceof WhiteNode)) {
						if (child instanceof BlackNode) {
							City c = ((BlackNode)child).getCity();
							if (c != null) {
								Rectangle2D.Float r = child.getRect();
								QuadDist qd1 = new QuadDist(r, x, y, child);
								pq.add(qd1);
							}
						}
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
			boolean firstTry = true;
			while (! pq1.isEmpty()) {
				QuadDist qd = pq1.poll();
				Node n = qd.getNode();
				if (n instanceof WhiteNode) {
					//break from this
					break;
				} else if (n instanceof GrayNode) {
					for (Node child: ((GrayNode) n).getChildren()) {
						if (!(child instanceof WhiteNode)) {
							if (child instanceof BlackNode) {
								City c = ((BlackNode) child).getCity();
								if (!(c == null)) {
									Rectangle2D.Float r = child.getRect();
									QuadDist qd1 = new QuadDist(r, x, y, child);
									pq1.add(qd1);
								}
							} else {
								Rectangle2D.Float r = child.getRect();
								QuadDist qd1 = new QuadDist(r, x, y, child);
								pq1.add(qd1);
							}
							
						}
						
					}
				} else if (n instanceof BlackNode) {
					double distance = qd.getBlackNodeDistance();
					if (distance == -1) {
						QuadDist cont = pq.poll();
						if (cont == null) {
							break;
						}
						pq1.add(cont);
						continue;
					}
					if (radius >= distance) {
						//add to possible solutions
						possibleSolutions.add(qd);
					} else {
						//we have looked at all of the possible solutions within radius.
						if (firstTry) {
							QuadDist cont = pq.poll();
							if (cont == null) {
								break;
							}
							pq1.add(cont);
							continue;
						}
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
			City c = ((BlackNode)qd.getNode()).getCity();
			if (c != null) {
				if (!cities.contains(c)) {
					cities.add(c);
				}
			}
		}
		
		//sort by city name.
		cities.sort(new CityComparator());
		
		
		if (cities.isEmpty()) {
			Error e = new Error(doc, "noCitiesExistInRange", "rangeCities", id);
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
			Success s = new Success(doc, "rangeCities", id);
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
	
	/*
	 * Trying the basic approach first of just looping through all of the roads and selecting 
	 * all of them. This will be O(N) where N is the number of roads.
	 */
	public XmlOutput rangeRoads(int x, int y, int radius, String fileName, Document doc, Integer id) {
		ArrayList<Road> roadsInside = new ArrayList<Road>();
		Circle2D.Float circle = new Circle2D.Float((float) x, (float) y, (float) radius);
		for (Road r: this.roads) {
			if (r.intersectsOrInsideCircle(circle)) {
				roadsInside.add(r);
			}
		}
		
		if (roadsInside.size() == 0) {
			Error e = new Error(doc, "noRoadsExistInRange", "rangeRoads", id);
			e.addParam("x", ""+x);
			e.addParam("y", ""+y);
			e.addParam("radius", ""+radius);
			return e;
		}
		roadsInside.sort(new RoadComparator());
		RoadList roadList = new RoadList(roadsInside);
		Success s = new Success(doc, "rangeRoads", id);
		s.addParams("x", "" + x);
		s.addParams("y", ""+y);
		s.addParams("radius", ""+radius);
		if (!fileName.equals("")) {
			saveMap(fileName, x, y, radius);
			s.addParams("saveMap", fileName);
		}
		s.addOutputElement(roadList.getXmlElement(doc));
		return s;
		
	}
	
	public XmlOutput nearestCity(int x, int y, Document doc, Integer id) {
		PriorityQueue<QuadDist> pq = new PriorityQueue<QuadDist>(new QuadDistComp());
		//add root of the spatial map.
		Node root = this.spatialMap;
		if (root instanceof WhiteNode) {
			//white root means no cities have been added.
			Error e = new Error(doc, "cityNotFound", "nearestCity", id);
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
					if (!(child instanceof WhiteNode)) {
						if (child instanceof BlackNode) {
							City c = ((BlackNode) child).getCity();
							if (!(c == null || c.isIsolated())) {
								Rectangle2D.Float r = child.getRect();
								QuadDist qd1 = new QuadDist(r, x, y, child);
								pq.add(qd1);
							}
						} else {
							Rectangle2D.Float r = child.getRect();
							QuadDist qd1 = new QuadDist(r, x, y, child);
							pq.add(qd1);
						}
						
					}		
				}
			} else if (n instanceof BlackNode) {
				City c = ((BlackNode) n).getCity();
				if (c != null && !c.isIsolated()) {
					possibleSolution = qd;
					break;
				}
			} else {
				Error e = new Error(doc, "cityNotFound", "nearestCity", id);
				e.addParam("x", ""+x);
				e.addParam("y", ""+y);
				return e;
			}
		}
		
		if (possibleSolution != null) {
			//need to only check the top of the PriorityQueue to make sure we have right answer.
			QuadDist otherSolution = pq.poll();
			boolean firstTry = true;
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
//						QuadDist cont = pq.poll();
//						if (cont == null) {
//							break;
//						}
//						pq1.add(cont);
//						continue;
						break;
					} else if (n instanceof BlackNode) {
						
						double dist = qd.getBlackNodeDistance();
						double psDist = possibleSolution.getBlackNodeDistance();
						if (psDist == dist) {
							//dead tie on closest city.
							City cityA = ((BlackNode) (possibleSolution.getNode())).getCity();
							City cityB = ((BlackNode) (qd.getNode())).getCity();
							if (cityA.getName().compareTo(cityB.getName()) > 0) {
								solution = cityA;
							} else {
								solution = cityB;
							}
							//solution = ((BlackNode) (possibleSolution.getNode())).getCity();
						} else {
							if (psDist > dist) {
								solution = ((BlackNode) (qd.getNode())).getCity();
							} else {
								solution = ((BlackNode) (possibleSolution.getNode())).getCity();
								if (firstTry) {
									QuadDist cont = pq.poll();
									if (cont == null) {
										break;
									}
									pq1.add(cont);
									firstTry = false;
									continue;
								}
							}
						}
						break;
					} else if (n instanceof GrayNode) {
//						City c = ((BlackNode) n).getCity();
//						if (c == null || c.isIsolated()) {
//							solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//							QuadDist cont = pq.poll();
//							if (cont == null) {
//								break;
//							}
//							pq1.add(cont);
//							continue;
//						}
//						double dist = qd.getBlackNodeDistance();
//						double psDist = possibleSolution.getBlackNodeDistance();
//						if (dist == -1) {
//							solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//							QuadDist cont = pq.poll();
//							if (cont ==  null) {
//								break;
//							}
//							pq1.add(cont);
//							//firstTry = false;
//							continue;
//						} else if (psDist == dist) {
//							//dead tie on closest city. Pick city with name alphabetical order.
//							City cityA = ((BlackNode) (possibleSolution.getNode())).getCity();
//							City cityB = ((BlackNode) (qd.getNode())).getCity();
//							if (cityB.isIsolated()) {
//								solution = cityA;
//							}
//							if (cityA.getName().compareTo(cityB.getName()) > 0) {
//								solution = cityA;
//							} else {
//								solution = cityB;
//							}
//							break;
//						} else {
//							
//							if (psDist > dist) {
//								c = ((BlackNode) (qd.getNode())).getCity();
//								if (!c.isIsolated()) {
//									solution = c;
//									if (firstTry) {
//										possibleSolution = qd;
//										firstTry = false;
//										QuadDist cont = pq.poll();
//										if (cont == null) {
//											break;
//										}
//										pq1.add(cont);
//										continue;
//									}
//									break;
//								} else {
//									solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//									break;
//								}
//							} else {
//								solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//								if (firstTry) {
//									firstTry = false;
//									QuadDist cont = pq.poll();
//									if (cont == null) {
//										break;
//									}
//									continue;
//								}
//								break;
//							}
						
//							if (firstTry) {
//								//QuadDist cont = pq.poll();
//								//pq1.add(cont);
//								if (psDist > dist) {
//									possibleSolution = qd;
//								}
//								solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//								firstTry = false;
//								continue;
//							}
//							if (psDist > dist) {
//								solution = ((BlackNode) (qd.getNode())).getCity();
//							} else {
//								solution = ((BlackNode) (possibleSolution.getNode())).getCity();
//							}
						}
						//break;
					
						
						for (Node child: ((GrayNode) n).getChildren()) {
							if (!(child instanceof WhiteNode)) {
								if (child instanceof BlackNode) {
									City c = ((BlackNode) child).getCity();
									if (!(c == null || c.isIsolated())) {
										Rectangle2D.Float r = child.getRect();
										QuadDist qd1 = new QuadDist(r, x, y, child);
										pq1.add(qd1);
									}
								} else {
									Rectangle2D.Float r = child.getRect();
									QuadDist qd1 = new QuadDist(r, x, y, child);
									pq1.add(qd1);
								}
								
							}
						}
						
					}
				}
			}
			
			if (solution != null) {
				//now that we have the solution as a city object, just need to return XmlOutput object
				Success s = new Success(doc, "nearestCity", id);
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
				Error e = new Error(doc, "cityNotFound", "nearestCity", id);
				e.addParam("x", ""+x);
				e.addParam("y", ""+y);
				return e;
			}
//		} else {
//			//probably only isolated cities found.
//			Error e = new Error(doc, "cityNotFound", "nearestCity", id);
//			e.addParam("x", ""+x);
//			e.addParam("y", ""+y);
//			return e;
//		}
	}
	
	public XmlOutput nearestIsolatedCity(int x, int y, Document doc, Integer id) {
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
			} else if (temp == dist) {
				if (c.getName().compareTo(nearest.getName()) > 0) {
					dist = temp;
					nearest = c;
				}
			}
		}
		
		if (nearest == null) {
			Error e = new Error(doc, "cityNotFound", "nearestIsolatedCity", id);
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
			Success s = new Success(doc, "nearestIsolatedCity", id);
			s.addParams("x", "" + x);
			s.addParams("y", "" + y);
			s.addOutputElement(nearestIso);
			return s;
		}
	}
	
	/*
	 * Doing this the same as range roads. Will be exactly O(N) so if you need it to be faster
	 * then you need another algorithm.
	 */
	public XmlOutput nearestRoad(int x, int y, Document doc, Integer id) {
		
		float minDist = -1;
		Road nearestRoad = null;
		if (this.roads.size() == 0) {
			Error e = new Error(doc, "roadNotFound", "nearestRoad", id);
			e.addParam("x", ""+x);
			e.addParam("y", ""+y);
			return e;
		}
		
		for (Road r: this.roads) {
			float tempDist = r.getDistanceToPoint(new Point2D.Float((float)x,(float)y));
			if (minDist == -1) {
				minDist = tempDist;
				nearestRoad = r;
			} else if (tempDist < minDist) {
				minDist = tempDist;
				nearestRoad = r;
			} else if (tempDist == minDist) {
				//need to pick road with greatest alphabetical ordering.
				String startA = nearestRoad.getStart();
				String startB = r.getStart();
				if (startA.compareTo(startB) < 0) {
					minDist = tempDist;
					nearestRoad = r;
				} else if (startA.compareTo(startB) == 0) {
					//tie...go to end road
					String endA  = nearestRoad.getEnd();
					String endB = r.getEnd();
					if (endA.compareTo(endB) < 0) {
						minDist = tempDist;
						nearestRoad = r;
					}
				}
			}
		}
		
		Success s = new Success(doc, "nearestRoad", id);
		s.addParams("x", ""+x);
		s.addParams("y", ""+y);
		s.addOutputElement(nearestRoad.printRoad(doc));
		return s;
	}
	
	public XmlOutput nearestCityToRoad(String start, String end, Document doc, Integer id) {
		//loop through every city mapped and just get the shortest distance.
		
		//first need to make sure the road exists.
		if (this.roadAlreadyMapped(start, end)) {
			City cityA = this.treap.get(start);
			City cityB = this.treap.get(end);
			Line2D.Float line = new Line2D.Float(cityA.getX(), cityA.getY(), cityB.getX(), cityB.getY());
			double shortestDist = -1;
			City shortestCity = null;
			for (String city: this.citiesMapped) {
				City c = this.treap.get(city);
				if (! c.equals(cityA) && !c.equals(cityB)) {
					//double dist = Shape2DDistanceCalculator.distance(line, new Rectangle2D.Float(c.getX(),c.getY(), 0, 0));
					double dist = line.ptSegDist(new Point2D.Float(c.getX(), c.getY()));
					if (shortestDist == -1) {
						shortestDist = dist;
						shortestCity = c;
					} else if (dist < shortestDist) {
						shortestDist = dist;
						shortestCity = c;
					} else if (dist == shortestDist) {
						//need to alphabetically compare the two names.
						if (shortestCity.getName().compareTo(c.getName()) < 0) {
							shortestDist = dist;
							shortestCity = c;
						}
					}
				}
			}
			
			if (shortestCity != null) {
				Element e = doc.createElement("city");
				e.setAttribute("name", shortestCity.getName());
				e.setAttribute("x", "" +shortestCity.getX());
				e.setAttribute("y", "" +shortestCity.getY());
				e.setAttribute("color",shortestCity.getColor());
				e.setAttribute("radius", "" +shortestCity.getRadius());
				Success s = new Success(doc, "nearestCityToRoad", id);
				s.addParams("start", start);
				s.addParams("end", end);
				s.addOutputElement(e);
				return s;
			} else {
				Error e = new Error(doc, "noOtherCitiesMapped", "nearestCityToRoad", id);
				e.addParam("start", start);
				e.addParam("end", end);
				return e;
			}
			
		} else {
			Error e = new Error(doc, "roadIsNotMapped","nearestCityToRoad", id);
			e.addParam("start",start);
			e.addParam("end", end);
			return e;
		}
	}
	
	public XmlOutput shortestPath(String start, String end, String saveMap, String saveHTML, Integer id) {
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
			for(Road r: this.roads) {
				Line2D.Float line = r.getLineSegment();
				cp.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), Color.BLACK);
			}
			cp.save(fileName);
		
		} catch (Exception e) {
			//System.out.println("error: " + e.getMessage());
			
		}
	}
	
	public void saveMap(String fileName, int x, int y, int radius) {
		try {
			CanvasPlus cp = new CanvasPlus();
			cp.setFrameSize(spatialWidth, spatialHeight);
			//cp.setFrameSize(100,100);
			cp = spatialMap.drawMap(cp);
			cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			//cp.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			//cp.addPoint("baltimore", 20, 20, Color.BLACK);
			
			for(Road r: this.roads) {
				Line2D.Float line = r.getLineSegment();
				cp.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), Color.BLACK);
			}
			cp.addCircle(x, y, radius, Color.BLUE, false);
			cp.save(fileName);
		
		} catch (Exception e) {
			//System.out.println("error: " + e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*****************************************************************************************************
	 * 
	 * BELOW THIS ARE ALL OF THE INNER CLASSES REPRESENTING THE STRUCTURE OF THE NODES IN THE PMQUADTREE.
	 *
	 *****************************************************************************************************/
	public class WhiteNode extends Node {
		Rectangle2D.Float rect;
		public WhiteNode() {
			rect = new Rectangle2D.Float(0, 0, spatialWidth, spatialHeight);
		}
		public Node addCity(City c, int startX, int startY, int x, int y, int height, int width) {
			BlackNode b = new BlackNode(startX, startY, height, width);
			b.addCity(c, startX, startY, x, y, height, width);
			return b;
		}
		
		public Node addRoad(Road r, int startX, int startY, int height, int width) {
			BlackNode b = new BlackNode(startX, startY, height, width);
			b.addRoad(r);
			return b;
		}
		
		public Node addRoad(Road r) {
			BlackNode b = new BlackNode(0, 0, spatialHeight, spatialWidth);
			b.addRoad(r);
			return b;
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
		
		public Rectangle2D.Float getRect() {
			return rect;
		}
	}
	
	public class BlackNode extends Node {
		private City city;
		private int startX, startY, x,y,height,width;
		private int numCities;
		private ArrayList<Road> roads;
		public BlackNode(int startX, int startY, int height, int width) {
			this.city = null;
			this.startX = startX;
			this.startY = startY;
			this.height = height;
			this.width = width;
			this.roads = new ArrayList<Road>();
			this.numCities = 0;
		}
		
		public Node addCity(City c, int startX, int startY, int x, int y, int height, int width) {
			
			if (this.city == null) {
				this.city = c;
				this.numCities = 1;
				this.x = x;
				this.y = y;
				return this;
			}
			if (c.equals(city)) {
				//this means we tried adding a city that is already in the map.
				return null;
			}
			GrayNode g = new GrayNode(this.startX, this.startY, this.height, this.width);
			g = (GrayNode) g.addCity(city, this.startX, this.startY, this.x, this.y, this.height, this.width);
			g = (GrayNode) g.addCity(c, this.startX, this.startY, x, y, this.height, this.width);
			for (Road r: this.roads) {
				g.addRoad(r);
			}
			if(PMQuadTree.this.isValid()) {
				return g;
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		public Node addRoad(Road r) {
			this.roads.add(r);
			return this;
		}
		
		private int getNumRoads() {
			return this.roads.size();
		}
		
		private Element printRoads(Element ele, Document doc) {
			this.roads.sort(new RoadComparator());
			for (Road r: this.roads) {
				Element road = r.printRoad(doc);
				ele.appendChild(road);
			}
			
			return ele;
		}
		public Node remove(City c) {
			if (c.equals(this.city)) {
				return whiteNode;
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		public Rectangle2D.Float getRect() {
			return new Rectangle2D.Float((float) startX,(float) startY, (float) height, (float) width);
		}
		
		public City getCity() {
			return this.city;
		}
		
		public Element printNode(Document doc) {
			Element blackNode = doc.createElement("black");
			Element c;
			if (this.numCities == 1) {
				if (city.isIsolated()) {
					c = doc.createElement("isolatedCity");
					blackNode.setAttribute("cardinality", "1");
					c.setAttribute("color", city.getColor());
					c.setAttribute("name", city.getName());
					c.setAttribute("x", "" + city.getX());
					c.setAttribute("y", "" + city.getY());
					c.setAttribute("radius", ""+city.getRadius());
				} else {
					c = doc.createElement("city");
					c.setAttribute("color", city.getColor());
					c.setAttribute("name", city.getName());
					c.setAttribute("x", "" + city.getX());
					c.setAttribute("y", "" + city.getY());
					c.setAttribute("radius", ""+city.getRadius());
				}
				blackNode.appendChild(c);
			}
			
			
			
			int cardinality = this.numCities + getNumRoads();
			blackNode.setAttribute("cardinality", "" + cardinality);
			Element ele = this.printRoads(blackNode, doc);
			return ele;
//			if (!city.isIsolated()) {
//				for (String neighbor: list) {
//					Element road = doc.createElement("road");
//					if (neighbor.compareTo(city.getName()) < 0) {
//						road.setAttribute("end", neighbor);
//						road.setAttribute("start", city.getName());
//					} else {
//						road.setAttribute("end", city.getName());
//						road.setAttribute("start", neighbor);
//					}
//					blackNode.appendChild(road);
//				}
//			}
		}
		
		public CanvasPlus drawMap(CanvasPlus cp) {
			cp.addRectangle(startX, startY, width, height, Color.GRAY, false);
			if (city != null) {
				cp.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
			}
			return cp;
		}
	}
	
	public class GrayNode extends Node {
		private Node[] nodes; //represents the 4 quadrants
		private int startX,startY,height,width;
		public GrayNode(int startX, int startY, int height, int width) {
			this.startX = startX;
			this.startY = startY;
			this.height = height;
			this.width = width;
			this.nodes = new Node[4];
			int mid = width / 2;
			this.nodes[0] = whiteNode;
			this.nodes[1] = whiteNode;
			this.nodes[2] = whiteNode;
			this.nodes[3] = whiteNode;
		}
		public Node addCity(City c, int startX, int startY, int x, int y, int height, int width) {
			//need to get the quadrant the city is to be added to.
			//can be multiple quadrants. Need to check each quadrant to see if they intersect.
			int cityX = c.getX();
			int cityY = c.getY();
			
			int midX = this.startX + (this.width / 2);
			int midY = this.startY + (this.height / 2);
			ArrayList<Integer> desiredQuadrants = getDesiredQuadrants(cityX, cityY, midX, midY);
			
			
//			int desiredX = getX(x, y, height, width, desiredQuadrant);
//			int desiredY = getY(x, y, height, width, desiredQuadrant);
			//need to add city to the node in that quadrant.
					
			for (Integer i: desiredQuadrants) {
				int sX = getStartX(i);
				int sY = getStartY(i);
				Node n = this.nodes[i - 1].addCity(c, sX, sY, x, y, height / 2, width / 2);
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
		
		public Node addRoad(Road r) {
			//check all 4 quadrants and see if road intersects it.
			//Q1
			Rectangle2D.Float q1 = new Rectangle2D.Float(startX,startY+(height /2), height /2, width / 2);
			Rectangle2D.Float q2 = new Rectangle2D.Float(startX + (width / 2), startY + (height / 2), height / 2, width / 2);
			Rectangle2D.Float q3 = new Rectangle2D.Float(startX, startY, (height / 2),  width / 2);
			Rectangle2D.Float q4 = new Rectangle2D.Float(startX + (width / 2), startY, (height / 2), (width/2));
			
			if (r.intersectsRectangle(q1)) {
				if (this.nodes[0] instanceof WhiteNode) {
					BlackNode b = new BlackNode(startX,startY+(height /2), height /2, width / 2);
					b.addRoad(r);
					this.nodes[0] = b;
				} else {
					this.nodes[0] = this.nodes[0].addRoad(r);
				}
			} 
			
			if (r.intersectsRectangle(q2)) {
				if (this.nodes[1] instanceof WhiteNode) {
					BlackNode b = new BlackNode(startX + (width / 2), startY + (height / 2), height / 2, width / 2);
					b.addRoad(r);
					this.nodes[1] = b;
				} else {
					this.nodes[1] = this.nodes[1].addRoad(r);
				}
			}
			
			if (r.intersectsRectangle(q3)) {
				if (this.nodes[2] instanceof WhiteNode) {
					BlackNode b = new BlackNode(startX, startY, (height / 2),  width / 2);
					b.addRoad(r);
					this.nodes[2] = b;
				} else {
					this.nodes[2] = this.nodes[2].addRoad(r);
				}
			} 
			
			if (r.intersectsRectangle(q4)) {
				if (this.nodes[3] instanceof WhiteNode) {
					BlackNode b = new BlackNode(startX + (width / 2), startY, (height / 2), (width/2));
					b.addRoad(r);
					this.nodes[3] = b;
				} else {
					this.nodes[3] = this.nodes[3].addRoad(r);
				}
			}
			return this;
		}
		
		private int getStartX(int quadrant) {
			switch (quadrant) {
			case 1:
				//1st quadrant... startX
				return this.startX;
			case 2:
				//2nd quadrant... startX + mid
				return this.startX + (this.width / 2);
			case 3:
				//3rd quadrant... startX
				return startX;
			case 4: 
				//4th quadrant... startX + mid
				return this.startX + (this.width / 2);
			default:
				return -1;
			}
		}
		
		private int getStartY(int quadrant) {
			switch (quadrant) {
			case 1:
				//1st quadrant... startY + mid
				return this.startY + (this.height / 2);
			case 2:
				//2nd quadrant... startY + mid
				return this.startY + (this.height / 2);
			case 3:
				//3rd quadrant... startY
				return startY;
			case 4: 
				//4th quadrant... startY
				return this.startY;
			default:
				return -1;
			}
		}
		
		public Node remove(City c) {
			//need to get the quadrant the city is supposed to be removed from.
			int cityX = c.getX();
			int cityY = c.getY();
			
			int midX = startX + (width / 2);
			int midY = startY + (height / 2);
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
			return new Rectangle2D.Float((float) startX, (float) startY, (float) height, (float) width);
		}
		
		public Node[] getChildren() {
			return this.nodes;
		}
		
		public Element printNode (Document doc) {
			//need to go through all children nodes and recursively print those.
			Element grayNode = doc.createElement("gray");
			grayNode.setAttribute("x", "" + (startX + (height / 2)));
			grayNode.setAttribute("y", "" + (startY + (width / 2)));
			
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
			//cp.addRectangle(startX, startY, width, height, Color.BLACK, false);
			cp.addRectangle(startX, startY, width / 2, height / 2, Color.GRAY, false);
			cp.addRectangle(startX + (width / 2), startY, width / 2, height / 2, Color.GRAY, false);
			cp.addRectangle(startX, startY + (height / 2), width / 2, height / 2, Color.GRAY, false);
			cp.addRectangle(startX + (width / 2), startY + (height / 2), width / 2, height / 2, Color.GRAY, false);
			for (Node n: nodes) {
				//cp.addRectangle(startX, startY, width, height, Color.BLACK, false);
				if (!(n instanceof WhiteNode)) {
					cp = n.drawMap(cp);
				} else {
					
				}
			}
			return cp;
		}
	}
}
