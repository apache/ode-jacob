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


import static org.apache.ode.jacob.Jacob.object;
import static org.apache.ode.jacob.oo.ProcessUtil.receive;

import java.util.ArrayList;
import java.util.List;

import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.vpu.ChannelFactory;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.junit.Assert;
import org.junit.Test;


public class JacobChannelsTest {

    @Test
    public void testMultipleSameTypeChannels() {
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl());

        Channel one = vpu.newChannel(Val.class, "");
        Channel two = vpu.newChannel(Val.class, "");
        CommChannel back1 = ChannelFactory.getBackend(one);
        CommChannel back2 = ChannelFactory.getBackend(two);
        Assert.assertEquals(back1.getType(), back2.getType());
        Assert.assertNotEquals(back1.getId(), back2.getId());
    }

    @Test
    @SuppressWarnings("serial")
    public void testMultipleReceiversSameChannel() {
        final JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl());

        final List<String> result = new ArrayList<String>();
        vpu.inject(new RunnableProcess() {
            public void run() {
                Val v = (Val)vpu.newChannel(Val.class, "");
                object(receive(v, new Val() {
                    public void val(Object retVal) {
                        result.add("Hello " + retVal);
                    }
                }));
                object(receive(v, new Val() {
                    public void val(Object retVal) {
                        result.add("Bonjour " + retVal);
                    }
                }));

                v.val("Hadrian");
            }
        });

        while (vpu.execute()) {
            // keep doing it...
        }
        // TODO: although it should probably be two
        //  not really clear what pi calculus says about shared channels
        Assert.assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("serial")
    public void testCompositeReceiver() {
        final JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl());

        final List<String> result = new ArrayList<String>();
        vpu.inject(new RunnableProcess() {
            public void run() {
                Val v = (Val)vpu.newChannel(Val.class, "");
                object(ProcessUtil.compose(receive(v, new Val() {
                    public void val(Object retVal) {
                        result.add("Hello " + retVal);
                    }
                })).or(receive(v, new Val() {
                    public void val(Object retVal) {
                        result.add("Bonjour " + retVal);
                    }
                })));

                v.val("Hadrian");
            }
        });

        while (vpu.execute()) {
            // keep doing it...
        }
        // TODO: although it should probably be two
        //  not really clear what pi calculus says about shared channels
        Assert.assertEquals(1, result.size());
    }

}
