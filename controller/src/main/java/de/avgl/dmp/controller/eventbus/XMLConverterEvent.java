package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * A converter event for XML data resources that provides a {@link DataModel}.
 * 
 * @author tgaengler
 */
public class XMLConverterEvent extends ConverterEvent {

	public XMLConverterEvent(final DataModel dataModel) {

		super(dataModel);
	}
}
