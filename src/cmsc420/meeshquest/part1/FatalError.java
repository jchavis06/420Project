package cmsc420.meeshquest.part1;
import cmsc420.xml.XmlUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FatalError implements XmlOutput{

	private Document doc;
	
	public FatalError(Document doc) {
		this.doc = doc;
	}
	public Element printOutput() {
		Element elt = doc.createElement("fatalError");
		//doc.appendChild(elt);
		return elt;
	}
}
