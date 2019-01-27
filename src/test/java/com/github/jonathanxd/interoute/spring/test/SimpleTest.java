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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jonathanxd.interoute.exception.RoutingException;
import com.github.jonathanxd.interoute.spring.route.RouteRequestResponse;
import com.github.jonathanxd.interoute.spring.test.repo.UserRepository;
import com.github.jonathanxd.iutils.object.result.Result;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InterouteConfig.class)
@SpringBootConfiguration
public class SimpleTest {

    private final String root = "http://test:80/";

    @Autowired
    private WebFetch webFetch;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void configure() {
        this.userRepository.register(new User("test", "test@domain.com"));
        this.userRepository.register(new User("test2", "test2@domain.com"));
    }

    @Test
    public void webFetchTest() throws ExecutionException, InterruptedException {
        SpringRestBackendConfigurer.setup("http://test:80/",
                this.template("Hello world", HttpStatus.OK)
        );
        Result<String, RoutingException> result =
                webFetch.hello()
                        .execute()
                        .get()
                        .map(RouteRequestResponse::getResult)
                        .map(Optional::get);

        if (result.isError()) {
            throw result.errorOrGet(() -> null);
        } else {
            Result.Ok<String, ?> ok = ((Result.Ok<String, ?>) result);
            Assert.assertEquals("Hello world", ok.success());
        }
    }

    @Test
    public void webFetchUserDetailsTest() throws ExecutionException, InterruptedException {
        SpringRestBackendConfigurer.setup("http://test:80/",
                this.template(new User("test", "test@domain.com"), HttpStatus.OK)
        );
        Result<User, RoutingException> result = webFetch.userDetails("test").execute().get();
        User user = this.expectResult(result);

        Assert.assertEquals("test", user.getName());
        Assert.assertEquals("test@domain.com", user.getEmail());
    }

    @Test
    public void webFetchUserDetailsWithRepoTest() throws ExecutionException, InterruptedException {
        SpringRestBackendConfigurer.setup(this.root,
                this.userReqTemplate((request, body, execution) -> {
                    Assert.assertEquals("/user/test", request.getURI().getPath());
                    return execution.execute(request, body);
                })
        );

        Result<User, RoutingException> result = webFetch.userDetails("test").execute().get();
        User user = this.expectResult(result);

        Assert.assertEquals("test", user.getName());
        Assert.assertEquals("test@domain.com", user.getEmail());
    }

    @Test
    public void webFetchRegisterUserTest() throws ExecutionException, InterruptedException {
        SpringRestBackendConfigurer.setup(this.root,
                this.userReqTemplate((request, body, execution) -> {
                    Assert.assertEquals("/user", request.getURI().getPath());
                    return execution.execute(request, body);
                })
        );

        Result<HttpStatus, RoutingException> result =
                webFetch.registerUser(new User("test3", "test3@domain.com"))
                        .execute()
                        .get()
                        .flatMap(RouteRequestResponse::getStatusAsResult);

        HttpStatus status = this.expectResult(result);

        Assert.assertEquals(HttpStatus.OK, status);
    }

    @Test
    public void webFetchRegisterExistingUserTest() throws ExecutionException, InterruptedException {
        SpringRestBackendConfigurer.setup(this.root,
                this.userReqTemplate((request, body, execution) -> {
                    Assert.assertEquals("/user", request.getURI().getPath());
                    return execution.execute(request, body);
                })
        );

        Result<HttpStatus, RoutingException> result =
                webFetch.registerUser(new User("test2", "test2@domain.com"))
                        .execute()
                        .get()
                        .flatMap(RouteRequestResponse::getStatusAsResult);

        HttpStatus status = this.expectResult(result);

        Assert.assertEquals(HttpStatus.CONFLICT, status);
    }

    private <R, E extends RoutingException> R expectResult(Result<R, E> result) {
        if (result.isError()) {
            throw new RuntimeException(result.errorOrGet(() -> null));
        } else {
            return ((Result.Ok<R, E>) result).success();
        }
    }

    RestTemplate template(String resultBody, HttpStatus status) {
        return new RestTemplateBuilder()
                .interceptors((request, body, execution) ->
                        new MockClientHttpResponse(resultBody.getBytes(StandardCharsets.UTF_8), status))
                .build();
    }

    <T> RestTemplate template(T resultBody, HttpStatus status) {
        return new RestTemplateBuilder()
                .interceptors((request, body, execution) -> this.jsonResponse(resultBody, status))
                .build();
    }

    <T> RestTemplate userReqTemplate() {
        return userReqTemplate((request, body, execution) -> execution.execute(request, body));
    }

    <T> RestTemplate userReqTemplate(ClientHttpRequestInterceptor assertionsInterceptor) {
        return new RestTemplateBuilder()
                .interceptors(assertionsInterceptor, (request, body, execution) -> {
                    if (request.getMethod() == HttpMethod.POST) {
                        if (this.userRepository.register(this.as(body, User.class))) {
                            return this.statusResponse(HttpStatus.OK);
                        } else {
                            return this.statusResponse(HttpStatus.CONFLICT);
                        }
                    } else {
                        String name = this.extractUserName(request.getURI());
                        Optional<User> byName = this.userRepository.findByName(name);
                        if (byName.isPresent()) {
                            return this.jsonResponse(byName.get(), HttpStatus.OK);
                        } else {
                            return this.statusResponse(HttpStatus.CONFLICT);
                        }
                    }

                })
                .build();
    }

    public <T> T as(byte[] bytes, Class<T> type) throws IOException {
        return new ObjectMapper().readValue(bytes, type);
    }

    private ClientHttpResponse jsonResponse(Object body, HttpStatus status) throws JsonProcessingException {
        MockClientHttpResponse response =
                new MockClientHttpResponse(new ObjectMapper().writeValueAsBytes(body), status);
        response.getHeaders().set("Content-Type", "application/json");
        return response;
    }

    private ClientHttpResponse statusResponse(HttpStatus status) throws JsonProcessingException {
        return new MockClientHttpResponse(new ObjectMapper().writeValueAsBytes(null), status);
    }

    private String extractUserName(URI uri) {
        String path = uri.getPath();
        return path.substring("/user/".length());
    }
}
