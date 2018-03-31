package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class GrayNode extends Node {

	private Node[] nodes; //represents the 4 quadrants
	private int x,y,height,width;
	public GrayNode(int x, int y, int height, int width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.nodes = new Node[4];
		int mid = width / 2;
		this.nodes[0] = new WhiteNode(x, y + mid, height / 2, width / 2);
		this.nodes[1] = new WhiteNode(x + mid, y + mid, height / 2, width / 2);
		this.nodes[2] = new WhiteNode(x, y, height / 2, width / 2);
		this.nodes[3] = new WhiteNode(x + mid, y, height / 2, width / 2);
	}
	
	public Node add(City c) {
		//need to get the quadrant the city is to be added to.
		int cityX = c.getX();
		int cityY = c.getY();
		
		int midX = x + (width / 2);
		int midY = y + (height / 2);
		int desiredQuadrant = getDesiredQuadrant(cityX, cityY, midX, midY);
		
		
//		int desiredX = getX(x, y, height, width, desiredQuadrant);
//		int desiredY = getY(x, y, height, width, desiredQuadrant);
		//need to add city to the node in that quadrant.
				
		Node x = this.nodes[desiredQuadrant - 1].add(c);
		if (x == null) {
			return null;
		}
		this.nodes[desiredQuadrant - 1] = x; 
		return this;
	}
	
	public Node remove(City c) {
		//need to get the quadrant the city is supposed to be removed from.
		int cityX = c.getX();
		int cityY = c.getY();
		
		int midX = x + (width / 2);
		int midY = y + (height / 2);
		int desiredQuadrant = getDesiredQuadrant(cityX, cityY, midX, midY);
		
		int desiredX = getX(x, y, height, width, desiredQuadrant);
		int desiredY = getY(x, y, height, width, desiredQuadrant);
		
		if (! (this.nodes[desiredQuadrant - 1] instanceof WhiteNode) ) {
			this.nodes[desiredQuadrant - 1] = this.nodes[desiredQuadrant - 1].remove(c);
		}
		
		int numBlackNodes = getNumBlackNodes();
		int numWhiteNodes = getNumWhiteNodes();
		
		if (numBlackNodes == 1 && numWhiteNodes == 3) {
			BlackNode onlyOne = getOnlyBlackNodeLeft();
			return onlyOne;
		} else {
			return this;
		} 
	}
	
	public int getNumBlackNodes() {
		//loop through list of nodes, just return num black ones left.
		int numBlack = 0;
		for (Node n: this.nodes) {
			if (n instanceof BlackNode) {
				numBlack ++;
			}
		}
		
		return numBlack;
	}
	
	public int getNumWhiteNodes() {
		//loop through list of nodes, just return num white ones left.
		int numWhite = 0;
		for (Node n: this.nodes) {
			if (n instanceof WhiteNode) {
				numWhite ++;
			}
		}
		
		return numWhite;
	}
	
	public BlackNode getOnlyBlackNodeLeft() {
		//assumed to only be called when we know that there is only one left.
		for (Node n: this.nodes) {
			if (n instanceof BlackNode) {
				return (BlackNode) n;
			}
		}
		
		//should never get to this point.
		return null;
	}
	
	public int getDesiredQuadrant(int x, int y, int midX, int midY) {
		
		if (x < midX && y < midY) {
			//third quadrant
			return 3;
		} else if (x < midX) {
			//first quadrant
			return 1;
		} else if (y < midY) {
			//fourth quadrant
			 return 4;
		} else {
			//second quadrant
			return 2;
		}
	}

	public int getX(int x, int y, int height, int width, int quadrant) {
		//quadrant 1 ==> x
		//quadrant 2 ==> x + width / 2;
		//quadrant 3 ==> x
		//quadrant 4 ==> x + width / 2;
		
		int desiredX = 0;
		
		switch (quadrant) {
		case 1: desiredX = x;
		break;
		case 2: desiredX = (x + width) / 2;
		break;
		case 3: desiredX = x;
		break; 
		case 4: desiredX = (x + width) / 2;
		break;
		}
		
		return desiredX;
	}
	
	public int getY(int x, int y, int height, int width, int quadrant) {
		//quadrant 1 ==> y + height / 2;
		//quadrant 2 ==> y + height / 2;
		//quadrant 3 ==> y;
		//quadrant 4 ==> y;
		
		int desiredY = 0;
		
		switch (quadrant) {
		case 1: desiredY = (y + height) / 2;
		break;
		case 2: desiredY = (y + height) / 2;
		break;
		case 3: desiredY = y;
		break; 
		case 4: desiredY = y;
		break;
		}
		
		return desiredY;
	}
	
	public Rectangle2D.Float getRect() {
		return new Rectangle2D.Float((float) x, (float) y, (float) height, (float) width);
	}
	
	public Node[] getChildren() {
		return this.nodes;
	}
	
	public Element printNode (Document doc) {
		//need to go through all children nodes and recursively print those.
		Element grayNode = doc.createElement("gray");
		grayNode.setAttribute("x", "" + (x + (height / 2)));
		grayNode.setAttribute("y", "" + (y + (width / 2)));
		
		
		Element child1 = nodes[0].printNode(doc);
		Element child2 = nodes[1].printNode(doc);
		Element child3 = nodes[2].printNode(doc);
		Element child4 = nodes[3].printNode(doc);
		
		grayNode.appendChild(child1);
		grayNode.appendChild(child2);
		grayNode.appendChild(child3);
		grayNode.appendChild(child4);

		return grayNode;
	}
	
	public CanvasPlus drawMap(CanvasPlus cp) {
		for (Node n: nodes) {
			cp.addRectangle(x, y, width, height, Color.BLACK, false);
			cp = n.drawMap(cp);
		}
		return cp;
	}
}
