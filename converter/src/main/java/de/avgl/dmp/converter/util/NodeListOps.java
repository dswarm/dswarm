package de.avgl.dmp.converter.util;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;

/**
 * Static class (object), that implements convenience methods for working
 * with {@link NodeList}s.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
public class NodeListOps {
	private NodeListOps() {}

	private final static Function<Node, Iterable<Node>> childrens = new Function<Node, Iterable<Node>>() {
		@Override
		public Iterable<Node> apply(@Nullable org.w3c.dom.Node input) {
			assert input != null;
			return new NodeListIterable(input.getChildNodes());
		}
	};

	/**
	 * Shortcut for returning the {@link org.w3c.dom.Node#getTextContent()}
	 * if the first {@link Node} that matches a given tag name.
	 *
	 * @param nodeName the tag name to filter for
	 * @return the trimmed text content of the first node, that matches the
	 * given tag name
	 */
//	public String getTextContent(final String nodeName) {
//		return getElementByTagName(nodeName).getTextContent().trim();
//	}

	/**
	 * Return the first {@link Node}, that matches a given tag name.
	 *
	 * @param nodes an Iterable of nodes that will be searched through.
	 * @param tag   the tag name to filter for
	 * @return the first node that matches the given tag name, wrapped in an
	 * {@link Optional}
	 */
	public static Optional<Node> getElementByTagName(Iterable<Node> nodes, String tag) {
		for (Node node : nodes) {
			if (Objects.equal(node.getNodeName(), tag)) {
				return Optional.of(node);
			}
		}
		return Optional.absent();
	}

	/**
	 * Return all children of the first element, that matches the given tag name.
	 *
	 * @param nodes  an Iterable of nodes that will be searched through.
	 * @param tag    the tag name to filter for
	 * @return all children of the first node that matches the given tag name,
	 * wrapped in an {@link Optional}
	 */
	public static Optional<Iterable<Node>> getChildrenFor(Iterable<Node> nodes, String tag) {
		return getElementByTagName(nodes, tag).transform(childrens);
	}
}
