package cmsc420.sortedmap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import cmsc420.sortedmap.*;
import cmsc420.sortedmap.Treap.Node;
public class testTreap {

	public static void main(String args[]) {
//		Treap<Integer,Integer> treap = new Treap<Integer,Integer>(new IntegerComp());
//		treap.put(6, 10);
//		treap.printTreap();
//		treap.put(8, 20);
//		treap.printTreap();
//		treap.put(10, 30);
//		treap.put(3, 3);
//		treap.printTreap();
		
		Treap<String, String> m = new Treap<String, String>(new StringComp());
		m.put("Auto","Fail");
		Set<Map.Entry<String, String>> s = m.entrySet();
		Iterator<Map.Entry<String,String>> i = s.iterator();
		System.out.println("Values:");
		while (i.hasNext()) {
			Map.Entry<String, String> x = i.next();
			System.out.println(x.getKey() + ", " + x.getValue());
		}
		m.put("F","---");
		if (s.contains(m.testNode("Auto","Fail"))) {
			System.out.println("You rock!");
		}
	} 
	
	public static class IntegerComp implements Comparator<Integer> {

		@Override
		public int compare(Integer arg0, Integer arg1) {
			return arg0 - arg1;
		}
		
	}
	
	public static class StringComp implements Comparator<String> {

		@Override
		public int compare(String arg0, String arg1) {
			return arg0.compareTo(arg1);
		}
		
	}
}
