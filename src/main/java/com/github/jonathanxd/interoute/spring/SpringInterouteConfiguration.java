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

import com.github.jonathanxd.interoute.Interoute;
import com.github.jonathanxd.interoute.exception.RouterCreationException;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.bytecode.classloader.CodeClassLoader;

import org.springframework.beans.factory.BeanCreationException;

public abstract class SpringInterouteConfiguration {

    public SpringInterouteConfiguration() {
    }

    @SuppressWarnings("unchecked")
    protected final <I> I createRouter(Class<I> type) {
        CodeClassLoader loader = new CodeClassLoader();
        Result<? extends I, RouterCreationException> router = Interoute.createRouter(type, loader);

        if (router instanceof Result.Ok<?, ?>) {
            return ((Result.Ok<? extends I, RouterCreationException>) router).success();
        } else {
            throw new BeanCreationException(
                    String.format("Failed to create router instance for interface '%s'", type.getCanonicalName()),
                    router.errorOrNull()
            );
        }
    }
}
