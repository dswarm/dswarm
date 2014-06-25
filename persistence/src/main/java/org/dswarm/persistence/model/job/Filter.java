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
	@Column(name = "EXPRESSION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String				expression;

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
