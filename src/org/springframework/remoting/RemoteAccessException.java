/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.remoting;

import org.springframework.core.NestedRuntimeException;

/**
 * Generic remote access exception. A service proxy for any remoting
 * protocol and toolkit should throw this exception or subclasses of it,
 * to be able to transparently expose a plain Java business interface.
 *
 * <p>When using conforming proxies, switching the actual remoting toolkit
 * e.g. from Hessian to Burlap does not affect client code. The latter
 * works with a plain Java business interface that the service exposes.
 * A client object simply receives an implementation for the interface that
 * it needs via a bean reference, like it does for local beans too.
 *
 * <p>A client can catch RemoteAccessException if it wants too, but as
 * remote access errors are typically unrecoverable, it will probably let
 * such exceptions propagate to a higher level that handles them generically.
 * In this case, the client code doesn't show any signs of being involved in
 * remote access, as there aren't any remoting-specific dependencies.
 *
 * <p>Even when switching from a remote service proxy to a local implementation
 * of the same interface, this amounts to just a matter of configuration.
 * Obviously, the client code should be somewhat aware that it _could work_
 * on a remote service, for example in terms of repeated method calls that
 * cause unnecessary roundtrips etc. But it doesn't have to be aware whether
 * it <i>actually works</i> on a remote service or a local implementation, or
 * with which remoting toolkit under the hood.
 *
 * @author Juergen Hoeller
 * @since 14.05.2003
 */
public class RemoteAccessException extends NestedRuntimeException {

	public RemoteAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
