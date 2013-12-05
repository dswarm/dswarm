package de.avgl.dmp.controller.doc;

import javax.servlet.ServletConfig;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.jersey.config.JerseyJaxrsConfig;
import com.wordnik.swagger.model.ApiInfo;

/**
 * 
 * @author tgaengler
 *
 */
public class SwaggerConfig extends JerseyJaxrsConfig {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
	public void init(final ServletConfig servletConfig) {

		super.init(servletConfig);

		final ApiInfo info = new ApiInfo("DMP 2000 Backend", /* title */
		"This is the DMP 2000 Backend server.  You can find out more about Swagger "
				+ "at <a href=\"http://swagger.wordnik.com\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger.",
				"http://helloreverb.com/terms/", "tgaengler@avantgarde-labs.de", /* Contact */
				"Apache 2.0", /* license */
				"http://www.apache.org/licenses/LICENSE-2.0.html" /* license URL */
		);

		ConfigFactory.config().setApiInfo(info);
		ConfigFactory.config().setSwaggerVersion("1.3.1");
	}

}
