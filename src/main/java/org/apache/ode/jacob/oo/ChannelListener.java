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
package org.apache.ode.jacob.oo;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.MessageChannel;
import org.apache.ode.jacob.MessageType;


/**
 * Base-class for method-list objects. Method-lists objects should extends this
 * class <em>and</em> implement one <code>Channel</code> interface.
 */
@SuppressWarnings("serial")
public abstract class ChannelListener extends JacobObject implements MessageChannel {

	public void onMessage(Message msg) {
		Class<? extends MessageType> type = msg.getType();

		Set<Method> methods = this.getImplementedMethods();
		for (Method m : methods) {
			if (type != null && type.equals(ClassUtil.getMessageType(m))) {
				if (this instanceof ReceiveProcess) {
					try {
						m.invoke(((ReceiveProcess)this).getReceiver(), (Object[])msg.getBody());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}

}
