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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * TODO: Document...
 * TODO: should anything be final here? the class itself?
 */

public class Message {
	private long id;
    private ChannelRef to;	
    private ChannelRef replyTo;
    private String action;
    private Map<String, Object> headers;
	private Object body;

	public Message() {
		id = 0;	// TODO: generate id
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
}
