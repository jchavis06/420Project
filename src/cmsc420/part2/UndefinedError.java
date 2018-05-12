package cmsc420.part2;
import cmsc420.xml.XmlUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UndefinedError implements XmlOutput{

	private Document doc;
	private Element root;
	
	public UndefinedError(Document doc, Element root) {
		this.doc = doc;
		this.root = root;
	}
	public Element printOutput() {
		Element elt = doc.createElement("undefinedError");
		//doc.appendChild(elt);
		return elt;
	}
}
