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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public class PartialResponse extends Response implements Serializable {

    PartialResponse(final ClientResponseContext responseContext) {
        this.responseContext = responseContext;
        this.entityStream = responseContext.getEntityStream();
    }

    @Override
    public int getStatus() {
        return responseContext.getStatus();
    }

    @Override
    public StatusType getStatusInfo() {
        return responseContext.getStatusInfo();
    }

    @Override
    public Object getEntity() {
        throw notSupported();
    }

    private RuntimeException notSupported() {
        RuntimeException ex = new RuntimeException("method call not supported");
        //ex.printStackTrace();
        return ex;
    }


    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T readEntity(Class<T> entityType) {

        if (entityType.isAssignableFrom(String.class)) {
            return (T) readStringEntity(entityStream);
        } else {
            throw notSupported();
        }
    }

    public static String readStringEntity(InputStream input) {
        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return buffer.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new WebApplicationException("Failed to read entity", e);
        }
    }


    @Override
    public <T> T readEntity(GenericType<T> entityType) {
        throw notSupported();
    }

    @Override
    public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
        throw notSupported();
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
        throw notSupported();
    }

    @Override
    public boolean hasEntity() {
        return responseContext.hasEntity();
    }

    @Override
    public synchronized boolean bufferEntity() {
        try {
            byte[] buffer = new byte[4096];
            int read = 0;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((read = entityStream.read(buffer)) >= 0) {
                outStream.write(buffer, 0, read);
            }
            entityStream = new ByteArrayInputStream(outStream.toByteArray());
            return true;
        } catch (Exception any) {
            return false;
        }
    }

    @Override
    public synchronized void close() {
        try {
            responseContext.getEntityStream().close();
        } catch (Throwable e) {
            // ignore
        }
    }

    @Override
    public MediaType getMediaType() {
        return responseContext.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return responseContext.getLanguage();
    }

    @Override
    public int getLength() {
        return responseContext.getLength();
    }

    @Override
    public Set<String> getAllowedMethods() {
        return responseContext.getAllowedMethods();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return responseContext.getCookies();
    }

    @Override
    public EntityTag getEntityTag() {
        return responseContext.getEntityTag();
    }

    @Override
    public Date getDate() {
        return responseContext.getDate();
    }

    @Override
    public Date getLastModified() {
        return responseContext.getLastModified();
    }

    @Override
    public URI getLocation() {
        return responseContext.getLocation();
    }

    @Override
    public Set<Link> getLinks() {
        return responseContext.getLinks();
    }

    @Override
    public boolean hasLink(String relation) {
        return responseContext.hasLink(relation);
    }

    @Override
    public Link getLink(String relation) {
        return responseContext.getLink(relation);
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        throw new RuntimeException("method call not supported");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MultivaluedMap<String, Object> getMetadata() {
        return (MultivaluedMap) responseContext.getHeaders();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return responseContext.getHeaders();
    }

    @Override
    public String getHeaderString(String name) {
        return responseContext.getHeaderString(name);
    }

    private InputStream entityStream;

    private final transient ClientResponseContext responseContext;
}
