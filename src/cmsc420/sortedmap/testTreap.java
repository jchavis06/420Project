package cmsc420.sortedmap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import cmsc420.sortedmap.*;
import cmsc420.sortedmap.Treap.EntrySet;
import cmsc420.sortedmap.Treap.Node;
public class testTreap {

	public static void main(String args[]) {

		
//		Treap<Integer, Integer> treap = new Treap<Integer, Integer>();
//		TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();
//		treap.put(1, 2);
//		treap.put(3,4);
//		treap.put(5,6);
//		treap.put(7, 8);
//		treap.put(9, 10);
//		
//		treeMap.put(1, 2);
//		treeMap.put(3,4);
//		treeMap.put(5,6);
//		treeMap.put(7, 8);
//		treeMap.put(9, 10);
//		
//		Set<Map.Entry<Integer,Integer>> treapSet = treap.entrySet();
//		Set<Map.Entry<Integer,Integer>> treeMapSet = treeMap.entrySet();
//		Iterator<Map.Entry<Integer,Integer>> treapIt = treapSet.iterator();
//		Iterator<Map.Entry<Integer,Integer>> treeMapIt = treeMapSet.iterator();
//		
//		System.out.println(treapSet);
//		System.out.println(treeMapSet);
//		while (treapIt.hasNext() && treeMapIt.hasNext()) {
//			Entry val1 = treapIt.next();
//			Entry val2 = treeMapIt.next();
//			System.out.println(val1.equals(val2));
//			System.out.println(val1.hashCode() == val2.hashCode());
//			System.out.println("-----");
//		}
		
		SortedMap<Integer,Integer> map = new Treap<Integer, Integer>();
		SortedMap<Integer,Integer> sub = map.subMap(2, 5);
		map.put(2, 3);
		map.put(54, 43);
		map.put(4, 3);
		map.put(3, 4);
		System.out.println(sub.size());
		System.out.println(sub.size());
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
