package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

public class ConverterEvent {

	private final DataModel	dataModel;

	public ConverterEvent(final DataModel dataModel) {
		
		this.dataModel = dataModel;
	}

	public DataModel getDataModel() {
		
		return dataModel;
	}
}
