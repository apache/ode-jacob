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
package org.apache.ode.jacob.vpu;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.JacobThread;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.MessageListener;
import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ChannelListener;
import org.apache.ode.jacob.oo.ClassUtil;
import org.apache.ode.jacob.oo.CompositeProcess;
import org.apache.ode.jacob.oo.ReceiveProcess;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.soup.CommGroup;
import org.apache.ode.jacob.soup.CommRecv;
import org.apache.ode.jacob.soup.CommSend;
import org.apache.ode.jacob.soup.ExecutionQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JACOB Virtual Processing Unit ("VPU").
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com" />
 */
public final class JacobVPU {
    private static final Logger LOG = LoggerFactory.getLogger(JacobVPU.class);

    // Thread-local for associating a thread with a VPU. Needs to be stored in a stack to allow reentrance.
    private static final ThreadLocal<Stack<JacobThread>> ACTIVE_THREAD = new ThreadLocal<Stack<JacobThread>>();

    /**
     * Persisted cross-VPU state (state of the channels)
     */
    private ExecutionQueue _executionQueue;
    private Map<Class<?>, Object> _extensions = new HashMap<Class<?>, Object>();

    /**
     * Classloader used for loading object continuations.
     */
    private ClassLoader _classLoader = getClass().getClassLoader();

    private int _cycle;

    private Statistics _statistics = new Statistics();

    /**
     * The fault "register" of the VPU .
     */
    private RuntimeException _fault;

    public JacobVPU() {
    }

    /**
     * Execute one VPU cycle.
     *
     * @return <code>true</code> if the run queue is not empty after this cycle, <code>false</code> otherwise.
     */
    public boolean execute() {
        LOG.trace(">> JacobVPU.execute()");

        if (_executionQueue == null) {
            throw new IllegalStateException("No state object for VPU!");
        }
        if (_fault != null) {
            throw _fault;
        }
        if (!_executionQueue.hasReactions()) {
            return false;
        }
        _cycle = _executionQueue.cycle();

        Message rqe = _executionQueue.dequeueMessage();
        JacobThreadImpl jt = new JacobThreadImpl(rqe);

        long ctime = System.currentTimeMillis();
        try {
            jt.run();
        } catch (RuntimeException re) {
            _fault = re;
            throw re;
        }

        long rtime = System.currentTimeMillis() - ctime;
        ++_statistics.numCycles;
        _statistics.totalRunTimeMs += rtime;
        _statistics.incRunTime(jt._targetStr, rtime);
        return true;
    }

    public void flush() {
        LOG.trace(">> JacobVPU.flush ()");
        _executionQueue.flush();
    }

    /**
     * Set the state of of the VPU; this is analagous to loading a CPU with a
     * thread's context (re-hydration).
     *
     * @param executionQueue
     *            process executionQueue (state)
     */
    public void setContext(ExecutionQueue executionQueue) {
        LOG.trace(">> setContext (executionQueue={})", executionQueue);

        _executionQueue = executionQueue;
        _executionQueue.setClassLoader(_classLoader);
    }
    
    public ExecutionQueue getContext() {
        return _executionQueue;
    }

    public void registerExtension(Class<?> extensionClass, Object obj) {
        LOG.trace(">> setContext (extensionClass={}, obj={})", extensionClass, obj);
        _extensions.put(extensionClass, obj);
    }

    /**
     * Add an item to the run queue.
     */
    public void addReaction(Runnable jo, String action, Object[] args, String desc) {
        LOG.trace(">> addReaction (jo={}, method={}, args={}, desc={})", jo, action, args, desc);

        _executionQueue.enqueueMessage(ClassUtil.createMessage(jo, action, args, null));
        ++_statistics.runQueueEntries;
    }

    public Channel newChannel(Class<?> channelType, String description) {
        CommChannel chnl = new CommChannel(channelType);
        chnl.setDescription(description);
        _executionQueue.add(chnl);

        Channel ret = ChannelFactory.createChannel(chnl, channelType);
        LOG.trace(">> [{}] : new {}", _cycle, ret);

        _statistics.channelsCreated++;
        return ret;
    }

    /**
     * Get the active Jacob thread, i.e. the one associated with the current Java thread.
     */
    public static JacobThread activeJacobThread() {
        return ACTIVE_THREAD.get().peek();
    }

