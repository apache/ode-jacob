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

import org.apache.ode.jacob.oo.ClassUtil;
import org.apache.ode.jacob.soup.Continuation;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson serializer for Continuation objects.
 * 
 * @author Tammo van Lessen
 *
 */
public class MessageSerializer extends StdSerializer<Continuation> {

    public MessageSerializer() {
        super(Continuation.class);
    }
    
    @Override
    public void serialize(Continuation value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonGenerationException {
        jgen.writeStartObject();
        serializeContents(value, jgen, provider);
        jgen.writeEndObject();
    }

    
    @Override
    public void serializeWithType(Continuation value, JsonGenerator jgen,
            SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForObject(value, jgen);
        serializeContents(value, jgen, provider);
        typeSer.writeTypeSuffixForObject(value, jgen);
    }
    
    private void serializeContents(Continuation value, JsonGenerator jgen,
            SerializerProvider provider) throws JsonGenerationException, IOException {

        jgen.writeObjectField("target", ClassUtil.getMessageClosure(value.getMessage()));
        jgen.writeStringField("method", value.getMessage().getAction());
        jgen.writeObjectField("args", value.getMessage().getBody());
    }
}