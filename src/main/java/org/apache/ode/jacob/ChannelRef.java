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

import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.vpu.ChannelFactory;
import org.apache.ode.jacob.vpu.JacobVPU;



/**
 * TODO: Document...
 */

public class ChannelRef implements Serializable {
    public enum Type { RUNNABLE, CHANNEL, MESSAGE_LISTENER }
    
    private static final long serialVersionUID = 1L;

    private final Type type;
    private final Object target;
    
    private transient Channel cachedChannel;
    
    public ChannelRef(Object target) {
        assert target != null;
        if (target instanceof CommChannel) {
            type = Type.CHANNEL;
        } else if (target instanceof Runnable) {
            type = Type.RUNNABLE;
        } else if (target instanceof MessageListener) {
            type = Type.MESSAGE_LISTENER;
        } else {
            throw new IllegalArgumentException("Unsupported endpoint reference");
        }
        
        this.target = target;
    }
    
    public Type getType() {
        return type;
    }
    
    public String export() {
        if (type == Type.CHANNEL) {
            return JacobVPU.activeJacobThread().exportCommChannel(((CommChannel)target));
        } else {
            throw new UnsupportedOperationException("Cannot export channels of type " + type.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getEndpoint(Class<T> clazz) {
        if (type.equals(Type.RUNNABLE) && Runnable.class.isAssignableFrom(clazz)) {
            return (T)target;
        } else if (type.equals(Type.CHANNEL) && CommChannel.class.isAssignableFrom(clazz)) {
            return (T)target;
        } else if (type.equals(Type.CHANNEL) && Channel.class.isAssignableFrom(clazz)) {
            if (cachedChannel == null) {
                cachedChannel = ChannelFactory.createChannel((CommChannel)target, clazz);
            }
            
            if (!clazz.isAssignableFrom(cachedChannel.getClass())) {
                throw new IllegalStateException("ChannelRef is already associated with a channel of a different type");
            }

            return (T)cachedChannel;
        } else if (type.equals(Type.MESSAGE_LISTENER) && MessageListener.class.isAssignableFrom(clazz)) {
            return (T)target;
        }
        
        return null;
    }

    @Override
    public String toString() {
        return "ChannelRef [target=" + target + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChannelRef other = (ChannelRef) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
   
}
