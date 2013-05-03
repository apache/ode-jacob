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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

/**
 * Customized JacksonAnnotationIntrospector that configures Jackson
 * to deal with some specialties of Jacob's execution queue.
 * 
 * @author Tammo van Lessen
 *
 */
public class JacobJacksonAnnotationIntrospector extends
        JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 1L;
    private TypeResolverBuilder<?> jacobTypeResolver = new JacobTypeResolverBuilder();

    /* enable object ids for all objects. */
    @Override
    public ObjectIdInfo findObjectIdInfo(Annotated ann) {
        return new ObjectIdInfo("@id", Object.class, ObjectIdGenerators.IntSequenceGenerator.class);
    }

    /* use custom type resolver */
    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config,
            AnnotatedClass ac, JavaType baseType) {
        return jacobTypeResolver;
    }

    /* disable creator visibility */
    // TODO: Check if there is a better way to only serialize non-transient fields.
    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac,
            VisibilityChecker<?> checker) {
        return VisibilityChecker.Std.defaultInstance().with(Visibility.NONE).withVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }
    
}
