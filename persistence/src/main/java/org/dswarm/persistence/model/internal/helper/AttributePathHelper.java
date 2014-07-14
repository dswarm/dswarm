package org.dswarm.persistence.model.internal.helper;

import java.util.LinkedList;

import org.dswarm.init.util.DMPStatics;

import com.google.common.collect.Lists;

public class AttributePathHelper {

	private final LinkedList<String>	attributePath	= Lists.newLinkedList();

	public void addAttribute(final String attribute) {

		attributePath.add(attribute);
	}

	public void setAttributePath(final LinkedList<String> attributePathArg) {

		attributePath.clear();
		attributePath.addAll(attributePathArg);
	}

	public LinkedList<String> getAttributePath() {

		return attributePath;
	}

	public int length() {

		return attributePath.size();
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < attributePath.size(); i++) {

			final String attribute = attributePath.get(i);

			sb.append(attribute);

			if (i < (attributePath.size() - 1)) {

				sb.append(DMPStatics.ATTRIBUTE_DELIMITER);
			}
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	@Override
	public boolean equals(final java.lang.Object obj) {

		return obj != null && AttributePathHelper.class.isInstance(obj) && toString().equals(obj.toString());
	}
}
