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

import org.junit.Assert;
import org.junit.Test;


public class JacobOOMappingTest {

    @Test
    public void testProcessAction() {
        String action = ClassUtil.getActionForMethod(methodOf(TestProcess.class, "run"));
        Assert.assertEquals("java.lang.Runnable#run", action);
    }


    @Test
    public void testDefaultChannelAction() {
        String action = ClassUtil.getActionForMethod(methodOf(TestChannel.class, "one"));
        Assert.assertEquals("org.apache.ode.jacob.oo.JacobOOMappingTest$TestChannel#one", action);
    }

    @Test
    public void testCustomChannelAction() {
        String action = ClassUtil.getActionForMethod(methodOf(TestChannel.class, "two"));
        Assert.assertEquals("TestChannel-custom", action);
    }

    private final Method methodOf(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name, new Class[]{});
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static interface TestChannel extends Channel {
        void one();
        @MessageHandler("TestChannel-custom") void two();
    }

    public static class TestProcess implements Runnable {
        public void run() {
            // do nothing
        }
    }

}
