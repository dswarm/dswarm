package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.DataModel;

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
