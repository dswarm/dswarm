package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * A converter event that provides a {@link DataModel}.
 * 
 * @author tgaengler
 */
public class ConverterEvent extends DataModelEvent {

	/**
	 * Creates a new converter event with the given data model.
	 * 
	 * @param dataModel a data model that can be utilised for further processing
	 */
	public ConverterEvent(final DataModel dataModel) {

		super(dataModel);
	}
}
