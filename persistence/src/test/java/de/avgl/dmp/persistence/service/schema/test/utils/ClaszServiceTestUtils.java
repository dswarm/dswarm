package de.avgl.dmp.persistence.service.schema.test.utils;

import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class ClaszServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<ClaszService, ProxyClasz, Clasz> {

	public ClaszServiceTestUtils() {

		super(Clasz.class, ClaszService.class);
	}

	public Clasz createClass(final String id, final String name) throws Exception {

		final Clasz clasz = new Clasz(id, name);
		Clasz updatedClasz = createObject(clasz, clasz);

		return updatedClasz;
	}

	@Override
	public void reset() {

	}
}
