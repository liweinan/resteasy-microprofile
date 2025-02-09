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

package org.jboss.resteasy.microprofile.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;


public class MpClientBuilderImpl extends ResteasyClientBuilderImpl {

    public final List<AsyncInvocationInterceptorFactory> asyncInterceptorFactories = new ArrayList<>();

    @Override
    protected ResteasyClient createResteasyClient(ClientHttpEngine engine, ExecutorService executor,
                                                  boolean cleanupExecutor,
                                                  ScheduledExecutorService scheduledExecutorService,
                                                  ClientConfiguration config) {
        return new MpClient(engine, executor, cleanupExecutor, scheduledExecutorService, config, asyncInterceptorFactories);
    }
}
