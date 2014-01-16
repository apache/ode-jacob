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
package org.apache.ode.jacob.vpu;

import static org.apache.ode.jacob.Jacob.newCommChannel;
import static org.junit.Assert.*;

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.oo.Synch;
import org.apache.ode.jacob.oo.Val;
import org.apache.ode.jacob.soup.CommChannel;
import org.junit.Test;

public class ChannelRefTest {

    @Test
    public void testConnectWithInterface() {
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl());

        vpu.inject(new RunnableProcess() {
            @Override
            public void run() {
                ChannelRef cref = newCommChannel("unbound channel");
                CommChannel cchannel = cref.getEndpoint(CommChannel.class);
                assertNotNull(cchannel);
                assertNull(cchannel.getType());

                // now connect it to Val.class
                Val val = cref.getEndpoint(Val.class);
                assertNotNull(val);
                assertEquals(Val.class, cchannel.getType());

                // now try to associate it with a different channel interface
                try {
                    cref.getEndpoint(Synch.class);
                    fail("we should get an IllegalStateException");
                } catch (IllegalStateException e) {
                    assertEquals("ChannelRef is already associated with a channel of a different type", e.getMessage());
                }

                // now try to associate with the same channel
                Val val2 = cref.getEndpoint(Val.class);
                assertNotNull(val2);
                assertSame(val, val2);
            }
        });

        assertEquals(true, vpu.getContext().hasReactions());
        vpu.execute();
        assertEquals(false, vpu.getContext().hasReactions());
    }

}
