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

package org.springframework.remoting.caucho;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;

/**
 * Concrete HessianSkeletonInvoker for the Hessian 2 protocol
 * (version 3.0.20 or higher).
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
class Hessian2SkeletonInvoker extends HessianSkeletonInvoker {

	public Hessian2SkeletonInvoker(HessianSkeleton skeleton, SerializerFactory serializerFactory) {
		super(skeleton, serializerFactory);
	}

	public void invoke(InputStream inputStream, OutputStream outputStream) throws Throwable {
		Hessian2Input in = new Hessian2Input(inputStream);
		if (this.serializerFactory != null) {
			in.setSerializerFactory(this.serializerFactory);
		}

		int code = in.read();
		if (code != 'c') {
			throw new IOException("expected 'c' in hessian input at " + code);
		}

		AbstractHessianOutput out = null;
		int major = in.read();
		int minor = in.read();
		if (major >= 2) {
			out = new Hessian2Output(outputStream);
		}
		else {
			out = new HessianOutput(outputStream);
		}
		if (this.serializerFactory != null) {
			out.setSerializerFactory(this.serializerFactory);
		}

		this.skeleton.invoke(in, out);
	}

}
