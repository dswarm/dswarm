package de.avgl.dmp.persistence.service.schema.test.utils;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

import java.util.Set;

public class ClaszServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<ClaszService, ProxyClasz, Clasz> {

	public static final Set<String>	excludeClasses	= Sets.newHashSet();

	static {

		excludeClasses.add("http://purl.org/ontology/bibo/Document");
	}

	public ClaszServiceTestUtils() {

		super(Clasz.class, ClaszService.class);
	}

	public Clasz createClass(final String id, final String name) throws Exception {

		final Clasz clasz = new Clasz(id, name);
		Clasz updatedClasz = createObject(clasz, clasz);

		return updatedClasz;
	}

	@Override
	public void deleteObject(Clasz object) {

		if (object == null) {

			return;
		}

		if (object.getUri() == null) {

			return;
		}

		if (excludeClasses.contains(object.getUri())) {

			// don't delete attributes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}
}
