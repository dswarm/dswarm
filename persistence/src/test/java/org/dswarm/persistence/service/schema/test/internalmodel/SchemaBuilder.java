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
package org.dswarm.persistence.service.schema.test.internalmodel;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public abstract class SchemaBuilder extends GuicedTest {

	//private static final Logger	LOG				= LoggerFactory.getLogger(SchemaBuilder.class);
	protected String			prefixPaths		= "";
	
	protected final ClaszServiceTestUtils	claszServiceTestUtils;
	protected final SchemaServiceTestUtils schemaServiceTestUtils;

	public SchemaBuilder() {
		super();
		
		claszServiceTestUtils = new ClaszServiceTestUtils();
		schemaServiceTestUtils = new SchemaServiceTestUtils();
	}

	public abstract Schema buildSchema() throws Exception;

	public String getPrefixPaths() {
		return prefixPaths;
	}

}
