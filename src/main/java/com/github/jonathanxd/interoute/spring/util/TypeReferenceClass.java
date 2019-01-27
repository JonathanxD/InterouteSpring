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

import com.github.jonathanxd.interoute.gen.ClassGenerationUtil;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.util.TypeVarUtil;
import com.github.jonathanxd.kores.util.conversion.TypeStructureKt;

import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;

public class TypeReferenceClass {

    private static long COUNT = 0L;

    public static Class<?> createTypeReferenceClass(Type type, ClassLoader loader) {
        long cnt = COUNT++;
        ClassDeclaration classDeclaration = ClassDeclaration.Builder.builder()
                .name("com.github.jonathanxd.interoute.type" + cnt + ".Type")
                .superClass(Generic.type(ParameterizedTypeReference.class).of(type))
                .build();

        return ClassGenerationUtil.load(ClassGenerationUtil.generate(classDeclaration), loader);
    }

}
