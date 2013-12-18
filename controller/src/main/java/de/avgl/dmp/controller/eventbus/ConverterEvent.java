package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

public class ConverterEvent extends DataModelEvent {

	public ConverterEvent(final DataModel dataModel) {

		super(dataModel);
	}
}
