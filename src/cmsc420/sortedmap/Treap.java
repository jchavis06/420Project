package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

public class Treap<K, V> extends AbstractMap<K,V> implements SortedMap<K,V>{

	private Comparator<? super K> comp;
	private int size;
	private Node<K,V> root;
	private Random rng;
	
	public Treap() {
		this.comp = null;
		this.size = 0;
		this.root = null;
		this.rng = new Random();
	}
	
	public Treap(Comparator<? super K> comp) {
		this.comp = comp;
		this.size = 0;
		this.root = null;
		this.rng = new Random();
	}

	@Override
	public Comparator<? super K> comparator() {
		return this.comp;
	}
	
	/**
	 * For this implementation I am going to return the value we are putting for key,
	 * rather than returning the old value. This is because it is assumed that there will be 
	 * no duplicates, so it would be useless information to return null every time. Now I will
	 * return null if operation didnt work, which is more useful.
	 */
	@Override
	public V put(K key, V value) {
		//make sure key isnt already in treap.
		if (this.containsKey(key)) {
			//if it does, this shouldnt be called (call contains before this method).
			//throw new IllegalArgumentException();
			return null;
		} else {
			Node<K,V> node = new Node<K,V>(key, value);
			//newNode is the node after insertion, that way we can re-heapify with parents.
			Node<K,V> newNode = insertNodeIntoTreap(this.root, node);
			if (! (this.root.equals(node))) {
				//not the first element inserted.
				reheapify(newNode);
			}
			return newNode.getValue();
		}
	}

