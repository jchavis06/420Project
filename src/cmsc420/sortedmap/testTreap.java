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

		
		SortedMap<Integer, Integer> treap = new Treap<Integer, Integer>();
		SortedMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();
		treap.put(5, 4);
		treap.put(10,11);
		SortedMap<Integer, Integer> subTreap = treap.subMap(6, 11);
		treap.put(7, 4);
		treap.put(123, 343);
		subTreap.put(8, 22);
		
		
		treeMap.put(5, 4);
		treeMap.put(10,11);
		SortedMap<Integer, Integer> subTreeMap = treeMap.subMap(6, 11);
		treeMap.put(7, 4);
		treeMap.put(123, 343);
		subTreeMap.put(8, 22);
		System.out.println("Size treap: " + treap.size());
		System.out.println("Size treeMap: " + treeMap.size());
		System.out.println("Equals treap?: " + treap.equals(treeMap));
		System.out.println("Equals subs?: " + subTreeMap.equals(subTreap));
		System.out.println("Size sub treap: " + subTreap.size());
		System.out.println("Size sub treemap: " + subTreeMap.size());
		Set<Entry<Integer, Integer>> treapSubSet = subTreap.entrySet();
		Set<Entry<Integer, Integer>> treeMapSubSet = subTreeMap.entrySet();
		System.out.println("Size entry treap: " + treapSubSet.size());
		System.out.println("Size entry treeMap: " + subTreeMap.size());
		
		
		System.out.println("Equals entry set: " + treapSubSet.equals(treeMapSubSet));
		System.out.println("HashCode entry set: " + (treapSubSet.hashCode() == treeMapSubSet.hashCode()));
		
		System.out.println("HashCode treap : " + (treap.hashCode() == treeMap.hashCode()));
		System.out.println("Hash code submap: " + (subTreap.hashCode() == subTreeMap.hashCode()));
		
		//Iterator<Entry> subTreapIt = treapSubSet.iterator();
		//Iterator<Entry> subTreeMapIt = treeMapSubSet.iterator();
		
		System.out.println(subTreap);
		System.out.println("-----");
		System.out.println(subTreeMap);
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
