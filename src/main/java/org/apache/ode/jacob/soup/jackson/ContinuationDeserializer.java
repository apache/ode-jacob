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

import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.soup.Continuation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for Continuation objects.
 * 
 * @author Tammo van Lessen
 *
 */
public class ContinuationDeserializer extends StdDeserializer<Continuation> {

	private static final long serialVersionUID = 1L;

	protected ContinuationDeserializer() {
        super(Continuation.class);
    }

    @Override
    public Continuation deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        JacobObject target = null;
        String methodName = null;
        Object[] args = null;
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                // if we're not already on the field, advance by one.
                jp.nextToken();
            }

            if ("target".equals(fieldname)) {
                target = jp.readValueAs(JacobObject.class); 
            } else if ("method".equals(fieldname)) {
                methodName = jp.getText();
            } if ("args".equals(fieldname)) {
                args = jp.readValueAs(Object[].class);
            } 
        }
        
        if (target == null) {
            throw ctxt.mappingException(Continuation.class);
        }
        
        if (methodName == null) {
            throw ctxt.mappingException(Continuation.class);
        }
        
        return new Continuation(target, target.getMethod(methodName), args);
    }
}