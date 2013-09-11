package de.avgl.dmp.converter.functional;

/**
 * Functional interface for any unary function(x) that, for any x of type
 *  <code>T</code>, produces an y of Type <code>U</code>.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 *
 * @param <T>  Type of input parameter
 * @param <U>  Type of return value
 */
public interface Function1<T, U> {

	/**
	 * Applies the function over <code>T obj</code>
	 * @param obj  input parameter
	 * @return     return value
	 */
	public U apply(final T obj);
}
