package de.avgl.dmp.converter.functional;

/**
 * Functional interface for any unary function(x) that operates on any x of type
 *  <code>T</code>, presumably introducing side effects.
 *
 * The function returns void, which is denoted as
 *   <a href="http://en.wikipedia.org/wiki/Unit_type">Unit</a>Function.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 *
 * @param <T>  Type of input parameter
 */
public interface UnitFunction1<T> {

	/**
	 * Applies the function over <code>T obj</code>
	 * @param obj  input parameter
	 */
	public void apply(final T obj);
}
