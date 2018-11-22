
package APTA;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;


/**
 * Class for each node of the Augmented Prefix Tree Acceptor (a tree).
 * Each node can be accepting, rejecting or unknown.
 * Labels in outgoing edges are exclusive (determinism).
 */
public class TNode<LabelT> {

	/**
	 * Enum class for the response in the current state.
	 * accepting, rejecting, or unknown
	 */
	public enum Response {
		ACCEPT, REJECT, UNKNOWN
	}


	// >>> Fields

	/* Identifier */
	private final long id;

	/* Each state is labelled with a Response */
	private Response response = Response.UNKNOWN;

	/* Tree structure: labelled arcs */
	private Map<LabelT, TNode<LabelT>> arcs = new HashMap<LabelT, TNode<LabelT>>();

	/* Connection with the parent */
	private TNode<LabelT> parent;
	private LabelT parentLabel;


	// >>> Private functions

	/**
	 * Set the connection with the parent
	 * @param parent The parent node
	 * @param parentLabel The label of the arc
	 */
	private void setParent(TNode<LabelT> parent, LabelT parentLabel) {
		this.parent = parent;
		this.parentLabel = parentLabel;
	}


	/**
	 * Remove parent pointer
	 */
	private void unsetParent() {
		this.parent = null;
		this.parentLabel = null;
	}


	// >>> Public functions

	/**
	 * Constructor: just set an id
	 * @param id Any identifier
	 */
	public TNode(long id) {
		this.id = id;
	}


	/**
	 * Constructor: id and response
	 * @param id Any identifier
	 * @param response Whether the state should accept, reject or not specified
	 */
	public TNode(long id, Response response) {
		this(id);
		this.response = response;
	}


	/**
	 * Get the id
	 * @return The numeric id
	 */
	public long getId() {
		return this.id;
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
	 * Returns the parent node
	 * @return The parent field
	 */
	public TNode<LabelT> getParent() {
		return this.parent;
	}


	/**
	 * Returns the label of the parent arc
	 * @return The label
	 */
	public LabelT getParentLabel() {
		return this.parentLabel;
	}


	/**
	 * Returns the node connected to the current node through the arc
	 * labelled as label.
	 * @param label The label of the arc
	 * @return Connected node, or null if there was no such arc.
	 */
	public TNode<LabelT> followArc(LabelT label) {
		return arcs.get(label);
	}


	/**
	 * Returns the set of labels available from this node
	 * @return A set of labels
	 */
	public Set<LabelT> getLabels() {
		return arcs.keySet();
	}


	/**
	 * Remove an arc from the current node.
	 * Detach the child connected with the arc labelled as label.
	 * @param label The label of the arc to remove.
	 * @return The connected node or null if there was no edge.
	 */
	public TNode<LabelT> removeArc(LabelT label) {
		
		TNode<LabelT> oldNode = arcs.remove(label);

		if (oldNode != null) {    // Such arc existed
			oldNode.unsetParent();
		}
		return oldNode;
	}


	/**
	 * Add an arc from the current node.
	 * The connection should be and must not create loops. This is not checked.
	 * If a connection with the same label already exists, that connection is
	 * substituted with the new one. The parent field of node is also set.
	 * @param label Label of the new connection
	 * @param node The target node
	 */
	public void addArc(LabelT label, TNode<LabelT> node) {

		// Checks
		if (label == null || node == null) {
			throw new IllegalArgumentException("Null argument");
		}

		// Remove the old connection if it exists
		removeArc(label);

		// Save the new connection
		arcs.put(label, node);
		node.setParent(this, label);
	}


	@Override
	public String toString() {
		return id + ":" + response.toString();
	}


	/**
	 * Debugging
	 */
	public static void test() {
		TNode<Character> n = new TNode<Character>(0);
		TNode<Character> n1 = new TNode<Character>(1, TNode.Response.ACCEPT);
		TNode<Character> n2 = new TNode<Character>(2);
		TNode<Character> n3 = new TNode<Character>(3, TNode.Response.REJECT);

		n.addArc('a', n1);
		n.addArc('b', n2);
		n2.addArc('a', n3);

		System.out.println(n); // 0
		System.out.println(n.followArc('a')); // 1
		System.out.println(n.followArc('b')); // 2
		System.out.println(n.followArc('b').followArc('a')); // 3
		System.out.println(n.followArc('b').followArc('b')); // null
		System.out.println(n3.getParent()); // 2
		System.out.println(n3.getParentLabel()); // a

		n2.removeArc(null);
		n2.removeArc('b');
		System.out.println(n3.getParent()); // as before
		System.out.println(n3.getParentLabel()); // ^
		n2.removeArc('a');
		System.out.println(n3.getParent()); // null
		System.out.println(n3.getParentLabel()); // null
	}
}
