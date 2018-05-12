package cmsc420.part2;
import cmsc420.xml.XmlUtility;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CityList {
	
	private ArrayList<City> cities;
	
	public CityList(ArrayList<City> cities) {
		this.cities = cities;
	}
	
	public Element getXmlElement(Document doc) {
		Element cityList = doc.createElement("cityList");
		for(City c: cities) {
			Element city = doc.createElement("city");
			city.setAttribute("name", c.getName());
			city.setAttribute("x", "" + c.getX());
			city.setAttribute("y", "" + c.getY());
			city.setAttribute("color", c.getColor());
			city.setAttribute("radius", "" + c.getRadius());
			cityList.appendChild(city);
		}
		return cityList;
	}
}
