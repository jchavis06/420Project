package cmsc420.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Airport extends City {
	protected ArrayList<Terminal> terminals;
	protected Terminal firstTerminal; //something used for mapAirport
	
	public Airport(String name, int localX, int localY, int remoteX, int remoteY, String terminalName, int terminalX, int terminalY, String terminalCity){
		super(name, localX, localY, remoteX, remoteY, 0, "Black");
		this.terminals = new ArrayList<Terminal>();
		Terminal t = new Terminal((int) terminalX, (int) terminalY, remoteX, remoteY, terminalName, terminalCity, name);
		this.terminals.add(t);
		this.firstTerminal = t;
	}
	
	public int getNumTerminals() {
		return this.terminals.size();
	}
	
	@Override
	public int getCityType() {
		return 2;
	}
	
	public Terminal getFirstTerminal(){
		return this.firstTerminal;
	}
	
	public void addTerminal(Terminal t) {
		this.terminals.add(t);
	}
	
	public ArrayList<Terminal> getTerminals() {
		return this.terminals;
	}
	
	@Override
	public Element printNode(Document doc) {
		Element ele = doc.createElement("airport");
		ele.setAttribute("name",  this.name);
		ele.setAttribute("localX", ""+this.localPT.getX());
		ele.setAttribute("localY", ""+this.localPT.getY());
		ele.setAttribute("remoteX", ""+this.remotePT.getX());
		ele.setAttribute("remoteY", "" + this.remotePT.getY());
		return ele;
	}
			

}
