/*
R * Licensed to the Apache Software Foundation (ASF) under one
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

import java.util.Collection;

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.MessageListener;
import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ChannelProxy;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.vpu.ChannelFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Customized StdTypeResolverBuilder that enables a custom type id resolving
 * mechanism for Channels.
 * 
 * @author Tammo van Lessen
 *
 */
public class JacobTypeResolverBuilder extends StdTypeResolverBuilder {

    public JacobTypeResolverBuilder() {
        init(JsonTypeInfo.Id.CLASS, null);
        inclusion(JsonTypeInfo.As.PROPERTY);
        typeProperty("@class");
    }
    

    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config,
            JavaType baseType, Collection<NamedType> subtypes, boolean forSer,
            boolean forDeser) {
        return new ChannelAwareTypeIdResolver(baseType, config.getTypeFactory());
    }


    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config,
            JavaType baseType, Collection<NamedType> subtypes) {
        
        return useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes) : null;
    }

    
    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
            JavaType baseType, Collection<NamedType> subtypes) {
        
        return (useForType(baseType)) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
    }

    private boolean useForType(JavaType t) {
        if (Runnable.class.isAssignableFrom(t.getRawClass())) {
            return true;
        }
        
        //TODO: check if still needed.
        if (Channel.class.isAssignableFrom(t.getRawClass()))  {
            return true;
        }
        
        if (CommChannel.class.isAssignableFrom(t.getRawClass()))  {
            return true;
        }
        
        if (ChannelRef.class.isAssignableFrom(t.getRawClass()))  {
            return true;
        }

        if (MessageListener.class.isAssignableFrom(t.getRawClass()))  {
            return true;
        }

        if (t.getRawClass() == Object.class || t.isArrayType()) {
            return true;
        }

        return false;
    }


    public static class ChannelAwareTypeIdResolver extends TypeIdResolverBase {

        private ClassNameIdResolver delegate;

        protected ChannelAwareTypeIdResolver(JavaType baseType,
                TypeFactory typeFactory) {
            super(baseType, typeFactory);
            delegate = new ClassNameIdResolver(baseType, typeFactory);
        }

        public String idFromValue(Object value) {
            if (value instanceof ChannelProxy) {
                CommChannel commChannel = ChannelFactory.getBackend((Channel)value);
                return commChannel.getType().getName();

            }
            return delegate.idFromValue(value);
        }

        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return delegate.idFromValueAndType(value, suggestedType);
        }

        public JavaType typeFromId(String id) {
            return delegate.typeFromId(id);
        }

        public Id getMechanism() {
            return Id.CUSTOM;
        }
        
    }
    
}
