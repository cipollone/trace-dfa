
package automata;

import java.util.*;

import util.*;


/**
 * This class serves as a common logic for all graph-based automata.
 * In this is a single conntected graph with labelled arcs and nodes. 
 * LabelT is the label type. NodeT is the type of each node.
 * Labels in outgoing edges are exclusive (for an automaton this means
 * determinism).
 * IDs in a graph are unique, but they are not guaranteed to be sequential.
 * The graph can be modified through the {@link AbstractNode} class and
 * the other protected methods, from inherited classes.
 * This class can be extended from a concrete Graph like:
 * <pre>{@code
 *	public class Graph<LabelT> extends AbstractGraph<LabelT, NodeClass<LabelT>>
 * }</pre>
 * @see AbstractNode
 */
public abstract class AbstractGraph<LabelT,
			NodeT extends AbstractNode<LabelT,NodeT>>
			implements Iterable<NodeT> {

	// >>> Fields
	
	/* The graph must contain at least one node: the first node */
	NodeT firstNode;

	/* Counters */
	private int nextFreeId;


	// >>> Protected functions

	/**
	 * Returns a new node object with the given id.
	 * Subclasses must override this method to use their own node class
	 * which extends AbstractNode.
	 * @param id the id
	 * @return A new Node
	 */
	abstract NodeT newNodeObj(int id);


	/**
	 * Returns a new node to be added to the graph (with a new id).
	 * @return A new node
	 */
	protected NodeT newNode() {
		return newNodeObj(nextFreeId++);
	}


	/**
	 * Creates a new child of parent, connected with arc label.
	 * @param parent The parent of the new node
	 * @param label The label of the new arc
	 * @return The created and attached node
	 */
	protected NodeT newChild(NodeT parent, LabelT label) {

		NodeT child = newNode();
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
	protected NodeT followPath(List<LabelT> path) {

		// Check
		if (path == null) {
			throw new IllegalArgumentException("Null sequence");
		}

		// Traverse the tree
		NodeT node = firstNode;
		for (LabelT label: path) {
			node = node.followArc(label);
			if (node == null) { return null; }
		}

		return node;
	}


	// >>> Public functions
	
	/**
	 * Constructor
	 */
	public AbstractGraph() {
		nextFreeId = 0;
		firstNode = newNode();
	}

	/**
	 * Returns the first node of the graph
	 * @return the first node of the graph
	 */
	public NodeT getFirstNode() {
		return firstNode;
	}

	/**
	 * Return a new iterator of nodes.
	 * Iterates the nodes with DepthPreIterator, a depth first visit.
	 * @return A new Iterator
	 */
	@Override
	public Iterator<NodeT> iterator() {
		return new DepthPreIterator();
	}


	// >>> Nested classes
	

	/**
	 * Iterator class for depth-visit of the graph.
	 * Nodes are returned in pre-order.
	 */
	private class DepthPreIterator implements Iterator<NodeT> {

		/* Stack of remaining nodes */
		private Stack<NodeT> nodesLeft = new Stack<>();

		/* Set of visited states */
		private Set<NodeT> visited = new HashSet<>();


		/* Constructor */
		public DepthPreIterator() {
			nodesLeft.add(firstNode);
		}


		@Override
		public boolean hasNext() {
			return !nodesLeft.empty();
		}


		@Override
		public NodeT next() {

			// Termination
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			// Get the next new node
			NodeT node = nodesLeft.pop();
			visited.add(node);

			// Expand
			Set<LabelT> labels = node.getLabels();
			for (LabelT l: labels) {
				NodeT n = node.followArc(l);
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
