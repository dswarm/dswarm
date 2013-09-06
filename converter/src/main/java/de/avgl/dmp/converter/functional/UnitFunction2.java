package de.avgl.dmp.converter.functional;

/**
 * Functional interface for any binary function(x, y) that operates on any x of
 * type <code>T</code> and any y of type <code>U</code>, presumably introducing
 * side effects.
 *
 * The function returns void, which is denoted as
 *   <a href="http://en.wikipedia.org/wiki/Unit_type">Unit</a>Function.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 *
 * @param <T> Type of first input parameter
 * @param <U> Type of second input parameter
 */
public interface UnitFunction2<T, U> {

	/**
	 * Applies the function over <code>T obj1</code> and <code>U obj2</code>
	 * @param obj1  first input parameter
	 * @param obj2  second input parameter
	 */
	public void apply(final T obj1, final U obj2);
}
