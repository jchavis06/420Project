package cmsc420.meeshquest.part2;

import java.util.TreeMap;
import java.util.TreeSet;

public class AdjacencyList {

	private TreeMap<String, TreeSet<String>> neighbors;
	
	public AdjacencyList() {
		this.neighbors = new TreeMap<String, TreeSet<String>>();
	}
	
	public void mapNeighbors(String cityA, String cityB) {
		if (neighbors.containsKey(cityA)) {
			TreeSet<String> list = neighbors.get(cityA);
			list.add(cityB);
		} else {
			TreeSet<String> list = new TreeSet<String>();
			list.add(cityB);
			neighbors.put(cityA, list);
		}
		
		if (neighbors.containsKey(cityB)) {
			TreeSet<String> list = neighbors.get(cityB);
			list.add(cityA);
		} else {
			TreeSet<String> list = new TreeSet<String>();
			list.add(cityA);
			neighbors.put(cityB, list);
		}
	}
	
	/*
	 * This method checks to see if A is mapped to B or if B is mapped to A
	 * (Note that we should only have to check if its one or the other since when we map roads we add them to both lists.
	 */
	public boolean containsRoad(String cityA, String cityB) {
		//we already know at this point that both cityA and cityB are in this adjacencyList object
		TreeSet<String> n1 = neighbors.get(cityA);
		TreeSet<String> n2 = neighbors.get(cityB);
		
		return ((n1 != null && n2 != null) && (n1.contains(cityB) || (n2.contains(cityA))));
	}
	
	public TreeSet<String> getNeighbors(String city) {
		if (neighbors.containsKey(city)) {
			TreeSet<String> list = neighbors.get(city);
			return list;
		} else {
			return null;
		}
	}
}
