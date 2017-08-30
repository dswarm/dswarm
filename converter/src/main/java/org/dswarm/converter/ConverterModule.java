/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.dswarm.converter.flow.*;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.converter.schema.SolrSchemaParser;
import org.dswarm.converter.schema.XMLSchemaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Guice configuration of the converter module. Interface/classes that are registered here can be utilised for injection.
 *
 * @author phorn
 */
public class ConverterModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(ConverterModule.class);

	@Override
	protected void configure() {

		bind(XMLSchemaParser.class);
		bind(SolrSchemaParser.class);

		install(new FactoryModuleBuilder().build(CSVResourceFlowFactory.class));
		install(new FactoryModuleBuilder().build(TimerBasedFactory.class));
		install(new FactoryModuleBuilder().build(JSONTransformationFlowFactory.class));
		install(new FactoryModuleBuilder().build(GDMModelTransformationFlowFactory.class));
		install(new FactoryModuleBuilder().build(XmlResourceFlowFactory.class));
		install(new FactoryModuleBuilder().build(JsonResourceFlowFactory.class));
	}
}
