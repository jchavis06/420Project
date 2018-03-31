package cmsc420.meeshquest.part2;
import cmsc420.xml.XmlUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Success implements XmlOutput{

	private Document doc;
	private String command;
	private LinkedHashMap<String, String> params;
	private ArrayList<Element> output;
	
	public Success(Document doc, String command) {
		this.doc = doc;
		this.command = command;
		params = new LinkedHashMap<String, String>();
		output = new ArrayList<Element>();
	}
	
	public void addParams(String param, String value) {
		params.put(param, value);
	}
	
	public void addOutputElement(Element e) {
		output.add(e);
	}
	
	@Override
	public Element printOutput() {
		Element elt = doc.createElement("success");
		Element com = doc.createElement("command");
		//doc.appendChild(elt);
		elt.appendChild(com);
		com.setAttribute("name", command);
		Element parameters = doc.createElement("parameters");
		elt.appendChild(parameters);
		if (! params.isEmpty()) {
			for(String s: params.keySet()) {
				Element param = doc.createElement(s);
				param.setAttribute("value", params.get(s));
				parameters.appendChild(param);
			}
		}
		Element outputEle = doc.createElement("output");
		if (!output.isEmpty()) {
			for (Element e: output) {
				outputEle.appendChild(e);
			}
		}
		elt.appendChild(outputEle);
		return elt;
	}

}
