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
package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * A converter event for CSV data resources that provides a {@link DataModel}.
 * 
 * @author tgaengler
 */
public class CSVConverterEvent extends ConverterEvent {

	public CSVConverterEvent(final DataModel dataModel, final UpdateFormat updateFormat, final boolean enableVersioning) {

		super(dataModel, updateFormat, enableVersioning);
	}
}
