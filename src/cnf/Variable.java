
package cnf;

/**
* Class implementing binary variables 
*/
public class Variable {

	// >>> Fields

	private String index;

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
	*/
	public String getIndex() {
		return index;
	}

	/**
	* Returns if the variable is true
	*/
	public boolean isTrue() {
		return assignment;
	}

	/**
	* Returns if the variable is false
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
		return "x_" + index;
	}

	public boolean equal(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!getClass().equals(o.getClass())) {
			return false;
		}
		return index == ((Variable) o).index;
	}
}
