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

import org.apache.ode.jacob.ChannelRef;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for Message objects.
 * 
 * @author Tammo van Lessen
 *
 */
public class ChannelRefDeserializer extends StdDeserializer<ChannelRef> {

    private static final long serialVersionUID = 1L;

    protected ChannelRefDeserializer() {
        super(ChannelRef.class);
    }

    @Override
    public ChannelRef deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        Object target = null;
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                // if we're not already on the field, advance by one.
                jp.nextToken();
            }

            if ("target".equals(fieldname)) {
                target = jp.readValueAs(Object.class); 
            } 
        }

        if (target == null) {
            throw ctxt.mappingException(ChannelRef.class);
        }

        return new ChannelRef(target);
    }
}
