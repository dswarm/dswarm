/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.providers.handler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.providers.BaseExceptionHandler;
import org.dswarm.converter.DMPMorphDefException;

/**
 * An exception handler for providing exact messages for wrong Metamorph definitions
 *
 * @author phorn
 */
@Provider
public class DMPMorphDefExceptionHandler extends BaseExceptionHandler<DMPMorphDefException> {

	@Override
	protected Response.Status getStatusFrom(final DMPMorphDefException exception) {
		return Response.Status.INTERNAL_SERVER_ERROR;
	}

	@Override
	protected String getErrorMessageFrom(final DMPMorphDefException exception) {
		return exception.getMessage();
	}
}
