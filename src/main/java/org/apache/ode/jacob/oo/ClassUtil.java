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

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.Expression;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.soup.CommChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ClassUtil {
    public static final Method RUN_METHOD;
    public static final String RUN_METHOD_NAME = "run";
    public static final String RUN_METHOD_ACTION = "java.lang.Runnable#run";
    public static final String SYNCH_RET_METHOD_ACTION = "org.apache.ode.jacob.oo.Synch#ret";
    private static final Set<Method> RUN_METHOD_SET = new HashSet<Method>();
    private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);

    static {
        try {
            // Resolve the {@link Runnable#run} method once statically
            RUN_METHOD = Runnable.class.getMethod(RUN_METHOD_NAME, new Class[]{});
            RUN_METHOD_SET.add(RUN_METHOD);
        } catch (Exception e) {
            throw new Error("Cannot resolve 'run()' method", e);
        }
    }

    private ClassUtil() {
        // Utility class
    }

    public static Set<Method> runMethodSet() {
        return RUN_METHOD_SET;
    }

    public static Message createMessage(Runnable target, String action, Object[] args, Channel replyTo) {
        Message message = new Message();
        message.setTo(new ChannelRef(target));
        message.setReplyTo(replyTo == null ? null : new ChannelRef(replyTo));
        message.setAction(action);
        message.setBody(args);
        return message;
    }

    public static Message createMessage(CommChannel target, String action, Object[] args, CommChannel replyTo) {
        Message message = new Message();
        message.setTo(new ChannelRef(target));
        message.setReplyTo(replyTo == null ? null : new ChannelRef(replyTo));
        message.setAction(action);
        message.setBody(args);
        return message;
    }

    public static String getActionForMethod(Method channelMethod) {
        if (channelMethod == null) {
            return null;
        }
        MessageHandler handler = channelMethod.getAnnotation(MessageHandler.class);
        if (handler != null) {
            return handler.value();
        }
        Class<?> clazz = channelMethod.getDeclaringClass();
        if (Runnable.class.isAssignableFrom(clazz) 
            && channelMethod.getName() == "run"
            && channelMethod.getParameterTypes().length == 0) {
            return RUN_METHOD_ACTION;
        }
        if (!Channel.class.isAssignableFrom(clazz)) {
            LOG.trace("Action '{}' can only be defined for a Channel extension", channelMethod.getName());
            return null;
        }
        LOG.trace("Probing Channel class '{}' for declaration of method '{}'", clazz.getName(), channelMethod.getName());
        for (Class<?> c : clazz.getInterfaces()) {
            String action = getChannelMethodAction(c, channelMethod);
            if (action != null) {
                return action;
            }
        }
        // ... if clazz is a Channel interface itself 
        return getChannelMethodAction(clazz, channelMethod);
    }

    /**
     * @param clazz
     * @param method
     * @return
     * 
     * The default action associated with a Channel method is defined as Class#method
     * following a convention similar to javadoc.
     */
    private static String getChannelMethodAction(Class<?> clazz, Method method) {
        if (Channel.class.isAssignableFrom(clazz)) {
            try {
                Method m = clazz.getMethod(method.getName(), method.getParameterTypes());
                return m.getDeclaringClass().getName() + "#" + m.getName();
            } catch (SecurityException e) {        // ignore
            } catch (NoSuchMethodException e) {    // ignore
            }
        }
        return null;
    }

    public static Expression findActionMethod(final Set<Method> implementedMethods) {
        return new Expression() {
            @SuppressWarnings("unchecked")
            public <T> T evaluate(Message message, Class<T> type) {
                String action = message.getAction();
                if (Method.class.equals(type) && action != null) {
                    for (Method m : implementedMethods) {
                        if (action.equals(ClassUtil.getActionForMethod(m))) {
                            return (T)m;
                        }
                    }
                }
                return null;
            }
        };
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
