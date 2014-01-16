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


import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ChannelListener;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.vpu.JacobVPU;

public class Jacob {

    public static Object getExtension(Class<?> extensionClass) {
        return JacobVPU.activeJacobThread().getExtension(extensionClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Channel> T importChannel(String channelId, Class<T> channelClass) {
        return (T) JacobVPU.activeJacobThread().importChannel(channelId, channelClass);
    }

    /**
     * Instantiation; the Java code <code>instance(new F(x,y,z))</code> is
     * equivalent to <code>F(x,y,z)</code> in the process calculus.
     *
     * @param concretion the concretion of a process template
     */
    public static void instance(RunnableProcess concretion) {
        JacobVPU.activeJacobThread().instance(concretion);
    }

    public static <T extends Channel> T newChannel(Class<T> channelType)
            throws IllegalArgumentException {
        return newChannel(channelType, null);
    }

    /**
     * Channel creation; the Java code <code>Channel x = newChannel(XChannel.class) ...</code>
     * is equivalent to <code>(new x) ... </code> in the process calculus.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Channel> T newChannel(Class<T> channelType, String description)
        throws IllegalArgumentException
    {
        return (T) JacobVPU.activeJacobThread().newChannel(channelType, description);
    }

    /**
     * Object; the Java code "object(x, ChannelListener)" is equivalent to
     * <code>x ? ChannelListener</code> in the process algebra.
     *
     * @param methodList method list for the communication reduction
     * @see JacobThread#object
     */
    public static void object(ChannelListener methodList) {
        JacobVPU.activeJacobThread().object(false, methodList);
    }

    public static void object(boolean replication, ChannelListener methodList) {
        JacobVPU.activeJacobThread().object(replication, methodList);
    }

    // calculus API

    /**
     * DOCUMENT ME
     * @param channel
     * @return
     */
    public static ChannelRef newCommChannel(String description) {
        return JacobVPU.activeJacobThread().newCommChannel(description);
    }
    
    /**
     * DOCUMENT ME
     * @param channel
     * @return
     */
    public static String exportCommChannel(CommChannel channel) {
        return JacobVPU.activeJacobThread().exportCommChannel(channel);
    }

    /**
     * DOCUMENT ME
     * @param channel
     * @return
     */
    public static ChannelRef importCommChannel(String channelId, Class<?> channelType) {
        return JacobVPU.activeJacobThread().importCommChannel(channelId, channelType);
    }
    
    /**
     * Send a message. 
     *
     * @param message
     *            self-contained message
     */
    public static void sendMessage(Message message) {
        JacobVPU.activeJacobThread().sendMessage(message);
    }

    public static void subscribe(boolean replicate, ChannelRef channel, MessageListener messageListener) throws IllegalArgumentException {
        JacobVPU.activeJacobThread().subscribe(replicate, channel, messageListener);
    }

}
