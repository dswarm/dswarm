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
package org.dswarm.persistence.model.internal.helper;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import org.dswarm.init.util.DMPStatics;

public class AttributePathHelper {

	private final List<String> attributePath;
	private final Boolean required;
	private final Boolean multivalue;

	public AttributePathHelper() {

		this.attributePath = Collections.emptyList();
		this.required = null;
		this.multivalue = null;
	}

	public AttributePathHelper(final List<String> attributePath) {

		this.attributePath = attributePath;
		this.required = null;
		this.multivalue = null;
	}

	public AttributePathHelper(final List<String> attributePath,
	                           final Boolean required,
	                           final Boolean multivalue) {

		this.attributePath = attributePath;
		this.required = required;
		this.multivalue = multivalue;
	}

	public List<String> getAttributePath() {

		return attributePath;
	}

	public Boolean isRequired() {

		return required;
	}

	public Boolean isMultivalue() {

		return multivalue;
	}

	public int length() {

		return attributePath.size();
	}

	@Override
	public String toString() {

		return Joiner.on(DMPStatics.ATTRIBUTE_DELIMITER).join(attributePath);
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	@Override
	public boolean equals(final java.lang.Object obj) {

		return !(obj == null || !(obj instanceof AttributePathHelper)) && attributePath.equals(((AttributePathHelper) obj).attributePath);
	}
}
