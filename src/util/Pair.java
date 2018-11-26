
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
		return "(" + left.toString() + ", " + right.toString() + ")";
	}
}
