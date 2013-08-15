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

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ChannelProxy;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl.ExecutionQueueImplDeserializer;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl.ExecutionQueueImplSerializer;
import org.apache.ode.jacob.soup.jackson.ChannelProxySerializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module that can be registered to configure a Jackson ObjectMapper
 * for serialization/deserialization of ExecutionQueues.
 * 
 * @author Tammo van Lessen
 *
 */
public class JacobModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JacobModule() {
        super("jacob-module", Version.unknownVersion());
        
        final ExecutionQueueImplSerializer cqis = new ExecutionQueueImplSerializer();
        addSerializer(ChannelProxy.class, new ChannelProxySerializer(cqis));
        addSerializer(Message.class, new MessageSerializer());
        addSerializer(JacksonExecutionQueueImpl.class, cqis);
        addSerializer(ChannelRef.class, new ChannelRefSerializer(cqis));
        addDeserializer(JacksonExecutionQueueImpl.class, new ExecutionQueueImplDeserializer());
        addDeserializer(Message.class, new MessageDeserializer());
        addDeserializer(Channel.class, new ChannelProxyDeserializer());
        addDeserializer(ChannelRef.class, new ChannelRefDeserializer());
        
        setDeserializerModifier(new BeanDeserializerModifier() {

            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                
                // use channel proxy deserializer for channels.
                if (Channel.class.isAssignableFrom(beanDesc.getBeanClass()) && beanDesc.getBeanClass().isInterface()) {
                    return new ChannelProxyDeserializer();
                }

                return super.modifyDeserializer(config, beanDesc, deserializer);
            }
        });
    }
    
    @Override
    public void setupModule(SetupContext context) {
        context.appendAnnotationIntrospector(new JacobJacksonAnnotationIntrospector());
        super.setupModule(context);
    }

}
