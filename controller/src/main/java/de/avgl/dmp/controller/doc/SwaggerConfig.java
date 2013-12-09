package de.avgl.dmp.controller.doc;

import javax.servlet.ServletConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.jersey.config.JerseyJaxrsConfig;
import com.wordnik.swagger.model.ApiInfo;

/**
 *
 * @author tgaengler
 *
 */
@Singleton
public class SwaggerConfig extends JerseyJaxrsConfig {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;
	private final String apiVersion;
	private final String apiBaseUrl;

	@Inject
	public SwaggerConfig(@Named("ApiVersion") final String apiVersion,
	                     @Named("ApiBaseUrl") final String apiBaseUrl) {

		this.apiVersion = apiVersion;
		this.apiBaseUrl = apiBaseUrl;
	}

	@Override
	public void init(final ServletConfig servletConfig) {

		super.init(servletConfig);

		final ApiInfo info = new ApiInfo(
				"DMP 2000 Backend", /* title */
				"This is the DMP 2000 Backend server.", /* description */
				"http://helloreverb.com/terms/", /* TOS Url */
				"tgaengler@avantgarde-labs.de", /* Contact */
				"Apache 2.0", /* license */
				"http://www.apache.org/licenses/LICENSE-2.0.html" /* license URL */
		);

		final com.wordnik.swagger.config.SwaggerConfig config = ConfigFactory.config();
		config.setApiVersion(apiVersion);
		config.setBasePath(apiBaseUrl);

		config.setApiInfo(info);

		// Not to be confused with the swagger framework version
		// This is the swagger specification version
		config.setSwaggerVersion("1.2");
	}

}
