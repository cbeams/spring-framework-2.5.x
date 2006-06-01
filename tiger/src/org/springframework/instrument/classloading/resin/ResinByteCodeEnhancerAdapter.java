/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.instrument.classloading.resin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;

import com.caucho.loader.enhancer.ByteCodeEnhancer;

/**
 * Adapter plugging a ClassFileTransformer with Resin's ByteCodeEnhancer.
 * 
 * @author Costin Leau
 * 
 */
public class ResinByteCodeEnhancerAdapter implements ByteCodeEnhancer {

	private ClassFileTransformer transformer;

	public ResinByteCodeEnhancerAdapter(ClassFileTransformer transformer) {
		this.transformer = transformer;
	}

	public byte[] enhance(String className, byte[] buffer, int offset, int length) {
		try {
			byte[] result = transformer.transform(null, className, null, null, buffer);
			return (result == null ? buffer : result);
		}
		catch (IllegalClassFormatException ex) {
			throw new RuntimeException("Cannot transform", ex);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResinByteCodeEnhancerAdapter for:");
		builder.append(transformer);
		return builder.toString();
	}
}
