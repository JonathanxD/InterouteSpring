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
import com.github.jonathanxd.iutils.object.result.Result;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public final class RouteRequestResponse<R> {

    private final ResponseEntity<R> responseEntity;
    private final RoutingException routingException;

    public RouteRequestResponse(ResponseEntity<R> responseEntity,
                                RoutingException routingException) {
        this.responseEntity = responseEntity;
        this.routingException = routingException;
    }

    public HttpStatus getStatus() {
        return this.getResponseEntity().getStatusCode();
    }

    public int getStatusCode() {
        return this.getResponseEntity().getStatusCodeValue();
    }

    public Optional<R> getResult() {
        return Optional.ofNullable(this.getResponseEntity().getBody());
    }

    public Result<R, HttpStatus> getAsResult() {
        if (this.getStatus().is2xxSuccessful()) {
            return Result.ok(this.getResult().orElse(null));
        } else {
            return Result.error(this.getStatus());
        }
    }

    public Result<R, RoutingException> getResponseAsResult() {
        return this.getResult()
                .<Result<R, RoutingException>>map(Result::ok)
                .orElseGet(() -> Result.error(this.getRoutingException()));
    }

    public Result<HttpStatus, RoutingException> getStatusAsResult() {
        return Result.ok(this.getResponseEntity().getStatusCode());
    }

    public ResponseEntity<R> getResponseEntity() {
        return this.responseEntity;
    }

    public RoutingException getRoutingException() {
        return this.routingException;
    }
}
