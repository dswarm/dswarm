/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.schema.test.utils;

import java.util.Map;

import org.dswarm.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;

public class AttributesResourceTestUtils extends AdvancedDMPResourceTestUtils<AttributeServiceTestUtils, AttributeService, ProxyAttribute, Attribute> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class, AttributeServiceTestUtils.class);
	}

	public void prepareAttribute(final String attributeJSONFileName, final Map<Long, Attribute> attributes) throws Exception {

		final Attribute actualAttribute = createObject(attributeJSONFileName);

		attributes.put(actualAttribute.getId(), actualAttribute);
	}
}
