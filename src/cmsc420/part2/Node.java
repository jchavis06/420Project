package cmsc420.part2;

import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public abstract class Node{

	public Node addCity(City c, int startX, int startY, int x, int y, int height, int width) {
		return null;
	}
	
	public Node addRoad(Road r) {
		return null;
	}
	
	public Node remove(City c){
		return null;
	}
	
	public Rectangle2D.Float getRect(){
		return null;
	}
	
	public Element printNode(Document doc) {
		return null;
	}
	
	public CanvasPlus drawMap(CanvasPlus cp) {
		return null;
	}
	
	
	
	
}
