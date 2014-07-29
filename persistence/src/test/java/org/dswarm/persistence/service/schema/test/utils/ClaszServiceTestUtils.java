package org.dswarm.persistence.service.schema.test.utils;

import java.util.Set;

import com.google.common.collect.Sets;

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class ClaszServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<ClaszService, ProxyClasz, Clasz> {

	public static final Set<String>	excludeClasses	= Sets.newHashSet();

	static {

		ClaszServiceTestUtils.excludeClasses.add("http://purl.org/ontology/bibo/Document");
		ClaszServiceTestUtils.excludeClasses.add("http://vocab.ub.uni-leipzig.de/bibrm/ContractItem");
		ClaszServiceTestUtils.excludeClasses.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#datensatzType");
	}

	public ClaszServiceTestUtils() {

		super(Clasz.class, ClaszService.class);
	}

	public Clasz createClass(final String id, final String name) throws Exception {

		final Clasz clasz = new Clasz(id, name);
		final Clasz updatedClasz = createObject(clasz, clasz);

		return updatedClasz;
	}

	@Override
	public void deleteObject(final Clasz object) {

		if (object == null) {

			return;
		}

		if (object.getUri() == null) {

			return;
		}

		if (ClaszServiceTestUtils.excludeClasses.contains(object.getUri())) {

			// don't delete classes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}
}
