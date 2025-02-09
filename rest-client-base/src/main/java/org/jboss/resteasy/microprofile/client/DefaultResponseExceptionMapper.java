/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2021 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.resteasy.microprofile.client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.resteasy.client.exception.WebApplicationExceptionWrapper;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class DefaultResponseExceptionMapper implements ResponseExceptionMapper {

    @Override
    public Throwable toThrowable(Response response) {
        try {
            response.bufferEntity();
        } catch (Exception ignored) {
        }
        return WebApplicationExceptionWrapper.wrap(new WebApplicationException("Unknown error, status code " + response.getStatus(), response));
    }

    @Override
    public boolean handles(int status, MultivaluedMap headers) {
        final Config config = ConfigProvider.getConfig();
        final boolean originalBehavior = config.getOptionalValue(ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR, boolean.class)
                .orElse(false);
        final boolean serverSide = ResteasyDeployment.onServer();
        return status >= (originalBehavior || !serverSide ? 400 : 300);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
