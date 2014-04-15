package de.avgl.dmp.persistence.model.job.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.PersistenceModule;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.types.Tuple;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class CustomTransformationDeserializerTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(CustomTransformationDeserializerTest.class);

	private ObjectMapper objectMapper;

	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper()
				.registerModule(new PersistenceModule.DmpDeserializerModule())
				.registerModule(new JaxbAnnotationModule());

		objectMapper.setInjectableValues(new GuiceInjectableValues(injector));
	}

	@Test
	public void deserializeTransformationTest() throws IOException {

		final CharSource source = Resources.asCharSource(Resources.getResource("deser-transformation.json"), Charset.forName("UTF-8"));

		final Transformation transformation = objectMapper.readValue(source.openStream(), Transformation.class);

		testTransformation(transformation);
	}

	@Test
	public void deserializeNestedTransformationTest() throws IOException {

		final CharSource source = Resources.asCharSource(Resources.getResource("deser-nested-transformation.json"), Charset.forName("UTF-8"));

		final Mapping mapping = objectMapper.readValue(source.openStream(), Mapping.class);

		final Component transformationComponent = mapping.getTransformation();

		assertThat(transformationComponent.getName(), equalTo("transformation"));
		assertThat(transformationComponent.getDescription(), equalTo("transformation"));

		assertThat(transformationComponent.getParameterMappings(), allOf(
				hasKey("dataset"),
				hasKey("variable_name"),
				hasKey("transformationOutputVariable")));

		assertThat(transformationComponent.getParameterMappings(), allOf(
				hasValue("http://data.slub-dresden.de/resources/1/schema#dataset"),
				hasValue("http://data.slub-dresden.de/resources/1/schema#variable_name"),
				hasValue("http://purl.org/dc/elements/1.1/title")));

		testTransformation((Transformation) transformationComponent.getFunction());
	}

	private static void testTransformation(final Transformation transformation) {

		assertThat(transformation.getId(), equalTo(42L));
		assertThat(transformation.getFunctionType(), equalTo(FunctionType.Transformation));
		assertThat(transformation.getName(), equalTo("transformation"));
		assertThat(transformation.getDescription(), equalTo("transformation"));
		assertThat(transformation.getFunctionDescription(), is(nullValue()));

		assertThat(transformation.getParameters(), hasSize(1));
		assertThat(transformation.getParameters(), hasItems("transformationInputString"));

		assertThat(transformation.getComponents(), hasSize(3));

		final Component c1 = checkComponent(transformation, 1, "compose", "Add pre- or postfix to a string.", Lists.newArrayList("prefix", "postfix"), Tuple.tuple("inputString", "variable_name"));
		final Component c2 = checkComponent(transformation, 2, "case", "Upper/lower-case transformation.", Lists.newArrayList("to", "language"));
		final Component c3 = checkComponent(transformation, 3, "count", "Returns the an increasing count for each received literal.", Collections.<String>emptyList(), Tuple.tuple("inputString", "dataset"));


		// *C1* --> C2
		assertThat(c1.getInputComponents(), is(nullValue()));
		assertThat(c1.getOutputComponents(), hasSize(1));
		assertThat(Iterators.getOnlyElement(c1.getOutputComponents().iterator()), is(sameInstance(c2)));

		// C1 --> *C2*
		assertThat(c2.getInputComponents(), hasSize(1));
		assertThat(c2.getOutputComponents(), is(nullValue()));
		assertThat(Iterators.getOnlyElement(c2.getInputComponents().iterator()), is(sameInstance(c1)));

		assertThat(c3.getInputComponents(), is(nullValue()));
		assertThat(c3.getOutputComponents(), is(nullValue()));
	}

	@SafeVarargs
	private static Component checkComponent(final Transformation transformation, final long id, final String name, final String description, final List<String> functionParameters, final Tuple<String, String>... parameterMappings) {

		final List<Component> configurations = filter(having(on(Configuration.class).getId(), equalTo(id)), transformation.getComponents());
		assertThat(configurations, hasSize(1));

		final Component component = configurations.get(0);

		assertThat(component.getId(), equalTo(id));
		assertThat(component.getName(), equalTo(name));
		assertThat(component.getDescription(), equalTo(description));

		final Map<String, String> componentParameterMappings = component.getParameterMappings();

		for (final Tuple<String, String> parameterMapping : parameterMappings) {
			final String key = parameterMapping.v1();
			final String value = parameterMapping.v2();
			assertThat(componentParameterMappings, hasKey(key));
			assertThat(componentParameterMappings, hasValue(value));
		}

		checkFunction(component, id, name, description, functionParameters.toArray(new String[functionParameters.size()]));

		return component;
	}

	private static Function checkFunction(final Component component, final long id, final String name, final String description, final String... parameters) {

		final Function function = component.getFunction();

		assertThat(function, is(notNullValue()));

		assertThat(function.getId(), equalTo(id));
		assertThat(function.getName(), equalTo(name));
		assertThat(function.getDescription(), equalTo(description));

		assertThat(function.getFunctionType(), equalTo(FunctionType.Function));

		assertThat(function.getParameters(), hasItems(parameters));

		return function;
	}
}
