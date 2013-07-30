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

public final class Message {
	private Class<? extends MessageType> type;
	private MessageChannel reply;
	private Map<String, Object> headers;
	private Object body;

	public Message() {
		// TODO: do we always need headers?
		headers = new ConcurrentHashMap<String, Object>();
	}
	public Message(Class<? extends MessageType> type) {
		this();
		this.type = type;
	}

	public Class<? extends MessageType> getType() {
		return type;
	}
	public void setType(Class<? extends MessageType> type) {
		this.type = type;
	}
	public MessageChannel getReply() {
		return reply;
	}
	public void setReply(MessageChannel reply) {
		this.reply = reply;
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
	// TODO: add any other convenience methods like addHeader, removeHeader? 
}
