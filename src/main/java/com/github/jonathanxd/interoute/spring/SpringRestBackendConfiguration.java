/*
 *      InterouteSpring -  <https://github.com/JonathanxD/InterouteSpring>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2019 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.interoute.spring;

import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;
import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.spring.uni.HeadersProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SpringRestBackendConfiguration implements InterouteBackendConfiguration {
    private final Supplier<String> rootInvocationPath;
    private final Supplier<RestTemplate> restTemplate;
    private final Map<Origin, HeadersProvider> headersProviderMap;

    public SpringRestBackendConfiguration(Supplier<String> rootInvocationPath,
                                          Supplier<RestTemplate> restTemplate,
                                          Map<Origin, HeadersProvider> headersProviderMap) {
        this.rootInvocationPath = rootInvocationPath;
        this.restTemplate = restTemplate;
        this.headersProviderMap = headersProviderMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRootInvocationPath() {
        return this.rootInvocationPath.get();
    }

    public Supplier<String> getRootInvocationPathProvider() {
        return this.rootInvocationPath;
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate.get();
    }

    public Supplier<RestTemplate> getRestTemplateProvider() {
        return this.restTemplate;
    }

    public Map<Origin, HeadersProvider> getHeadersProviderMap() {
        return this.headersProviderMap;
    }

    public Optional<HttpHeaders> getHttpHeaders(Origin origin) {
        return Optional.ofNullable(this.headersProviderMap.get(origin)).map(HeadersProvider::get);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Supplier<String> rootInvocationPath;
        private Supplier<RestTemplate> restTemplate;
        private Map<Origin, HeadersProvider> headersProviderMap;

        Builder() {
        }

        Builder(SpringRestBackendConfiguration defaults) {
            this.rootInvocationPath = defaults.rootInvocationPath;
            this.restTemplate = defaults.restTemplate;
            this.headersProviderMap = defaults.getHeadersProviderMap();
        }

        public Builder rootInvocationPath(Supplier<String> rootInvocationPath) {
            this.rootInvocationPath = rootInvocationPath;
            return this;
        }

        public Builder restTemplate(Supplier<RestTemplate> restTemplate) {
            this.restTemplate = restTemplate;
            return this;
        }

        public Builder headersProviderMap(Map<Origin, HeadersProvider> headersProviderMap) {
            this.headersProviderMap = headersProviderMap;
            return this;
        }

        public SpringRestBackendConfiguration build() {
            return new SpringRestBackendConfiguration(
                    this.rootInvocationPath,
                    this.restTemplate,
                    this.headersProviderMap
            );
        }

    }
}
