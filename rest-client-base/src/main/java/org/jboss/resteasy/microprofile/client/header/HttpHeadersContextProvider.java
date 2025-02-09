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

package org.jboss.resteasy.microprofile.client.header;

import static org.jboss.resteasy.microprofile.client.utils.ListCastUtils.castToListOfStrings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.util.CookieParser;
import org.jboss.resteasy.util.DateUtil;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.WeightedLanguage;

/**
 * Used to inject HttpHeaders to the client providers (filters, etc).
 *
 * based on {@link org.jboss.resteasy.specimpl.ResteasyHttpHeaders}
 */
public class HttpHeadersContextProvider implements HttpHeaders {

    private final ClientRequestContext context;

    public HttpHeadersContextProvider(final ClientRequestContext context) {
        this.context = context;
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        MultivaluedMap<String, Object> headers = context.getHeaders();
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        headers.forEach(
                (key, values) ->
                        result.put(key, castToListOfStrings(values))
        );
        return result;
    }

    @Override
    public List<String> getRequestHeader(String name) {
        List<String> vals = getRequestHeaders().get(name);
        if (vals == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(vals);
    }

    @Override
    public Map<String, Cookie> getCookies() {
        Map<String, Cookie> cookies = new HashMap<>();
        List<String> cookieHeader = getRequestHeaders().get(HttpHeaders.COOKIE);
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            for (String s : cookieHeader) {
                List<Cookie> list = CookieParser.parseCookies(s);
                for (Cookie cookie : list) {
                    cookies.put(cookie.getName(), cookie);
                }
            }
        }
        return Collections.unmodifiableMap(cookies);
    }

    @Override
    public Date getDate() {
        String date = getRequestHeaders().getFirst(DATE);
        return date == null
                ? null
                : DateUtil.parseDate(date);
    }

    @Override
    public String getHeaderString(String name) {
        List<String> vals = getRequestHeaders().get(name);
        return vals == null
                ? null
                : String.join(",", vals);
    }

    @Override
    public Locale getLanguage() {
        String obj = getRequestHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE);
        return obj == null
                ? null
                : new Locale(obj);
    }

    @Override
    public int getLength() {
        String obj = getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (obj == null) {
            return -1;
        }
        return Integer.parseInt(obj);
    }

    @Override
    public MediaType getMediaType() {
        String contentType = getRequestHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return contentType == null
                ? null
                : MediaType.valueOf(contentType);
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        String accepts = getHeaderString(ACCEPT);
        if (accepts == null) {
            return Collections.singletonList(MediaType.WILDCARD_TYPE);
        } else {
            return parseToStream(accepts)
                    .map(MediaType::valueOf)
                    .sorted(MediaTypeHelper::compareWeight)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        String accepts = getHeaderString(ACCEPT_LANGUAGE);
        if (accepts == null) {
            return Collections.singletonList(Locale.forLanguageTag("*"));
        }

        return parseToStream(accepts)
                .map(WeightedLanguage::parse)
                .sorted()
                .map(WeightedLanguage::getLocale)
                .collect(Collectors.toList());
    }

    private Stream<String> parseToStream(String accepts) {
        String[] splitValues = accepts.split(",");
        return Arrays.stream(splitValues)
                .map(String::trim);
    }
}
