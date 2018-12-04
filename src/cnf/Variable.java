
package cnf;

import java.util.*;

/**
* Class implementing binary variables 
*/
public class Variable {

	// >>> Fields

	// Any unique descriptor: usually something like "x_3,5"
	private final String index;

	private boolean assignment = false;

	// >>> Public functions

	/**
	* Constructor: just sets an index
	* @param i The string that sets the index
	*/
	public Variable(String i) {
		this.index = i;
	}

	/**
	* Returns the index of the variable
	* @return the index of the variable
	*/
	public String getIndex() {
		return index;
	}

	/**
	* Returns if the variable is true
	* @return if the variable is true
	*/
	public boolean isTrue() {
		return assignment;
	}

	/**
	* Returns if the variable is false
	* @return if the variable is false
	*/
	public boolean isFalse() {
		return !assignment;
	}

	/**
	* Assign a boolean value to the variable
	* @param value The value to assign
	*/
	public void assign(boolean value) {
		assignment = value;
	}

	/**
	* Override of the toString method
	*/
	@Override
	public String toString() {
		return index;
	}


	/**
	 * Utility function: creates new variables with the given names.
	 * @param names The name of each variable
	 * @return The list of the new variables
	 */
	public static List<Variable> newVars(String... names) {
		ArrayList<Variable> vars = new ArrayList<>();
		for (String name: names) {
			vars.add(new Variable(name));
		}
		return vars;
	}
}
