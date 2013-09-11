package de.avgl.dmp.converter.functional;

import com.google.common.collect.ImmutableList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for {@link NodeList}, offering a convenient API for iterating
 * over the represented {@link Node}s.
 * The class makes use of the functional interfaces defined in
 * {@link de.avgl.dmp.converter.functional}. It implements the iteration and
 * leaves the details of each iteration step at the caller.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
public class NodeListOps implements Iterable<Node> {

	/**
	 * Provide a {@link List} interface for the underlying collection.
	 */
	private final List<Node> underlying;

	/**
	 * Class Constructor. Wraps the given {@link NodeList} in an
	 * {@link ImmutableList}, so that the underlying collection can be easily
	 * shared and re-used.
	 *
	 * @param underlying  the original collection of Nodes
	 */
	public NodeListOps(final NodeList underlying) {
		final ImmutableList.Builder<Node> builder = ImmutableList.builder();

		for (int i = 0, l = underlying.getLength(); i < l; i++) {
			builder.add(underlying.item(i));
		}

		this.underlying = builder.build();
	}

	/**
	 * Internal Constructor.  This one is used by the iterator methods that
	 * will return a new <code>NodeListOps</code> instance, representing the
	 * result of the iteration. Since <code>NodeListOps</code> uses an
	 * {@link ImmutableList} as the underlying data structure, this is a
	 * zero-copy operation.
	 *
	 * @param underlying  the collection of nodes.
	 */
	private NodeListOps(final List<Node> underlying) {
		this.underlying = underlying;
	}

	/**
	 * Iterate over every {@link Node} and apply the function <code>fun</code>
	 * over it. <code>forEach</code> is the base operation for every other
	 * high level iterator method.
	 *
	 * @param fun  The function that is applied for its side-effect to every
	 *             element. The result of function fun is discarded.
	 */
	public void forEach(final UnitFunction1<Node> fun) {
		for (Node node : underlying) {
			fun.apply(node);
		}
	}

	/**
	 * Selects all elements of this collection which satisfy a predicate.
	 *
	 * @param pred  the predicate used to test elements.
	 * @return a new <code>NodeListOps</code> consisting of all elements of
	 * this collection that satisfy the given predicate <code>pred</code>.
	 */
	public NodeListOps filter(final Function1<Node, Boolean> pred) {
		final ImmutableList.Builder<Node> builder = ImmutableList.builder();

		forEach(new UnitFunction1<Node>() {
			@Override
			public void apply(Node in) {
				if (pred.apply(in)) {
					builder.add(in);
				}
			}
		});

		return new NodeListOps(builder.build());
	}

	/**
	 * Builds a new collection by applying a function to all elements of this
	 * collection.
	 *
	 * @param fun  the function to apply to each element.
	 * @param <T>  the element type of the returned collection.
	 * @return a new collection resulting from applying the given function f to
	 * each element of this collection and collecting the results.
	 */
	public <T> List<T> map(final Function1<Node, T> fun) {
		final ImmutableList.Builder<T> builder = ImmutableList.builder();

		forEach(new UnitFunction1<Node>() {
			@Override
			public void apply(Node in) {
				builder.add(fun.apply(in));
			}
		});

		return builder.build();
	}

	/**
	 * Implement the Iterator interface for convenience.
	 * @return a {@link Node} Iterator
	 */
	@Override
	public Iterator<Node> iterator() {
		return underlying.iterator();
	}

	/**
	 * Expose the underlying collection. Since this is an immutable collection,
	 * this can be shared freely without having to make defensive copies.
	 *
	 * @return the current collection
	 */
	public List<Node> get() {
		return underlying;
	}

	/**
	 * Shortcut for accessing a specific element of the underlying List.
	 * @param idx the element index, 0-based.
	 * @return the {@link Node} at the index position
	 */
	public Node get(int idx) {
		return underlying.get(idx);
	}

	/**
	 * Shortcut for filtering all {@link Node}s, that match a given tag name.
	 *
	 * @param tagName the tag name to filter for
	 * @return a new collection consisting only of {@link Node}s, that match
	 * the given tag name
	 */
	public List<Node> getElementsByTagName(final String tagName) {
		return filter(new Function1<Node, Boolean>() {
			@Override
			public Boolean apply(Node obj) {
				return obj.getNodeName().equals(tagName);
			}
		}).get();
	}

	/**
	 * Shortcut for returning the first {@link Node}, that matches a given
	 * tag name.
	 * @see NodeListOps#getElementsByTagName(String) getElementsByTagName
	 *
	 * @param tagName the tag name to filter for
	 * @return the first node that matches the given tag name
	 */
	public Node getElementByTagName(final String tagName) {
		return getElementsByTagName(tagName).get(0);
	}

	/**
	 * Shortcut for returning the {@link org.w3c.dom.Node#getTextContent()}
	 * if the first {@link Node} that matches a given tag name.
	 *
	 * @param nodeName the tag name to filter for
	 * @return the trimmed text content of the first node, that matches the
	 * given tag name
	 */
	public String getTextContent(final String nodeName) {
		return getElementByTagName(nodeName).getTextContent().trim();
	}
}
