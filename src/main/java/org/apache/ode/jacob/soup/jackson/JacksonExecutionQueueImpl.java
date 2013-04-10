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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.ChannelProxy;
import org.apache.ode.jacob.soup.Continuation;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Variant of {@link org.apache.ode.jacob.vpu.ExecutionQueueImpl} that can be
 * serialized and deserialized with Jackson.
 */
public class JacksonExecutionQueueImpl extends ExecutionQueueImpl {

	public JacksonExecutionQueueImpl() {
		super(null);
	}
	
    public static void configureMapper(ObjectMapper om) {
        
        SimpleModule sm = new SimpleModule("jacobmodule");
        sm.addSerializer(ChannelProxy.class, new ChannelProxySerializer());
        sm.addSerializer(Continuation.class, new ContinuationSerializer());
        sm.addSerializer(JacksonExecutionQueueImpl.class, new ExecutionQueueImplSerializer());
        sm.addDeserializer(JacksonExecutionQueueImpl.class, new ExecutionQueueImplDeserializer());
        sm.addDeserializer(Continuation.class, new ContinuationDeserializer());
        sm.addDeserializer(Channel.class, new ChannelProxyDeserializer());
        
        om.registerModule(sm);
        om.disable(MapperFeature.AUTO_DETECT_CREATORS);
        om.disable(MapperFeature.AUTO_DETECT_GETTERS);
        om.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        om.setDefaultTyping(new JacobTypeResolverBuilder());
        om.setAnnotationIntrospector(new JacobJacksonAnnotationIntrospector());
        
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        //om.enable(SerializationFeature.INDENT_OUTPUT);
    }

	
    public static class ExecutionQueueImplSerializer extends StdSerializer<JacksonExecutionQueueImpl> {

        public ExecutionQueueImplSerializer() {
            super(JacksonExecutionQueueImpl.class);
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

        	jgen.writeNumberField("objIdCounter", value._objIdCounter);
            jgen.writeNumberField("currentCycle", value._currentCycle);
            
            jgen.writeObjectField("continuations", value._reactions.toArray(new Continuation[] {}));
            jgen.writeObjectField("channels", value._channels.values().toArray(new ChannelFrame[] {}));
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
                } else if ("continuations".equals(fieldname)) {
                    Continuation[] cs = (Continuation[])jp.readValueAs(Continuation[].class);
                    soup._reactions = new HashSet<Continuation>(Arrays.asList(cs));
                } else if ("channels".equals(fieldname)) {
                    soup._channels = new HashMap<Integer, ChannelFrame>();
                    ChannelFrame[] frames = jp.readValueAs(ChannelFrame[].class); 
                    for (ChannelFrame f : frames) {
                        soup._channels.put(f.getId(), f);
                    }
                }

            }

            // Garbage collection
            // TODO

            return soup;
        }

    }


}
