
package automata;


/**
 * Interface for data structures which can be printed as a graph in a Latex Tikz
 * pucture.
 */
public interface LatexPrintableGraph {

	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * Nodes can also have two custom styles "accept" and "reject". Arcs can also
	 * use styles named "self loop" and "backward".
	 * For example, a simple model might return:
	 * <pre>{@code
	 *		0 [accept] ->
	 *		1 [reject,>"c"] -> {
	 *		6 [accept,>"a"] ,
	 *		2 [accept,>"i"] ->
	 *		3 [>"a"] -> {
	 *		5 [reject,>"r"] ,
	 *		4 [accept,>"o"] }}
	 * }
	 * </pre>
	 * @return The string for this graph
	 */
	public String getLatexGraphRepresentation();


	/**
	 * This function is used for other Latex code. 
	 * The string returned will be pasted inside the document body.
	 * @return Latex environment(s) or null if you don't want to use this feature
	 */
	public String extraLatexEnv();


	/**
	 * This function is used to pass options to standalone document class.
	 * A list of options like: [varwidth], or null.
	 * @return Latex options for standalone document class, or null for no options.
	 */
	public String standaloneClassLatexOptions();
}
