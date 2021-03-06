package cmsc420.part2;
import java.util.LinkedHashMap;

import org.w3c.dom.Document;

import cmsc420.xml.XmlUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Error implements XmlOutput{

	private Document doc;
	private String type;
	private String command;
	private Integer id;
	private LinkedHashMap<String, String> params;
	
	public Error (Document doc, String type, String command) {
		this.doc = doc;
		this.type = type;
		this.command = command;
		params = new LinkedHashMap<String, String>();
	}
	
	public Error (Document doc, String type, String command, Integer id) {
		this.doc = doc;
		this.type = type;
		this.command = command;
		params = new LinkedHashMap<String, String>();
		this.id = id;
	}
	public void addParam(String param, String value) {
		params.put(param, value);
	}
	
	public Element printOutput() {
		Element elt = doc.createElement("error");
		//doc.appendChild(elt);
		Element com = doc.createElement("command");
		elt.appendChild(com);
		if (this.id != null) {
			com.setAttribute("id", ""+id);
		}
		com.setAttribute("name", command);
		elt.setAttribute("type", type);
		Element parameters = doc.createElement("parameters");
		elt.appendChild(parameters);
		if (! params.isEmpty()) {
			for(String s: params.keySet()) {
				Element param = doc.createElement(s);
				param.setAttribute("value", params.get(s));
				parameters.appendChild(param);
			}
		}
		return elt;			
	}
}
