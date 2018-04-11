package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.meeshquest.part2.*;
import cmsc420.meeshquest.part2.Error;

public class Treap<K, V> extends AbstractMap<K,V> implements SortedMap<K,V>{

	private Comparator<? super K> comp;
	private int size, modCount;
	private Node root;
	private Random rng;
	
	public Treap() {
		this.comp = null;
		this.size = 0;
		this.root = null;
		this.rng = new Random();
		this.modCount = 0;
	}
	
	public Treap(Comparator<? super K> comp) {
		this.comp = comp;
		this.size = 0;
		this.root = null;
		this.rng = new Random();
		this.modCount = 0;
	}

	@Override
	public Comparator<? super K> comparator() {
		return this.comp;
	}
	
	private int compare (K key1, K key2) {
		if (this.comp == null) {
			Comparable<? super K> k1 = (Comparable<? super K>)key1;
			return k1.compareTo(key2);
		} else {
			return this.comp.compare(key1, key2);
		}
	}
	
	@Override
	public V put(K key, V value) {
		//make sure key isnt already in treap.
		if (this.containsKey(key)) {
			//if it does, this shouldnt be called (call contains before this method).
			//throw new IllegalArgumentException();
			Node curr = this.getEntry(key);
			V prev = curr.value;
			curr.setValue(value);
			return prev;
		} else {
			Node node = new Node(key, value);
			//newNode is the node after insertion, that way we can re-heapify with parents.
			Node newNode = insertNodeIntoTreap(this.root, node);
			//System.out.println(newNode.getPriority());
			if (! (this.root.equals(node))) {
				//not the first element inserted.
				reheapify(newNode);
			}
			this.size ++;
			return newNode.getValue();
		}
	}

	public V get(Object key) {
		if (key == null) {
			throw new NullPointerException();
		} 
		
		K k = (K) key;
		Node p = root;
		while (p != null) {
			int cmp = compare(k, p.key);
			//int cmp = k.compareTo(p.key);
		    if (cmp < 0) {
		    	p = p.left;
		    } else if (cmp > 0) {
		    	p = p.right;
		    } else {
		    	return p.value;
		    }
		}
		return null;
	}
	
	private Node getEntry(Object key) {
		if (key == null) {
			throw new NullPointerException();
		} 
		
		K k = (K) key;
		Node p = root;
		while (p != null) {
			int cmp = compare (k, p.getKey());
		    if (cmp < 0) {
		    	p = p.left;
		    } else if (cmp > 0) {
		    	p = p.right;
		    } else {
		    	return p;
		    }
		}
		return null;
	}
	
	public void clear() {
		this.size = 0;
		this.root = null;
	}
	/*
	 * Will recursively add node into the treap
	 * Returns the node at the end so we can use it and re-heapify.
	 */
	private Node insertNodeIntoTreap(Node root, Node node) {
		if (this.root == null) {
			this.root = node;
			return node;
		}
		
		int leftOrRight;
		leftOrRight = compare(node.key, root.key);
		
		if (leftOrRight < 0) {
			//insert new node into left tree.
			if (root.left != null) {
				return insertNodeIntoTreap(root.left, node);
			} else {
				//new node will be left of root.
				node.parent = root;
				root.left = node;
				return node;
			}
		} else {
			//insert new node into right tree.
			if (root.right != null) {
				return insertNodeIntoTreap(root.right, node);
			} else {
				//new node will be right of root
				node.parent = root;
				root.right = node;
				return node;
			}
		}
	}
	
	/**
	 * Takes a newly inserted node and will re-heapify the treap if necessary.
	 */
	private void reheapify(Node node) {
		//start from where node is in the tree, iteratively go up the tree until heap is satisfied
		boolean heapSatisfied = false;
		while (node != null && node.parent != null && !heapSatisfied) {
				if (node.getPriority() > node.parent.getPriority()) {
					//heap property is invalid. Depending on whether node is left/right child,
					//do proper rotations.
					if (node.equals(node.parent.left)) {
						//node is left child. Perform right rotation.
						node = rightRotation(node.parent);
					} else if (node.equals(node.parent.right)) {
						//node is right child. Perform left rotation
						node = leftRotation(node.parent);
					} else {
						//insertion didn't go correctly, check error.
					}
				} else {
					//we have satisfied the heap property now.
					heapSatisfied = true;
				}
		}
	}
	
