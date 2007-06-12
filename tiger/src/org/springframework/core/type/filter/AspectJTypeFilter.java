/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.core.type.filter;

import java.io.IOException;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.patterns.Bindings;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.SimpleScope;
import org.aspectj.weaver.patterns.TypePattern;

import org.objectweb.asm.ClassReader;

import org.springframework.core.type.asm.ClassMetadataReadingVisitor;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * Type filter that uses AspectJ type pattern for matching.
 * 
 * A critical implementation details of this type fitler is that it does not
 * load the class being examined to match with a type pattern.
 * 
 * @author Ramnivas Laddad
 * 
 */
public class AspectJTypeFilter implements TypeFilter {
    // TODO: Keep a soft reference to the world? Or make it an instance variable? Offer API to clear the world? Or all these?
    private static final World world = new BcelWorld(ClassUtils.getDefaultClassLoader(), IMessageHandler.THROW, null);

    static {
    	world.setBehaveInJava5Way(true);
    }
    
    private TypePattern typePattern;

    public AspectJTypeFilter(String typePatternExpression) {
        PatternParser patternParser = new PatternParser(typePatternExpression);
        typePattern = patternParser.parseTypePattern();
        typePattern.resolve(world);
        IScope scope = new SimpleScope(world, new FormalBinding[0]);
        typePattern = typePattern.resolveBindings(scope, Bindings.NONE, false, false);
    }

    public boolean match(ClassReader classReader, ClassReaderFactory classReaderFactory) throws IOException {
        ClassMetadataReadingVisitor typesReadingVisitor = new ClassMetadataReadingVisitor();
        classReader.accept(typesReadingVisitor, true);
        String className = typesReadingVisitor.getClassName();

        ResolvedType resolvedType = world.resolve(className);

        return typePattern.matchesStatically(resolvedType);
    }
}
