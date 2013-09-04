package de.avgl.dmp.converter.functional;

public interface Function2<T, U, V> {

	public V apply(final T obj1, final U obj2);
}
