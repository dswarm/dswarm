package org.dswarm.converter.morph.model;

/**
 * @author tgaengler
 */
public class FilterExpression {

	private final FilterExpressionType type;

	private final String expression;

	public FilterExpression(final String expressionArg, final FilterExpressionType typeArg) {

		expression = expressionArg;
		type = typeArg;
	}

	public FilterExpressionType getType() {

		return type;
	}

	public String getExpression() {

		return expression;
	}
}
