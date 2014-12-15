package org.dswarm.persistence.service;

import java.util.UUID;

/**
 * @author tgaengler
 */
public class UUIDService {

	public String getUUID(final String base) {

		final UUID uuid = UUID.randomUUID();

		return String.format(base + ":%s", uuid.toString());
	}
}
