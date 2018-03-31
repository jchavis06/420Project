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
}
