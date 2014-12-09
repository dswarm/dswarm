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
package org.dswarm.controller.resources.job.test;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;

import org.dswarm.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.TransformationsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;
import org.dswarm.persistence.service.job.test.utils.TransformationServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class TransformationsResourceTest
		extends
		BasicResourceTest<TransformationsResourceTestUtils, TransformationServiceTestUtils, TransformationService, ProxyTransformation, Transformation, Long> {

	private FunctionsResourceTestUtils functionsResourceTestUtils;

	private ComponentsResourceTestUtils componentsResourceTestUtils;

	public TransformationsResourceTest() {

		super(Transformation.class, TransformationService.class, "transformations", "transformation.json", new TransformationsResourceTestUtils());
	}

	@Override protected void initObjects() {
		super.initObjects();

		pojoClassResourceTestUtils = new TransformationsResourceTestUtils();
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final Function function = functionsResourceTestUtils.createObject("function.json");

		// prepare component json for function manipulation
		String componentJSONString = DMPPersistenceUtil.getResourceAsString("component.json");
		final ObjectNode componentJSON = objectMapper.readValue(componentJSONString, ObjectNode.class);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);

		Assert.assertNotNull("the function JSON string shouldn't be null", finalFunctionJSONString);

		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		Assert.assertNotNull("the function JSON shouldn't be null", finalFunctionJSON);

		componentJSON.set("function", finalFunctionJSON);

		// re-init expect component
		componentJSONString = objectMapper.writeValueAsString(componentJSON);
		final Component expectedComponent = objectMapper.readValue(componentJSONString, Component.class);

		Component component = componentsResourceTestUtils.createObject(componentJSONString, expectedComponent);

		// prepare transformation json for component manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalComponentJSONString = objectMapper.writeValueAsString(component);

		Assert.assertNotNull("the component JSON string shouldn't be null", finalComponentJSONString);

		final ObjectNode finalComponentJSON = objectMapper.readValue(finalComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the component JSON shouldn't be null", finalComponentJSON);

		final ArrayNode componentsJSONArray = objectMapper.createArrayNode();

		componentsJSONArray.add(finalComponentJSON);

		objectJSON.set("components", componentsJSONArray);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	protected Transformation updateObject(final Transformation persistedTransformation) throws Exception {

		final String functionJSONString = DMPPersistenceUtil.getResourceAsString("function.json");
		final ObjectNode functionJSON = objectMapper.readValue(functionJSONString, ObjectNode.class);

		String componentJSONString = DMPPersistenceUtil.getResourceAsString("component.json");
		final ObjectNode componentJSON = objectMapper.readValue(componentJSONString, ObjectNode.class);

		// update function in component object
		componentJSON.set("function", functionJSON);
		componentJSONString = objectMapper.writeValueAsString(componentJSON);
		final Component expectedComponent = objectMapper.readValue(componentJSONString, Component.class);
		Component updateComponent = componentsResourceTestUtils.createObject(componentJSONString, expectedComponent);

		final Set<Component> components = new LinkedHashSet<Component>();
		components.add(updateComponent);
		persistedTransformation.setComponents(components);

		String updateTransformationJSONString = objectMapper.writeValueAsString(persistedTransformation);
		final ObjectNode updateTransformationJSON = objectMapper.readValue(updateTransformationJSONString, ObjectNode.class);

		// update name
		final String updateTransformationNameString = persistedTransformation.getName() + " update";
		updateTransformationJSON.put("name", updateTransformationNameString);

		// update description
		final String updateTransformationDescriptionString = persistedTransformation.getDescription() + " update";
		updateTransformationJSON.put("description", updateTransformationDescriptionString);

		updateTransformationJSONString = objectMapper.writeValueAsString(updateTransformationJSON);
		final Transformation expectedTransformation = objectMapper.readValue(updateTransformationJSONString, Transformation.class);
		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformationJSONString);

		final Transformation updateTransformation = pojoClassResourceTestUtils.updateObject(updateTransformationJSONString,
				expectedTransformation);

		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformation);
		Assert.assertEquals("transformation id shoud be equal", updateTransformation.getId(), persistedTransformation.getId());
		Assert.assertEquals("transformation name shoud be equal", updateTransformation.getName(), updateTransformationNameString);
		Assert.assertEquals("transformation description shoud be equal", updateTransformation.getDescription(),
				updateTransformationDescriptionString);

		final Set<Component> components1 = expectedTransformation.getComponents();
		final Set<Component> components2 = updateTransformation.getComponents();
		Assert.assertEquals("number of components should be equal", components1.size(), components2.size());
		Assert.assertTrue("components of the transformation should be equal", components1.equals(components2));

		Assert.assertTrue("transformation function should be equal", updateTransformation.getComponents().contains(updateComponent));

		return updateTransformation;
	}
}