    /**
     * Inject a concretion into the process context. This amounts to chaning the
     * process context from <code>P</code> to <code>P|Q</code> where
     * <code>P</code> is the previous process context and <code>Q</code> is
     * the injected process. This method is equivalent to the parallel operator,
     * but is intended to be used from outside of an active {@link JacobThread}.
     */
    public void inject(RunnableProcess concretion) {
        LOG.debug("injecting {}", concretion);
        addReaction(concretion, ClassUtil.RUN_METHOD_ACTION, new Class[]{},
            (LOG.isInfoEnabled() ? concretion.toString() : null));
    }

    static String stringifyMethods(Class<?> kind) {
        StringBuffer buf = new StringBuffer();
        Method[] methods = kind.getMethods();
        boolean found = false;

        for (Method method : methods) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (found) {
                buf.append(" & ");
            }
            buf.append(method.getName()).append('(');
            Class<?>[] argTypes = method.getParameterTypes();
            for (int j = 0; j < argTypes.length; ++j) {
                if (j > 0) {
                    buf.append(", ");
                }
                buf.append(argTypes[j].getName());
            }
            buf.append(") {...}");
            found = true;
        }
        return buf.toString();
    }

    static String stringify(Object obj) {
        if (obj == null) {
            return "";
        }
        
        if (obj instanceof Object[]) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < ((Object[])obj).length; ++i) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(((Object[])obj)[i]);
            }
            return buf.toString();
        } else {
            return obj.toString();
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
        if (_executionQueue != null) {
            _executionQueue.setClassLoader(classLoader);
        }
    }

    /**
     * Dump the state of the VPU for debugging purposes.
     */
    public void dumpState() {
        _statistics.printToStream(System.err);
        _executionQueue.dumpState(System.err);
    }

    public boolean isComplete() {
        return _executionQueue.isComplete();
    }

    private class JacobThreadImpl implements Runnable, JacobThread {
        private final Message message;

        /** Text string identifying the left side of the reduction (for debug). */
        private String _source;

        /** Text string identifying the target class and method (for debug) . */
        private String _targetStr = "Unknown";

        JacobThreadImpl(Message msg) {
            message = msg;
            _targetStr = msg.getAction();
        }

        public void instance(Runnable template) {
            LOG.trace(">> [{}] : {}", _cycle, template);

            _statistics.numReductionsStruct++;
            addReaction(template, ClassUtil.RUN_METHOD_ACTION, null, 
                LOG.isInfoEnabled() ? template.toString() : null);
        }

        public Channel message(Channel channel, Method method, Object[] args) {
            Channel replyChannel = null;
            CommChannel replyCommChannel = null;
            // Check for synchronous methods; create a synchronization channel
            if (method.getReturnType() != void.class) {
                if (!Channel.class.isAssignableFrom(method.getReturnType())) {
                    throw new IllegalStateException(
                        "Channel method '" + method + "' must only return void or an implementation of " + Channel.class.getName());
                }
                replyChannel = newChannel(method.getReturnType(), "Reply Channel");
                replyCommChannel = ChannelFactory.getBackend((Channel)replyChannel);
            }
            
            CommChannel chnl = ChannelFactory.getBackend((Channel)channel);
            Message msg = ClassUtil.createMessage(chnl, ClassUtil.getActionForMethod(method), args, replyCommChannel);

            sendMessage(msg);
            return replyChannel;
        }
        
        public void sendMessage(Message msg) {
            LOG.trace(">> [{}] : {} ! {} ({})", _cycle, msg.getTo(), msg.getAction(),
                    LOG.isTraceEnabled() ? stringify(msg.getBody()) : null);

            _statistics.messagesSent++;

            CommGroup grp = new CommGroup(false);
            CommSend send = new CommSend(msg);
            grp.add(send);
            _executionQueue.add(grp);
        }

        public Channel newChannel(Class<?> channelType, String description) {
            return JacobVPU.this.newChannel(channelType, description);
        }

        public ChannelRef newCommChannel(String description) {
            CommChannel chnl = new CommChannel();
            chnl.setDescription(description);
            _executionQueue.add(chnl);

            LOG.trace(">> [{}] : new {}", _cycle, chnl);

            _statistics.channelsCreated++;
            return new ChannelRef(chnl);
        }
        
        public String exportChannel(Channel channel) {
            LOG.trace(">> [{}] : export<{}>", _cycle, channel);

            CommChannel chnl = ChannelFactory.getBackend((Channel)channel);
            return _executionQueue.createExport(chnl);
        }

        public String exportCommChannel(CommChannel channel) {
            LOG.trace(">> [{}] : export<{}>", _cycle, channel);

            return _executionQueue.createExport(channel);
        }

        //XXX: check if channelType is really needed, could be get from cframe.getType()
        public Channel importChannel(String channelId, Class<?> channelType) {
            CommChannel cframe = _executionQueue.consumeExport(channelId);
            return ChannelFactory.createChannel(cframe, channelType);
        }

        public ChannelRef importCommChannel(String channelId, Class<?> channelType) {
            return new ChannelRef(_executionQueue.consumeExport(channelId));
        }
        
        public void object(boolean replicate, ChannelListener ml) {
            if (LOG.isTraceEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append(_cycle);
                msg.append(": ");
                if (ml instanceof ReceiveProcess) {
                    ReceiveProcess rp = (ReceiveProcess)ml;
                    msg.append(rp.getChannel());
                    msg.append(" ? ");
                    msg.append(rp.toString());
                }
                LOG.trace(msg.toString());
            }

            _statistics.numContinuations++;

            CommGroup grp = new CommGroup(replicate);
            addCommChannel(grp, ml);
            _executionQueue.add(grp);
        }
        
        public void subscribe(boolean replicate, ChannelRef channel, MessageListener listener) {
            assert channel.getType() == ChannelRef.Type.CHANNEL;
            if (LOG.isTraceEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append(_cycle);
                msg.append(": ");
                msg.append(channel);
                msg.append(" ? ");
                msg.append(listener.toString());
                LOG.trace(msg.toString());
            }

            _statistics.numContinuations++;

            CommGroup grp = new CommGroup(replicate);
            CommRecv recv = new CommRecv(channel.getEndpoint(CommChannel.class), listener);
            grp.add(recv);

            _executionQueue.add(grp);
        }

        private void addCommChannel(CommGroup group, ChannelListener receiver) {
            if (receiver instanceof CompositeProcess) {
                for (ChannelListener r : ((CompositeProcess)receiver).getProcesses()) {
                    addCommChannel(group, r);
                }
            } else if (receiver instanceof ReceiveProcess) {
                CommChannel chnl = ChannelFactory.getBackend(((ReceiveProcess)receiver).getChannel());
                    // TODO see below..
                    // oframe.setDebugInfo(fillDebugInfo());
                    CommRecv recv = new CommRecv(chnl, receiver);
                    group.add(recv);
            } else {
                throw new IllegalStateException("Don't know how to handle Process type...");
            }
        }

        /* UNUSED
         private DebugInfo fillDebugInfo() {
            // Some of the debug information is a bit lengthy, so lets not put
            // it in all the time... eh.
            DebugInfo frame = new DebugInfo();
            frame.setCreator(_source);
            Exception ex = new Exception();
            StackTraceElement[] st = ex.getStackTrace();
            if (st.length > 2) {
                StackTraceElement[] stcut = new StackTraceElement[st.length - 2];
                System.arraycopy(st, 2, stcut, 0, stcut.length);
                frame.setLocation(stcut);
            }

            return frame;
        }
        */

        public Object getExtension(Class<?> extensionClass) {
            return _extensions.get(extensionClass);
        }

        public void run() {
            LOG.trace(">> [{}] : {}", _cycle, _source);

            stackThread();
            CommChannel replyTo = message.getReplyTo() != null ? message.getReplyTo().getEndpoint(CommChannel.class) : null;

            long ctime = System.currentTimeMillis();
            try {
                switch (message.getTo().getType()) {
                    case CHANNEL:
                        throw new UnsupportedOperationException();
                    case RUNNABLE:
                        Runnable target = message.getTo().getEndpoint(Runnable.class);
                        if (target instanceof ReceiveProcess) {
                            ((ReceiveProcess)target).onMessage(message);
                        } else {
                            ((Runnable)target).run();
                        }
                        break;
                    case MESSAGE_LISTENER:
                        MessageListener ml = message.getTo().getEndpoint(MessageListener.class);
                        ml.onMessage(message);
                        break;
                }
                
                if (replyTo != null) {
                    //XXX: All replys have the same Synch.ret() action 
                    sendMessage(ClassUtil.createMessage(replyTo, ClassUtil.SYNCH_RET_METHOD_ACTION, null, null));
                }
            } finally {
                ctime = System.currentTimeMillis() - ctime;
                _statistics.totalClientTimeMs += ctime;
                unstackThread();
            }
        }

        public String toString() {
            return "PT[ " + message.getAction() + " ]";
        }

        private void stackThread() {
            Stack<JacobThread> crt = ACTIVE_THREAD.get();
            if (crt == null) {
                crt = new Stack<JacobThread>();
                ACTIVE_THREAD.set(crt);
            }
            crt.push(this);
        }

        private JacobThread unstackThread() {
            return ACTIVE_THREAD.get().pop();
        }
    }
}
