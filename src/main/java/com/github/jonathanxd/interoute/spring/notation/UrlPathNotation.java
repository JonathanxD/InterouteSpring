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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class UrlPathNotation {

    public static List<Result<? extends UrlPathNotation, DestinationParseException>> parse(String notation) {
        List<Result<? extends UrlPathNotation, DestinationParseException>> parsed = new ArrayList<>();
        char[] chars = notation.toCharArray();

        StringBuilder plainBuilder = new StringBuilder();
        Type type = Type.PLAIN;

        boolean lastEscape = false;

        for (char aChar : chars) {
            if (lastEscape) {
                lastEscape = false;
                plainBuilder.append(aChar);
            } else if (aChar == '\\') {
                lastEscape = true;
            } else if (aChar == '{') {
                UrlPathNotation.build(plainBuilder, type).ifPresent(parsed::add);
                type = Type.PARAMETER_ACCESS;
            } else if ((aChar == '(' || aChar == ')') && type != Type.PLAIN) {
                type = Type.METHOD_INVOKE;
            } else if (aChar == '}' && type != Type.PLAIN) {
                UrlPathNotation.build(plainBuilder, type).ifPresent(parsed::add);
                type = Type.PLAIN;
            } else {
                plainBuilder.append(aChar);
            }
        }

        UrlPathNotation.build(plainBuilder, type).ifPresent(parsed::add);

        return parsed;
    }

    private static Optional<Result<? extends UrlPathNotation, DestinationParseException>> build(StringBuilder sb, Type type) {
        if (sb.length() != 0) {
            String notation = sb.toString();
            sb.setLength(0);
            switch (type) {
                case PLAIN:
                    return Optional.of(Plain.parsePlain(notation));
                case METHOD_INVOKE:
                    return Optional.of(MethodInvocation.parseMethodInvocation(notation));
                case PARAMETER_ACCESS:
                    return Optional.of(ParameterAccess.parseParameterAccess(notation));
                default:
                    return Optional.of(Result.error(new DestinationParseException(
                            String.format("Could not build notation path of type '%s'. Notation: %s", type.name(), notation))));
            }
        } else {
            return Optional.empty();
        }


    }

    public abstract Instruction resolve(RouteSpec routeSpec);

    enum Type {
        PLAIN,
        METHOD_INVOKE,
        PARAMETER_ACCESS
    }
}
