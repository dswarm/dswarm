package de.avgl.dmp.converter.mf.stream.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.input.BOMInputStream;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.source.Opener;
import org.culturegraph.mf.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a file and passes a reader for it to the receiver. It also checks and discards an UTF8 BOM.
 * 
 * @author phorn
 */
@Description("Opens a file and checks for UTF8 BOM.")
@In(String.class)
@Out(java.io.Reader.class)
public class BOMResourceOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> implements Opener {

	private static final Logger	LOG			= LoggerFactory.getLogger(BOMResourceOpener.class);

	private String				encoding	= "UTF-8";

	/**
	 * Returns the encoding used to open the resource.
	 * 
	 * @return current default setting
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the encoding used to open the resource.
	 * 
	 * @param encoding new encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	private InputStream getFileInputStream(final String fileName) throws FileNotFoundException {
		return new BOMInputStream(ResourceUtil.getStream(fileName));
	}

	@Override
	public void process(final String file) throws MetafactureException {

		InputStream is = null;
		try {
			is = getFileInputStream(file);
			final Reader reader = new InputStreamReader(is, encoding);
			getReceiver().process(reader);

		} catch (final FileNotFoundException | UnsupportedEncodingException e) {
			throw new MetafactureException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (final IOException e) {
				BOMResourceOpener.LOG.error("IO error while closing file inputstream", e);
			}
		}
	}

}
