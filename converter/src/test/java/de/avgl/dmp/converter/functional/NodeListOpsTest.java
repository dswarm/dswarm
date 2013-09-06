package de.avgl.dmp.converter.functional;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NodeListOpsTest {
	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private NodeListOps ops;
	private Node node1;
	private Node node2;
	private Node node3;

	@Before
	public void setUp() throws Exception {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		Element root = doc.createElement("root");

		node1 = doc.createElement("tag1");
		node1.setTextContent("text1");
		node2 = doc.createElement("tag2");
		node2.setTextContent("text2");
		node3 = doc.createElement("tag3");
		node3.setTextContent("text3");

		root.appendChild(node1);
		root.appendChild(node2);
		root.appendChild(node3);

		NodeList nodeList = root.getChildNodes();
		ops = new NodeListOps(nodeList);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testForEach() throws Exception {
		final String[] expected = new String[] {"tag1", "tag2", "tag3"};
		final List<String> actuals = new ArrayList<>();

		ops.forEach(new UnitFunction1<Node>() {
			@Override
			public void apply(Node obj) {
				actuals.add(obj.getNodeName());
			}
		});

		final String[] actual = actuals.toArray(new String[actuals.size()]);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testFilter() throws Exception {
		final Node[] expected = new Node[]{node1};

		List<Node> actuals = ops.filter(new Function1<Node, Boolean>() {
			@Override
			public Boolean apply(Node obj) {
				return obj.getNodeName().equals("tag1");
			}
		}).get();

		final Node[] actual = actuals.toArray(new Node[actuals.size()]);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testMap() throws Exception {
		final String[] expected = new String[]{"text1", "text2", "text3"};

		List<String> actuals = ops.map(new Function1<Node, String>() {
			@Override
			public String apply(Node obj) {
				return obj.getTextContent();
			}
		});

		final String[] actual = actuals.toArray(new String[actuals.size()]);
		assertArrayEquals(expected, actual);

	}

	@Test
	public void testGet() throws Exception {
		List<Node> expected = Lists.newArrayList(node1, node2, node3);
		assertEquals(expected, ops.get());
	}

	@Test
	public void testGetWithIndex() throws Exception {
		assertEquals(node1, ops.get(0));
		assertEquals(node2, ops.get(1));
		assertEquals(node3, ops.get(2));
	}

	@Test
	public void testGetElementsByTagName() throws Exception {
		List<Node> expected = Lists.newArrayList(node1);
		assertEquals(expected, ops.getElementsByTagName("tag1"));
	}

	@Test
	public void testGetElementByTagName() throws Exception {
		assertEquals(node1, ops.getElementByTagName("tag1"));
		assertEquals(node2, ops.getElementByTagName("tag2"));
		assertEquals(node3, ops.getElementByTagName("tag3"));
	}

	@Test
	public void testGetTextContent() throws Exception {
		assertEquals("text1", ops.getTextContent("tag1"));
		assertEquals("text2", ops.getTextContent("tag2"));
		assertEquals("text3", ops.getTextContent("tag3"));
	}
}
