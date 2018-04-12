package cmsc420.meeshquest.part2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	//public static MeeshMap meeshMap;
	public static PMQuadTree pmQuadTree;
    public static void main(String[] args) {
    	
    	Document results = null;
    	//String testFile = "testFiles\\part2Tests\\part2.public.treap.input.xml";
    	String testFile = "testFiles//part2Tests//test3.xml";
        try {
        	//Document doc = XmlUtility.validateNoNamespace(System.in);
        	Document doc = XmlUtility.validateNoNamespace(new File(testFile));
        	results = XmlUtility.getDocumentBuilder().newDocument();
        
        	Element commandNode = doc.getDocumentElement();
        	int spatialHeight = Integer.parseInt(commandNode.getAttribute("spatialHeight"));
        	int spatialWidth = Integer.parseInt(commandNode.getAttribute("spatialWidth"));
        	int pmOrder = Integer.parseInt(commandNode.getAttribute("pmOrder"));
        	//meeshMap = new MeeshMap(spatialHeight, spatialWidth);
        	if (pmOrder == 1) {
        		pmQuadTree = new PM1QuadTree(spatialHeight, spatialWidth);
        	} else {
        		pmQuadTree = new PM3QuadTree(spatialHeight, spatialWidth);
        	}
        	
        	final NodeList nl = commandNode.getChildNodes();
        

        	Element res = results.createElement("results");
        	res = (Element) results.appendChild(res);
        	for (int i = 0; i < nl.getLength(); i++) {
        		if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
        			commandNode = (Element) nl.item(i);
        			String tag = commandNode.getTagName();
        			if (isValidTag(tag)) {
        				res.appendChild(processTag(results, res, tag, commandNode));
        			} else {
        				FatalError fe = new FatalError(doc);
        				res.appendChild(fe.printOutput());
        			}
        		}
        	}
        } catch (SAXException | IOException | ParserConfigurationException e) {
        	
        	/* TODO: Process fatal error here */
        	//System.out.println("Exception: " + e.getMessage());
        	try {
        		//System.out.println("Errorrrrrrrr: " + e.getMessage());
        		results = XmlUtility.getDocumentBuilder().newDocument();
            	FatalError fe = new FatalError(results);
            	//System.out.println("Error: " + e.getMessage());
            	//Element t = results.createElement("Error");
            	//t.setAttribute("Error: ", e.getMessage());
            	//Element t = results.createElement("Error: "+ e.getMessage());
            	//Element res = results.createElement("results");
            	//res = (Element) results.appendChild(res);
            	//res.appendChild(fe.printOutput());
        		//Error fe = new Error(results, "Error", e.getMessage());
            	results.appendChild(fe.printOutput());
        	} catch (Exception f) {
        		//System.out.println("Cant do that dummy: " + f.get);
        	}
        	
		} finally {
            try {
				XmlUtility.print(results);
			} catch (TransformerException e) {
				//e.printStackTrace();
			}
        }
    }
    
    public static boolean isValidTag(String tagName) {
    	return true; //todo
    }
    
    public static Element processTag(Document doc, Element root, String tagName, Element node) {
    	Integer id;
    	XmlOutput output;
    	int x,y,radius;
    	String cityName;
    	switch(tagName) {
    	case "createCity": 
    		cityName = node.getAttribute("name");
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		radius = Integer.parseInt(node.getAttribute("radius"));
    		id = getId(node);
    		String color = node.getAttribute("color");
    		//output = meeshMap.createCity(cityName, x, y, radius, color, doc);
    		output = pmQuadTree.createCity(cityName, x, y, radius, color, doc, id);
    		root = output.printOutput();
    		break;
    		
    	case "listCities":
    		String sortBy = node.getAttribute("sortBy");
    		id = getId(node);
    		//ArrayList<City> cities = meeshMap.listCities(sortBy);
    		output = pmQuadTree.listCities(sortBy, doc, id);
    		root = output.printOutput();
    		break;
    		
    	case "clearAll":
    		//System.out.println("Cleared all cities.");
    		//meeshMap.clearAll();
    		pmQuadTree.clearAll();
    		id = getId(node);
    		Success s = new Success(doc, "clearAll", id);
    		root = s.printOutput();
    		break;
    	case "mapCity":
    		cityName = node.getAttribute("name");
    		id = getId(node);
    		//output = meeshMap.mapCity(city, doc);
    		output = pmQuadTree.mapIsolatedCity(cityName, doc, id);
    		root = output.printOutput();
    		break;
    	case "unmapCity":
    		cityName = node.getAttribute("name");
    		id = getId(node);
    		//output = meeshMap.unmapCity(city, doc);
    		output = pmQuadTree.unmapCity(cityName, doc, id);
    		root = output.printOutput();
    		break;
    	case "printPMQuadtree":
    		id = getId(node);
    		//output = meeshMap.printPRQuadTree(doc);
    		output = pmQuadTree.printPMQuadtree(doc, id);
    		root = output.printOutput();
    		break;
    	case "deleteCity":
    		cityName = node.getAttribute("name");
    		id = getId(node);
    		//output = meeshMap.deleteCity(c, doc);
    		output = pmQuadTree.deleteCity(cityName, doc, id);
    		root = output.printOutput();
    		break;
    	case "nearestCity":
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		id = getId(node);
    		//output = meeshMap.nearestCity(ex, why, doc);
    		output = pmQuadTree.nearestCity(x, y, doc, id);
    		root = output.printOutput();
    		break;
    	case "rangeCities":
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		int radiuss = Integer.parseInt(node.getAttribute("radius"));
    		id = getId(node);
    		String filename = node.getAttribute("saveMap");
    		//output = meeshMap.rangeCities(x,y, radiuss, filename, doc);
    		output = pmQuadTree.rangeCities(x, y, radiuss, filename, doc, id);
    		root = output.printOutput();
    		break;
    	case "saveMap":
    		String fname = node.getAttribute("name");
    		id = getId(node);
    		//meeshMap.saveMap(fname);
    		pmQuadTree.saveMap(fname);
    		Success ss = new Success(doc, "saveMap", id);
    		ss.addParams("name", fname);
    		root = ss.printOutput();
    		break;
    	case "printTreap":
    		id = getId(node);
    		output = pmQuadTree.printTreap(doc, id);
    		root = output.printOutput();
    		break;
    	case "mapRoad":
    		id = getId(node);
    		String start = node.getAttribute("start");
    		String end = node.getAttribute("end");
    		output = pmQuadTree.mapRoad(start, end, doc, id);
    		root = output.printOutput();
    		break;
    	case "rangeRoads":
    		id = getId(node);
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		radius = Integer.parseInt(node.getAttribute("radius"));
    		String fileName = node.getAttribute("saveMap");
    		output = pmQuadTree.rangeRoads(x, y, radius, fileName, doc, id);
    		root = output.printOutput();
    		break;
    	case "nearestIsolatedCity":
    		id = getId(node);
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		output = pmQuadTree.nearestIsolatedCity(x, y, doc, id);
    		root = output.printOutput();
    		break;
    	case "nearestRoad":
    		id = getId(node);
    		x = Integer.parseInt(node.getAttribute("x"));
    		y = Integer.parseInt(node.getAttribute("y"));
    		output = pmQuadTree.nearestRoad(x, y, doc, id);
    		root = output.printOutput();
    		break;
    	case "nearestCityToRoad":
    		id = getId(node);
    		String startP = node.getAttribute("start");
    		String endP = node.getAttribute("end");
    		output = pmQuadTree.nearestCityToRoad(startP, endP, doc, id);
    		root = output.printOutput();
    		break;
    	case "shortestPath":
    		id = getId(node);
    		String st = node.getAttribute("start");
    		String en = node.getAttribute("end");
    		String saveM = node.getAttribute("saveMap");
    		String saveHTML = node.getAttribute("saveHTML");
    		output = pmQuadTree.shortestPath(st, en, saveM, saveHTML, id);
    		root = output.printOutput();
    		break;
    	}
    	
    	return root;
    }
    
    public static Integer getId(Element node) {
    	String idString = node.getAttribute("id");
    	Integer id;
		if (idString == null || idString.equals("")) {
			id = null;
		} else {
			id = Integer.parseInt(node.getAttribute("id"));
		}
		return id;
    }
}
