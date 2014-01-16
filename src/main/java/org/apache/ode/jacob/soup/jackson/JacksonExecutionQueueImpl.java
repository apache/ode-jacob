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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Variant of {@link org.apache.ode.jacob.vpu.ExecutionQueueImpl} that can be
 * serialized and deserialized with Jackson.
 * 
 * @author Tammo van Lessen
 * 
 */
public class JacksonExecutionQueueImpl extends ExecutionQueueImpl {

    private static final Logger LOG = LoggerFactory.getLogger(JacksonExecutionQueueImpl.class);
    
    public JacksonExecutionQueueImpl() {
        super(null);
    }
    
    public static class ExecutionQueueImplSerializer extends StdSerializer<JacksonExecutionQueueImpl> {

        private Set<Integer> usedChannels = new LinkedHashSet<Integer>();
        
        public ExecutionQueueImplSerializer() {
            super(JacksonExecutionQueueImpl.class);
        }
        
        public void markChannelUsed(int channelId) {
            usedChannels.add(channelId);
        }
        
        @Override
        public void serialize(JacksonExecutionQueueImpl value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonGenerationException {
            jgen.writeStartObject();
            serializeContents(value, jgen, provider);
            jgen.writeEndObject();
        }

        
        @Override
        public void serializeWithType(JacksonExecutionQueueImpl value, JsonGenerator jgen,
                SerializerProvider provider, TypeSerializer typeSer)
                throws IOException, JsonProcessingException {
            typeSer.writeTypePrefixForObject(value, jgen);
            serializeContents(value, jgen, provider);
            typeSer.writeTypeSuffixForObject(value, jgen);
        }
        
        private void serializeContents(JacksonExecutionQueueImpl value, JsonGenerator jgen,
                SerializerProvider provider) throws JsonGenerationException, IOException {

            usedChannels.clear();
            
            // write metadata
            jgen.writeNumberField("objIdCounter", value._objIdCounter);
            jgen.writeNumberField("currentCycle", value._currentCycle);
            
            // write continuations
            jgen.writeObjectField("messages", value._messages.toArray(new Message[]{}));
            
            
            // channel garbage collection
            //   - traverse whole object graph and record referenced channel proxies.
            //     - first, regularily serialize continuations.
            //     - second, serialize channels to a null serializer in order to record channel references
            //       without writing them to the stream.
            //   - remove unused channels.
            //   - serialize remaining channels.

            // write channels to null serializer
            JsonGenerator nullgen = new NullJsonGenerator(null, 0, jgen.getCodec());
            nullgen.writeObjectField("channels", value._channels.values().toArray(new ChannelFrame[] {}));
            nullgen.close();

            // remove unreferenced channels (and keep those which have been exported using export()).
            for (Iterator<ChannelFrame> i = value._channels.values().iterator(); i.hasNext();) {
                ChannelFrame cframe = i.next();
                if (usedChannels.contains(cframe.getId()) || cframe.getRefCount() > 0) {
                    // skip
                } else {
                    LOG.debug("GC Channel: {}", cframe);
                    i.remove();
                }
            }

            // write channels
            jgen.writeObjectField("channels", value._channels.values().toArray(new ChannelFrame[] {}));
            
            // write global data
            jgen.writeObjectField("global", value._gdata);
        }
    }

    public static class ExecutionQueueImplDeserializer extends StdDeserializer<JacksonExecutionQueueImpl> {

        private static final long serialVersionUID = 1L;

        public ExecutionQueueImplDeserializer() {
            super(JacksonExecutionQueueImpl.class);
        }

        @Override
        public JacksonExecutionQueueImpl deserialize(JsonParser jp,
                DeserializationContext ctxt) throws IOException,
                JsonProcessingException {

            JacksonExecutionQueueImpl soup = new JacksonExecutionQueueImpl();
            
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                    // if we're not already on the field, advance by one.
                    jp.nextToken();
                }

                if ("objIdCounter".equals(fieldname)) {
                    soup._objIdCounter = jp.getIntValue();
                } else if ("currentCycle".equals(fieldname)) {
                    soup._currentCycle = jp.getIntValue();
                } else if ("messages".equals(fieldname)) {
                    Message[] cs = (Message[])jp.readValueAs(Message[].class);
                    soup._messages = new HashSet<Message>(Arrays.asList(cs));
                } else if ("channels".equals(fieldname)) {
                    soup._channels = new HashMap<Integer, ChannelFrame>();
                    ChannelFrame[] frames = jp.readValueAs(ChannelFrame[].class); 
                    for (ChannelFrame f : frames) {
                        soup._channels.put(f.getId(), f);
                    }
                } else if ("global".equals(fieldname)) {
                    soup._gdata = jp.readValueAs(Serializable.class);
                }
            }

            return soup;
        }

    }


}
