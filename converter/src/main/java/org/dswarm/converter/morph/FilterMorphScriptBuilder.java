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
package org.dswarm.converter.morph;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.persistence.model.job.Task;

/**
 * @author tgaengler
 */
public class FilterMorphScriptBuilder extends AbstractMorphScriptBuilder<FilterMorphScriptBuilder> {

	@Override public FilterMorphScriptBuilder apply(final Task task) throws DMPConverterException {

		// TODO: add skip filter morph script generation here

		return this;
	}
}