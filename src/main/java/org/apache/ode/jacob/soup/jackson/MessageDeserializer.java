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
import java.util.HashMap;
import java.util.Map;

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.Message;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for Message objects.
 * 
 * @author Tammo van Lessen
 *
 */
public class MessageDeserializer extends StdDeserializer<Message> {

    private static final long serialVersionUID = 1L;
    private TypeReference<HashMap<String,Object>> mapTypeRef = new TypeReference<HashMap<String, Object>>() {}; 

    protected MessageDeserializer() {
        super(Message.class);
    }

    @Override
    public Message deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        long id = 0;
        String action = null;
        ChannelRef to = null;
        ChannelRef replyTo = null;
        Map<String, Object> headers = null;
        Object body = null;
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                // if we're not already on the field, advance by one.
                jp.nextToken();
            }

            if ("id".equals(fieldname)) {
                id = jp.getLongValue(); 
            } else if ("action".equals(fieldname)) {
                action = jp.getText();
            } else if ("to".equals(fieldname)) {
                to = jp.readValueAs(ChannelRef.class);
            } else if ("replyTo".equals(fieldname)) {
                replyTo = jp.readValueAs(ChannelRef.class);
            } else if ("headers".equals(fieldname)) {
                headers = jp.readValueAs(mapTypeRef);
            } else if ("body".equals(fieldname)) {
                body = jp.readValueAs(Object.class);
            } 
        }

        if (action == null) {
            throw ctxt.mappingException(Message.class);
        }

        if (to == null) {
            throw ctxt.mappingException(Message.class);
        }

        Message msg = new Message(to, replyTo, action);
        msg.setHeaders(headers);
        msg.setBody(body);
        msg.setId(id);
        
        return msg;
    }
}
