package de.avgl.dmp.converter.util;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeListOpsTest {
	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private Node node1;
	private Node node2;
	private Node node3;
	private Iterable<Node> nodes;
	private Node child1;
	private Node child2;
	private Node child3;

	@Before
	public void setUp() throws Exception {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		Node root = doc.createElement("root");

		node1 = doc.createElement("tag1");
		node1.setTextContent("text1");
		node2 = doc.createElement("tag2");
		node2.setTextContent("text2");
		node3 = doc.createElement("tag3");
		node3.setTextContent("text3");

		child1 = doc.createElement("child1");
		child2 = doc.createElement("child2");
		child3 = doc.createElement("child3");

		node1.appendChild(child1);
		node1.appendChild(child2);
		node1.appendChild(child3);

		root.appendChild(node1);
		root.appendChild(node2);
		root.appendChild(node3);

		NodeList nodeList = root.getChildNodes();
		nodes = new NodeListIterable(nodeList);
	}

	@Test
	public void testGetElementByTagName() throws Exception {
		final Optional<Node> tag1 = NodeListOps.getElementByTagName(nodes, "tag1");
		assertTrue(tag1.isPresent());
		assertEquals(node1, tag1.get());

		final Optional<Node> tag2 = NodeListOps.getElementByTagName(nodes, "tag2");
		assertTrue(tag2.isPresent());
		assertEquals(node2, tag2.get());

		final Optional<Node> tag3 = NodeListOps.getElementByTagName(nodes, "tag3");
		assertTrue(tag3.isPresent());
		assertEquals(node3, tag3.get());
	}

	@Test
	public void testGetChildrenFor() throws Exception {
		final Optional<Iterable<Node>> tag1Children = NodeListOps.getChildrenFor(nodes, "tag1");
		assertTrue(tag1Children.isPresent());
		assertEquals(Lists.newArrayList(child1, child2, child3), Lists.newArrayList(tag1Children.get()));
	}
}
