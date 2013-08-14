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
package org.apache.ode.jacob;

import java.io.Serializable;

import org.apache.ode.jacob.soup.CommChannel;



/**
 * TODO: Document...
 */

public class ChannelRef implements Serializable {
    public enum Type { JACOB_OBJECT, CHANNEL }
    
    private static final long serialVersionUID = 1L;

    private final Type type;
    private final Object target;
    
    public ChannelRef(Object target) {
        type = target instanceof CommChannel ? Type.CHANNEL : Type.JACOB_OBJECT;
        this.target = target;
    }
    
    public Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getEndpoint(Class<T> clazz) {
        if (type.equals(Type.JACOB_OBJECT) && JacobObject.class.isAssignableFrom(clazz)) {
            return (T)target;
        } else if (type.equals(Type.CHANNEL) && CommChannel.class.isAssignableFrom(clazz)) {
            return (T)target;
        }
        
        return null;
    }
    
}
