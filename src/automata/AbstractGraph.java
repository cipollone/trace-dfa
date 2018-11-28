
package automata;

import java.util.*;

import util.*;


/**
 * This class serves as a common logics for all graph-based automata.
 * In this is a single conntected graph with labelled arcs and nodes. 
 * LabelT is the label type.  
 * Labels in outgoing edges are exclusive (for an automaton this means
 * determinism).
 * The graph can be modified through the {@link AbstractGraph.Node} class and
 * the other protected methods, from inherited classes.
 */
public abstract class AbstractGraph<LabelT>
		implements Iterable<AbstractGraph.Node<LabelT>> {

	// >>> Fields
	
	/* The graph must contain at least one node: the first node */
	protected final Node<LabelT> firstNode;

	/* Counters */
	private int nextFreeId;


	// >>> Protected functions

	/**
	 * Returns a new node to be added to the graph.
	 * Subclasses can override this method to use a different node class.
	 * @param id the id
	 * @return A new Node
	 */
	protected Node<LabelT> newNode(int id) {
		return new Node<LabelT>(id);
	}


	/**
	 * Creates a new child of parent, connected with arc label.
	 * @param parent The parent of the new node
	 * @param label The label of the new arc
	 * @return The created and attached node
	 * @see Node#addArc
	 */
	protected Node<LabelT> newChild(Node<LabelT> parent, LabelT label) {

		Node<LabelT> child = newNode(nextFreeId++);
		parent.addArc(label, child);
		return child;
	}


	/**
	 * Follow the path on the graph of the given list of labels.
	 * Traverses every arc from the first node in sequence and returns the
	 * last node reached.
	 * @param path The list of labels
	 * @return The last node or null if there was some impossible transition
	 */
	protected Node<LabelT> followPath(List<LabelT> path) {

		// Check
		if (path == null) {
			throw new IllegalArgumentException("Null sequence");
		}

		// Traverse the tree
		Node<LabelT> node = firstNode;
		for (LabelT label: path) {
			node = node.followArc(label);
			if (node == null) { return null; }
		}

		return node;
	}


	/**
	 * Return a new iterator.
	 * Iterates the nodes with DepthPreIterator, a depth first visit.
	 * @return A new Iterator
	 */
	@Override
	public Iterator<Node<LabelT>> iterator() {
		return new DepthPreIterator();
	}


	// >>> Public functions
	
	/**
	 * Constructor
	 */
	public AbstractGraph() {
		nextFreeId = 0;
		firstNode = newNode(nextFreeId++);
	}


	/**
	 * Debugging
	 */
	public static void test() {

		Node.test();

		System.out.println("Testing AbstractGraph");
		
		// Define a graph
		AbstractGraph<Character> graph = new AbstractGraph<Character>() {};
		Node<Character> n1 = graph.newChild(graph.firstNode, 'a');
		Node<Character> n2 = graph.newChild(n1, 'b');
		Node<Character> n3 = graph.newChild(n1, 'c');
		n3.addArc('a', n1);
		n3.addArc('g', n3);

		// Tests
		System.out.println(n1); // 1
		System.out.println(graph.firstNode.followArc('a').followArc('b')); // 2

		List<Character> path = Arrays.asList('a','b');
		System.out.println(path.toString() + ": " + graph.followPath(path)); // 2
		path = Arrays.asList('a','c','a','c','g','g');
		System.out.println(path.toString() + ": " + graph.followPath(path)); // 3
		path = Arrays.asList('f');
		System.out.println(path.toString() + ": " + graph.followPath(path)); // null

		for (Node<Character> n: graph) { // all nodes
			System.out.println(n);
		}

		System.out.println();
	}


	// >>> Nested classes

	/**
	 * Class for each node of the graph.
	 */
	protected static class Node<LabelT> {

		// >>> Fields
		
		/* Identifier */
		public final int id;

		/* Graph structure: output labelled arcs */
		private final Map<LabelT,Node<LabelT>> outArcs = new HashMap<>();
		

		// >>> Public functions
		
		/**
		 * Constructor: just set an id
		 * @param id Any numeric identifier
		 */
		public Node(int id) {
			this.id = id;
		}


		/**
		 * Returns whether label exists from the current node.
		 * @param label The label to check
		 */
		public boolean hasArc(LabelT label) {
			return outArcs.containsKey(label);
		}


		/**
		 * Returns the set of labels available from this node
		 * @return An unmodifiable set of labels
		 */
		public Set<LabelT> getLabels() {
			return Collections.unmodifiableSet(outArcs.keySet());
		}


		/**
		 * Returns the node connected through the arc label.
		 * @param label The label of the arc
		 * @return Connected node, or null if there was no such arc.
		 */
		public Node<LabelT> followArc(LabelT label) {
			return outArcs.get(label);
		}


		/**
		 * Remove an arc from the current node.
		 * @param label The label of the arc to remove.
		 * @return The connected node or null if there was no edge.
		 */
		public Node<LabelT> removeArc(LabelT label) {
			return outArcs.remove(label);
		}


		/**
		 * Add an arc from the current node.
		 * If a connection with the same label already exists, that connection is
		 * substituted with the new one.
		 * @param label Label of the new connection
		 * @param node The target node
		 * @return this node
		 */
		public Node<LabelT> addArc(LabelT label, Node<LabelT> node) {

			// Checks
			if (label == null || node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			outArcs.put(label, node);
			return this;
		}


		/**
		 * String representation
		 * @return The id
		 */
		@Override
		public String toString() {
			return Integer.toString(id);
		}


		/**
		 * Debugging
		 */
		public static void test() {

			System.out.println("Testing AbstractGraph.Node");

			// Testing
			Node<Character> n1 = new Node<Character>(1);
			Node<Character> n2 = new Node<Character>(2);
			Node<Character> n3 = new Node<Character>(3);

			// Adding connections
			n1.addArc('a',n2);
			n1.addArc('b',n3);
			n2.addArc('b',n3);

			// Testing basics
			System.out.println(n1.hasArc('a')); // true
			System.out.println(n1.getLabels()); // [a,b]
			System.out.println(n1.followArc('a').followArc('b')); // n3
			System.out.println(n1.followArc('g')); // null
			n2.removeArc('b');
			System.out.println(n1.followArc('a').followArc('b')); // null

			System.out.println();
		}
	}


	/**
	 * Iterator class for depth-visit of the graph.
	 * Nodes are returned in pre-order.
	 */
	private class DepthPreIterator
			implements Iterator<Node<LabelT>> {

		/* Stack of remaining nodes */
		private Stack<Node<LabelT>> nodesLeft = new Stack<>();

		/* Set of visited states */
		private Set<Node<LabelT>> visited = new HashSet<>();


		/* Constructor */
		public DepthPreIterator() {
			nodesLeft.add(firstNode);
		}


		@Override
		public boolean hasNext() {
			return !nodesLeft.empty();
		}


		@Override
		public Node<LabelT> next() {

			// Termination
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			// Get the next new node
			Node<LabelT> node = nodesLeft.pop();
			visited.add(node);

			// Expand
			Set<LabelT> labels = node.getLabels();
			for (LabelT l: labels) {
				Node<LabelT> n = node.followArc(l);
				if (!visited.contains(n)) {
					nodesLeft.push(n);
				}
			}

			// Remove visited nodes from the top of the stack
			while ((!nodesLeft.isEmpty()) && visited.contains(nodesLeft.peek())) {
				nodesLeft.pop();
			}

			// Return expanded
			return node;
		}
	}
}
