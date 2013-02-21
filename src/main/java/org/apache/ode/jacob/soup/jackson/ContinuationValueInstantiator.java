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

import java.lang.reflect.Method;

import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.soup.Continuation;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

public class ContinuationValueInstantiator extends ValueInstantiator {

    @Override
    public String getValueTypeDesc() {
        return Continuation.class.getName();
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return true;
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments(
            DeserializationConfig config) {
        return  new CreatorProperty[] {
                new CreatorProperty("_closure", config.constructType(JacobObject.class), null, null, null, 0, null),
                new CreatorProperty("_method", config.constructType(Method.class), null, null, null, 1, null),
                new CreatorProperty("_args", config.constructType(Object[].class), null, null, null, 2, null)};

    }
}