
package util;


/**
 * Tiny constant container for two elements
 */
public class Pair<L,R> {

	public final L left;
	public final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}

	@Override
	public boolean equals(Object other) {
		// Basic cases
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof Pair<?,?>)) return false;

		//
		Pair<?,?> otherP = (Pair<?,?>)other;
		return (left.equals(otherP.left) && right.equals(otherP.right));
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (left == null ? 0 : left.hashCode());
		hash = 31 * hash + (right == null ? 0 : right.hashCode());
		return hash;
	}
}
