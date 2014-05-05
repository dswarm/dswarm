package de.avgl.dmp.converter.flow.test.csv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.types.Triple;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;

public class CSVSourceResourceTriplesFlowTest {

	private void testFlow(final CSVSourceResourceTriplesFlow flow) throws DMPConverterException {
		final ResourceOpener opener = new ResourceOpener();

		@SuppressWarnings("unchecked")
		final Matcher<String> predicateMatcher = CoreMatchers.anyOf(CoreMatchers.equalTo("id"), CoreMatchers.equalTo("name"),
				CoreMatchers.equalTo("description"), CoreMatchers.equalTo("isbn"), CoreMatchers.equalTo("year"));

		final List<String> subjects = new ArrayList<String>();
		for (int i = 1; i <= 19; i++) {
			for (int j = 0; j < 5; j++) {
				subjects.add(String.valueOf(i));
			}
		}
		final Iterator<String> subjectsIterator = subjects.iterator();

		final ImmutableList<Triple> triples = flow.apply("test_csv.csv", opener);
		for (final Triple triple : triples) {
			MatcherAssert.assertThat(triple.getSubject(), CoreMatchers.equalTo(subjectsIterator.next()));
			MatcherAssert.assertThat(triple.getPredicate(), predicateMatcher);
			MatcherAssert.assertThat(triple.getObjectType(), CoreMatchers.equalTo(Triple.ObjectType.STRING));
		}

		Assert.assertFalse(subjectsIterator.hasNext());
	}

	@Test
	public void testEndToEnd() throws Exception {

		final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromConfigurationParameters("UTF-8", '\\', '"', ';', "\n",
				CSVSourceResourceTriplesFlow.class);

		testFlow(flow);
	}

	@Test
	public void testFromConfiguration() throws Exception {
		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));

		final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceTriplesFlow.class);

		testFlow(flow);
	}

	@Test(expected = DMPConverterException.class)
	public void testNullConfiguration() throws Exception {
		final Configuration configuration = null;
		@SuppressWarnings("UnusedDeclaration")
		final CSVSourceResourceTriplesFlow flow = new CSVSourceResourceTriplesFlow(configuration);
	}

	@Test(expected = DMPConverterException.class)
	public void testNullConfigurationParameter() throws Exception {
		final Configuration configuration = new Configuration();
		@SuppressWarnings("UnusedDeclaration")
		final CSVSourceResourceTriplesFlow flow = new CSVSourceResourceTriplesFlow(configuration);
	}

	@Test
	public void testDefaultValues() throws Exception {
		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));

		final CSVSourceResourceTriplesFlow flow = new CSVSourceResourceTriplesFlow(configuration);

		testFlow(flow);
	}

}
