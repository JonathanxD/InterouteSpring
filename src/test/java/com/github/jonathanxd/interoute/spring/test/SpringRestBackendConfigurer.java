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
package com.github.jonathanxd.interoute.spring.test;

import com.github.jonathanxd.interoute.backend.InterouteBackendConfigurer;
import com.github.jonathanxd.interoute.spring.SpringRestBackendConfiguration;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.RestTemplate;

public class SpringRestBackendConfigurer implements InterouteBackendConfigurer<SpringRestBackendConfiguration> {

    private static String ROOT_INVOCATION_PATH;
    private static RestTemplate REST_TEMPLATE;

    static void setup(String root, RestTemplate restTemplate) {
        SpringRestBackendConfigurer.ROOT_INVOCATION_PATH = root;
        SpringRestBackendConfigurer.REST_TEMPLATE = restTemplate;
    }

    @NotNull
    @Override
    public SpringRestBackendConfiguration configure(@NotNull SpringRestBackendConfiguration configuration) {
        return configuration.toBuilder()
                .rootInvocationPath(() -> ROOT_INVOCATION_PATH)
                .restTemplate(() -> REST_TEMPLATE).build();
    }
}
