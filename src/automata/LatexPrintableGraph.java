
package automata;


/**
 * Interface for data structures which can be printed as a graph in a Latex Tikz
 * pucture.
 */
public interface LatexPrintableGraph {

	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * Acceptable node styles are default (no specification), "accept" and
	 * "reject".  For example, a simple model might return:
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
}
