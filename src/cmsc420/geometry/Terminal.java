package cmsc420.geometry;

import java.awt.geom.Point2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Terminal extends City {

	protected Point2D.Float localPT, remotePT;
	protected String terminalCity, terminalName;
	protected String airportName;
	
	public Terminal(int localX, int localY, int remoteX, int remoteY, String name, String cityName, String airportName) {
		super(name, localX, localY, remoteX, remoteY, 0, "black");
		this.terminalName = name;
		this.terminalCity = cityName;
		this.airportName = airportName;
	}
	
	public String getTerminalCity() {
		return this.terminalCity;
	}
	
	public String getTerminalName() {
		return this.terminalName;
	}

	public String getAirportName() {
		return this.airportName;
	}
	
	@Override
	public int getType() {
		return POINT;
	}
	
	@Override
	public int getCityType() {
		return 3;
	}
	
	@Override
	public Element printNode(Document doc) {
		Element ele = doc.createElement("terminal");
		ele.setAttribute("name", this.terminalName);
		ele.setAttribute("localX", ""+super.localPT.getX());
		ele.setAttribute("localY", ""+super.localPT.getY());
		ele.setAttribute("remoteX", ""+super.remotePT.getX());
		ele.setAttribute("remoteY", ""+super.remotePT.getY());
		ele.setAttribute("airportName", this.airportName);
		ele.setAttribute("cityName", this.terminalCity);
		return ele;
	}

}
