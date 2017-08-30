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

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A job is a collection of {@link Mapping}s that can be execution on a given input {@link DataModel} and be written to a given
 * output {@link DataModel}.
 *
 * @author tgaengler
 */
@XmlRootElement
public class Job extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The collection of mappings of the job.
	 */
	// @JsonSerialize(using = SetMappingReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @XmlIDREF
	@XmlList
	private Set<Mapping> mappings;

	/**
	 * The skip filter of this job. A skip filter to skip the job for records that doesn't match the filter conditions.
	 */
	@XmlElement(name = "skip_filter")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter skipFilter;

	protected Job() {

	}

	public Job(final String uuidArg) {

		super(uuidArg);
	}

	/**
	 * Gets the collection of mappings of the job.
	 *
	 * @return the collection of mappings of the job
	 */
	public Set<Mapping> getMappings() {

		return mappings;
	}

	/**
	 * Sets the collection of mappings of the job.
	 *
	 * @param mappingsArg a new collection of mappings
	 */
	public void setMappings(final Set<Mapping> mappingsArg) {

		if (mappingsArg == null && mappings != null) {

			mappings.clear();
		}

		if (mappingsArg != null) {

			if (mappings == null) {

				mappings = Sets.newCopyOnWriteArraySet();
			}

			if (!DMPPersistenceUtil.getMappingUtils().completeEquals(mappings, mappingsArg)) {

				mappings.clear();
				mappings.addAll(mappingsArg);
			}
		}
	}

	/**
	 * Gets the skip filter of the job.
	 *
	 * @return the skip filter of the job
	 */
	public Filter getSkipFilter() {

		return skipFilter;
	}

	/**
	 * Sets the skip filter of the job.
	 *
	 * @param skipFilterArg a new skip filter
	 */
	public void setSkipFilter(final Filter skipFilterArg) {

		skipFilter = skipFilterArg;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Job.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getMappingUtils().completeEquals(((Job) obj).getMappings(), getMappings())
				&& DMPPersistenceUtil.getFilterUtils().completeEquals(((Job) obj).getSkipFilter(), getSkipFilter());
	}
}
