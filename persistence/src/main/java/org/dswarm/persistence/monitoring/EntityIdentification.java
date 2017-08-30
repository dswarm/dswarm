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
package org.dswarm.persistence.monitoring;

import java.util.Arrays;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.service.UUIDService;

final class EntityIdentification {

	private static final CharMatcher MATCHER =
			CharMatcher.ASCII
					.and(CharMatcher.JAVA_LETTER_OR_DIGIT)
					.negate()
					.or(CharMatcher.WHITESPACE);

	private final String className;
	private final String description;
	private final String name;
	private final String uuid;

	private EntityIdentification(final String className, final String uuid, final String name, final String description) {
		this.className = className;
		this.uuid = uuid;
		this.name = name;
		this.description = description;
	}

	@Override
	public String toString() {
		return abbreviated();
	}

	String getUuid() {
		return uuid;
	}

	MDC.MDCCloseable putMDC() {
		return MDC.putCloseable("entityIdentifier", abbreviated());
	}

	private String abbreviated() {
		return StringUtils.abbreviate(normalized(), 125);
	}

	private String joined() {
		return Joiner.on('-').skipNulls().join(Arrays.asList(
				className, uuid, name, description
		));
	}

	private String normalized() {
		final String normalizedIdentifier =
				StringUtils.stripAccents(joined());

		final Iterable<String> asciiParts =
				Splitter.on(MATCHER).omitEmptyStrings().split(normalizedIdentifier);

		return Joiner.on('-').join(asciiParts);
	}

	static EntityIdentification of(final ExtendedBasicDMPJPAObject object) {
		final String objectClass = object.getClass().getSimpleName();

		final String uuid = Strings.isNullOrEmpty(object.getUuid())
				? UUIDService.getUUID(objectClass)
				: object.getUuid();

		final String name =
				StringUtils.defaultIfEmpty(object.getName(), "Unknown " + objectClass);

		final String description =
				Strings.emptyToNull(object.getDescription());

		return new EntityIdentification(objectClass, uuid, name, description);
	}
}
