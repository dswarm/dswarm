package org.dswarm.controller.doc;

import javax.servlet.ServletConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.wordnik.swagger.config.ConfigFactory$;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jersey.config.JerseyJaxrsConfig;
import com.wordnik.swagger.model.ApiInfo;

/**
 * The configuration for the documentation generation application Swagger. It is utilised for generating the documentation of the
 * backend API.
 * 
 * @author tgaengler
 * @author phorn
 */
@Singleton
public class SwaggerConfiguration extends JerseyJaxrsConfig {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The version of the backend API.
	 */
	private final String		apiVersion;

	/**
	 * The base URI of the backend API.
	 */
	private final String		apiBaseUrl;

	/**
	 * Creates a new Swagger configuration with the given version and base URI of the backend API.
	 * 
	 * @param apiVersion the version of the backend API
	 * @param apiBaseUrl the base URI of the backend API
	 */
	@Inject
	public SwaggerConfiguration(@Named("dswarm.api.version") final String apiVersion, @Named("dswarm.api.base-url") final String apiBaseUrl) {

		this.apiVersion = apiVersion;
		this.apiBaseUrl = apiBaseUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final ServletConfig servletConfig) {

		super.init(servletConfig);

		final ApiInfo info = new ApiInfo("d:swarm Backend", /* title */
		"This is the d:swarm Backend server.", /* description */
		"http://helloreverb.com/terms/", /* TOS Url */
		"tgaengler@avantgarde-labs.de", /* Contact */
		"Apache 2.0", /* license */
		"http://www.apache.org/licenses/LICENSE-2.0.html" /* license URL */
		);

		final SwaggerConfig config = ConfigFactory$.MODULE$.config();

		config.setApiVersion(apiVersion);
		config.setBasePath(apiBaseUrl);
		config.setApiInfo(info);

		// Not to be confused with the swagger framework version
		// This is the swagger specification version
		config.setSwaggerVersion("1.2");
	}

}
