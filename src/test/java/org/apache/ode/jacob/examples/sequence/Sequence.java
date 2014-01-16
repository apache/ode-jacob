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
package org.apache.ode.jacob.examples.sequence;


import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.oo.ReceiveProcess;
import org.apache.ode.jacob.oo.Synch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.apache.ode.jacob.Jacob.instance;
import static org.apache.ode.jacob.Jacob.newChannel;
import static org.apache.ode.jacob.Jacob.object;


/**
 * Abstract process that executes a number of steps sequentially.
 */
@SuppressWarnings("serial")
public abstract class Sequence extends RunnableProcess {
    private final SequenceData data = new SequenceData();

    /**
     * Create a {@link Sequence} with a number of steps.
     *
     * @param steps number of steps
     * @param done synchronous callback
     */
    public Sequence(int steps, Synch done) {
        data._steps = steps;
        data._current = 0;
        data._done = done;
    }

    public void run() {
        if (data._current >= data._steps) {
            if (data._done != null) {
                data._done.ret();
            }
        } else {
            Synch r = newChannel(Synch.class);
            object(new ReceiveProcess().setChannel(r).setReceiver(new SequenceSynch(data, this)));
            instance(doStep(data._current, r));
        }
    }

    /**
     * Execute a step
     * @param step step number
     * @param done notification after step completion
     * @return runnable process
     */
    protected abstract RunnableProcess doStep(int step, Synch done);

    public static class SequenceData {
        public int _steps;
        public int _current;
        public Synch _done;
        //public Sequence _seq;
    }

    static class SequenceSynch implements Synch {
        private final SequenceData data;
        private final Sequence parent;

        @JsonCreator
        public SequenceSynch(@JsonProperty("data") SequenceData data, @JsonProperty("parent") Sequence parent) {
            this.data = data;
            this.parent = parent;
        }
        public void ret() {
            ++data._current;
            instance(parent);
        }
    }
}