	/*
	 * Turn the right node's left subtree into this nodes right subtree.
	 * Make this node's parent pointer point to the right node.
	 * Put this node onto the original right node's left subtree.
	 */
	private Node leftRotation(Node node) {
		Node right = node.right;
		node.right = right.left;
		if (right.left != null) {
			right.left.parent = node;
		}
		right.parent = node.parent;
		if (node.parent == null) {
			//right node is the root now.
			this.root = right;
		} else if (node.equals(node.parent.left)) {
			//node is left sub child
			node.parent.left = right;
		} else {
			//node is right sub child.
			node.parent.right = right;
		}
		right.left = node;
		node.parent = right;
		return node.parent; //we need to return the parent because that is actually the node that was newly inserted.
	}
	
	/*
	 * Opposite of the left rotation.
	 * Turn the left node's right subtree into this nodes left subtree.
	 * Make this node's parent pointer point to the left node.
	 * Put this node onto the original left node's right subtree.
	 */
	private Node rightRotation(Node node) {
		Node left = node.left;
		node.left = left.right;
		if (left.right != null) {
			left.right.parent = node;
		}
		left.parent = node.parent;
		if (node.parent == null) {
			//left is root now.
			this.root = left;
		} else if (node.equals(node.parent.right)) {
			//node is right subtree
			node.parent.right = left;
		} else {
			//node is left subtree
			node.parent.left = left;
		}
		left.right = node;
		node.parent = left;
		return node;
	}
	
	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		// TODO Auto-generated method stub
		return new SubMap(fromKey, toKey);
	}

	/**
	 * Returns the first key of the treap in ascending order
	 * This simply goes all the way to the LEFT most node in the treap
	 * because of the notion of in-order traversals.
	 */
	@Override
	public K firstKey() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		Node first = this.getFirstEntry();
		if (first != null) {
			return first.key;
		} else {
			return null;
		}
	}
	
	/**
	 * Goes all the way left in the treap to grab the first element.
	 * This returns the node that firstKey() uses.
	 * @returns first element in treap, or null if treap is empty.
	 */
	private Node getFirstEntry() {
		if (this.size != 0) {
			Node temp = root;
			while (temp.left != null) {
				temp = temp.left;
			}
			return temp;
		}
		return null;
	}

	/**
	 * Returns the last key of the treap in ascending order
	 * This simply goes all the way to the RIGHT most node in the treap
	 * because of the notion of in-order traversals.
	 */
	@Override
	public K lastKey() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		Node first = this.getLastEntry();
		if (first != null) {
			return first.key;
		} else {
			return null;
		}
	}
	
	/**
	 * Goes all the way right in the treap to grab the last element. 
	 * This returns the node that lastKey() uses.
	 * @returns last element in treap, or null if treap is empty.
	 */
	private Node getLastEntry() {
		if (this.size != 0) {
			Node temp = root;
			while (temp != null) {
				temp = temp.right;
			}
			return temp;
		}
		return null;
	}

	public boolean containsKey(Object obj) {
		return (this.get(obj) != null);
	}
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	public int getRandomNumber() {
		int random = rng.nextInt();
		return random;
	}
	
	public Node testNode(K key, V val) {
		return new Node(key, val);
	}
	protected class EntrySet extends AbstractSet<Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {

		private Iterator <Map.Entry<K, V>> iterator;
		
		public EntrySet() {
			this.iterator = null;
		}
		
		public EntrySet(Iterator<Map.Entry<K, V>> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public Iterator <Map.Entry<K, V>> iterator() {
			if (this.iterator != null) {
				return this.iterator;
			} else {
				return new Iterator<Map.Entry<K,V>>() {
					private Node lastReturned = null;
					int expectedModCount = modCount;
					private Node next = getFirstEntry();
					
					@Override
					public boolean hasNext() {
						getFirstEntry();
						return (next != null);
					}
	
					@Override
					public Map.Entry<K, V> next() {
						if (modCount != expectedModCount) {
							throw new ConcurrentModificationException();
						}
						Node element = this.next;
						if (element == null) {
							throw new NoSuchElementException();
						} else {
							this.next = successor(element);
							lastReturned = element;
							return element;
						}
					}	
					@Override
					public void remove() {
//						if (lastReturned == null) {
//							throw new IllegalStateException();
//						}
//						if (modCount != expectedModCount) {
//							throw new ConcurrentModificationException();
//						}	
//						// deleted entries are replaced by their successors
//						if (lastReturned.left != null && lastReturned.right != null){
//							next = lastReturned; 
//						}
//						deleteEntry(lastReturned);
//						expectedModCount = modCount;
//						lastReturned = null;
						//throw new UnsupportedOperationException();
						
					}
				};
			}
		}

		@Override
		public int size() {
			//if this is a submap, we need to iterate over this ourselves.
			if (this.iterator != null) {
				Iterator<Map.Entry<K,V>> it = this.iterator;
				int count = 0;
				while (it.hasNext()) {
					it.next(); //we dont even need to save the next val, we just want it to move on.
					count ++;
				}
				return count;
			}
			return Treap.this.size;
		}
		
		//From the part 2 spec
		public boolean remove(Object o) {
//			@SuppressWarnings("unchecked")
//			Node me = (Node)o;
//			boolean b = Treap.this.containsKey(me.getKey());
//			Treap.this.remove(me.getKey());
//			return b;
			throw new UnsupportedOperationException();
		}
		
		//From the part 2 spec
		public boolean contains(Object o) {
			@SuppressWarnings("unchecked")
			Node me = (Node)o;
			return Treap.this.containsKey(me.getKey())
					&& (me.getValue() == null ? 
							Treap.this.get(me.getKey()) == null :
							me.getValue().equals(Treap.this.get(me.getKey())));
			
		}

		
	}
	
	
	protected class Node extends AbstractMap.SimpleEntry<K, V> {

		private K key;
		private V value;
		private Node left;
		private Node right;
		private Node parent;
		private int priorityNum;
		
		public Node(K key, V val) {
			super(key, val);
			this.key = key;
			this.value = val;
			this.parent = null;
			this.left = null;
			this.right = null;
			this.priorityNum = getRandomNumber();
			//TODO get priorty number and do work with that.
		}
		
		//num used for heap property.
		public int getPriority() {
			return this.priorityNum;
		}
		
		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			V oldVal = this.value;
			this.value = value;
			return oldVal;
		}
		
//		/*
//		 * Hopefully this equals method works.
//		 */
//		public boolean equals(Object o) {
//			if (o == this) {
//				return true;
//			}
//			
//			if (!(o instanceof Treap.Node)) {
//				return false;
//			}
//			
//			Node n = (Node)o;
//			if (n.getKey().equals(this.key) && n.getValue().equals(this.value) && n.priorityNum == this.priorityNum) {
//				return true;
//			} else {
//				return false;
//			}
//		}
	}
	
	
	protected class SubMap extends AbstractMap<K,V> implements SortedMap<K,V>{

		private K fromKey, toKey;
		public SubMap(K fromKey, K toKey) {
			this.fromKey = fromKey;
			this.toKey = toKey;
			if (Treap.this.comparator() != null) {
				if (Treap.this.comparator().compare(fromKey, toKey) > 0) {
					throw new IllegalArgumentException();
				}
			} else {
				Comparable<? super K> kC = (Comparable<? super K>) fromKey;
				if (kC.compareTo(toKey) > 0) {
					throw new IllegalArgumentException();
				}
			}
			
			//NOTE: If fromKey == toKey, the sub map will be empty.
		}

		public void clear() {
			Treap.this.clear();
		}
		/**
		 * Ok so from the AbstractMap docs, containsKey() ITERATES through entrySet and tries to find val.
		 * size() calls entrySet().size() so we need to edit entrySet.size()
		 * toString() calls entrySet().iterator and returns string
		 * @author jchavis06
		 *
		 */
		public class SubMapIterator implements Iterator<Map.Entry<K, V>> {

			Node next;
			
			public SubMapIterator() {
				//need to get first element to be between bounds.
				Node first = Treap.this.getFirstEntry();
				if (first == null) {
					this.next = null;
					return;
				}
				
				while (compare(first.getKey(),fromKey) < 0) {
					first = successor(first);
					if (first == null) {
						break;
					}
				}
					
				if (first == null) {
					this.next = null;
				} else {
					//now we need to make sure first key isnt greater than toKey.
					if (compare (first.getKey(), toKey) >= 0) {
						this.next = null;
					} else {
						this.next = first;
					}
				}
			}
			
			@Override
			public boolean hasNext() {
				if (next == null) {
					return false;
				}
				K key = next.getKey();
				//if element is greater than toKey, we are out of bounds for submap.
				if (compare(key, toKey) >= 0) {
					return false; 
				} else {
					return true;
				}
			}

			@Override
			public Entry<K, V> next() {
				Node element = this.next;
				if (element != null) {
					this.next = successor(element);
					return element;
				} else {
					throw new NoSuchElementException();
				}
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
		
		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			return new EntrySet(new SubMapIterator());
		}
		
		/**
		 * We just need to override the get method so that it checks for out of bounds checks.
		 */
		@Override 
		public V get(Object o) {
			@SuppressWarnings("unchecked")
			K key = (K)o;
			if (compare(key, fromKey) >= 0 && compare(key,toKey) < 0) {
				return Treap.this.get(key);
			} else {
				//key given is out of bounds.
				return null;
			}
		}
		
		/**
		 * Need to override the put method so that it checks to make sure it is putting a value that
		 * is within bounds in the submap. 
		 * NOTE: This will also add the pair into the real treap.
		 */
		@Override
		public V put(K key, V val) {
			if (compare(key, fromKey) >= 0 && compare(key, toKey) < 0) {
				V oldVal = Treap.this.get(key);
				Treap.this.put(key, val);
				return oldVal;
			} else {
				//out of bounds key. Should we throw an exception???
				throw new IllegalArgumentException();
			}
		}
	
		public K firstKey() {
			Iterator<Map.Entry<K, V>> it = new SubMapIterator();
			if (it.hasNext()) {
				Node node = (Node) it.next();
				return node.getKey();
			} else {
				return null;
			}
			
		}
		
		public K lastKey() {
			Iterator<Map.Entry<K, V>> it = new SubMapIterator();
			Node last = null;
			while (it.hasNext()) {
				last = (Node) it.next();	
			}
			return last.getKey();
		}
		
		private int compare(K key1, K key2) {
			if (Treap.this.comparator() == null) {
				Comparable<? super K> k1 = (Comparable<? super K>)key1;
				return k1.compareTo(key2);
			} 
			
			return Treap.this.comparator().compare(key1, key2);
		}
		

		@Override
		public Comparator<? super K> comparator() {
			// TODO Auto-generated method stub
			return Treap.this.comp;
		}

		@Override
		public SortedMap<K, V> headMap(K toKey) {
			// TODO Auto-generated method stub
			return Treap.this.headMap(toKey);
		}

		@Override
		public SortedMap<K, V> subMap(K fromKey, K toKey) {
			// TODO Auto-generated method stub
			return Treap.this.subMap(fromKey, toKey);
		}

		@Override
		public SortedMap<K, V> tailMap(K fromKey) {
			// TODO Auto-generated method stub
			return Treap.this.tailMap(fromKey);
		}
	
		public boolean remove() {
			throw new UnsupportedOperationException();
		}
	
	}
	
	
	
	
	/**
	 * This method returns this current node's successor, according to an
	 * in-order traversal. Will return null if node is last in the treap.
	 */
	public Node successor(Node n) {
		Node curr = n;
		Node rightTree = curr.right;
		//successor will either be in right tree or in one of the ancestor nodes.
		if (rightTree != null) {
			//return node all the way to the LEFT in the RIGHT subtree.
			curr = rightTree;
			while (curr.left != null) {
				curr = curr.left;
			}
			return curr;
		} else {
			//in one of the ancestors. Specifically the ancestor in which this current node is 
			//in the left subtree of. 
			Node parent = curr.parent;
			while (parent != null && curr == parent.right) {
				//move up in treap until parent is null or this node is in the left subtree of parent.
				curr = parent;
				parent = parent.parent;
			}
			
			//if parent ends up being null that just means we were at the end of the treap.
			return parent;
		}
	}
	
	/*
	 * Prints the treap in an in-order traversal.
	 */
	public XmlOutput printTreap(Document doc, Integer id) {
		Element treap = doc.createElement("treap");
		if (this.size == 0) {
			Error e = new Error(doc, "emptyTree", "printTreap");
			return e;
		}
		treap.setAttribute("cardinality", "" + this.size);
		Element t = printNode(this.root, doc);
		treap.appendChild(t);
		
		Success s = new Success(doc, "printTreap", id);
		s.addOutputElement(treap);
		return s;
	}
	
	public Element printNode(Node node, Document doc) {
		if (node != null) {
			Element n = doc.createElement("node");
			n.setAttribute("key", "" + node.getKey());
			n.setAttribute("priority", "" + node.getPriority());
			n.setAttribute("value", "" + node.getValue());
			
			Element l = printNode(node.left, doc);
			//System.out.println("Key: " + node.getKey() + ", Value: " + node.getValue() + ", Priority: " + node.getPriority());
			Element r = printNode(node.right, doc);
			n.appendChild(l);
			n.appendChild(r);
			return n;
		} else {
			Element e = doc.createElement("emptyChild");
			return e;
		}
	}
	
	
	
	
	
	/*
	 * WE DONT NEED TO IMPLEMENT ANY OF THE METHODS BELOW.
	 */
	@Override
	public SortedMap<K, V> headMap(K toKey) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	

}