	/*
	 * Will recursively add node into the treap
	 * Returns the node at the end so we can use it and re-heapify.
	 */
	private Node<K,V> insertNodeIntoTreap(Node<K,V> root, Node<K,V> node) {
		if (this.root == null) {
			this.root = node;
			return node;
		}
		int leftOrRight = this.comp.compare(node.getKey(), root.getKey());
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
	private void reheapify(Node<K,V> node) {
		//start from where node is in the tree, iteratively go up the tree until heap is satisfied
		boolean heapSatisfied = false;
		while (node != null && !heapSatisfied) {
			if (node.parent != null) {
				if (node.getPriority() > node.parent.getPriority()) {
					//heap property is invalid. Depending on whether node is left/right child,
					//do proper rotations.
					if (node.equals(node.parent.left)) {
						//node is left child. Perform right rotation.
						node = rightRotation(node);
					} else if (node.equals(node.parent.right)) {
						//node is right child. Perform left rotation
						node = leftRotation(node);
					} else {
						//insertion didn't go correctly, check error.
					}
				} else {
					//we have satisfied the heap property now.
					heapSatisfied = true;
				}
			}
		}
	}
	
	/*
	 * Turn the right node's left subtree into this nodes right subtree.
	 * Make this node's parent pointer point to the right node.
	 * Put this node onto the original right node's left subtree.
	 */
	private Node<K,V> leftRotation(Node<K,V> node) {
		Node<K,V> right = node.right;
		node.right = right.left;
		if (right.left != null) {
			right.left.parent = node;
		}
		right.parent = node.parent;
		if (node.parent == null) {
			//right node is the root now.
			this.root = node.right;
		} else if (node.equals(node.parent.left)) {
			//node is left sub child
			node.parent.left = right;
		} else {
			//node is right sub child.
			node.parent.right = right;
		}
		right.left = node;
		node.parent = right;
		return node;
	}
	
	/*
	 * Opposite of the left rotation.
	 * Turn the left node's right subtree into this nodes left subtree.
	 * Make this node's parent pointer point to the left node.
	 * Put this node onto the original left node's right subtree.
	 */
	private Node<K,V> rightRotation(Node<K,V> node) {
		Node<K,V> left = node.left;
		node.left = left.right;
		if (left.right != null) {
			left.right.parent = node;
		}
		left.parent = node.parent;
		if (node.parent == null) {
			//left is root now.
			this.root = node.left;
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
		Node<K,V> first = this.getFirstEntry();
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
	private Node<K,V> getFirstEntry() {
		if (this.size != 0) {
			Node<K,V> temp = root;
			while (temp != null) {
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
		Node<K,V> first = this.getLastEntry();
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
	private Node<K,V> getLastEntry() {
		if (this.size != 0) {
			Node<K,V> temp = root;
			while (temp != null) {
				temp = temp.right;
			}
			return temp;
		}
		return null;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	public int getRandomNumber() {
		int random = rng.nextInt();
		return random;
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
	
					private Node<K,V> next = Treap.this.getFirstEntry();
					
					@Override
					public boolean hasNext() {
						return (next != null);
					}
	
					@Override
					public Node<K,V> next() {
						Node<K,V> element = this.next;
						if (element == null) {
							throw new NoSuchElementException();
						} else {
							this.next = successor(element);
							return element;
						}
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
			@SuppressWarnings("unchecked")
			Node<K,V> me = (Node<K,V>)o;
			boolean b = Treap.this.containsKey(me.getKey());
			Treap.this.remove(me.getKey());
			return b;
		}
		
		//From the part 2 spec
		public boolean contains(Object o) {
			@SuppressWarnings("unchecked")
			Node<K,V> me = (Node<K,V>)o;
			return Treap.this.containsKey(me.getKey())
					&& (me.getValue() == null ? 
							Treap.this.get(me.getKey()) == null :
							me.getValue().equals(Treap.this.get(me.getKey())));
			
		}

		
	}
	
	
	protected class Node<K,V> implements Map.Entry<K, V> {

		private K key;
		private V value;
		private Node<K,V> left;
		private Node<K,V> right;
		private Node<K,V> parent;
		private int priorityNum;
		
		public Node(K key, V val) {
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
		
		/*
		 * Hopefully this equals method works.
		 */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			
			if (!(o instanceof Node)) {
				return false;
			}
			
			Node<K,V> n = (Node<K,V>)o;
			if (n.getKey().equals(this.key) && n.getValue().equals(this.value) && n.priorityNum == this.priorityNum) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	
	protected class SubMap extends Treap<K,V>{

		private K fromKey, toKey;
		public SubMap(K fromKey, K toKey) {
			this.fromKey = fromKey;
			this.toKey = toKey;
			if (Treap.this.comparator().compare(fromKey, toKey) > 0) {
				throw new IllegalArgumentException();
			}
			//NOTE: If fromKey == toKey, the sub map will be empty.
		}

		/**
		 * Ok so from the AbstractMap docs, containsKey() ITERATES through entrySet and tries to find val.
		 * size() calls entrySet().size() so we need to edit entrySet.size()
		 * toString() calls entrySet().iterator and returns string
		 * @author jchavis06
		 *
		 */
		public class SubMapIterator implements Iterator<Map.Entry<K, V>> {

			Node<K,V> next;
			
			public SubMapIterator() {
				//need to get first element to be between bounds.
				Node<K,V> first = Treap.this.getFirstEntry();
				while (Treap.this.comparator().compare(first.getKey(), fromKey) < 0) {
					first = successor(first);
				}
				
				//now we need to make sure first key isnt greater than toKey.
				if (Treap.this.comparator().compare(first.getKey(), toKey) >= 0) {
					this.next = null;
				} else {
					this.next = first;
				}
			}
			@Override
			public boolean hasNext() {
				if (next == null) {
					return false;
				}
				K key = next.getKey();
				//if element is greater than toKey, we are out of bounds for submap.
				if (Treap.this.comparator().compare(key, toKey) >= 0) {
					return false;
				} else {
					//we have a key, we already know its in bounds. 
					return true;
				}
			}

			@Override
			public Entry<K, V> next() {
				Node<K,V> element = this.next;
				if (element != null) {
					this.next = successor(element);
					return element;
				} else {
					throw new NoSuchElementException();
				}
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
			if (Treap.this.comparator().compare(key, fromKey) >= 0 && 
					Treap.this.comparator().compare(key, toKey) < 0) {
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
			if (Treap.this.comparator().compare(key, fromKey) >= 0 && 
					Treap.this.comparator().compare(key, toKey) < 0) {
				V oldVal = Treap.this.get(key);
				Treap.this.put(key, val);
				return oldVal;
			} else {
				//out of bounds key. Should we throw an exception???
				return null;
			}
		}
	
		public K firstKey() {
			Iterator<Map.Entry<K, V>> it = new SubMapIterator();
			if (it.hasNext()) {
				Node<K,V> node = (Node<K,V>) it.next();
				return node.getKey();
			} else {
				return null;
			}
			
		}
		
		public K lastKey() {
			Iterator<Map.Entry<K, V>> it = new SubMapIterator();
			Node<K,V> last = null;
			while (it.hasNext()) {
				last = (Node<K,V>) it.next();	
			}
			return last.getKey();
		}
	
	
	}
	
	
	
	
	/**
	 * This method returns this current node's successor, according to an
	 * in-order traversal. Will return null if node is last in the treap.
	 */
	public Node<K,V> successor(Node<K,V> n) {
		Node<K,V> curr = n;
		Node<K,V> rightTree = curr.right;
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
			Node<K,V> parent = curr.parent;
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
	public void printTreap() {
		inOrder(this.root);
	}
	
	public void inOrder(Node<K,V> node) {
		if (node != null) {
			inOrder(node.left);
			System.out.println("Key: " + node.getKey() + ", Value: " + node.getValue() + ", Priority: " + node.getPriority());
			inOrder(node.right);
		} else {
			System.out.println("Null");
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

