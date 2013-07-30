/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jacob.oo;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.ode.jacob.MessageType;


public final class ClassUtil {
    public static final Method RUN_METHOD;
    private static final Set<Method> RUN_METHOD_SET = new HashSet<Method>();

    static {
        try {
            // Resolve the {@link Runnable#run} method once statically
            RUN_METHOD = Runnable.class.getMethod("run", new Class[]{});
            RUN_METHOD_SET.add(RUN_METHOD);
        } catch (Exception e) {
            throw new Error("Cannot resolve 'run()' method", e);
        }
    }

    private ClassUtil() {
        // Utility class
    }

    public static Class<? extends MessageType> getMessageType(Method channelMethod) {
    	MessageHandler handler = channelMethod.getAnnotation(MessageHandler.class);
    	return handler == null ? null : handler.value();
    }
    public static Set<Method> runMethodSet() {
    	return RUN_METHOD_SET;
    }

    public static Set<Method> getImplementedMethods(Set<Method> methods, Class<?> clazz) {
        // TODO: this can be optimized (some 20 times faster in my tests) by keeping a private 
        //  map of interfaces to methods: Map<Class<?>, Method[]> and just do lookups
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> iface : interfaces) {
            // TODO: the test below could be more generic...
            if (iface != ChannelProxy.class) {
                for (Method method : iface.getDeclaredMethods()) {
                    methods.add(method);
                }
                getImplementedMethods(methods, iface);
            }
        }
        return methods;
    }
}
