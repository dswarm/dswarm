package de.avgl.dmp.persistence.model.job;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;

/**
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

	@Column(name = "EXPRESSION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String				expression;

	public String getExpression() {

		return expression;
	}

	public void setExpression(final String expressionArg) {

		this.expression = expressionArg;
	}

	@Override
	public boolean equals(final Object obj) {

		return Filter.class.isInstance(obj) && super.equals(obj);

	}
}
