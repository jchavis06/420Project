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
    	String testFile = "testFiles\\part1Tests\\testAll.xml";
    	
        try {
        	Document doc = XmlUtility.validateNoNamespace(System.in);
        	//Document doc = XmlUtility.validateNoNamespace(new File(testFile));
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
        		results = XmlUtility.getDocumentBuilder().newDocument();
            	FatalError fe = new FatalError(results);
            	//Element t = results.createElement("Error: "+ e.getMessage());
            	//Element res = results.createElement("results");
            	//res = (Element) results.appendChild(res);
            	//res.appendChild(fe.printOutput());
            	results.appendChild(fe.printOutput());
        	} catch (Exception f) {
        		//System.out.println("")
        	}
        	
		} finally {
            try {
				XmlUtility.print(results);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
        }
    }
    
    public static boolean isValidTag(String tagName) {
    	return true; //todo
    }
    
    public static Element processTag(Document doc, Element root, String tagName, Element node) {
    	XmlOutput output;
    	switch(tagName) {
    	case "createCity": 
    		String cityName = node.getAttribute("name");
    		int x = Integer.parseInt(node.getAttribute("x"));
    		int y = Integer.parseInt(node.getAttribute("y"));
    		int radius = Integer.parseInt(node.getAttribute("radius"));
    		String color = node.getAttribute("color");
    		//output = meeshMap.createCity(cityName, x, y, radius, color, doc);
    		output = pmQuadTree.createCity(cityName, x, y, radius, color, doc);
    		root = output.printOutput();
    		break;
    		
    	case "listCities":
    		String sortBy = node.getAttribute("sortBy");
    		//ArrayList<City> cities = meeshMap.listCities(sortBy);
    		output = pmQuadTree.listCities(sortBy, doc);
    		root = output.printOutput();
    		break;
    		
    	case "clearAll":
    		//System.out.println("Cleared all cities.");
    		//meeshMap.clearAll();
    		pmQuadTree.clearAll();
    		Success s = new Success(doc, "clearAll");
    		root = s.printOutput();
    		break;
    	case "mapCity":
    		String city = node.getAttribute("name");
    		//output = meeshMap.mapCity(city, doc);
    		output = pmQuadTree.mapIsolatedCity(city, doc);
    		root = output.printOutput();
    		break;
    	case "unmapCity":
    		city = node.getAttribute("name");
    		//output = meeshMap.unmapCity(city, doc);
    		output = pmQuadTree.unmapCity(city, doc);
    		root = output.printOutput();
    		break;
    	case "printPRQuadtree":
    		//output = meeshMap.printPRQuadTree(doc);
    		output = pmQuadTree.printPMQuadtree(doc);
    		root = output.printOutput();
    		break;
    	case "deleteCity":
    		String c = node.getAttribute("name");
    		//output = meeshMap.deleteCity(c, doc);
    		output = pmQuadTree.deleteCity(c, doc);
    		root = output.printOutput();
    		break;
    	case "nearestCity":
    		int ex = Integer.parseInt(node.getAttribute("x"));
    		int why = Integer.parseInt(node.getAttribute("y"));
    		//output = meeshMap.nearestCity(ex, why, doc);
    		output = pmQuadTree.nearestCity(ex, why, doc);
    		root = output.printOutput();
    		break;
    	case "rangeCities":
    		int xx = Integer.parseInt(node.getAttribute("x"));
    		int yy = Integer.parseInt(node.getAttribute("y"));
    		int radiuss = Integer.parseInt(node.getAttribute("radius"));
    		String filename = node.getAttribute("saveMap");
    		//output = meeshMap.rangeCities(xx,yy, radiuss, filename, doc);
    		output = pmQuadTree.rangeCities(xx, yy, radiuss, filename, doc);
    		root = output.printOutput();
    		break;
    	case "saveMap":
    		String fname = node.getAttribute("name");
    		//meeshMap.saveMap(fname);
    		pmQuadTree.saveMap(fname);
    		Success ss = new Success(doc, "saveMap");
    		ss.addParams("name", fname);
    		root = ss.printOutput();
    		break;
    	case "printTreap":
    		output = pmQuadTree.printTreap(doc);
    		root = output.printOutput();
    		break;
    	case "mapRoad":
    		
    		break;
    	case "rangeRoads":
    		
    		break;
    	case "nearestIsolatedCity":
    		
    		break;
    	case "nearestRoad":
    		
    		break;
    	case "nearestCityToRoad":
    		
    		break;
    	case "shortestPath":
    		
    		break;
    	}
    	
    	return root;
    }
}
