package cmsc420.meeshquest.part2;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RoadList {
		private ArrayList<Road> roads;
		
		public RoadList(ArrayList<Road> roads) {
			this.roads = roads;
		}
		
		public Element getXmlElement(Document doc) {
			Element cityList = doc.createElement("roadList");
			for(Road r: roads) {
				Element road = r.printRoad(doc);
				cityList.appendChild(road);
			}
			return cityList;
		}
}
