package cmsc420.sortedmap;
import cmsc420.sortedmap.*;
public class testTreap {

	public static void main(String args[]) {
		Treap<Integer,Integer> treap = new Treap<Integer,Integer>();
		treap.put(6, 10);
		treap.put(8, 20);
		treap.printTreap();
	} 
}
