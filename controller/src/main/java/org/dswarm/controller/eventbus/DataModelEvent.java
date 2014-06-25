package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.DataModel;

/**
 * An (abstract) event that provides a {@link DataModel}.
 *
 * @author tgaengler
 */
public abstract class DataModelEvent {

	/**
	 * A data model that can be utilised for further processing.
	 */
	private final DataModel	dataModel;

	/**
	 * Creates a new data model event with the given data model.
	 *
	 * @param dataModel a data model that can be utilised for further processing
	 */
	public DataModelEvent(final DataModel dataModel) {

		this.dataModel = dataModel;
	}

	/**
	 * Gets the data model of the event for further processing.
	 *
	 * @return the data model of the event for further processing
	 */
	public DataModel getDataModel() {

		return dataModel;
	}
}
