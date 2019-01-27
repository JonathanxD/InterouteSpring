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
package com.github.jonathanxd.interoute.spring.route;

import com.github.jonathanxd.interoute.exception.RoutingException;
import com.github.jonathanxd.interoute.route.AbstractRoute;
import com.github.jonathanxd.interoute.route.Destination;
import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.route.SuppliedExecutorRoute;
import com.github.jonathanxd.iutils.object.result.Result;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.concurrent.CompletableFuture;

public class SuppliedExecutorSpringRoute<R> extends AbstractRoute<RouteRequestResponse<R>> implements SpringRoute<R> {
    private final SuppliedExecutorRoute<ResponseEntity<R>> executorRoute;

    public SuppliedExecutorSpringRoute(
            @NotNull Origin origin,
            @NotNull Destination destination,
            @NotNull SuppliedExecutorRoute<ResponseEntity<R>> executorRoute) {
        super(origin, destination);
        this.executorRoute = executorRoute;
    }

    @Override
    public CompletableFuture<Result<RouteRequestResponse<R>, RoutingException>> execute() {
        return this.executorRoute.execute().thenApply(rRoutingExceptionResult ->
                rRoutingExceptionResult.flatMap(
                        this::transformResult,
                        this::transformException
                ));
    }

    private Result<RouteRequestResponse<R>, RoutingException> transformResult(ResponseEntity<R> result) {
        return Result.ok(new RouteRequestResponse<>(result, null));
    }

    private Result<RouteRequestResponse<R>, RoutingException> transformException(RoutingException routingException) {
        Throwable cause = routingException.getCause();
        if (cause instanceof HttpStatusCodeException) {
            HttpStatusCodeException statusCodeException = (HttpStatusCodeException) cause;
            return Result.ok(new RouteRequestResponse<>(new ResponseEntity<>(
                    null,
                    statusCodeException.getResponseHeaders(),
                    statusCodeException.getStatusCode()
            ), routingException));
        } else {
            return Result.error(routingException);
        }
    }
}
