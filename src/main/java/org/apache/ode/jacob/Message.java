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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * TODO: Document...
 * TODO: should anything be final here? the class itself?
 */

public class Message implements Serializable {
    private static final long serialVersionUID = -2118625760125445959L;
    
    private static final AtomicLong idGen = new AtomicLong();
    
    private long id;
    private ChannelRef to;    
    private ChannelRef replyTo;
    private String action;
    private Map<String, Object> headers;
    private Object body;

    public Message() {
        id = idGen.incrementAndGet();
        // TODO: always allocating headers may not be a good idea
        //  checking for non-null headers in the getters below is 
        //  not great either; should look into a better option later
        //  after finishing pi-calculus refactoring and running some
        //  perf tests
        headers = new ConcurrentHashMap<String, Object>();
    }

    public Message(ChannelRef to, ChannelRef replyTo, String action) {
        this();
        this.to = to;
        this.replyTo = replyTo;
        this.action = action;
    }

    // TODO: add any other convenience methods like addHeader, removeHeader? 
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public ChannelRef getTo() {
        return to;
    }
    public void setTo(ChannelRef to) {
        this.to = to;
    }
    public ChannelRef getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(ChannelRef replyTo) {
        this.replyTo = replyTo;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
    public Object getBody() {
        return body;
    }
    public void setBody(Object body) {
        this.body = body;
    }

    public boolean containsHeader(String header) {
        return headers.containsKey(header);
    }
    
    
    public String toString() {
        return "Message [id=" + id + ", "
                + (to != null ? "to=" + to + ", " : "")
                + (replyTo != null ? "replyTo=" + replyTo + ", " : "")
                + (action != null ? "action=" + action + ", " : "")
                + (headers != null ? "headers=" + headers + ", " : "")
                + (body != null ? "body=" + body : "") + "]";
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public static Message copyFrom(Message message) {
        Message result = new Message();
        
        result.setId(idGen.incrementAndGet());
        
        result.setAction(message.getAction());
        result.setBody(message.getBody());
        result.setHeaders(message.getHeaders());
        result.setTo(message.getTo());
        result.setReplyTo(message.getReplyTo());
        
        return result;
    }
}
