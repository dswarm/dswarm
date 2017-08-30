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
package org.dswarm.converter.morph;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.morph.model.FilterExpression;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Task;

/**
 * @author tgaengler
 */
public class FilterMorphScriptBuilder extends AbstractMorphScriptBuilder<FilterMorphScriptBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(FilterMorphScriptBuilder.class);

	private static final String SKIP_FILTER = "skip_filter_";

	private static final String MF_ELEMENT_CONSTANT = "constant";

	private static final String COMBINE_VALUE_VARIABLE = "out";

	@Override public FilterMorphScriptBuilder apply(final Task task) throws DMPConverterException {

		final Optional<Filter> optionalSkipFilter = Optional.ofNullable(task.getJob().getSkipFilter());

		if (!optionalSkipFilter.isPresent()) {

			LOG.debug("there is no skip filter at job '{}' of task '{}'", task.getJob().getUuid(), task.getUuid());

			return this;
		}

		final Filter skipFilter = optionalSkipFilter.get();
		final Optional<String> optionalSkipFilterExpression = Optional.ofNullable(skipFilter.getExpression());

		if (!optionalSkipFilterExpression.isPresent()) {

			LOG.debug("there is no filter expression in skip filter '{}' of job '{}' of task '{}'", skipFilter.getUuid(), task.getJob().getUuid(),
					task.getUuid());

			return this;
		}

		final String filterExpressionString = optionalSkipFilterExpression.get();

		final Map<String, FilterExpression> filterExpressionMap = extractFilterExpressions(filterExpressionString);

		if (filterExpressionMap == null || filterExpressionMap.isEmpty()) {

			LOG.debug("there are no filter conditions in filter expression in skip filter '{}' of job '{}' of task '{}'", skipFilter.getUuid(),
					task.getJob().getUuid(), task.getUuid());

			return this;
		}

		super.apply(task);

		metaName.setTextContent(SKIP_FILTER + task.getJob().getUuid());

		final String attributePathStringXMLEscaped = Iterators.getLast(filterExpressionMap.keySet().iterator());

		addFilter(attributePathStringXMLEscaped, COMBINE_VALUE_VARIABLE, filterExpressionMap, rules, false);

		return this;
	}

	@Override
	protected Element createFilterDataElement(final String variable, final String attributePathString, final Optional<FilterExpression> optionalCombineAsFilterDataOutFilter) {

		final Element combineAsFilterDataOut = doc.createElement(METAMORPH_ELEMENT_DATA);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_TARGET, variable);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_SOURCE, attributePathString);

		final Element constantElement = doc.createElement(MF_ELEMENT_CONSTANT);
		constantElement.setAttribute(MF_ELEMENT_VALUE_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);

		combineAsFilterDataOut.appendChild(constantElement);

		return combineAsFilterDataOut;
	}
}
