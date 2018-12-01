
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
	* Add a positive variable to the clause
	* @param variable The variable to add
	*/
	public void addPositiveVariable(Variable variable) {
		positiveVariables.add(variable);
	}

	/**
	* Add a negated variable to the clause
	* @param variable The variable to add negated
	*/
	public void addNegatedVariable(Variable variable) {
		negatedVariables.add(variable);
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
	* Returns the number of all variables in the clause
	* @return the number of all variables in the clause
	*/
	public int getVarNum() {
		return getAllVariables().size();
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
