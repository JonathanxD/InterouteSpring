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
package com.github.jonathanxd.interoute.spring.util;

import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;
import com.github.jonathanxd.interoute.backend.def.DefaultBackendDestination;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteGenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.route.Destination;
import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.route.Router;
import com.github.jonathanxd.interoute.route.SuppliedExecutorRoute;
import com.github.jonathanxd.interoute.spring.SpringRestBackendConfiguration;
import com.github.jonathanxd.interoute.spring.annotation.Body;
import com.github.jonathanxd.interoute.spring.destination.UrlDestination;
import com.github.jonathanxd.interoute.spring.route.SpringRoute;
import com.github.jonathanxd.interoute.spring.route.SuppliedExecutorSpringRoute;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpringRestBackendUtil {
    /**
     * Invokes {@link SpringRestBackendConfiguration#getRestTemplate()}.
     *
     * @return Rest template instance.
     */
    public static Instruction invokeRestTemplate(Instruction restTemplate,
                                                 Instruction url,
                                                 Instruction origin,
                                                 Instruction body,
                                                 HttpMethod httpMethod,
                                                 Type responseType,
                                                 ClassLoader loader) {
        return InvocationFactory.invokeVirtual(
                RestTemplate.class,
                restTemplate,
                "exchange",
                Factories.typeSpec(ResponseEntity.class, String.class, HttpMethod.class, HttpEntity.class, ParameterizedTypeReference.class, Map.class),
                Collections3.listOf(
                        url,
                        Factories.accessStaticField(HttpMethod.class, HttpMethod.class, httpMethod.name()),
                        SpringRestBackendUtil.createHttpEntity(origin, body),
                        SpringRestBackendUtil.createParameterizedTypeReference(responseType, loader),
                        InvocationFactory.invokeStatic(Collections.class, "emptyMap", Factories.typeSpec(Map.class), Collections.emptyList())
                )
        );
    }

    public static Instruction responseEntityToValue(Instruction responseEntity,
                                                    Type responseType) {
        if (ImplicitKoresType.is(responseType, ResponseEntity.class)) {
            return responseEntity;
        } else {
            return InvocationFactory.invokeVirtual(
                    ResponseEntity.class,
                    responseEntity,
                    "getBody",
                    Factories.typeSpec(Object.class),
                    Collections.emptyList()
            );
        }
    }

    public static Instruction createHttpEntity(Instruction origin,
                                               Instruction body) {
        if (body == null) {
            return InvocationFactory.invokeConstructor(HttpEntity.class,
                    Factories.constructorTypeSpec(MultiValueMap.class),
                    Collections3.listOf(SpringRestBackendUtil.getHttpHeaders(origin))
            );
        } else {
            return InvocationFactory.invokeConstructor(HttpEntity.class,
                    Factories.constructorTypeSpec(Object.class, MultiValueMap.class),
                    Collections3.listOf(body, SpringRestBackendUtil.getHttpHeaders(origin))
            );
        }
    }

    /**
     * Invoke constructor of type reference class created by {@link TypeReferenceClass} generator.
     *
     * @param responseType Type.
     * @param loader       Class loader to load type reference class.
     * @return Invocation of constructor of type reference helper.
     */
    public static Instruction createParameterizedTypeReference(Type responseType,
                                                               ClassLoader loader) {
        if (KoresTypes.getAsGeneric(responseType).isWildcard()) {
            return InvocationFactory.invokeConstructor(TypeReferenceClass.createTypeReferenceClass(Object.class, loader));
        } else {
            return InvocationFactory.invokeConstructor(TypeReferenceClass.createTypeReferenceClass(responseType, loader));
        }
    }

    /**
     * Invokes {@link SpringRestBackendConfiguration#getRestTemplate()}.
     *
     * @return Rest template instance.
     */
    public static Instruction getRestTemplate() {
        return InvocationFactory.invoke(
                InvokeType.INVOKE_VIRTUAL,
                SpringRestBackendConfiguration.class,
                SpringRestBackendUtil.invokeGetterAsSpringBackendConfiguration(),
                "getRestTemplate",
                Factories.typeSpec(RestTemplate.class),
                Collections.emptyList()
        );
    }

    /**
     * Invokes {@link SpringRestBackendConfiguration#getHttpHeaders(Origin)}.
     *
     * @return Rest template instance.
     */
    public static Instruction getHttpHeaders(Instruction origin) {
        return Factories.cast(Object.class, HttpHeaders.class,
                SpringRestBackendUtil.invokeOptionalGetOrElse(InvocationFactory.invoke(
                        InvokeType.INVOKE_VIRTUAL,
                        SpringRestBackendConfiguration.class,
                        SpringRestBackendUtil.invokeGetterAsSpringBackendConfiguration(),
                        "getHttpHeaders",
                        Factories.typeSpec(Optional.class, Origin.class),
                        Collections.singletonList(origin)
                ), InvocationFactory.invokeConstructor(HttpHeaders.class))
        );
    }

    /**
     * Invokes {@link Router#getBackend()} and cast to {@link SpringRestBackendConfiguration}.
     *
     * @return Invocation of {@link Router#getBackend()} casted to {@link
     * SpringRestBackendConfiguration}.
     */
    public static Instruction invokeGetterAsSpringBackendConfiguration() {
        return Factories.cast(
                InterouteBackendConfiguration.class,
                SpringRestBackendConfiguration.class,
                GenerationUtil
                        .invokeOptionalGet(GenerationUtil.invokeOptionalGetter(GenerationUtil.backendConfigurationProperty()))
        );
    }

    /**
     * Creates instruction that constructs {@link DefaultBackendDestination}.
     *
     * @param destinationInstance Root destination instance.
     * @param destinationMethod   Destination method.
     * @return Instruction that constructs {@link DefaultBackendDestination}.
     */
    public static Instruction createDestination(Instruction destinationInstance,
                                                MethodTypeSpec destinationMethod) {
        return InvocationFactory.invokeConstructor(
                DefaultBackendDestination.class,
                Factories.constructorTypeSpec(Object.class, MethodTypeSpec.class),
                Collections3.listOf(destinationInstance, GenerationUtil.createMethodTypeSpec(destinationMethod))
        );
    }

    /**
     * Gets the request body from {@code routeSpec}.
     *
     * @param routeSpec Specification of routing.
     * @return Request body, if present.
     */
    public static Optional<Instruction> getRequestBody(RouteSpec routeSpec) {
        return routeSpec.getOriginParameterSpecs().stream()
                .filter(it -> it.getRouteSpecInfos().stream().anyMatch(it2 -> it2.getAnnotationType() == Body.class))
                .map(it -> ConversionsKt.getAccess(it.getParameter()))
                .findFirst();
    }

    /**
     * Gets the result type from origin method return type.
     *
     * @param returnType Origin method return type.
     * @return Result type.
     */
    public static Type getResultType(Type returnType) {
        return Optional.of(KoresTypes.getAsGeneric(returnType))
                .filter(it -> !ImplicitKoresType.is(it.getResolvedType(), Void.TYPE))
                .filter(it -> it.getBounds().length != 0)
                .<Type>map(it -> it.getBounds()[0].getType())
                .orElse(Void.TYPE);
    }

    /**
     * Creates instruction which invokes {@link Optional#orElse(Object)}
     *
     * @param optionalInstance {@link Optional optional instance}.
     * @param orElse           Alternative value.
     * @return Instruction which invokes {@link Optional#orElse(Object)} .
     */
    public static Instruction invokeOptionalGetOrElse(Instruction optionalInstance, Instruction orElse) {
        return InvocationFactory.invokeVirtual(
                Optional.class,
                optionalInstance,
                "orElse",
                Factories.typeSpec(Generic.type("T"), Generic.type("T")),
                Collections.singletonList(orElse)
        );
    }

    public static Instruction createExecutorRoute(List<VariableRef> variables,
                                                  Instruction routeTargetInvocation,
                                                  Instruction destination,
                                                  RouteSpec routeSpec) {

        Instruction suppliedExecutorRoute = RouteGenerationUtil.createSuppliedExecutorRoute(
                variables,
                routeTargetInvocation,
                destination,
                routeSpec
        );

        KoresType concreteType = ImplicitKoresType.getConcreteType(routeSpec.getOrigin().getReturnType());

        if (ImplicitKoresType.is(concreteType, SpringRoute.class)) {
            return SpringRestBackendUtil.createSpringSuppliedExecutorRoute(
                    suppliedExecutorRoute,
                    destination,
                    routeSpec
            );
        } else {
            return suppliedExecutorRoute;
        }
    }

    /**
     * Creates the {@link SuppliedExecutorSpringRoute} based on a {@link SuppliedExecutorRoute} to
     * handle http status exceptions.
     *
     * @param suppliedExecutorRoute The normal {@link SuppliedExecutorRoute}.
     * @param destination           {@link Destination} creation or retrieval instruction.
     * @param routeSpec             Route specification.
     * @return {@link SuppliedExecutorRoute} which will be returned by the routing method.
     */
    public static Instruction createSpringSuppliedExecutorRoute(Instruction suppliedExecutorRoute,
                                                                Instruction destination,
                                                                RouteSpec routeSpec) {
        return InvocationFactory.invokeConstructor(
                SuppliedExecutorSpringRoute.class,
                Factories.constructorTypeSpec(Origin.class, Destination.class, SuppliedExecutorRoute.class),
                Collections3.listOf(
                        GenerationUtil.createMethodTypeSpecOrigin(routeSpec),
                        destination,
                        suppliedExecutorRoute
                )
        );
    }

    public static Instruction createUrlDestination(RouteSpec routeSpec) {
        return InvocationFactory.invokeConstructor(
                UrlDestination.class,
                Factories.constructorTypeSpec(String.class),
                Collections3.listOf(Literals.STRING(routeSpec.getDestination()))
        );
    }
}
