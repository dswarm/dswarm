package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * A converter event for CSV data resources that provides a {@link DataModel}.
 * 
 * @author tgaengler
 *
 */
public class CSVConverterEvent extends ConverterEvent {

	public CSVConverterEvent(final DataModel dataModel) {

		super(dataModel);
	}
}
