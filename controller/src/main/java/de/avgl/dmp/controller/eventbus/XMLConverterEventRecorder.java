package de.avgl.dmp.controller.eventbus;

import java.util.Collection;
import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.XMLSourceResourceGDMStmtsFlow;
import de.avgl.dmp.graph.json.Resource;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event recorder for converting XML documents.
 * 
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class XMLConverterEventRecorder {

	private static final Logger LOG = LoggerFactory.getLogger(XMLConverterEventRecorder.class
	);

	/**
	 * The internal model service factory
	 */
	private final InternalModelServiceFactory internalServiceFactory;

	/**
	 * Creates a new event recorder for converting XML documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 * @param eventBus an event bus, where this event record will be registered
	 */
	@Inject
	public XMLConverterEventRecorder(final InternalModelServiceFactory internalModelServiceFactory, final EventBus eventBus) {

		internalServiceFactory = internalModelServiceFactory;
		eventBus.register(this);
	}

	/**
	 * Processes the XML document of the data model of the given event and persists the converted data.
	 *
	 * @param event an converter event that provides a data model
	 */
	@Subscribe
	public void processDataModel(final XMLConverterEvent event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();

		GDMModel result = null;

		try {

			final XMLSourceResourceGDMStmtsFlow flow = new XMLSourceResourceGDMStmtsFlow(dataModel);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
			final List<GDMModel> gdmModels = flow.applyResource(path);

			// write GDM models at once
			final de.avgl.dmp.graph.json.Model model = new de.avgl.dmp.graph.json.Model();
			String recordClassUri = null;

			for (final GDMModel gdmModel : gdmModels) {

				if (gdmModel.getModel() != null) {

					final de.avgl.dmp.graph.json.Model aModel = gdmModel.getModel();

					if (aModel.getResources() != null) {

						final Collection<Resource> resources = aModel.getResources();

						for (final Resource resource : resources) {

							model.addResource(resource);

							if (recordClassUri == null) {

								recordClassUri = gdmModel.getRecordClassURI();
							}
						}
					}
				}
			}

			result = new GDMModel(model, null, recordClassUri);

		} catch (final DMPConverterException | NullPointerException e) {

			final String message = "couldn't convert the XML data of data model '" + dataModel.getId() + "'";

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		} catch (final Exception e) {

			final String message = "really couldn't convert the XML data of data model '" + dataModel.getId() + "'";

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}

		if (result != null) {

			try {

				internalServiceFactory.getInternalGDMGraphService().createObject(dataModel.getId(), result);
			} catch (final DMPPersistenceException e) {

				final String message = "couldn't persist the converted data of data model '" + dataModel.getId() + "'";

				XMLConverterEventRecorder.LOG.error(message, e);

				throw new DMPControllerException(message + " " + e.getMessage(), e);
			}
		}
	}
}
