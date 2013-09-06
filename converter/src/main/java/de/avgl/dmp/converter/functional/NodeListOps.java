package de.avgl.dmp.converter.functional;

import com.google.common.collect.ImmutableList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class NodeListOps {

	private final List<Node> underlying;

	public NodeListOps(final NodeList underlying) {
		final ImmutableList.Builder<Node> builder = ImmutableList.builder();

		for (int i = 0, l = underlying.getLength(); i < l; i++) {
			builder.add(underlying.item(i));
		}

		this.underlying = builder.build();
	}

	private NodeListOps(final List<Node> underlying) {
		this.underlying = underlying;
	}

	public NodeListOps forEach(final UnitFunction1<Node> fun) {
		for (Node node : underlying) {
			fun.apply(node);
		}
		return this;
	}

	public NodeListOps filter(final Function1<Node, Boolean> fun) {
		final ImmutableList.Builder<Node> builder = ImmutableList.builder();

		forEach(new UnitFunction1<Node>() {
			@Override
			public void apply(Node in) {
				if (fun.apply(in)) {
					builder.add(in);
				}
			}
		});

		return new NodeListOps(builder.build());
	}

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

	public List<Node> get() {
		return underlying;
	}

	public Node get(int idx) {
		return underlying.get(idx);
	}

	public List<Node> getElementsByTagName(final String tagName) {
		return filter(new Function1<Node, Boolean>() {
			@Override
			public Boolean apply(Node in) {
				return in.getNodeName().equals(tagName);
			}
		}).get();
	}

	public Node getElementByTagName(final String tagName) {
		return getElementsByTagName(tagName).get(0);
	}

	public String getTextContent(final String nodeName) {
		return getElementByTagName(nodeName).getTextContent().trim();
	}
}
