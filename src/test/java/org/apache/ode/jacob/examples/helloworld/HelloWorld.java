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
package org.apache.ode.jacob.examples.helloworld;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.ode.jacob.ChannelRef;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.MessageListener;
import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.examples.sequence.Sequence;
import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ReceiveProcess;
import org.apache.ode.jacob.oo.Synch;
import org.apache.ode.jacob.oo.Val;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl;
import org.apache.ode.jacob.soup.jackson.JacobModule;
import org.apache.ode.jacob.vpu.JacobVPU;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import static org.apache.ode.jacob.Jacob.instance;
import static org.apache.ode.jacob.Jacob.newChannel;
import static org.apache.ode.jacob.Jacob.newCommChannel;
import static org.apache.ode.jacob.Jacob.object;
import static org.apache.ode.jacob.Jacob.sendMessage;
import static org.apache.ode.jacob.Jacob.subscribe;


/**
 * Simple Hello World example to showcase different
 * features and approaches of the Jacob API.
 * 
 * Inspired by http://scienceblogs.com/goodmath/2007/04/16/back-to-calculus-a-better-intr-1/
 * 
 * @author Tammo van Lessen
 * 
 */
@SuppressWarnings("serial")
public class HelloWorld extends RunnableProcess {

    public static interface Callback<T, R extends Channel> extends Channel {

        public void invoke(T value, R callback);

    }

    static class ReliablePrinterProcess extends RunnableProcess {
        private Callback<String, Synch> in;

        @JsonCreator
        public ReliablePrinterProcess(@JsonProperty("in") Callback<String, Synch> in) {
            this.in = in;
        }

        public void run() {
            object(true, new ReceiveProcess().setChannel(in).setReceiver(new ReliablePrinterCallback()));
        }

        static class ReliablePrinterCallback implements Callback<String, Synch> {
            public void invoke(String value, Synch callback) {
                System.out.println(value);
                callback.ret();
            }
        }
    }

    static class ReliableStringEmitterProcess extends RunnableProcess {
        private String str;
        private Callback<String, Synch> to;

        @JsonCreator
        public ReliableStringEmitterProcess(@JsonProperty("str")String str, @JsonProperty("to") Callback<String, Synch> to) {
            this.str = str;
            this.to = to;
        }

        public void run() {
            Synch callback = newChannel(Synch.class, "callback channel to ACK " + str);
            object(new ReceiveProcess().setChannel(callback).setReceiver(new ReliableStringEmitterSynch(str)));
            to.invoke(str, callback);
        }

        static class ReliableStringEmitterSynch implements Synch {
            private String str;

            @JsonCreator
            public ReliableStringEmitterSynch(@JsonProperty("str") String str) {
                this.str = str;
            }

            public void ret() {
                System.out.println(str + " ACKed");
            }
        }
    }

    static class PrinterProcess extends RunnableProcess {
        private Val _in;

        @JsonCreator
        public PrinterProcess(@JsonProperty("in") Val in) {
            _in = in;
        }

        public void run() {
            object(true, new ReceiveProcess().setChannel(_in).setReceiver(new PrinterProcessVal()));
        }

        static class PrinterProcessVal implements Val {
            public void val(Object o) {
                System.out.println(o);
            }
        }
    }

    static class StringEmitterProcess extends RunnableProcess {
        private String str;
        private Val to;

        @JsonCreator
        public StringEmitterProcess(@JsonProperty("str") String str, @JsonProperty("to") Val to) {
            this.str = str;
            this.to = to;
        }

        public void run() {
            to.val(str);
        }
    }

    static class ForwarderProcess extends RunnableProcess {
        private Val in;
        private Val out;

        @JsonCreator
        public ForwarderProcess(@JsonProperty("in") Val in, @JsonProperty("out") Val out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
             object(true, new ReceiveProcess().setChannel(in).setReceiver(new ForwarderProcessVal(out)));
        }

        static class ForwarderProcessVal implements Val {
            private Val out;
            @JsonCreator
            public ForwarderProcessVal(@JsonProperty("out")Val out) {
                this.out = out;
            }
            public void val(Object o) {
                out.val(o);
            }
        }

    }

    protected void simpleHelloWorld() {
        // new(out)
        final Val out = newChannel(Val.class, "simpleHelloWorld-out");
        // new(x)
        final Val x = newChannel(Val.class, "simpleHelloWorld-x");
        // *(?out(str).!sysout(str))
        instance(new PrinterProcess(out));
        // *(?x(str).!out(str))
        instance(new ForwarderProcess(x, out));

        // !out(hello) | !out(world)
        instance(new StringEmitterProcess("Hello", x));
        instance(new StringEmitterProcess("World", x));
    }
    
