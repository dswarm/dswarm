package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

public abstract class DataModelEvent {

	private final DataModel	dataModel;

	public DataModelEvent(final DataModel dataModel) {
		
		this.dataModel = dataModel;
	}

	public DataModel getDataModel() {
		
		return dataModel;
	}
}
