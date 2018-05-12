package cmsc420.command;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.dijkstra.Dijkstranator;
import cmsc420.dijkstra.Path;
import cmsc420.drawing.CanvasPlus;
import cmsc420.exception.AirportDoesNotExistThrowable;
import cmsc420.exception.AirportNotInSameMetropoleThrowable;
import cmsc420.exception.AirportOutOfBoundsThrowable;
import cmsc420.exception.AirportViolatesPMRulesThrowable;
import cmsc420.exception.CityAlreadyExistsThrowable;
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.exception.ConnectingCityDoesNotExistThrowable;
import cmsc420.exception.ConnectingCityNotInSameMetropoleThrowable;
import cmsc420.exception.ConnectingCityNotMappedThrowable;
import cmsc420.exception.DuplicateTerminalCoordinatesThrowable;
import cmsc420.exception.DuplicateTerminalNameThrowable;
import cmsc420.exception.MetropoleAlreadyMappedException;
import cmsc420.exception.PMRuleViolation;
import cmsc420.exception.RoadIntersectsAnotherRoadThrowable;
import cmsc420.exception.TerminalOutOfBoundsThrowable;
import cmsc420.exception.TerminalViolatesPMRulesThrowable;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.geometry.Airport;
import cmsc420.geometry.City;
import cmsc420.geometry.CityLocationComparator;
import cmsc420.geometry.CityNameComparator;
import cmsc420.geometry.Geometry;
import cmsc420.geometry.Metropole;
import cmsc420.geometry.Road;
import cmsc420.geometry.RoadAdjacencyList;
import cmsc420.geometry.Terminal;
import cmsc420.pmquadtree.AirportAlreadyExistsThrowable;
import cmsc420.pmquadtree.OutOfBoundsThrowable;
import cmsc420.pmquadtree.PM1Quadtree;
import cmsc420.pmquadtree.PM3Quadtree;
import cmsc420.pmquadtree.PMQuadtree;
import cmsc420.pmquadtree.RoadAlreadyExistsThrowable;
import cmsc420.prquadtree.InternalPRNode;
import cmsc420.prquadtree.LeafPRNode;
import cmsc420.prquadtree.PRNode;
import cmsc420.prquadtree.PRQuadtree;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;
import cmsc420.pmquadtree.PMQuadtree.Node;
import cmsc420.sortedmap.Treap;
import cmsc420.xml.XmlUtility;

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities
	 * command)
	 */
	protected Treap<String, City> citiesByName;

	/**
	 * stores created cities sorted by their locations (used with listCities
	 * command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());

	private final RoadAdjacencyList roads = new RoadAdjacencyList();

	/** stores mapped cities in a spatial data structure */
	protected PMQuadtree pmQuadtree;
	
	protected PRQuadtree remoteMap;

	/** order of the PM Quadtree */
	protected int pmOrder;

	/** spatial width of the PM Quadtree */
	protected int remoteSpatialWidth;
	protected int localSpatialWidth;
	protected int localSpatialHeight;
	/** spatial height of the PM Quadtree */
	protected int remoteSpatialHeight;
	
	protected HashMap<Point2D.Float, PMQuadtree> localMaps;

	/**
	 * Set the DOM Document tree to send the results of processed commands to.
	 * Creates the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());
		
		if (node.hasAttribute("id")) {
		    commandNode.setAttribute("id", node.getAttribute("id"));
		}
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private Element addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		localSpatialWidth = Integer.parseInt(node.getAttribute("localSpatialWidth"));
		localSpatialHeight = Integer.parseInt(node.getAttribute("localSpatialHeight"));
		remoteSpatialHeight = Integer.parseInt(node.getAttribute("remoteSpatialWidth"));
		remoteSpatialWidth = Integer.parseInt(node.getAttribute("remoteSpatialHeight"));
		pmOrder = Integer.parseInt(node.getAttribute("pmOrder"));
		pmQuadtree = null;
		remoteMap = new PRQuadtree();
		remoteMap.setRange(remoteSpatialWidth, remoteSpatialHeight);
		localMaps = new HashMap<Point2D.Float, PMQuadtree>();
		
		
//		if (pmOrder == 3) {
//			pmQuadtree = new PM3Quadtree(spatialWidth, spatialHeight);
//		}
        citiesByName = new Treap<String, City>(new Comparator<String>() {

    		@Override
    		public int compare(String o1, String o2) {
    			return o2.compareTo(o1);
    		}
    		
    	});
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, localX, localY, remoteX, remoteY, radius, color);

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode, parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);

			//parametersNode.appendChild(city.printNode(results));
			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		pmQuadtree.clear();
		roads.clear();

		/* clear canvas */
		// canvas.clear();
		/* add a rectangle to show where the bounds of the map are located */
		// canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
		// false);
		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");
			
			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("localX", Integer.toString((int) city.getLocalX()));
		cityNode.setAttribute("localY", Integer.toString((int) city.getLocalY()));
		cityNode.setAttribute("remoteX", Integer.toString((int) city.getRemoteX()));
		cityNode.setAttribute("remoteY", Integer.toString((int) city.getRemoteY()));
		cityNode.setAttribute("radius",
				Integer.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}

	private void addIsolatedCityNode(final Element node, final City city) {
		addCityNode(node, "isolatedCity", city);
	}

	private void addRoadNode(final Element node, final Road road) {
		addRoadNode(node, "road", road);
	}

	private void addRoadNode(final Element node, final String roadNodeName,
			final Road road) {
		final Element roadNode = results.createElement(roadNodeName);
		roadNode.setAttribute("start", road.getStart().getName());
		roadNode.setAttribute("end", road.getEnd().getName());
		node.appendChild(roadNode);
	}

	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if (start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		} else {
			// add to spatial structure
			City city1 = citiesByName.get(start);
			City city2 = citiesByName.get(end);
			int r1X = city1.getRemoteX();
			int r1Y = city1.getRemoteY();
			int r2X = city2.getRemoteX();
			int r2Y = city2.getRemoteY();
				
			if (r1X != r2X || r1Y != r2Y) {
				addErrorNode("roadNotInOneMetropole", commandNode, parametersNode);
			}
			pmQuadtree = this.getLocalPM(r1X, r1Y);
			if (pmQuadtree == null) {
				//mapping to a new metropole.
				Metropole newMetro = new Metropole(r1X, r1Y);
				pmQuadtree = createPM();
				
					
				try {
					this.remoteMap.add(new Point2D.Float(r1X, r1Y));
				} catch (CityOutOfBoundsException e) {
					//dont do anything yet
				} catch (MetropoleAlreadyMappedException e) {
					//dont do anything yet
				}
				this.localMaps.put(new Point2D.Float(r1X, r1Y), pmQuadtree);
			}
			try {
				pmQuadtree.addRoad(new Road(city1, city2));
				roads.addRoad(new Road(city1, city2));
				
				final Element roadCreatedNode = results.createElement("roadCreated");
				roadCreatedNode.setAttribute("start", start);
				roadCreatedNode.setAttribute("end", end);
				outputNode.appendChild(roadCreatedNode);

				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (RoadAlreadyExistsThrowable e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (OutOfBoundsThrowable e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} catch (RoadIntersectsAnotherRoadThrowable e) {
				addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
			} catch (PMRuleViolation e) {
				addErrorNode("roadViolatesPMRules", commandNode, parametersNode);
			}
		}
	}
    
    public void processPrintTreap(Element node) {
        final Element commandNode = getCommandNode(node);
        final Element parametersNode = results.createElement("parameters");
        final Element outputNode = results.createElement("output");

        if (citiesByName.isEmpty()) {
            addErrorNode("emptyTree", commandNode, parametersNode);
        } else {
			citiesByName.createXml(outputNode);
            addSuccessNode(commandNode, parametersNode, outputNode);
        }
    }
//    
//	public void processMapCity(Element node) {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//
//		final String name = processStringAttribute(node, "name", parametersNode);
//		final Element outputNode = results.createElement("output");
//
//		if (!citiesByName.containsKey(name)) {
//			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
//		} else {
//			try {
//				pmQuadtree.addCity(citiesByName.get(name));
//				/* add success node to results */
//				addSuccessNode(commandNode, parametersNode, outputNode);
//			} catch (RoadAlreadyExistsThrowable e) {
//				addErrorNode("airportAlreadyMapped", commandNode, parametersNode);
//			} catch (CityAlreadyExistsThrowable e) {
//				addErrorNode("airportAlreadyMapped", commandNode, parametersNode);
//			} catch (OutOfBoundsThrowable e) {
//				addErrorNode("cityOutOfBounds", commandNode, parametersNode);
//			}
//		}
//	}

	public void processShortestPath(final Element node) throws IOException,
			ParserConfigurationException, TransformerException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start",
				parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		String saveMapName = "";
		if (!node.getAttribute("saveMap").equals("")) {
			saveMapName = processStringAttribute(node, "saveMap",
					parametersNode);
		}

		String saveHTMLName = "";
		if (!node.getAttribute("saveHTML").equals("")) {
			saveHTMLName = processStringAttribute(node, "saveHTML",
					parametersNode);
		}

		if (!pmQuadtree.containsCity(start)) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
		} else if (!pmQuadtree.containsCity(end)) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
		} else if (!roads.getCitySet().contains(citiesByName.get(start))
				|| !roads.getCitySet().contains(citiesByName.get(end))) {
			// start or end is isolated
			if (start.equals(end)) {
				final Element outputNode = results.createElement("output");
				final Element pathNode = results.createElement("path");
				pathNode.setAttribute("length", "0.000");
				pathNode.setAttribute("hops", "0");

				LinkedList<City> cityList = new LinkedList<City>();
				cityList.add(citiesByName.get(start));
				/* if required, save the map to an image */
				if (!saveMapName.equals("")) {
					saveShortestPathMap(saveMapName, cityList);
				}
				if (!saveHTMLName.equals("")) {
					saveShortestPathMap(saveHTMLName, cityList);
				}

				outputNode.appendChild(pathNode);
				Element successNode = addSuccessNode(commandNode,
						parametersNode, outputNode);

				if (!saveHTMLName.equals("")) {
					/* save shortest path to HTML */
					Document shortestPathDoc = XmlUtility.getDocumentBuilder()
							.newDocument();
					org.w3c.dom.Node spNode = shortestPathDoc.importNode(
							successNode, true);
					shortestPathDoc.appendChild(spNode);
					XmlUtility.transform(shortestPathDoc, new File(
							"shortestPath.xsl"), new File(saveHTMLName
							+ ".html"));
				}
			} else {
				addErrorNode("noPathExists", commandNode, parametersNode);
			}
		} else {
			final DecimalFormat decimalFormat = new DecimalFormat("#0.000");

			final Dijkstranator dijkstranator = new Dijkstranator(roads);

			final City startCity = (City) citiesByName.get(start);
			final City endCity = (City) citiesByName.get(end);

			final Path path = dijkstranator.getShortestPath(startCity, endCity);

			if (path == null) {
				addErrorNode("noPathExists", commandNode, parametersNode);
			} else {
				final Element outputNode = results.createElement("output");

				final Element pathNode = results.createElement("path");
				pathNode.setAttribute("length",
						decimalFormat.format(path.getDistance()));
				pathNode.setAttribute("hops", Integer.toString(path.getHops()));

				final LinkedList<City> cityList = path.getCityList();

				/* if required, save the map to an image */
				if (!saveMapName.equals("")) {
					saveShortestPathMap(saveMapName, cityList);
				}
				if (!saveHTMLName.equals("")) {
					saveShortestPathMap(saveHTMLName, cityList);
				}

				if (cityList.size() > 1) {
					/* add the first road */
					City city1 = cityList.remove();
					City city2 = cityList.remove();
					Element roadNode = results.createElement("road");
					roadNode.setAttribute("start", city1.getName());
					roadNode.setAttribute("end", city2.getName());
					pathNode.appendChild(roadNode);

					while (!cityList.isEmpty()) {
						City city3 = cityList.remove();

						/* process the angle */
						Arc2D.Float arc = new Arc2D.Float();
						arc.setArcByTangent(city1.toPoint2D(),
								city2.toPoint2D(), city3.toPoint2D(), 1);

						/* print out the direction */
						double angle = arc.getAngleExtent();
						final String direction;
						while (angle < 0) {
							angle += 360;
						}
						while (angle > 360) {
							angle -= 360;
						}
						/* This forces boundary between left and straight to be "go straight"go
						 * and boundary between right and straight to be "go right"
						 * -- Eric
						 */
						if (angle > 180 && angle < 180 + 135) {
							direction = "left";
						} else if (angle >= 45 && angle <= 180 ) {
							direction = "right";
						} else {
							direction = "straight";
						}
						Element directionNode = results
								.createElement(direction);
						pathNode.appendChild(directionNode);

						/* print out the next road */
						roadNode = results.createElement("road");
						roadNode.setAttribute("start", city2.getName());
						roadNode.setAttribute("end", city3.getName());
						pathNode.appendChild(roadNode);

						/* increment city references */
						city1 = city2;
						city2 = city3;
					}
				}
				outputNode.appendChild(pathNode);
				Element successNode = addSuccessNode(commandNode,
						parametersNode, outputNode);

				if (!saveHTMLName.equals("")) {
					/* save shortest path to HTML */
					Document shortestPathDoc = XmlUtility.getDocumentBuilder()
							.newDocument();
					org.w3c.dom.Node spNode = shortestPathDoc.importNode(
							successNode, true);
					shortestPathDoc.appendChild(spNode);
					XmlUtility.transform(shortestPathDoc, new File(
							"shortestPath.xsl"), new File(saveHTMLName
							+ ".html"));
				}
			}
		}
	}

	private void saveShortestPathMap(final String mapName,
			final List<City> cityList) throws IOException {
		final CanvasPlus map = new CanvasPlus();
		/* initialize map */
		map.setFrameSize(localSpatialWidth, localSpatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		map.addRectangle(0, 0, localSpatialWidth, localSpatialHeight, Color.BLACK, false);

		final Iterator<City> it = cityList.iterator();
		City city1 = it.next();

		/* map green starting point */
		map.addPoint(city1.getName(), city1.getLocalX(), city1.getLocalY(), Color.GREEN);

		if (it.hasNext()) {
			City city2 = it.next();
			/* map blue road */
			map.addLine(city1.getLocalX(), city1.getLocalY(), city2.getLocalX(), city2.getLocalY(),
					Color.BLUE);

			while (it.hasNext()) {
				/* increment cities */
				city1 = city2;
				city2 = it.next();

				/* map point */
				map.addPoint(city1.getName(), city1.getLocalX(), city1.getLocalY(),
						Color.BLUE);

				/* map blue road */
				map.addLine(city1.getLocalX(), city1.getLocalY(), city2.getLocalX(),
						city2.getLocalY(), Color.BLUE);
			}

			/* map red end point */
			map.addPoint(city2.getName(), city2.getLocalX(), city2.getLocalY(), Color.RED);

		}

		/* save map to image file */
		map.save(mapName);

		map.dispose();
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		CanvasPlus canvas = drawPMQuadtree();

		/* save canvas to '(name).png' */
		canvas.save(name);

		canvas.dispose();

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	private CanvasPlus drawPMQuadtree() {
		final CanvasPlus canvas = new CanvasPlus("MeeshQuest");

		/* initialize canvas */
		canvas.setFrameSize(localSpatialWidth, localSpatialHeight);

		/* add a rectangle to show where the bounds of the map are located */
		canvas.addRectangle(0, 0, localSpatialWidth, localSpatialHeight, Color.BLACK,
				false);

		/* draw PM Quadtree */
		drawPMQuadtreeHelper(pmQuadtree.getRoot(), canvas);

		return canvas;
	}

	private void drawPMQuadtreeHelper(Node node, CanvasPlus canvas) {
		if (node.getType() == Node.BLACK) {
			Black blackNode = (Black) node;
			for (Geometry g : blackNode.getGeometry()) {
				if (g.isCity()) {
					City city = (City) g;
					canvas.addPoint(city.getName(), city.getLocalX(), city.getLocalY(),
							Color.BLACK);
				} else {
					Road road = (Road) g;
					canvas.addLine(road.getStart().getLocalX(), road.getStart()
							.getLocalY(), road.getEnd().getLocalX(),
							road.getEnd().getLocalY(), Color.BLACK);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			Gray grayNode = (Gray) node;
			canvas.addCross(grayNode.getCenterX(), grayNode.getCenterY(),
					grayNode.getHalfWidth(), Color.GRAY);
			for (int i = 0; i < 4; i++) {
				drawPMQuadtreeHelper(grayNode.getChild(i), canvas);
			}
		}
	}

	/**
	 * Prints out the structure of the PM Quadtree in an XML format.
	 * 
	 * @param node
	 *            printPMQuadtree command to be processed
	 */

	public void processPrintPMQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
	
		
		Metropole newMetro = new Metropole(remoteX, remoteY);
		PMQuadtree localMap = null;
		localMap = this.getLocalPM(remoteX, remoteY);
//		if (this.remoteMap.contains(new Point2D.Float(remoteX, remoteY))) {
//			localMap = this.localMaps.get(newMetro);
//		}
		
		if (localMap == null || localMap.isEmpty()) {
			/* NO Local Map exists for these coordinates  */
			addErrorNode("metropoleIsEmpty", commandNode, parametersNode);
		} else {
			/* print PM Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", Integer.toString(pmOrder));
			printPMQuadtreeHelper(localMap.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PR Quadtree.
	 * 
	 * @param currentNode
	 *            PR Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PR Quadtree node
	 */

	private void printPMQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.WHITE) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else if (currentNode.getType() == Node.BLACK) {
			Black currentLeaf = (Black) currentNode;
			Element blackNode = results.createElement("black");
			blackNode.setAttribute("cardinality",
					Integer.toString(currentLeaf.getGeometry().size()));
			for (Geometry g : currentLeaf.getGeometry()) {
				blackNode.appendChild(g.printNode(results));
//				if (g.isCity()) {
//					City c = (City) g;
//					//Element city = results.createElement(pmQuadtree
//						//	.isIsolatedCity(c) ? "isolatedCity" : "city");
//					Element city = results.createElement("city");
//					city.setAttribute("name", c.getName());
//					city.setAttribute("localX", Integer.toString((int) c.getLocalX()));
//					city.setAttribute("local", Integer.toString((int) c.getLocalY()));
//					city.setAttribute("x", Integer.toString((int) c.getLocalX()));
//					city.setAttribute("y", Integer.toString((int) c.getLocalY()));
//					city.setAttribute("radius",
//							Integer.toString((int) c.getRadius()));
//					city.setAttribute("color", c.getColor());
//					blackNode.appendChild(city);
//				} else if (g.isAirport()) {
//					Airport a = (Airport) g;
//					Element airport = results.createElement("airport");
//					airport.setAttribute(", value);
//				} else if (g.isTerminal()) {
//					
//				} else {
//					City c1 = ((Road) g).getStart();
//					City c2 = ((Road) g).getEnd();
//					Element road = results.createElement("road");
//					road.setAttribute("start", c1.getName());
//					road.setAttribute("end", c2.getName());
//					blackNode.appendChild(road);
//				}
			}
			xmlNode.appendChild(blackNode);
		} else {
			final Gray currentInternal = (Gray) currentNode;
			final Element gray = results.createElement("gray");
			gray.setAttribute("x",
					Integer.toString((int) currentInternal.getCenterX()));
			gray.setAttribute("y",
					Integer.toString((int) currentInternal.getCenterY()));
			for (int i = 0; i < 4; i++) {
				printPMQuadtreeHelper(currentInternal.getChild(i), gray);
			}
			xmlNode.appendChild(gray);
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (!node.getAttribute("saveMap").equals("")) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}

		final TreeSet<Geometry> citiesInRange = new TreeSet<Geometry>();
		rangeHelper(new Circle2D.Double(x, y, radius), pmQuadtree.getRoot(),
				citiesInRange, false, true);

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (Geometry g : citiesInRange) {
				addCityNode(cityListNode, (City) g);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				CanvasPlus canvas = drawPMQuadtree();
				if(radius != 0) {
					canvas.addCircle(x, y, radius, Color.BLUE, false);
				}
				canvas.save(pathFile);
				canvas.dispose();
			}
		}
	}

	public void processRangeRoads(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (!node.getAttribute("saveMap").equals("")) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		final TreeSet<Geometry> roadsInRange = new TreeSet<Geometry>();
		rangeHelper(new Circle2D.Double(x, y, radius), pmQuadtree.getRoot(),
				roadsInRange, true, false);

		/* print out cities within range */
		if (roadsInRange.isEmpty()) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
		} else {
			/* get road list */
			final Element roadListNode = results.createElement("roadList");
			for (Geometry g : roadsInRange) {
				addRoadNode(roadListNode, (Road) g);
			}
			outputNode.appendChild(roadListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				CanvasPlus canvas = drawPMQuadtree();
				if (radius != 0) {
					canvas.addCircle(x, y, radius, Color.BLUE, false);
				}
				canvas.save(pathFile);
				canvas.dispose();
			}
		}
	}

	/**
	 * Helper function for both rangeCities and rangeRoads
	 * 
	 * @param range
	 *            defines the range as a circle
	 * @param node
	 *            is the node in the pmQuadtree being processed
	 * @param gInRange
	 *            stores the results
	 * @param includeRoads
	 *            specifies if the range search should include roads
	 * @param includeCities
	 *            specifies if the range search should include cities
	 */
	private void rangeHelper(final Circle2D.Double range, final Node node,
			final TreeSet<Geometry> gInRange, final boolean includeRoads,
			final boolean includeCities) {
		if (node.getType() == Node.BLACK) {
			final Black leaf = (Black) node;
			for (Geometry g : leaf.getGeometry()) {
				if (includeCities
						&& g.isCity()
						&& !gInRange.contains(g)
						&& Inclusive2DIntersectionVerifier.intersects(
								((City) g).toPoint2D(), range)) {
					gInRange.add(g);
				}
				if (includeRoads
						&& g.isRoad()
						&& !gInRange.contains(g)
						&& (((Road) g).toLine2D().ptSegDist(range.getCenter()) <= range
								.getRadius())) {
					gInRange.add(g);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			final Gray internal = (Gray) node;
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(
						internal.getChildRegion(i), range)) {
					rangeHelper(range, internal.getChild(i), gInRange,
							includeRoads, includeCities);
				}
			}
		}
	}

	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);

		pmQuadtree = this.getLocalPM(remoteX, remoteY);
		if (pmQuadtree == null) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		if (pmQuadtree.getNumCities() - pmQuadtree.getNumAirports() == 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			Point2D.Float point = new Point2D.Float(localX, localY);
			addCityNode(outputNode, nearestCityHelper(pmQuadtree, point));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

//	public void processNearestIsolatedCity(Element node) {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//		final Element outputNode = results.createElement("output");
//
//		/* extract attribute values from command */
//		final int x = processIntegerAttribute(node, "x", parametersNode);
//		final int y = processIntegerAttribute(node, "y", parametersNode);
//
//		final Point2D.Float point = new Point2D.Float(x, y);
//		pmQuadtree = this.getLocalPM(remoteX, remoteY)
//		if (pmQuadtree.getNumAirports() == 0) {
//			addErrorNode("cityNotFound", commandNode, parametersNode);
//		} else {
//			addIsolatedCityNode(outputNode, nearestCityHelper(point, true));
//			addSuccessNode(commandNode, parametersNode, outputNode);
//		}
//	}

	private City nearestCityHelper(PMQuadtree quadtree, Point2D.Float point) {
		Node n = quadtree.getRoot();
		PriorityQueue<NearestSearchRegion> nearCities = new PriorityQueue<NearestSearchRegion>();

		if (n.getType() == Node.BLACK) {
			Black b = (Black) n;
			
			if (b.getCity() != null && this.isActualCity(b.getCity())) {
				return b.getCity();
			}
			
		}

		while (n.getType() == Node.GRAY) {
			Gray g = (Gray) n;
			Node kid;
			
			for (int i = 0; i < 4; i++) {
				kid = g.getChild(i);
				
				if (kid.getType() == Node.BLACK) {
					Black b = (Black) kid;
					City c = b.getCity();
					
					if (c != null
							&&  this.isActualCity(c)) {
						double dist = point.distance(c.toPoint2D());
						nearCities.add(new NearestSearchRegion(kid, dist, c));
					}
				} else if (kid.getType() == Node.GRAY) {
					double dist = Shape2DDistanceCalculator.distance(point,
							g.getChildRegion(i));
					nearCities.add(new NearestSearchRegion(kid, dist, null));
				}
			}
			
			try {
				n = nearCities.remove().node;
			} catch (Exception ex) {
				throw new IllegalStateException();
			}
		}
		return ((Black) n).getCity();
	}
	
	public void processGlobalRangeCitie(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);
		
		//first need to use PRQuadtree to get metropoles that are in the range.
		Point2D.Float point = new Point2D.Float(remoteX, remoteY);
		TreeSet<Point2D.Float> metropolesInRange = new TreeSet<Point2D.Float>();
		prRangeCitiesHelper(point, radius, this.remoteMap.getRoot(), metropolesInRange);
		if (metropolesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		}
		//now, all metropoles in range are in the tree set.
		ArrayList<City> allCities = new ArrayList<City>(); //will be a list of every city that is in range.
		for(Point2D.Float dot: metropolesInRange) {
			PMQuadtree localTree = this.getLocalPM((int)dot.x, (int)dot.y);
			TreeSet<City> cities = localTree.getCitiesInMap();
			allCities.addAll(cities);
		}
		
		//now we need to sort by name.
		allCities.sort(new CityNameComparator());
		final Element cityListNode = results.createElement("cityList");
		for(City c: allCities) {
			if (c.isCity()) {
				addCityNode(cityListNode, c);
			}
		}
		outputNode.appendChild(cityListNode);
		addSuccessNode(commandNode, parametersNode, outputNode);
	}
	
	

	/**
	 * Determines if any cities within the PR Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PR Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void prRangeCitiesHelper(final Point2D.Float point,
			final int radius, final PRNode node, final TreeSet<Point2D.Float> metropolesInRange) {
		if (node.getType() == PRNode.LEAF) {
			final LeafPRNode leaf = (LeafPRNode) node;
			final double distance = point.distance(leaf.getMetropole());
			if (distance <= radius) {
				/* city is in range */
				final Point2D.Float pole = leaf.getMetropole();
				metropolesInRange.add(pole);
			}
		} else if (node.getType() == PRNode.INTERNAL) {
			/* check each quadrant of internal PRNode */
			final InternalPRNode internal = (InternalPRNode) node;

			final Circle2D.Float circle = new Circle2D.Float(point, radius);
			for (int i = 0; i < 4; i++) {
				if (this.remoteMap.intersects(circle, internal.getChildRegion(i))) {
					prRangeCitiesHelper(point, radius, internal.getChild(i),
							metropolesInRange);
				}
			}
		}
	}

	public void processNearestRoad(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		if (pmQuadtree.getNumRoads() <= 0) {
			addErrorNode("roadNotFound", commandNode, parametersNode);
		} else {
			final Point2D.Float pt = new Point2D.Float(x, y);
			Road road = nearestRoadHelper(pt);
			addRoadNode(outputNode, road);
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	private Road nearestRoadHelper(Point2D.Float point) {
		Node n = pmQuadtree.getRoot();
		PriorityQueue<NearestSearchRegion> nearRoads = new PriorityQueue<NearestSearchRegion>();
		NearestSearchRegion region = null;
		
		if (n.getType() == Node.BLACK) {
			List<Geometry> gList = ((Black) n).getGeometry();
			double minDist = Double.MAX_VALUE;
			Road road = null;
			
			for (Geometry geom : gList) {
				if (geom.isRoad()) {
					double d = ((Road) geom).toLine2D().ptSegDist(point);
					
					if (d < minDist) {
						minDist = d;
						road = (Road) geom;
					}
				}
			}
			return road;
		}

		while (n.getType() == Node.GRAY) {
			Gray g = (Gray) n;
			Node kid;
			
			for (int i = 0; i < 4; i++) {
				kid = g.getChild(i);
				
				if (kid.getType() == Node.BLACK) {
					Black b = (Black) kid;
					List<Geometry> gList = b.getGeometry();
					double minDist = Double.MAX_VALUE;
					Road road = null;
					
					for (Geometry geom : gList) {
						if (geom.isRoad()) {
							double d = ((Road) geom).toLine2D()
									.ptSegDist(point);
							
							if (d < minDist) {
								minDist = d;
								road = (Road) geom;
							}
						}
					}
					if (road == null) {
						continue;
					}
					nearRoads.add(new NearestSearchRegion(kid, minDist, road));
				} else if (kid.getType() == Node.GRAY) {
					double dist = Shape2DDistanceCalculator.distance(point,
							g.getChildRegion(i));
					nearRoads.add(new NearestSearchRegion(kid, dist, null));
				}
			}
			
			try {
				region = nearRoads.remove();
				n = region.node;
			} catch (Exception ex) {
				// should be impossible to reach here
				throw new IllegalStateException();
			}
		}
		assert region.node.getType() == Node.BLACK;
		return (Road) region.g;
	}

	public void processNearestCityToRoad(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String start = processStringAttribute(node, "start",
				parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		final City startCity = (City) citiesByName.get(start);
		final City endCity = (City) citiesByName.get(end);
		if (startCity == null || endCity == null) {
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			return;
		}
		final Road road = new Road(startCity, endCity);

		if (pmQuadtree.containsRoad(road)) {
			City nc = nearestCityToRoadHelper(road);
			if (nc == null || road.contains(nc)) {
				addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
			} else {
				addCityNode(outputNode, nc);
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		} else {
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
		}
	}

	private City nearestCityToRoadHelper(Road road) {
		Node n = pmQuadtree.getRoot();
		PriorityQueue<NearestSearchRegion> nearCities = new PriorityQueue<NearestSearchRegion>();

		while (n.getType() == Node.GRAY) {
			Gray g = (Gray) n;
			Node kid;
			for (int i = 0; i < 4; i++) {
				kid = g.getChild(i);
				if (kid.getType() == Node.BLACK) {
					City c = ((Black) kid).getCity();
					if (c != null && !road.contains(c)) {
						double dist = road.toLine2D().ptSegDist(c.toPoint2D());
						nearCities.add(new NearestSearchRegion(kid, dist, c));
					}
				} else if (kid.getType() == Node.GRAY) {
					double dist = Shape2DDistanceCalculator.distance(
							road.toLine2D(), g.getChildRegion(i));
					nearCities.add(new NearestSearchRegion(kid, dist, null));
				}
			}
			try {
				if (nearCities.isEmpty()) {
					// no other cities mapped
					return null;
				}
				n = nearCities.remove().node;
			} catch (Exception ex) {
				throw new IllegalStateException();
			}
		}
		return ((Black) n).getCity();
	}
	
	/*
	 * Deletes city from dictionaries....
	 * If mapped in a quadtree, the roads need to be unmapped and
	 * this method will print those out.
	 * Only error case is the cityDoesNotExist error.
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String name = processStringAttribute(node, "name", parametersNode);
		
		//first, make sure the city is in the dictionary.
		City c = this.citiesByName.get(name);
		if (c == null || c.isAirport() || c.isTerminal()) {
			//city does not exist in the dictionary.
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
			return;
		} 
		
		//city exists in the dictionary. we need to unmap everything first, before deleting.
		//To do this, see if it is mapped in the remote map.
		pmQuadtree = this.getLocalPM(c.getRemoteX(), c.getRemoteY());
		if (pmQuadtree.containsCity(name)) {
			ArrayList<Geometry> deletedItems = pmQuadtree.unmapCity(c);
			Element cityUnmapped = results.createElement("cityUnmapped");
			cityUnmapped.setAttribute("name", c.getName());
			cityUnmapped.setAttribute("remoteX", "" + c.getRemoteX());
			cityUnmapped.setAttribute("remoteY", "" + c.getRemoteY());
			cityUnmapped.setAttribute("localX", "" + c.getLocalX());
			cityUnmapped.setAttribute("localY", "" + c.getLocalY());
			cityUnmapped.setAttribute("color", c.getColor());
			cityUnmapped.setAttribute("radius", "" + c.getRadius());
			outputNode.appendChild(cityUnmapped);
			for (Geometry g: deletedItems) {
				if (g.isRoad()) {
					Element roadUnmapped = results.createElement("roadUnmapped");
					roadUnmapped.setAttribute("start", ((Road) g).getStart().getName());
					roadUnmapped.setAttribute("end", ((Road) g).getEnd().getName());
					outputNode.appendChild(roadUnmapped);
				} else {
					//could remove other cities as a side effect, but don't print out.
				}
			}
		}
		
		//now that we have unmapped the city, we can delete it from dictionary.
		deleteCityHelper(c);
		
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/*
	 * Helper method to do the deleting so that we can add structures to this function
	 * where we delete the city from it. This is incase we later on add structures to the class
	 * and it contains this city. Delete in one spot rather than multiple and forget.
	 */
	private void deleteCityHelper(City c) {
		this.citiesByName.remove(c.getName());
		this.citiesByLocation.remove(c);
	}
	
	public void processMapAirport(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String terminalName = processStringAttribute(node, "terminalName", parametersNode);
		final int terminalX = processIntegerAttribute(node, "terminalX", parametersNode);
		final int terminalY = processIntegerAttribute(node, "terminalY", parametersNode);
		final String terminalCity = processStringAttribute(node, "terminalCity", parametersNode);
		
		
		final Airport newAirport = new Airport(name, localX, localY, remoteX, remoteY, terminalName, terminalX, terminalY, terminalCity);
		//check for duplicate airport name
		if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateAirportName", commandNode, parametersNode);
			return;
		}
		//check for duplicate airport coordinates
		if (citiesByLocation.contains(newAirport)) {
			addErrorNode("duplicateAirportCoordinates", commandNode, parametersNode);
			return;
		}
		
		pmQuadtree = this.getLocalPM(remoteX, remoteY);
		if (pmQuadtree == null) {
			//adding airport to new local map
			pmQuadtree = createPM();
			this.localMaps.put(new Point2D.Float(remoteX, remoteY), pmQuadtree);
		}
		
		//check airport out of bounds.
		if (! pmQuadtree.isPointInBounds(new Point2D.Float(localX, localY))) {
			addErrorNode("airportOutOfBounds", commandNode, parametersNode);
			return;
		}
		
		//check duplicate terminal name/coordinates.
		if (citiesByName.containsKey(terminalName)) {
			addErrorNode("duplicateTerminalName", commandNode, parametersNode);
			return;
		}
		
		if (citiesByLocation.contains(newAirport.getFirstTerminal())) {
			addErrorNode("duplicateTerminalCoordinates", commandNode, parametersNode);
			return;
		}
		
		//check terminal out of bounds.
		if (! pmQuadtree.isPointInBounds(new Point2D.Float(terminalX, terminalY))){
			addErrorNode("terminalOutOfBounds", commandNode, parametersNode);
			return;
		}
		
		if (this.citiesByName.containsKey(terminalCity)) {
			
			City t = this.citiesByName.get(terminalCity);
			int terminalRemoteX = t.getRemoteX();
			int terminalRemoteY = t.getRemoteY();
			if (remoteX != terminalRemoteX || remoteY != terminalRemoteY) {
				addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
			}
			
		} else {
			addErrorNode("connectingCityDoesNotExist", commandNode, parametersNode);
		}
		
		
		try {
			pmQuadtree.addAirport(newAirport);
			citiesByName.put(name, newAirport);
			citiesByLocation.add(newAirport);
			addSuccessNode(commandNode, parametersNode, outputNode);
		} catch (ConnectingCityNotMappedThrowable e) {
			addErrorNode("connectingCityNotMapped", commandNode, parametersNode);
		} catch (AirportViolatesPMRulesThrowable e) {
			addErrorNode("airportViolatesPMRules", commandNode, parametersNode);
		} catch (TerminalViolatesPMRulesThrowable e) {
			addErrorNode("terminalViolatesPMRules", commandNode, parametersNode);
		} catch (RoadIntersectsAnotherRoadThrowable e) {
			addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
		}
	}
	
	public void processMapTerminal(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String cityName = processStringAttribute(node, "cityName", parametersNode);
		final String airportName = processStringAttribute(node, "airportName", parametersNode);
		
		
//		duplicateTerminalName
//		duplicateTerminalCoordinates
//		terminalOutOfBounds
//		airportDoesNotExist
//		airportNotInSameMetropole
//		connectingCityDoesNotExist
//		connectingCityNotInSameMetropole
//		connectingCityNotMapped
//		terminalViolatesPMRules
//		roadIntersectsAnotherRoad

		if (this.citiesByName.containsKey(name)) {
			addErrorNode("duplicateTerminalName", commandNode, parametersNode);
			return;
		}
		
		Terminal newTerminal = new Terminal(localX, localY, remoteX, remoteY, name, cityName, airportName);
		if (this.citiesByLocation.contains(newTerminal)) {
			addErrorNode("duplicateTerminalCoordinates", commandNode, parametersNode);
			return;
		}
		
		pmQuadtree = this.getLocalPM(remoteX, remoteY);
		if (pmQuadtree == null) {
			//adding terminal to new local map
			pmQuadtree = createPM();
			this.localMaps.put(new Point2D.Float(remoteX, remoteY), pmQuadtree);
		}
		
		if (! pmQuadtree.isPointInBounds(new Point2D.Float(localX, localY))) {
			addErrorNode("terminalOutOfBounds", commandNode, parametersNode);
			return;
		}
		
		if (this.citiesByName.containsKey(airportName)) {
			Airport a = (Airport) this.citiesByName.get(airportName);
			int airportX = a.getLocalX();
			int airportY = a.getLocalY();
			if (airportX != localX || airportY != localY) {
				addErrorNode("airportNotInSameMetropole", commandNode, parametersNode);
				return;
			}
		} else {
			addErrorNode("airportDoesNotExist", commandNode, parametersNode);
			return;
		}
		
		if (this.citiesByName.containsKey(cityName)) {
			City c = this.citiesByName.get(cityName);
			int cityX = c.getLocalX();
			int cityY = c.getLocalY();
			if (cityX != localX || cityY != localY) {
				addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
			}
		} else {
			addErrorNode("connectingCityDoesNotExist", commandNode, parametersNode);
			return;
		}
		
		try {
			pmQuadtree.addTerminal(newTerminal);
			citiesByName.put(name, newTerminal);
			citiesByLocation.add(newTerminal);
			addSuccessNode(commandNode, parametersNode, outputNode);
		} catch (ConnectingCityNotMappedThrowable e) {
			addErrorNode("connectingCityNotMapped", commandNode, parametersNode);
		} catch (TerminalViolatesPMRulesThrowable e) {
			addErrorNode("terminalViolatesPMRules", commandNode, parametersNode);
		} catch (RoadIntersectsAnotherRoadThrowable e) {
			addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
		}
		
		
	}
	
	public void processUnmapRoad(final Element node) {
//		startPointDoesNotExist
//		endPointDoesNotExist
//		startEqualsEnd
//		roadNotMapped
		
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		
		//first make sure that the start/end points exist in a local map.
		City city1 = this.citiesByName.get(start);
		if (city1 == null || !city1.isCity()) {
			//either city is null, airport, or terminal.
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
			return;
		}
		
		City city2 = this.citiesByName.get(end);
		if (city2 == null || !city2.isCity()) {
			//either city is null, airport, or terminal.
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
			return;
		}
		
		if (city1.equals(city2)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
			return;
		}
		
		//now we need to get the road that this is mapped to.
		int remoteX = city1.getRemoteX();
		int remoteY = city1.getRemoteY();
		if (remoteX != city2.getRemoteX() || remoteY != city2.getRemoteY()) {
			//cant be mapped road if in different metropoles.
			addErrorNode("roadNotMapped", commandNode, parametersNode);
			return;
		}
		
		pmQuadtree = this.getLocalPM(remoteX, remoteY);
		Road r = new Road(city1, city2);
		if (pmQuadtree.containsRoad(r)) {
			//may continue
			pmQuadtree.unmapRoad(r);
			Element roadDeleted = results.createElement("roadDeleted");
			roadDeleted.setAttribute("start", start);
			roadDeleted.setAttribute("end", end);
			outputNode.appendChild(roadDeleted);
			addSuccessNode(commandNode, parametersNode, outputNode);
		} else {
			addErrorNode("roadNotMapped", commandNode, parametersNode);
			return;
		}
	}

	public void processUnmapAirport(final Element node) {
		
	}
	
	public void processUnmapTerminal(final Element node) {
		
	}
	
	public void processMst(final Element node) {
		
	}
	/**
	 * Helper class for nearest everything (city/road/etc)
	 */
	private class NearestSearchRegion implements
			Comparable<NearestSearchRegion> {
		private Node node;
		private double distance;
		private Geometry g;

		public NearestSearchRegion(Node node, double distance, Geometry g) {
			this.node = node;
			this.distance = distance;
			this.g = g;
		}

		public int compareTo(NearestSearchRegion o) {
			if (distance == o.distance) {
				if (node.getType() == Node.BLACK
						&& o.node.getType() == Node.BLACK) {
					return g.compareTo(o.g);
				} else if (node.getType() == Node.BLACK
						&& o.node.getType() == Node.GRAY) {
					return 1;
				} else if (node.getType() == Node.GRAY
						&& o.node.getType() == Node.BLACK) {
					return -1;
				} else {
					return ((Gray) node).hashCode()
							- ((Gray) o.node).hashCode();
				}
			}
			return (distance < o.distance) ? -1 : 1;
		}
	}
	
	private PMQuadtree getLocalPM(int remoteX, int remoteY) {
		Point2D.Float point = new Point2D.Float(remoteX, remoteY);
		if (this.remoteMap.contains(point)) {
			return this.localMaps.get(point);
		} else {
			return null;
		}
	}
	
	private PMQuadtree createPM() {
		//this method will return a new pmquadtree that is either PM1 or PM3
		//depending on what order the commands node says.
		
		if (pmOrder == 1) {
			return new PM1Quadtree(localSpatialWidth, localSpatialHeight);
		} else {
			return new PM3Quadtree(localSpatialWidth, localSpatialHeight);
		}
	}
	
	private boolean isActualCity(Geometry g) {
		return g.isCity();
	}
}
