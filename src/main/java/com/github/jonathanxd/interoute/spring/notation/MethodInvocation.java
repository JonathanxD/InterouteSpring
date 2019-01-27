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
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;

import java.util.Collections;

class MethodInvocation extends UrlPathNotation {

    private final String methodName;

    MethodInvocation(String methodName) {
        this.methodName = methodName;
    }

    public static Result<MethodInvocation, DestinationParseException> parseMethodInvocation(String invocationPart) {
        if (!invocationPart.endsWith("()")) {
            return Result.error(new DestinationParseException(
                    String.format("Missing () in method invocation notation: %s", invocationPart)));
        } else {
            return Result.ok(new MethodInvocation(invocationPart.substring(0, invocationPart.indexOf("("))));
        }
    }

    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public Instruction resolve(RouteSpec routeSpec) {
        return InvocationFactory.invokeInterface(
                Alias.THIS.INSTANCE,
                Access.THIS,
                this.getMethodName(),
                Factories.typeSpec(String.class),
                Collections.emptyList()
        );
    }
}
