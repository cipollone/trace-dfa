
package APTA;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;


/**
 * Class for each node of the Augmented Prefix Tree Acceptor (a tree).
 * Each node can be accepting, rejecting or unknown.
 */
public class Node<LabelT> {

	/**
	 * Enum class for the response in the current state.
	 * accepting, rejecting, or unknown
	 */
	public enum Response {
		ACCEPT, REJECT, UNKNOWN
	}


	// >>> Fields

	/** Identifier */
	private final long id;		// Just an identifier

	/** Each state is labelled with a Response */
	private Response response = Response.UNKNOWN;

	/** Tree structure: labelled arcs */
	private Map<LabelT, Node<LabelT>> arcs = new HashMap<LabelT, Node<LabelT>>();


	// >>> Functions

	/**
	 * Constructor: just set an id
	 * @param id Any identifier
	 */
	public Node(long id) {
		this.id = id;
	}


	/**
	 * Constructor: id and response
	 * @param id Any identifier
	 * @param response Whether the state should accept, reject or not specified
	 */
	public Node(long id, Response response) {
		this(id);
		this.response = response;
	}


	/**
	 * Set the response variable
	 * @param response Reject, accept or unknown
	 */
	public void setResponse(Response response) {
		this.response = response;
	}


	/**
	 * Returns the response
	 * @return The response at the current node
	 */
	public Response getResponse() {
		return this.response;
	}


	/**
	 * Add an arc from the current node.
	 * The connection should be and must not create loops. This is not checked.
	 * If a connection with the same label already exists, that connection is
	 * substituted with the new one.
	 * @param label Label of the new connection
	 * @param node The target node
	 */
	public void addArc(LabelT label, Node<LabelT> node) {

		// Checks
		if (label == null || node == null) {
			throw new IllegalArgumentException("Null argument");
		}

		arcs.put(label, node);
	}


	/**
	 * Remove an arc from the current node.
	 * Detach the child connected with the arc labelled as label.
	 * @param label The label of the arc to remove.
	 * @return The connected node or null if there was no edge.
	 */
	public Node<LabelT> removeArc(LabelT label) {
		
		// Checks
		if (label == null) {
			throw new IllegalArgumentException("Null argument");
		}

		return arcs.remove(label);
	}


	/**
	 * Returns the node connected to the current node through the arc
	 * labelled as label.
	 * @param label The label of the arc
	 * @return Connected node, or null if there was no such arc.
	 */
	public Node<LabelT> followArc(LabelT label) {
		return arcs.get(label);
	}


	/**
	 * Returns the set of labels available from this node
	 * @return A set of labels
	 */
	public Set<LabelT> getLabels() {
		return arcs.keySet();
	}


	@Override
	public String toString() {
		return id + ":" + response.toString();
	}


	// NOTE: just testing here
	public static void test() {
		Node<Character> n = new Node<Character>(0);
		Node<Character> n2 = new Node<Character>(2);
		Node<Character> n3 = new Node<Character>(3);
		Node<Character> n1 = new Node<Character>(1);

		n.addArc('a', n1);
		n.addArc('b', n2);
		n2.addArc('a', n3);

		System.out.println(n);
		System.out.println(n.followArc('a'));
		System.out.println(n.followArc('b'));
		System.out.println(n.followArc('b').followArc('a'));
		System.out.println(n.followArc('b').followArc('b'));
		System.out.println(n.getLabels());
	}
}
