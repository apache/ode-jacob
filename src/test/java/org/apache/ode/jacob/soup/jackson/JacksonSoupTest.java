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
package org.apache.ode.jacob.soup.jackson;

import java.util.ArrayList;
import java.util.List;

import org.apache.ode.jacob.examples.helloworld.HelloWorld;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl;
import org.apache.ode.jacob.soup.jackson.JacobModule;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Simple testcase to verify the jackson based serialization/
 * deserialization code.
 * 
 * @author Tammo van Lessen
 *
 */
public class JacksonSoupTest {
    
    private ObjectMapper mapper;
    private JacksonExecutionQueueImpl queue;
    private List<String> fixtures = new ArrayList<String>();
    
    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JacobModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        queue = new JacksonExecutionQueueImpl();
        
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":1,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess\",\"@id\":1,\"_in\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess\",\"@id\":1,\"in\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2},\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"Hello\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"World\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":false,\"objFrames\":[],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"},{\"@id\":2,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":2,\"refCount\":0,\"replicated\":false,\"objFrames\":[],\"msgFrames\":[],\"description\":\"simpleHelloWorld-x\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":2,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess\",\"@id\":1,\"in\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2},\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"Hello\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"World\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":2,\"commGroupFrame\":{\"@id\":3,\"replicated\":true,\"commFrames\":[2]},\"channelFrame\":1,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":4,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"},{\"@id\":5,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":2,\"refCount\":0,\"replicated\":false,\"objFrames\":[],\"msgFrames\":[],\"description\":\"simpleHelloWorld-x\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":3,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"Hello\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"World\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":2,\"commGroupFrame\":{\"@id\":3,\"replicated\":true,\"commFrames\":[2]},\"channelFrame\":1,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":4,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"},{\"@id\":5,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":2,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":6,\"commGroupFrame\":{\"@id\":7,\"replicated\":true,\"commFrames\":[6]},\"channelFrame\":5,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":8,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":9,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-x\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":4,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"java.lang.Runnable#run\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$StringEmitterProcess\",\"@id\":1,\"str\":\"World\",\"to\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":2}}},\"body\":[\"[Ljava.lang.Class;\",[]]},{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":2,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}},\"body\":[\"[Ljava.lang.Object;\",[\"Hello\"]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":2,\"commGroupFrame\":{\"@id\":3,\"replicated\":true,\"commFrames\":[2]},\"channelFrame\":1,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":4,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"},{\"@id\":5,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":2,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":6,\"commGroupFrame\":{\"@id\":7,\"replicated\":true,\"commFrames\":[6]},\"channelFrame\":5,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":8,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":9,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-x\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":5,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":2,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}},\"body\":[\"[Ljava.lang.Object;\",[\"Hello\"]]},{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":2,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}},\"body\":[\"[Ljava.lang.Object;\",[\"World\"]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":2,\"commGroupFrame\":{\"@id\":3,\"replicated\":true,\"commFrames\":[2]},\"channelFrame\":1,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":4,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":6,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$ForwarderProcess$ForwarderProcessVal\",\"@id\":2,\"out\":{\"@class\":\"org.apache.ode.jacob.oo.Val\",\"channelType\":\"org.apache.ode.jacob.oo.Val\",\"channelId\":1}}}},\"body\":[\"[Ljava.lang.Object;\",[\"World\"]]},{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}},\"body\":[\"[Ljava.lang.Object;\",[\"Hello\"]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[{\"@id\":1,\"type\":\"org.apache.ode.jacob.oo.Val\",\"id\":1,\"refCount\":0,\"replicated\":true,\"objFrames\":[{\"@id\":2,\"commGroupFrame\":{\"@id\":3,\"replicated\":true,\"commFrames\":[2]},\"channelFrame\":1,\"_continuation\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":4,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}}],\"msgFrames\":[],\"description\":\"simpleHelloWorld-out\"}]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":7,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}},\"body\":[\"[Ljava.lang.Object;\",[\"Hello\"]]},{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}},\"body\":[\"[Ljava.lang.Object;\",[\"World\"]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":8,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[{\"id\":0,\"action\":\"org.apache.ode.jacob.oo.Val#val\",\"to\":{\"@class\":\"org.apache.ode.jacob.ChannelRef\",\"target\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessReceiveProcess\",\"@id\":1,\"receiver\":{\"@class\":\"org.apache.ode.jacob.examples.helloworld.HelloWorld$PrinterProcess$PrinterProcessVal\"}}},\"body\":[\"[Ljava.lang.Object;\",[\"World\"]]}]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[]],\"global\":null}");
        fixtures.add("{\"objIdCounter\":2,\"currentCycle\":9,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[]],\"global\":null}");
    }
    
    @Test
    public void testEmptySerialization() throws Exception {
        Assert.assertEquals("{\"objIdCounter\":0,\"currentCycle\":0,\"messages\":[\"[Lorg.apache.ode.jacob.Message;\",[]],\"channels\":[\"[Lorg.apache.ode.jacob.vpu.ExecutionQueueImpl$ChannelFrame;\",[]],\"global\":null}", mapper.writeValueAsString(queue));
    }

    
    /**
     * Serializes every execution step and compares it with our fixtures.
     * 
     * @throws Exception
     */
    @Test
    @Ignore("ignore this test until soup structure is stable")
    public void testSimpleHelloWorldSerializeAndTestAgainstFixtures() throws Exception {
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(queue);
        vpu.inject(new HelloWorld() {
            @Override
            public void run() {
                simpleHelloWorld();
            }
        });
        
        int i = 0;
        while (vpu.execute()) {
            String ser = mapper.writeValueAsString(queue);
            Assert.assertEquals(fixtures.get(i), ser);
            i++;
        }

        Assert.assertEquals(9, i);
    }
    
    /**
     * Deserializes each execution step from fixtures and resumes the execution from
     * each point.
     * 
     * @throws Exception
     */
    @Test
    @Ignore("ignore this test until soup structure is stable")
    public void testSimpleHelloWorldDeserializeFromFixtures() throws Exception {
        JacobVPU vpu = new JacobVPU();
        
        for (String state : fixtures) {
            vpu.setContext(mapper.readValue(state, JacksonExecutionQueueImpl.class));
            int i = 0;
            while (vpu.execute()) {
                i++;
            }
            
            // sum of pre-loaded & then-completed steps is always 8.
            Assert.assertEquals(8, i + fixtures.indexOf(state));
        }
    }
    
    @Test
    public void testSimpleHelloWorldSerializeAndDeserialize() throws Exception {
        List<String> states = new ArrayList<String>();
        
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(queue);
        vpu.inject(new HelloWorld() {
            @Override
            public void run() {
                simpleHelloWorld();
            }
        });
        
        int i = 0;
        while (vpu.execute()) {
            String ser = mapper.writeValueAsString(queue);
            states.add(ser);
            i++;
        }
        Assert.assertEquals(9, i);
        
        // deserialize
        for (String state : states) {
            vpu.setContext(mapper.readValue(state, JacksonExecutionQueueImpl.class));
            i = 0;
            while (vpu.execute()) {
                i++;
            }
            
            // sum of pre-loaded & then-completed steps is always 8.
            Assert.assertEquals(8, i + states.indexOf(state));
        }
    }

}
