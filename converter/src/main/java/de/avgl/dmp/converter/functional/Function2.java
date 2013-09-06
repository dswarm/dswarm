package de.avgl.dmp.converter.functional;

/**
 * Functional interface for any binary function(x, y) that, for any x of type
 * <code>T</code> and any y of type <code>U</code>, produces an z of Type
 * <code>V</code>.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 *
 * @param <T> Type of first input parameter
 * @param <U> Type of second input parameter
 * @param <V> Type of return value
 */
public interface Function2<T, U, V> {

	/**
	 * Applies the function over <code>T obj1</code> and <code>U obj2</code>
	 * @param obj1  first input parameter
	 * @param obj2  second input parameter
	 * @return      return value
	 */
	public V apply(final T obj1, final U obj2);
}
