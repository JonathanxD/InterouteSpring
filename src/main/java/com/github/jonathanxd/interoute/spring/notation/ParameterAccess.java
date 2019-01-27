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
package com.github.jonathanxd.interoute.spring.notation;

import com.github.jonathanxd.interoute.exception.DestinationParseException;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.helper.Predefined;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParameterAccess extends UrlPathNotation {
    private static final Pattern ACCESS_PATTERN = Pattern.compile("([0-9]+).*");
    private final int parameterPos;

    ParameterAccess(int parameterPos) {
        this.parameterPos = parameterPos;
    }

    public static Result<ParameterAccess, DestinationParseException> parseParameterAccess(String accessPart) {
        Matcher matcher = ACCESS_PATTERN.matcher(accessPart);
        if (!matcher.matches()) {
            return Result.error(new DestinationParseException(
                    String.format("Cannot parse parameter access, malformed notation: %s", accessPart)));
        } else {
            return Result
                    .Try(() -> Integer.parseInt(matcher.group(1)))
                    .map(ParameterAccess::new)
                    .mapError(DestinationParseException::new);
        }
    }

    public int getParameterPos() {
        return this.parameterPos;
    }

    @Override
    public Instruction resolve(RouteSpec routeSpec) {
        KoresParameter parameter = routeSpec.getOrigin().getParameters().get(this.getParameterPos());
        Instruction access = Factories.accessVariable(
                parameter.getType(),
                parameter.getName()
        );
        if (!ImplicitKoresType.is(parameter.getType(), String.class)) {
            return InvocationFactory.invokeStatic(
                    Objects.class,
                    "toString",
                    Factories.typeSpec(String.class, Object.class),
                    Collections.singletonList(access)
            );
        } else {
            return access;
        }
    }
}
