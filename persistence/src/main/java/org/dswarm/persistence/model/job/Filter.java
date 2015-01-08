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
package org.dswarm.persistence.model.job;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

import org.dswarm.persistence.model.BasicDMPJPAObject;

/**
 * A filter is a graph pattern for reducing records. It can be applied at the beginning or the end of a {@link Transformation}
 * instantiation, i.e., a {@link Mapping}, to filter incoming or outgoing records.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "FILTER")
public class Filter extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The filter expression that should be evaluated at execution time.
	 */
	@Column(name = "EXPRESSION", columnDefinition = "BLOB")
	private String				expression;

	public Filter(final String uuidArg) {

		super(uuidArg);
	}

	protected Filter() {

	}

	/**
	 * Gets the filter expression.
	 *
	 * @return the filter expression
	 */
	public String getExpression() {

		return expression;
	}

	/**
	 * Sets the filter expression
	 *
	 * @param expressionArg a new filter expression
	 */
	public void setExpression(final String expressionArg) {

		expression = expressionArg;
	}

	@Override
	public boolean equals(final Object obj) {

		return Filter.class.isInstance(obj) && super.equals(obj);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Filter.class.isInstance(obj) && super.completeEquals(obj) && Objects.equal(((Filter) obj).getExpression(), getExpression());
	}
}
