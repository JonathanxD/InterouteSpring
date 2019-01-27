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

import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.spring.uni.HeadersProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

class SpringRestCLBackendConfiguration extends SpringRestBackendConfiguration {
    private final SpringRestBackendConfiguration root;
    private final ClassLoader classLoader;

    SpringRestCLBackendConfiguration(SpringRestBackendConfiguration root,
                                     ClassLoader classLoader) {
        super(root.getRootInvocationPathProvider(), root.getRestTemplateProvider(), root.getHeadersProviderMap());
        this.root = root;
        this.classLoader = classLoader;
    }

    public String getRootInvocationPath() {
        return this.root.getRootInvocationPath();
    }

    public RestTemplate getRestTemplate() {
        return this.root.getRestTemplate();
    }

    public Map<Origin, HeadersProvider> getHeadersProviderMap() {
        return this.root.getHeadersProviderMap();
    }

    public Optional<HttpHeaders> getHttpHeaders(Origin origin) {
        return this.root.getHttpHeaders(origin);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Builder toBuilderCl() {
        return new Builder(this);
    }

    public SpringRestBackendConfiguration getRoot() {
        return this.root;
    }

    public static class Builder {
        private SpringRestBackendConfiguration root;
        private ClassLoader classLoader;

        Builder() {
        }

        Builder(SpringRestCLBackendConfiguration defaults) {
            this.root = defaults.getRoot();
            this.classLoader = defaults.getClassLoader();
        }

        public Builder root(SpringRestBackendConfiguration root) {
            this.root = root;
            return this;
        }

        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public SpringRestCLBackendConfiguration build() {
            return new SpringRestCLBackendConfiguration(this.root, this.classLoader);
        }

    }
}
