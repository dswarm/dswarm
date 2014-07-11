package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.DataModel;

/**
 * A converter event for CSV data resources that provides a {@link DataModel}.
 * 
 * @author tgaengler
 */
public class CSVConverterEvent extends ConverterEvent {

	public CSVConverterEvent(final DataModel dataModel) {

		super(dataModel);
	}
}
