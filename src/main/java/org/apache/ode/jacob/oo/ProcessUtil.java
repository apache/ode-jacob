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

import java.util.Arrays;

import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.ode.jacob.Jacob.instance;
import static org.apache.ode.jacob.Jacob.newChannel;
import static org.apache.ode.jacob.Jacob.object;


public final class ProcessUtil {
    // TODO: add more logging at TRACE level
    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    private ProcessUtil() {
        // Utility class
    }

    public static String exportChannel(Channel channel) {
        if (channel != null && channel instanceof ChannelProxy) {
            // TODO: replace the brute force call on the activeThread with
            //  something that doesn't expose the implementation once the
            //  cleaner refactored api becomes available
            return JacobVPU.activeJacobThread().exportChannel(channel);
        }
        throw new IllegalArgumentException("Invalid proxy type: "
            + channel == null ? "<null>" : channel.getClass().toString());
    }
    
    public static CompositeProcess compose(ChannelListener process) {
        CompositeProcess result = new CompositeProcess();
        return result.or(process);
    }

    public static <T extends Channel> ChannelListener receive(T proxy, T listener) {
        // TODO: NOTE: this *only* works when the listener doesn't need to be Serializable really
        //  because we cannot declare a static serialVersionUID like this
        //  once we fix serialization, this can be simplified significantly via a dsl
        return new ReceiveProcess().setChannel(proxy).setReceiver(listener);
    }

    /**
     * 
     * @return    A noop RunnableProcess
     */
    public static RunnableProcess nil() {
        return new Nil();
    }

    /**
     * 
     * @param callback
     * @return a RunnableProcess that wraps a return notification in a separate process
     * 
     */
    public static RunnableProcess terminator(final Synch callback) {
        return callback != null ? new Terminator(callback) : null;
    }

    /**
     * 
     * @param callback
     * @param process
     * @return
     * 
     * Returns a synchronized process embedding the runnable process. Once the process finishes it 
     * will notify that on the callback return channel
     */
    public static Synchronized sync(final Synch callback, final RunnableProcess process) {
        return new SynchronizedWrapper(callback, process);
    }

    /**
     * 
     * @param callback
     * @param process
     * @return
     * 
     * Intercepts the execution of a synchronized process and executes an interceptor before the 
     * termination of the process is actually signaled
     */
    public static Synchronized intercept(final Synchronized process, final RunnableProcess interceptor) {
        if (interceptor == null) {
            return process;
        }
        Synch callback = newChannel(Synch.class, "");
        object(receive(callback, new InterceptorSynch(process.getCallback(), interceptor)));
        process.setCallback(callback);
        return process;
    }

    /**
     * 
     * @param processes
     * @return a Synchronized process
     * 
     * Ensures the sequential execution of processes
     */
    public static Synchronized sequence(final RunnableProcess... processes) {
        return sequence(null, processes);
    }

    /**
     * 
     * @param callback
     * @param processes
     * @return
     * 
     * Ensures the sequential execution of processes. After the execution is complete a 
     * notification is sent to the callback channel
     */
    public static Synchronized sequence(final Synch callback, final RunnableProcess... processes) {
        return new SequenceProcess(callback, processes);
    }

    // Helpers Process composers

    /**
     * TODO: Document me
     */
    public static class Nil extends RunnableProcess {
        private static final long serialVersionUID = 1L;
        public void run() {
            // do nothing
        }
    }

    /**
     * TODO: Document me
     */
    public static class Terminator extends RunnableProcess {
        private static final long serialVersionUID = 1L;
        protected Synch callback;
        public Terminator(final Synch callback) {
            this.callback = callback;
        }
        public Synch getCallback() {
            return callback;
        }
        public void run() {
            callback.ret();
        }
    }

    public static abstract class Synchronized extends RunnableProcess {
        private static final long serialVersionUID = 1L;
        protected Synch callback;

        public abstract void execute();

        public Synchronized(final Synch callback) {
            setCallback(callback);
        }
        public Synch getCallback() {
            return callback;
        }
        public void setCallback(final Synch callback) {
            this.callback = callback;
        }
        public void run() {
            execute();
            if (callback != null) {
                callback.ret();
            }
        }
    }

    public static class SynchronizedWrapper extends Synchronized {
        private static final long serialVersionUID = 1L;
        protected final RunnableProcess process;
        public SynchronizedWrapper(final Synch callback, final RunnableProcess process) {
            super(callback);
            this.process = process;
        }
        public void execute() {
            process.run();
        }
    }

    public static final class InterceptorSynch implements Synch {
        private static final long serialVersionUID = 1L;
        protected final RunnableProcess interceptor;
        private final Synch target;
        public InterceptorSynch(final Synch target, final RunnableProcess interceptor) {
            this.target = target;
            this.interceptor = interceptor;
        }
        public void ret() {
            instance(sync(target, interceptor));
        }
    }

    public static final class SequenceProcess extends Synchronized {
        private static final long serialVersionUID = 1L;
        private final RunnableProcess[] processes;

        public SequenceProcess(final Synch callback, final RunnableProcess[] processes) {
            super(callback);
            this.processes = processes;
        }

        public void execute() {
            // can only sequence synchronized processes
            final Synchronized current = ensureSynchronized(processes[0]);
            instance(intercept(current, processes.length > 1 ? 
                sequence(this.callback, Arrays.copyOfRange(processes, 1, processes.length)) :
                terminator(this.callback)));
            this.callback = null;
        }
        
        public Synchronized ensureSynchronized(RunnableProcess process) {
            return process instanceof Synchronized ? (Synchronized)process : sync(null, process);
        }
    }

}
