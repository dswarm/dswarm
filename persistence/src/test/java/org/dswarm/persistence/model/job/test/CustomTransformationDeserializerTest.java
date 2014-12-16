/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.model.job.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.PersistenceModule;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.types.Tuple;

public class CustomTransformationDeserializerTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(CustomTransformationDeserializerTest.class);

	private ObjectMapper objectMapper;

	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper().registerModule(new PersistenceModule.DmpDeserializerModule()).registerModule(new JaxbAnnotationModule());

		objectMapper.setInjectableValues(new GuiceInjectableValues(GuicedTest.injector));
	}

	@Test
	public void deserializeTransformationTest() throws IOException {

		final CharSource source = Resources.asCharSource(Resources.getResource("deser-transformation.json"), Charset.forName("UTF-8"));

		final Transformation transformation = objectMapper.readValue(source.openStream(), Transformation.class);

		CustomTransformationDeserializerTest.testTransformation(transformation);
	}

	@Test
	public void deserializeNestedTransformationTest() throws IOException {

		final CharSource source = Resources.asCharSource(Resources.getResource("deser-nested-transformation.json"), Charset.forName("UTF-8"));

		final Mapping mapping = objectMapper.readValue(source.openStream(), Mapping.class);

		final Component transformationComponent = mapping.getTransformation();

		MatcherAssert.assertThat(transformationComponent.getName(), Matchers.equalTo("transformation"));
		MatcherAssert.assertThat(transformationComponent.getDescription(), Matchers.equalTo("transformation"));

		MatcherAssert.assertThat(transformationComponent.getParameterMappings(),
				Matchers.allOf(Matchers.hasKey("dataset"), Matchers.hasKey("variable_name"), Matchers.hasKey("transformationOutputVariable")));

		MatcherAssert.assertThat(
				transformationComponent.getParameterMappings(),
				Matchers.allOf(Matchers.hasValue("http://data.slub-dresden.de/resources/1/schema#dataset"),
						Matchers.hasValue("http://data.slub-dresden.de/resources/1/schema#variable_name"),
						Matchers.hasValue("http://purl.org/dc/elements/1.1/title")));

		CustomTransformationDeserializerTest.testTransformation((Transformation) transformationComponent.getFunction());
	}

	private static void testTransformation(final Transformation transformation) {

		MatcherAssert.assertThat(transformation.getUuid(), Matchers.equalTo("42L"));
		MatcherAssert.assertThat(transformation.getFunctionType(), Matchers.equalTo(FunctionType.Transformation));
		MatcherAssert.assertThat(transformation.getName(), Matchers.equalTo("transformation"));
		MatcherAssert.assertThat(transformation.getDescription(), Matchers.equalTo("transformation"));
		MatcherAssert.assertThat(transformation.getFunctionDescription(), Matchers.is(Matchers.nullValue()));

		MatcherAssert.assertThat(transformation.getParameters(), Matchers.hasSize(1));
		MatcherAssert.assertThat(transformation.getParameters(), Matchers.hasItems("transformationInputString"));

		MatcherAssert.assertThat(transformation.getComponents(), Matchers.hasSize(3));

		final Component c1 = CustomTransformationDeserializerTest.checkComponent(transformation, "1", "compose", "Add pre- or postfix to a string.",
				Lists.newArrayList("prefix", "postfix"), Tuple.tuple("inputString", "variable_name"));
		final Component c2 = CustomTransformationDeserializerTest.checkComponent(transformation, "2", "case", "Upper/lower-case transformation.",
				Lists.newArrayList("to", "language"));
		final Component c3 = CustomTransformationDeserializerTest
				.checkComponent(transformation, "3", "count", "Returns the an increasing count for each received literal.",
						Collections.<String>emptyList(), Tuple.tuple("inputString", "dataset"));

		// *C1* --> C2
		MatcherAssert.assertThat(c1.getInputComponents(), Matchers.is(Matchers.nullValue()));
		MatcherAssert.assertThat(c1.getOutputComponents(), Matchers.hasSize(1));
		MatcherAssert.assertThat(Iterators.getOnlyElement(c1.getOutputComponents().iterator()), Matchers.is(Matchers.sameInstance(c2)));

		// C1 --> *C2*
		MatcherAssert.assertThat(c2.getInputComponents(), Matchers.hasSize(1));
		MatcherAssert.assertThat(c2.getOutputComponents(), Matchers.is(Matchers.nullValue()));
		MatcherAssert.assertThat(Iterators.getOnlyElement(c2.getInputComponents().iterator()), Matchers.is(Matchers.sameInstance(c1)));

		MatcherAssert.assertThat(c3.getInputComponents(), Matchers.is(Matchers.nullValue()));
		MatcherAssert.assertThat(c3.getOutputComponents(), Matchers.is(Matchers.nullValue()));
	}

	@SafeVarargs
	private static Component checkComponent(final Transformation transformation, final String uuid, final String name, final String description,
			final List<String> functionParameters, final Tuple<String, String>... parameterMappings) {

		final List<Component> configurations = Lambda.filter(Lambda.having(Lambda.on(Configuration.class).getUuid(), Matchers.equalTo(uuid)),
				transformation.getComponents());
		MatcherAssert.assertThat(configurations, Matchers.hasSize(1));

		final Component component = configurations.get(0);

		MatcherAssert.assertThat(component.getUuid(), Matchers.equalTo(uuid));
		MatcherAssert.assertThat(component.getName(), Matchers.equalTo(name));
		MatcherAssert.assertThat(component.getDescription(), Matchers.equalTo(description));

		final Map<String, String> componentParameterMappings = component.getParameterMappings();

		for (final Tuple<String, String> parameterMapping : parameterMappings) {
			final String key = parameterMapping.v1();
			final String value = parameterMapping.v2();
			MatcherAssert.assertThat(componentParameterMappings, Matchers.hasKey(key));
			MatcherAssert.assertThat(componentParameterMappings, Matchers.hasValue(value));
		}

		CustomTransformationDeserializerTest.checkFunction(component, uuid, name, description,
				functionParameters.toArray(new String[functionParameters.size()]));

		return component;
	}

	private static Function checkFunction(final Component component, final String uuid, final String name, final String description,
			final String... parameters) {

		final Function function = component.getFunction();

		MatcherAssert.assertThat(function, Matchers.is(Matchers.notNullValue()));

		MatcherAssert.assertThat(function.getUuid(), Matchers.equalTo(uuid));
		MatcherAssert.assertThat(function.getName(), Matchers.equalTo(name));
		MatcherAssert.assertThat(function.getDescription(), Matchers.equalTo(description));

		MatcherAssert.assertThat(function.getFunctionType(), Matchers.equalTo(FunctionType.Function));

		MatcherAssert.assertThat(function.getParameters(), Matchers.hasItems(parameters));

		return function;
	}
}
