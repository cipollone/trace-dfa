
package cnf;

import java.util.*;


/**
* Class implementing propositional clauses
*/
public class Clause {

	// >>> Fields

	private Set<Variable> positiveVariables = new HashSet<>();

	private Set<Variable> negatedVariables = new HashSet<>();

	// >>> Public functions

	/**
	* Add positive variables to the clause
	* @param variables The variables to add
	*/
	public void addPositiveVariable(Variable... variables) {
		for (Variable v: variables) {
			positiveVariables.add(v);
		}
	}

	/**
	* Add a negated variables to the clause
	* @param variables The variables to add negated
	*/
	public void addNegatedVariable(Variable... variables) {
		for (Variable v: variables) {
			negatedVariables.add(v);
		}
	}

	/**
	* Returns if the clause is satisfied or not
	* @return if the clause is satisfied or not
	*/
	public boolean isSatisfied() {
		for (Variable variable : positiveVariables) {
			if (variable.isTrue()) {
				return true;
			}
		}
		for (Variable variable : negatedVariables) {
			if (variable.isFalse()) {
				return true;
			}
		}
		return false;
	}

	/**
	* Returns all the variables in the clause
	* @return all the variables in the clause
	*/
	public Set<Variable> getAllVariables() {
		Set<Variable> variables = new HashSet<>();
		variables.addAll(positiveVariables);
		variables.addAll(negatedVariables);
		return variables;
	}

	/**
	 * Returns the iterable set of all positive variables in this clause
	 * @return The set of posive variables
	 */
	public Set<Variable> positiveVars() {
		return Collections.unmodifiableSet(positiveVariables);
	}

	/**
	 * Returns the iterable set of all negated variables in this clause
	 * @return The set of negated variables
	 */
	public Set<Variable> negatedVars() {
		return Collections.unmodifiableSet(negatedVariables);
	}

	/**
	* Returns the number of all variables in the clause
	* @return the number of all variables in the clause
	*/
	public int getVarNum() {
		return positiveVariables.size() + negatedVariables.size();
	}

	/**
	* Override of the toString method
	*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		String separator = "";
		for (Variable variable : positiveVariables) {
			sb.append(separator);
			separator = " V ";
			sb.append(variable.toString());
		}
		for (Variable variable : negatedVariables) {
			sb.append(separator);
			separator = " V ";
			sb.append("-" + variable.toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
