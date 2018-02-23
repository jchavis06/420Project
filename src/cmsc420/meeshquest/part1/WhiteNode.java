package cmsc420.meeshquest.part1;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class WhiteNode extends Node{

//	private static WhiteNode myInstance;
//	private WhiteNode() {
//		
//	}
//	
//	public static WhiteNode getInstance() {
//		if (myInstance == null) {
//			myInstance = new WhiteNode();
//		}
//		return myInstance;
//	}
//	
//	public Node add(City c, int x, int y, int height, int width) {
//		return new BlackNode(c, x, y, height, width);
//	}
//	
//	public Node remove(City c) {
//		//seriously hope this doesn't happen.
//		//ideally should throw an exception here.
//		return null;
//	}
//	
//	public Element printNode(Document doc) {
//		Element whiteNode = doc.createElement("white");
//		return whiteNode;
//	}
	
	private int x, y, height, width;
	public WhiteNode(int x, int y, int height, int width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}
	
	public Node add(City c) {
		return new BlackNode(c, x, y, height, width);
	}
	
	public Node remove(City c) {
		//seriously hope this doesnt happen.
		return null;
	}
	
	public Element printNode(Document doc) {
		Element whiteNode = doc.createElement("white");
		return whiteNode;
	}
	
	public Rectangle2D.Float getRect() {
		return new Rectangle2D.Float((float) x, (float) y, (float) height, (float) width);
	}
	
	public CanvasPlus drawMap(CanvasPlus cp) {
		cp.addRectangle(x, y, width, height, Color.BLACK, false);
		return cp;
	}
}