    protected void reliableHelloWorld() {
        // reliable version of the code above
        // (new(callback).!out(hello).?callback) | (new(callback).!out(world).?callback)
        
        // new(rout)
        @SuppressWarnings("unchecked")
        Callback<String, Synch> rout = newChannel(Callback.class, "reliableHelloWorld-rout");
        // *(?rout(str).!sysout(str))
        instance(new ReliablePrinterProcess(rout));
        // (new(callback).!out(hello).?callback)
        instance(new ReliableStringEmitterProcess("Hello", rout));
        // (new(callback).!out(world).?callback)
        instance(new ReliableStringEmitterProcess("World", rout));
    }
    
    
    protected void sequencedHelloWorld() {
        // send hello world as a sequence
        // !out(hello).!out(world)

        // new(out)
        final Val out = newChannel(Val.class, "sequencedHelloWorld-out");

        // *(?out(str).!sysout(str))
        instance(new PrinterProcess(out));

        final String[] greeting = {"Hello", "World"};

        instance(new HWSequence(greeting, out, null));
    }

    protected void calculusHelloWorld() {
        // new(out)
        final ChannelRef out = newCommChannel("calculusHelloWorld-out");
        // new(x)
        final ChannelRef x = newCommChannel("calculusHelloWorld-x");
        
        // *(?out(str).!sysout(str))
        subscribe(true, out, new PrinterMessageListener());
        // *(?x(str).!out(str))
        subscribe(true, x, new ForwarderMessageListener(out));
        // !out(hello) | !out(world)
        instance(new StringEmitterRunnable("Hello", x));
        instance(new StringEmitterRunnable("World", x));
    }

    static class PrinterMessageListener implements MessageListener {

        @Override
        public void onMessage(Message msg) {
            System.out.println(msg.getBody());
        }
        
    }
    
    static class ForwarderMessageListener implements MessageListener {
        private ChannelRef to;
        
        @JsonCreator
        public ForwarderMessageListener(@JsonProperty("to") ChannelRef to) {
            this.to = to;
        }

        @Override
        public void onMessage(Message msg) {
            Message msg2 = new Message(to, null, msg.getAction());
            msg2.setBody(msg.getBody());
            sendMessage(msg2);
        }
    }
    
    static class StringEmitterRunnable extends RunnableProcess {
        private String str;
        private ChannelRef to;

        @JsonCreator
        public StringEmitterRunnable(@JsonProperty("str") String str, @JsonProperty("to") ChannelRef to) {
            this.str = str;
            this.to = to;
        }

        public void run() {
            Message msg = new Message(to, null, "printHW");
            msg.setBody(str);
            sendMessage(msg);
        }
    }
    
    static class HWSequence extends Sequence {

        private final String[] greetings;
        private final Val out;

        @JsonCreator
        public HWSequence(@JsonProperty("greetings") String[] greetings, @JsonProperty("out") Val out, @JsonProperty("done") Synch done) {
            super(greetings.length, done);
            this.greetings = greetings;
            this.out = out;
        }

        @Override
        protected RunnableProcess doStep(int step, Synch done) {
            return new SequenceItemEmitter(greetings[step], done, out);
        }

        static class SequenceItemEmitter extends RunnableProcess {
            private final String string;
            private final Synch done;
            private final Val out;

            @JsonCreator
            public SequenceItemEmitter(@JsonProperty("string") String string, @JsonProperty("done") Synch done, @JsonProperty("out") Val out) {
                this.string = string;
                this.done = done;
                this.out = out;
            }

            @Override
            public void run() {
                instance(new StringEmitterProcess(string, out));
                done.ret();
            }
        }
    }
    

    @Override
    public void run() {
        simpleHelloWorld();
        reliableHelloWorld();
        sequencedHelloWorld();
        calculusHelloWorld();
    }

    @SuppressWarnings("unchecked")
    public static void main(String args[]) throws Exception {
        // enable logging
        // BasicConfigurator.configure();
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for ( Logger logger : loggers ) {
            logger.setLevel(Level.OFF);
        }
        
        SmileFactory sf = null; 
        // // enable smile:
        // sf = new SmileFactory();
        // sf.enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES);
        // sf.enable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT);
        
        ObjectMapper mapper = new ObjectMapper(sf); 
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        mapper.registerModule(new JacobModule());

        JacobVPU vpu = new JacobVPU();
        JacksonExecutionQueueImpl queue = new JacksonExecutionQueueImpl();
        vpu.setContext(queue);

        long start = System.currentTimeMillis();
        vpu.inject(new HelloWorld());
        while (vpu.execute()) {
            queue = loadAndRestoreQueue(mapper, (JacksonExecutionQueueImpl)vpu.getContext());
            vpu.setContext(queue);
            System.out.println(vpu.isComplete() ? "<0>" : ".");
            //vpu.dumpState();
        }
        System.out.println("Runtime in ms: " + (System.currentTimeMillis() - start));
        vpu.dumpState();
    }

    public static JacksonExecutionQueueImpl loadAndRestoreQueue(ObjectMapper mapper, JacksonExecutionQueueImpl in) throws Exception {
        byte[] json = mapper.writeValueAsBytes(in);
        // print json
        // System.out.println(new String(json));
        JacksonExecutionQueueImpl q2 = mapper.readValue(json, JacksonExecutionQueueImpl.class);
        return q2;
    }

}
