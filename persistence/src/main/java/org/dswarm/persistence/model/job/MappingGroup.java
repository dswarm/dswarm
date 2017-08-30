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
package org.dswarm.persistence.model.job;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

/**
 * @author tgaengler
 */
@XmlRootElement
public class MappingGroup extends AdvancedDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private List<Mapping>		mappings;

	private String				description;

	protected MappingGroup() {

	}

	public MappingGroup(final String uuid) {

		super(uuid);
	}

	public List<Mapping> getMappings() {

		return mappings;
	}

	public void setMappings(final List<Mapping> mappingsArg) {

		mappings = mappingsArg;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(final String descriptionArg) {

		description = descriptionArg;
	}
}
