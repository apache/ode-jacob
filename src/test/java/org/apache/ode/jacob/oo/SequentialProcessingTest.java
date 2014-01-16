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


import org.apache.ode.jacob.Jacob;
import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.oo.ProcessUtil.Synchronized;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.ode.jacob.Jacob.instance;
import static org.apache.ode.jacob.Jacob.object;
import static org.apache.ode.jacob.oo.ProcessUtil.intercept;
import static org.apache.ode.jacob.oo.ProcessUtil.receive;
import static org.apache.ode.jacob.oo.ProcessUtil.sequence;
import static org.apache.ode.jacob.oo.ProcessUtil.sync;


public class SequentialProcessingTest {

    @SuppressWarnings("serial")
    @Test
    public void testParallelProcesses() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                instance(intercept(sync(null, atomicProcess("A", out)), atomicProcess("0", out)));
                instance(intercept(sync(null, atomicProcess("B", out)), atomicProcess("1", out)));
            }
        });
        // parallelism is proven by process "B" being executed before "A0" and "A1"
        Assert.assertEquals("AB01", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSynchronizeProcess() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                instance(sync(c1, atomicProcess("A", out)));
            }
        });
        // Return hook "x" is executed after process "A" 
        Assert.assertEquals("Ax", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSynchronizeSynchronizedProcess() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                Synch c2 = Jacob.newChannel(Synch.class, "");
                object(receive(c2, new Synch() {
                    public void ret() {
                        out.append("y");
                    }
                }));
                Synchronized process = synchronizedProcess(c1, "S", out);
                instance(sync(c2, process));
            }
        });
        // Both return hooks "x" and "y" are executed after synchronized process "S" 
        Assert.assertEquals("Sxy", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testInterceptProcess() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                instance(intercept(sync(c1, atomicProcess("A", out)), atomicProcess("B", out)));
            }
        });
        // Return interceptor "B" is executed after process "A", but before the hook "x" 
        Assert.assertEquals("ABx", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testInterceptSynchronizedProcess() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                Synch c2 = Jacob.newChannel(Synch.class, "");
                object(receive(c2, new Synch() {
                    public void ret() {
                        out.append("y");
                    }
                }));
                instance(intercept(sync(c1, atomicProcess("A", out)), sync(c2, atomicProcess("B", out))));
            }
        });
        // Return interceptor "B" is executed after process "A", but before the hook "x" 
        Assert.assertEquals("AByx", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSimpleSequence() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Jacob.instance(sequence(
                    atomicProcess("A", out), 
                    atomicProcess("B", out), 
                    atomicProcess("C", out)));
            }
        });
        // TODO: explain 
        Assert.assertEquals("ABC", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSynchronizedSequence() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                Jacob.instance(sequence(c1,
                    atomicProcess("A", out), 
                    atomicProcess("B", out), 
                    atomicProcess("C", out), 
                    atomicProcess("D", out)));
            }
        });
        // TODO: explain 
        Assert.assertEquals("ABCDx", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testTransitiveSequence() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                Jacob.instance(sequence(c1,
                    atomicProcess("A", out), 
                    sequence(atomicProcess("B", out), atomicProcess("C", out)), 
                    atomicProcess("D", out)));
            }
        });
        // TODO: explain 
        Assert.assertEquals("ABCDx", out.toString());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSequenceComposition() {
        final StringBuffer out = new StringBuffer();
        executeProcess(new RunnableProcess() {
            public void run() {
                Synch c1 = Jacob.newChannel(Synch.class, "");
                object(receive(c1, new Synch() {
                    public void ret() {
                        out.append("x");
                    }
                }));
                Synch c2 = Jacob.newChannel(Synch.class, "");
                object(receive(c2, new Synch() {
                    public void ret() {
                        out.append("y");
                    }
                }));
                // just test a more complex scenario once
                Jacob.instance(sequence(c1,
                    sequence(
                        sequence(
                            atomicProcess("A", out),
                            atomicProcess("B", out),
                            atomicProcess("C", out)),
                        atomicProcess("D", out)), 
                    atomicProcess("E", out), 
                    sequence(c2,
                        atomicProcess("F", out), 
                        sequence(
                            atomicProcess("G", out),
                            atomicProcess("H", out)))));
            }
        });
        // TODO: explain 
        Assert.assertEquals("ABCDEFGHxy", out.toString());
    }

    @SuppressWarnings("serial")
    protected RunnableProcess atomicProcess(final String id, final StringBuffer out) {
        return new RunnableProcess() {
            public void run() {
                out.append(id);
            }
        };
    }

    @SuppressWarnings("serial")
    protected Synchronized synchronizedProcess(final Synch callback, final String id, final StringBuffer out) {
        return new Synchronized(callback) {
            public void execute() {
                out.append(id);
            }
        };
    }
    
    protected void executeProcess(final RunnableProcess process) {
        final JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl());
        vpu.inject(process);
        while (vpu.execute()) {
            // keep doing it...
        }
    }

}
