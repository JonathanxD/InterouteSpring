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

import com.github.jonathanxd.interoute.annotation.RequiresConfiguration;
import com.github.jonathanxd.interoute.annotation.RouteTo;
import com.github.jonathanxd.interoute.backend.AbstractInterouteBackend;
import com.github.jonathanxd.interoute.exception.GenerationException;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.gen.RouterSpec;
import com.github.jonathanxd.interoute.route.Route;
import com.github.jonathanxd.interoute.spring.notation.UrlNotation;
import com.github.jonathanxd.interoute.spring.route.SpringRoute;
import com.github.jonathanxd.interoute.spring.util.RequestMethodUtil;
import com.github.jonathanxd.interoute.spring.util.SpringRestBackendUtil;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.InstructionsKt;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Concat;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpMethod;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring {@link org.springframework.web.client.RestTemplate} backend.
 *
 * The {@link RouteTo#value() route destination} should be the relative or absolute url. You could
 * use {@code {n}} or {@code {n name}} to refer to a parameter in the annotated method, example:
 *
 * <pre>
 *     {@code
 *      @RouteTo("/user/{0}?tab={1 tab}")
 *      Route<String> user(String name, String tab);
 *      }
 * </pre>
 *
 * The string in front of numeric position of parameter does not have any effect on the behavior of
 * routing logic, you could put anything in front of the numeric value, example: {@code {7tab 5}}:
 * refers to parameter number 7 of the method, {@code {75tab 5}}: refers to parameter number 75 of
 * the method. Anything after a valid number in the start of routing notation is ignored. You could
 * also invoke methods (but only if it does no take any argument), example:
 *
 * <pre>
 *     {@code
 *      @RouteTo("{userServicePath()}/user/{0}?tab={1}")
 *      Route<String> user(String name, String tab);
 *
 *      default String userServicePath() {
 *          return ((SpringRestBackendConfiguration)this.getBackendConfiguration().get()).get("userServicePath");
 *      }
 *
 *      }
 * </pre>
 */
@RequiresConfiguration(SpringRestBackendConfiguration.class)
public class SpringRestBackend extends AbstractInterouteBackend<SpringRestBackendConfiguration> {

    @NotNull
    @Override
    public SpringRestBackendConfiguration createConfiguration() {
        return new SpringRestBackendConfiguration(
                null,
                null,
                Collections.emptyMap());
    }

    @NotNull
    @Override
    public <T> Result<? extends T, GenerationException> generate(@NotNull RouterSpec<SpringRestBackendConfiguration> routerSpec,
                                                                 @Nullable ClassLoader loader) {
        RouterSpec<SpringRestBackendConfiguration> newSpec =
                new RouterSpec<>(routerSpec.getRouterInterface(),
                        routerSpec.getBackend(),
                        new SpringRestCLBackendConfiguration(routerSpec.getConfiguration(), loader),
                        routerSpec.getRouteSpecList(),
                        routerSpec.getRouteSpecInfoList());

        return super.generate(newSpec, loader);
    }

    @Override
    protected Result<Instruction, GenerationException> route(RouteSpec routeSpec, SpringRestBackendConfiguration configuration) {
        return UrlNotation
                .parse(routeSpec.getDestination())
                .flatMap(
                        notation -> this.generateRouteInstanceWithInvocation(routeSpec, notation,
                                ((SpringRestCLBackendConfiguration) configuration).getClassLoader()),
                        destinationParseError -> Result.error(new GenerationException(destinationParseError))
                );
    }

    private Result<Instruction, GenerationException> routeTargetInstruction(RouteSpec routeSpec,
                                                                            UrlNotation destinationNotation,
                                                                            ClassLoader classLoader) {
        return Result.<Instruction, GenerationException>ok(new Concat(
                destinationNotation.getNotationList()
                        .stream()
                        .map(it -> it.resolve(routeSpec))
                        .collect(Collectors.toList())
        )).map(concat ->
                SpringRestBackendUtil.invokeRestTemplate(
                        SpringRestBackendUtil.getRestTemplate(),
                        concat,
                        GenerationUtil.createMethodTypeSpecOrigin(routeSpec),
                        SpringRestBackendUtil.getRequestBody(routeSpec).orElse(Literals.NULL),
                        RequestMethodUtil.getRequestMethod(routeSpec.getRouteSpecInfoList()).orElse(HttpMethod.GET),
                        SpringRestBackendUtil.getResultType(routeSpec.getOrigin().getReturnType()),
                        classLoader
                )
        ).flatMap(responseEntity -> this.transformResponseInvocation(routeSpec, responseEntity));

    }

    private Result<Instruction, GenerationException> transformResponseInvocation(RouteSpec routeSpec,
                                                                                 Instruction responseEntity) {
        KoresType concreteType = ImplicitKoresType.getConcreteType(routeSpec.getOrigin().getReturnType());
        Type leaveType = InstructionsKt.getLeaveType(responseEntity);
        Type resultType = SpringRestBackendUtil.getResultType(routeSpec.getOrigin().getReturnType());
        KoresType concreteResultType = ImplicitKoresType.getConcreteType(resultType);

        boolean sameReturnType = leaveType != null &&
                ImplicitKoresType.is(
                        ImplicitKoresType.getConcreteType(leaveType),
                        concreteResultType
                );

        boolean springRoute = ImplicitKoresType.is(concreteType, SpringRoute.class);

        if (springRoute || sameReturnType) {
            return Result.ok(responseEntity);
        } else {
            return Result.ok(SpringRestBackendUtil.responseEntityToValue(responseEntity, resultType));
        }
    }

    private Result<Instruction, GenerationException> generateRouteInstanceWithInvocation(RouteSpec routeSpec,
                                                                                         UrlNotation destinationNotation,
                                                                                         ClassLoader classLoader) {
        return this.routeTargetInstruction(routeSpec, destinationNotation, classLoader)
                .map(instruction ->
                        this.generateRouteCreationInstruction(
                                instruction,
                                SpringRestBackendUtil.createUrlDestination(routeSpec),
                                routeSpec
                        )
                )
                .map(instruction -> {
                    if (ImplicitKoresType.is(GenerationUtil.getRouteOriginReturnType(routeSpec), Types.VOID)) {
                        return GenerationUtil.executeRouteAndWait(instruction);
                    } else {
                        return Factories.returnValue(Route.class, instruction);
                    }
                });
    }

    private Instruction generateRouteCreationInstruction(Instruction routeTargetInvocation,
                                                         Instruction destination,
                                                         RouteSpec routeSpec) {
        return SpringRestBackendUtil.createExecutorRoute(
                this.getVariables(routeSpec),
                routeTargetInvocation,
                destination,
                routeSpec
        );
    }

    private List<VariableRef> getVariables(RouteSpec routeSpec) {
        return GenerationUtil.getRouteOriginParameters(routeSpec).stream()
                .map(p -> new VariableRef(p.getType(), p.getName()))
                .collect(Collectors.toList());
    }

}
