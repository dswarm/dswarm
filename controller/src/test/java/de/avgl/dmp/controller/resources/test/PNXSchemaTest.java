package de.avgl.dmp.controller.resources.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Test;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;


public class PNXSchemaTest extends ResourceTest {

	private final ObjectMapper mapper;

	public PNXSchemaTest() {
		super(null);

		this.mapper = injector.getInstance(ObjectMapper.class);
	}

	private Resource uploadResource() throws URISyntaxException, IOException {

		final String name = "pnx";
		final String description = "pnx file";
		final File pnxFile = new File(Resources.getResource("test-pnx.xml").toURI());
		final FileDataBodyPart filePart = new FileDataBodyPart("file", pnxFile, MediaType.MULTIPART_FORM_DATA_TYPE);

		final MultiPart multiPart = new FormDataMultiPart()
				.field("name", name)
				.field("description", description)
				.bodyPart(filePart);

		final Response resourceResponse = target("resources")
				.request()
				.buildPost(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))
				.invoke();
		final Resource resource = mapper.readValue(resourceResponse.readEntity(String.class), Resource.class);

		assertThat(resourceResponse.getStatus(), equalTo(201));
		assertThat(resource.getName(), equalTo(name));
		assertThat(resource.getDescription(), equalTo(description));
		assertThat(resource.getType(), equalTo(ResourceType.FILE));

		return resource;
	}

	private DataModel createDataModel(final Resource resource) throws IOException {

		final String name = "pnx";
		final String description = "pnx config";

		final Configuration configuration = new Configuration() {{
			setName(name);
			setDescription(description);
			addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));
			addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
			addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://foo-bar.de/"));
		}};

		final String dataModelAsString = mapper.writeValueAsString(new DataModel() {{
			setDataResource(resource);
			setConfiguration(configuration);
		}});

		final Response dataModelResponse = target("datamodels")
				.request()
				.buildPost(Entity.json(dataModelAsString))
				.invoke();
		final DataModel dataModel = mapper.readValue(dataModelResponse.readEntity(String.class), DataModel.class);

		assertThat(dataModelResponse.getStatus(), equalTo(201));
		assertThat(dataModel.getDataResource(), equalTo(resource));
		assertThat(dataModel.getConfiguration().getName(), equalTo(name));
		assertThat(dataModel.getConfiguration().getDescription(), equalTo(description));

		return dataModel;
	}

	private Fn makeExpected(final String uri) {
		final Fn fn = new Fn(uri);

		final String[] attributeNames = {"sort", "lsr06", "booster2", "frbr", "ilsapiid", "dedup", "search",
				"lds22", "lds28", "toplevel", "delcategory", "f6", "lds21", "lad04", "lad05", "lfc01",
				"f1", "availlibrary", "sourcesystem", "lds32", "format", "cop", "pub", "publisher", "ranking",
				"availpnx", "btitle", "addata", "searchscope", "prefilter", "creationdate", "scope", "control",
				"lds12", "linktoholdings", "lds17", "addsrcrecordid", "links", "lds18", "relation", "delivery",
				"rsrctype", "lds39", "lsr01", "display", "lsr04", "lsr05", "lsr20", "lsr21", "lds31", "addtitle",
				"lfc02", "recordid", "t", "f10", "ispartof", "facets", "c4", "availinstitution", "c1", "genre",
				"title", "sourcerecordid", "sourceformat", "sourceid", "language", "institution"};

		for (final String attributeName : attributeNames) {
			fn.make(attributeName);
		}

		fn.path("sort", "creationdate");
		fn.path("sort", "title");
		fn.path("links", "linktoholdings");
		fn.path("frbr", "t");
		fn.path("dedup", "f10");
		fn.path("dedup", "c4");
		fn.path("dedup", "c1");
		fn.path("dedup", "f6");
		fn.path("dedup", "f1");
		fn.path("dedup", "t");
		fn.path("search", "rsrctype");
		fn.path("search", "scope");
		fn.path("search", "addsrcrecordid");
		fn.path("search", "title");
		fn.path("search", "lsr06");
		fn.path("search", "lsr01");
		fn.path("search", "lsr04");
		fn.path("search", "searchscope");
		fn.path("search", "lsr20");
		fn.path("search", "lsr21");
		fn.path("search", "creationdate");
		fn.path("search", "sourceid");
		fn.path("search", "addtitle");
		fn.path("search", "recordid");
		fn.path("search", "lsr05");
		fn.path("display", "availlibrary");
		fn.path("display", "lds12");
		fn.path("display", "ispartof");
		fn.path("display", "publisher");
		fn.path("display", "lds17");
		fn.path("display", "format");
		fn.path("display", "availinstitution");
		fn.path("display", "availpnx");
		fn.path("display", "lds39");
		fn.path("display", "title");
		fn.path("display", "lds28");
		fn.path("display", "relation");
		fn.path("display", "lds18");
		fn.path("display", "lds31");
		fn.path("display", "creationdate");
		fn.path("display", "lds32");
		fn.path("display", "language");
		fn.path("display", "lds21");
		fn.path("display", "lds22");
		fn.path("facets", "language");
		fn.path("facets", "toplevel");
		fn.path("facets", "prefilter");
		fn.path("facets", "creationdate");
		fn.path("facets", "lfc02");
		fn.path("facets", "rsrctype");
		fn.path("facets", "lfc01");
		fn.path("ranking", "booster2");
		fn.path("delivery", "delcategory");
		fn.path("delivery", "institution");
		fn.path("addata", "genre");
		fn.path("addata", "format");
		fn.path("addata", "cop");
		fn.path("addata", "btitle");
		fn.path("addata", "pub");
		fn.path("addata", "addtitle");
		fn.path("addata", "lad04");
		fn.path("addata", "lad05");
		fn.path("control", "sourceformat");
		fn.path("control", "sourcesystem");
		fn.path("control", "sourcerecordid");
		fn.path("control", "addsrcrecordid");
		fn.path("control", "ilsapiid");
		fn.path("control", "sourceid");
		fn.path("control", "recordid");

		return fn;
	}

	@Test
	public void testPNXSchemaCreation() throws Exception {

		final Resource resource = uploadResource();
		final DataModel dataModel = createDataModel(resource);
		final Schema schema = dataModel.getSchema();

		final String dataModelUri = "http://data.slub-dresden.de/resources/" + dataModel.getId() + "/schema";

		final Fn expected = makeExpected(dataModelUri);

		final Fn actual = new Fn(dataModelUri);

		for (final AttributePath attributePath : schema.getAttributePaths()) {
			final LinkedList<Attribute> attributes = attributePath.getAttributePath();

			final String[] attributeNames = new String[attributes.size()];
			int idx = 0;

			for (final Attribute attribute : attributes) {
				actual.make(attribute.getName());
				attributeNames[idx++] = attribute.getName();
			}
			actual.path(attributeNames);
		}

		assertThat(actual.attributePaths, everyItem(isIn(expected.attributePaths)));
		assertThat(expected.attributePaths, everyItem(isIn(actual.attributePaths)));
	}

	private class Fn {
		private final String uri;

		private Fn(final String uri) {
			this.uri = uri;
		}

		private final Map<String, Attribute> attributes = Maps.newHashMap();
		private final Set<AttributePath> attributePaths = Sets.newHashSet();

		public void path(final String... a) {
			final LinkedList<Attribute> list = Lists.newLinkedList();
			for (final String s : a) {
				list.add(attributes.get(s));
			}
			attributePaths.add(new AttributePath(list));
		}

		public void make(final String name) {
			attributes.put(name, new Attribute(uri + "#" + name, name));
		}
	}
}
