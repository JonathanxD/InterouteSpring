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

import com.github.jonathanxd.interoute.annotation.BackendConfigurer;
import com.github.jonathanxd.interoute.annotation.RouteTo;
import com.github.jonathanxd.interoute.annotation.RouterInterface;
import com.github.jonathanxd.interoute.route.Route;
import com.github.jonathanxd.interoute.spring.SpringRestBackend;
import com.github.jonathanxd.interoute.spring.annotation.Body;
import com.github.jonathanxd.interoute.spring.annotation.Get;
import com.github.jonathanxd.interoute.spring.annotation.Headers;
import com.github.jonathanxd.interoute.spring.annotation.Post;
import com.github.jonathanxd.interoute.spring.route.SpringRoute;

@RouterInterface(SpringRestBackend.class)
@BackendConfigurer(SpringRestBackendConfigurer.class)
@Headers(AuthHeaders.class)
public interface WebFetch {

    @RouteTo("/hello")
    @Get
    SpringRoute<String> hello();

    @RouteTo("/user/{0 name}")
    @Get
    Route<User> userDetails(String name);

    @RouteTo("/user/{0}?context={1}")
    @Get
    Route<User> userDetails(String name, String context);

    @RouteTo("/user")
    @Post
    SpringRoute<Void> registerUser(@Body User user);
}
