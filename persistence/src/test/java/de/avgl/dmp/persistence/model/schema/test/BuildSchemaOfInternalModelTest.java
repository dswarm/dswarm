package de.avgl.dmp.persistence.model.schema.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;

public class BuildSchemaOfInternalModelTest {

	private static final String NS_FOAF = "http://purl.org/foaf/";

	@Test
	public void test() {
		
		// nothing to see here, just needed to write sth to be able to push the new branch to my remote ...
		
		AttributePath attributePath1 = new AttributePath();
		attributePath1.addAttribute(new Attribute(NS_FOAF + "creator"));
		
		System.out.println(attributePath1.toAttributePath());
		
		fail("Not yet implemented");
	}

}
