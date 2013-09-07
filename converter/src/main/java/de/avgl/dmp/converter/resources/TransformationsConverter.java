package de.avgl.dmp.converter.resources;

import com.google.common.collect.Lists;
import de.avgl.dmp.persistence.model.Transformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TransformationsConverter {

	public static File toMetamorph(final String json) throws IOException, TransformationsCoverterException {
		final List<Transformation> pojos = new JsonToPojoMapper().apply(json);

		return toMetamorph(pojos);
	}

	public static File toMetamorph(final List<Transformation> transformations) throws IOException, TransformationsCoverterException {
		final String xml = new PojoToXMLBuilder().apply(transformations).toString();
		return writeMetamorph(xml);
	}

	public static File toMetamorph(final Transformation transformation) throws IOException, TransformationsCoverterException {
		return toMetamorph(Lists.newArrayList(transformation));
	}

	public static File writeMetamorph(final String xmlContent) throws IOException {
		final File file = File.createTempFile("avgl_dmp", ".tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(xmlContent);
		bw.close();

		return file;
	}

	// static object
	private TransformationsConverter() {}
}
