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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.ode.jacob.IndexedObject;
import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.MessageListener;
import org.apache.ode.jacob.oo.Channel;
import org.apache.ode.jacob.oo.ChannelListener;
import org.apache.ode.jacob.soup.Comm;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.soup.CommGroup;
import org.apache.ode.jacob.soup.CommRecv;
import org.apache.ode.jacob.soup.CommSend;
import org.apache.ode.jacob.soup.ExecutionQueue;
import org.apache.ode.jacob.soup.ExecutionQueueObject;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fast, in-memory {@link org.apache.ode.jacob.soup.ExecutionQueue} implementation.
 */
public class ExecutionQueueImpl implements ExecutionQueue {
    public static ConcurrentHashMap<String, ObjectStreamClass> _classDescriptors = 
        new ConcurrentHashMap<String, ObjectStreamClass>();

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionQueueImpl.class);

    private ClassLoader _classLoader;

    /**
     * Cached set of enqueued {@link Message} objects (i.e. those read using
     * {@link #enqueueMessage(Message)}).
     * These reactions are "cached"--that is it is not sent directly to the DAO
     * layer--to minimize unnecessary serialization/deserialization of closures.
     * This is a pretty useful optimization, as most {@link Message}s are
     * enqueued, and then immediately dequeued in the next cycle. By caching
     * {@link Message}s, we eliminate practically all serialization of
     * these objects, the only exception being cases where the system decides to
     * stop processing a particular soup despite the soup being able to make
     * forward progress; this scenario would occur if a maximum processign
     * time-per-instance policy were in effect.
     */
    protected Set<Message> _messages = new LinkedHashSet<Message>();

    protected Map<Integer, ChannelFrame> _channels = new LinkedHashMap<Integer, ChannelFrame>();

    /**
     * The "expected" cycle counter, use to detect database serialization
     * issues.
     */
    protected int _currentCycle;

    protected int _objIdCounter;

    private ReplacementMap _replacementMap;

    protected Serializable _gdata;

    private Map<Object, LinkedList<IndexedObject>> _index = new LinkedHashMap<Object, LinkedList<IndexedObject>>();

    public ExecutionQueueImpl() {}
    
    public ExecutionQueueImpl(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    public void setReplacementMap(ReplacementMap replacementMap) {
        _replacementMap = replacementMap;
    }

    public Map<Object, LinkedList<IndexedObject>> getIndex() {
        return _index;
    }

    public void add(CommChannel channel) {
        LOG.trace(">> add (channel={})", channel);

        verifyNew(channel);
        ChannelFrame cframe = new ChannelFrame(channel.getType(), ++_objIdCounter, 
                channel.getDescription());
        _channels.put(cframe.getId(), cframe);
        assignId(channel, cframe.getId());
    }

    public void enqueueMessage(Message message) {
        LOG.trace(">> enqueueMessage (message={})", message);

        _messages.add(message);
    }

    public Message dequeueMessage() {
        LOG.trace(">> dequeueMessage ()");

        Message message = null;
        if (!_messages.isEmpty()) {
            Iterator<Message> it = _messages.iterator();
            message = it.next();
            it.remove();
        }
        return message;
    }

    public void add(CommGroup group) {
        LOG.trace(">> add (group={})", group);

        verifyNew(group);
        CommGroupFrame commGroupFrame = new CommGroupFrame(group.isReplicated());
        for (Iterator<Comm> i = group.getElements(); i.hasNext();) {
            Comm comm = i.next();
            ChannelFrame chnlFrame = findChannelFrame(comm.getChannel().getId());
            if (comm instanceof CommSend) {
                CommSend commSend = (CommSend) comm;
                MessageFrame mframe = new MessageFrame(commGroupFrame, chnlFrame, commSend.getMessage());
                commGroupFrame.commFrames.add(mframe);
                chnlFrame.msgFrames.add(mframe);
            } else if (comm instanceof CommRecv) {
                if (chnlFrame.replicated) {
                    // TODO: JACOB "bad-process" ex
                    throw new IllegalStateException(
                            "Receive attempted on channel containing replicated receive! Channel= " + comm.getChannel());
                }
                if (group.isReplicated()) {
                    chnlFrame.replicated = true;
                }
                CommRecv commRecv = (CommRecv) comm;
                ListenerFrame oframe = new ListenerFrame(commGroupFrame, chnlFrame, commRecv.getListener());
                commGroupFrame.commFrames.add(oframe);
                chnlFrame.objFrames.add(oframe);
            }
        }

        // Match communications.
        for (Iterator<Comm> i = group.getElements(); i.hasNext();) {
            Comm comm = i.next();
            matchCommunications(comm.getChannel());
        }
    }

    private ChannelFrame findChannelFrame(Object id) {
        ChannelFrame chnlFrame = _channels.get(id);
        if (chnlFrame == null) {
            throw new IllegalArgumentException("No such channel; id=" + id);
        }
        return chnlFrame;
    }

    public int cycle() {
        LOG.trace(">> cycle ()");
        return ++_currentCycle;
    }

    public String createExport(CommChannel channel) {
        LOG.trace(">> createExport (channel={})", channel);

        ChannelFrame cframe = findChannelFrame(channel.getId());
        cframe.refCount++;
        return channel.getId().toString();
    }

    public CommChannel consumeExport(String exportId) {
        LOG.trace(">> consumeExport (exportId={})", exportId);

        Integer id = Integer.valueOf(exportId);
        ChannelFrame cframe = findChannelFrame(id);
        cframe.refCount--;
        CommChannel commChannel = new CommChannel(cframe.type);
        commChannel.setId(id);
        commChannel.setDescription("EXPORTED CHANNEL");
        return commChannel;
    }

    public boolean hasReactions() {
        return !_messages.isEmpty();
    }

    public void flush() {
        LOG.trace(">> flush ()");
    }

    public void read(InputStream iis) throws IOException, ClassNotFoundException {
        _channels.clear();
        _messages.clear();
        _index.clear();

        ExecutionQueueInputStream sis = new ExecutionQueueInputStream(iis);

        _objIdCounter = sis.readInt();
        _currentCycle = sis.readInt();
        int reactions = sis.readInt();
        for (int i = 0; i < reactions; ++i) {
            _messages.add((Message)sis.readObject());
        }

        int numChannels = sis.readInt();
        for (int i = 0; i < numChannels; ++i) {
            int objFrames = sis.readInt();
            for (int j = 0; j < objFrames; ++j) {
                sis.readObject();
            }
            int msgFrames = sis.readInt();
            for (int j = 0; j < msgFrames; ++j) {
                sis.readObject();
            }
        }

        numChannels = sis.readInt();
        for (int i = 0; i < numChannels; ++i) {
            ChannelFrame cframe = (ChannelFrame) sis.readObject();
            _channels.put(cframe.getId(), cframe);
        }
        _gdata = (Serializable) sis.readObject();
        sis.close();
    }

    private void index(IndexedObject object) {
        LinkedList<IndexedObject> vals = _index.get(object.getKey());
        if (vals == null) {
            vals = new LinkedList<IndexedObject>();
            _index.put(object.getKey(), vals);
        }
        vals.add(object);
    }

    public void write(OutputStream oos) throws IOException {
        flush();

        ExecutionQueueOutputStream sos = new ExecutionQueueOutputStream(oos);
//        XQXMLOutputStream sos = createObjectOutputStream(new OutputStreamWriter(oos));

        sos.writeInt(_objIdCounter);
        sos.writeInt(_currentCycle);

        // Write out the reactions.
        sos.writeInt(_messages.size());
        for (Message m : _messages) {
            sos.writeObject(m);
        }

        sos.writeInt(_channels.values().size());
        for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = i.next();
            sos.writeInt(cframe.objFrames.size());
            for (Iterator<ListenerFrame> j = cframe.objFrames.iterator(); j.hasNext();) {
                sos.writeObject(j.next());
            }
            sos.writeInt(cframe.msgFrames.size());
            for (Iterator<MessageFrame> j = cframe.msgFrames.iterator(); j.hasNext();) {
                sos.writeObject(j.next());
            }
        }

        Set<Object> referencedChannels = sos.getSerializedChannels();
        for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = i.next();
            if (referencedChannels.contains(Integer.valueOf(cframe.id)) || cframe.refCount > 0) {
                // skip
            } else {
                LOG.debug("GC Channel: {}", cframe);
                i.remove();
            }

        }

        sos.writeInt(_channels.values().size());
        for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = i.next();
            LOG.debug("Writing Channel: {}", cframe);
            sos.writeObject(cframe);
        }

        // Write the global data.
        sos.writeObject(_gdata);
        sos.close();
    }

    public boolean isComplete() {
        // If we have more reactions we're not done.
        if (!_messages.isEmpty()) {
            return false;
        }

        // If we have no reactions, but there are some channels that have
        // external references, we are not done.
        for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext();) {
            if (i.next().refCount > 0) {
                return false;
            }
        }
        return true;
    }

    public void dumpState(PrintStream ps) {
        ps.print(this.toString());
        ps.println(" state dump:");
        ps.println("-- GENERAL INFO");
        ps.println("   Current Cycle          : " + _currentCycle);
        ps.println("   Num. Reactions  : " + _messages.size());
        if (!_messages.isEmpty()) {
            ps.println("-- REACTIONS");
            int cnt = 0;
            for (Message m : _messages) {
                ps.println("   #" + (++cnt) + ":  " + m.toString());
            }
        }
        if (!_channels.isEmpty()) {
            ps.println("-- CHANNELS");
            int cnt = 0;
            for (ChannelFrame channel : _channels.values()) {
                ps.println("   #" + (++cnt) + ":  " + channel.toString());
            }
        }

    }

    private void matchCommunications(CommChannel channel) {
        LOG.trace(">> matchCommunications (channel={})", channel);

        ChannelFrame cframe = _channels.get(channel.getId());
        while (cframe != null && !cframe.msgFrames.isEmpty() && !cframe.objFrames.isEmpty()) {
            MessageFrame mframe = cframe.msgFrames.iterator().next();
            ListenerFrame oframe = cframe.objFrames.iterator().next();

            Message msg = Message.copyFrom(mframe.message);
            msg.setTo(new org.apache.ode.jacob.ChannelRef(oframe.listener));
            
            enqueueMessage(msg);
            if (!mframe.commGroupFrame.replicated) {
                removeCommGroup(mframe.commGroupFrame);
            }
            if (!oframe.commGroupFrame.replicated) {
                removeCommGroup(oframe.commGroupFrame);
            }
        }

        // Do some cleanup, if the channel is empty we can remove it from memory.
        // if (cframe != null && cframe.msgFrames.isEmpty() &&
        // cframe.objFrames.isEmpty() && cframe.refCount ==0)
        // _channels.values().remove(cframe);
    }

    /**
     * Verify that a {@link ExecutionQueueObject} is new, that is it has not
     * already been added to the soup.
     *
     * @param so object to check.
     * @throws IllegalArgumentException in case the object is not new
     */
    private void verifyNew(ExecutionQueueObject so) throws IllegalArgumentException {
        if (so.getId() != null)
            throw new IllegalArgumentException("The object " + so + " is not new!");
    }

    private void assignId(ExecutionQueueObject so, Integer id) {
        so.setId(id);
    }

    private void removeCommGroup(CommGroupFrame groupFrame) {
        // Add all channels reference in the group to the GC candidate set.
        for (Iterator<CommFrame> i = groupFrame.commFrames.iterator(); i.hasNext();) {
            CommFrame frame = i.next();
            if (frame instanceof ListenerFrame) {
                assert frame.channelFrame.objFrames.contains(frame);
                frame.channelFrame.objFrames.remove(frame);
            } else {
                assert frame instanceof MessageFrame;
                assert frame.channelFrame.msgFrames.contains(frame);
                frame.channelFrame.msgFrames.remove(frame);
            }
        }
    }

    public void setGlobalData(Serializable data) {
        _gdata = data;
    }

    public Serializable getGlobalData() {
        return _gdata;
    }

    protected static class ChannelFrame implements Externalizable {
        Class<?> type;

        int id;

        /** External Reference Count */
        int refCount;

        boolean replicated;

        Set<ListenerFrame> objFrames = new LinkedHashSet<ListenerFrame>();

        Set<MessageFrame> msgFrames = new LinkedHashSet<MessageFrame>();

        public String description;

        // Used for deserialization
        public ChannelFrame() {
        }

        public ChannelFrame(Class<?> type, int id, String description) {
            this.type = type;
            this.id = id;
            this.description = description;
        }

        public Integer getId() {
            return Integer.valueOf(id);
        }
        
        public int getRefCount() {
            return refCount;
        }
        
        public Set<ListenerFrame> getObjFrames() {
            return objFrames;
        }

        public Set<MessageFrame> getMsgFrames() {
            return msgFrames;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            type = (Class<?>)in.readObject();
            id = in.readInt();
            description = in.readUTF();
            refCount = in.readInt();
            replicated = in.readBoolean();
            int cnt = in.readInt();
            for (int i = 0; i < cnt; ++i) {
                objFrames.add((ListenerFrame) in.readObject());
            }
            cnt = in.readInt();
            for (int i = 0; i < cnt; ++i) {
                msgFrames.add((MessageFrame) in.readObject());
            }
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(type);
            out.writeInt(id);
            out.writeUTF(description == null ? "" : description);
            out.writeInt(refCount);
            out.writeBoolean(replicated);
            out.writeInt(objFrames.size());
            for (Iterator<ListenerFrame> i = objFrames.iterator(); i.hasNext();) {
                out.writeObject(i.next());
            }
            out.writeInt(msgFrames.size());
            for (Iterator<MessageFrame> i = msgFrames.iterator(); i.hasNext();)
                out.writeObject(i.next());
        }

        public String toString() {
            StringBuffer buf = new StringBuffer(32);
            buf.append("{CFRAME ");
            buf.append(type == null ? "untyped" : type.getSimpleName());
            buf.append(':');
            buf.append(description);
            buf.append('#');
            buf.append(id);
            buf.append(" refCount=");
            buf.append(refCount);
            buf.append(", msgs=");
            buf.append(msgFrames.size());
            buf.append(", objs=");
            buf.append(objFrames.size());
            if (replicated) {
                buf.append("R");
            }
            buf.append("}");
            return buf.toString();
        }
    }

    @SuppressWarnings("serial")
    protected static class CommGroupFrame implements Serializable {
        boolean replicated;
        public Set<CommFrame> commFrames = new LinkedHashSet<CommFrame>();

        // default constructor for deserialization
        public CommGroupFrame() {}

        public CommGroupFrame(boolean replicated) {
            this.replicated = replicated;
        }
    }

    protected static class CommFrame implements Externalizable {
        CommGroupFrame commGroupFrame;
        ChannelFrame channelFrame;

        public CommFrame() {
        }

        CommFrame(CommGroupFrame commGroupFrame, ChannelFrame channelFrame) {
            this.commGroupFrame = commGroupFrame;
            this.channelFrame = channelFrame;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            commGroupFrame = (CommGroupFrame) in.readObject();
            channelFrame = (ChannelFrame) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(commGroupFrame);
            out.writeObject(channelFrame);
        }
    }

    protected static class ListenerFrame extends CommFrame implements Externalizable {
        private static final long serialVersionUID = -7212430608484116919L;

        MessageListener listener;

        // Used for deserialization
        public ListenerFrame() {
        }

        public ListenerFrame(CommGroupFrame commGroupFrame, ChannelFrame channelFrame, MessageListener listener) {
            super(commGroupFrame, channelFrame);
            this.listener = listener;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            listener = (ChannelListener)in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(listener);
        }
    }

    protected static class MessageFrame extends CommFrame implements Externalizable {
        private static final long serialVersionUID = -1112437852498126297L;

        Message message;

        // Used for deserialization
        public MessageFrame() {
        }

        public MessageFrame(CommGroupFrame commFrame, ChannelFrame channelFrame, Message msg) {
            super(commFrame, channelFrame);
            this.message = msg;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            message = (Message)in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(message);
        }
    }

    /**
     * DOCUMENTME.
     * <p>
     * Created on Feb 16, 2004 at 8:09:48 PM.
     * </p>
     *
     * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
     */
    private class ExecutionQueueOutputStream extends ObjectOutputStream {
        private Set<Object> _serializedChannels = new HashSet<Object>();

        public ExecutionQueueOutputStream(OutputStream outputStream) throws IOException {
            super(new GZIPOutputStream(outputStream));
            enableReplaceObject(true);
        }

        public Set<Object> getSerializedChannels() {
            return _serializedChannels;
        }

        protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
            if (Serializable.class.isAssignableFrom(desc.forClass())) {
                writeBoolean(true);
                writeUTF(desc.getName());
            } else {
                writeBoolean(false);
                super.writeClassDescriptor(desc);
            }
        }

        /**
         * Use this method to spy on any channels that are being serialized to
         * this stream.
         *
         * @param obj
         * @return
         * @throws IOException
         */
        protected Object replaceObject(Object obj) throws IOException {
            if (!Serializable.class.isAssignableFrom(obj.getClass()) &&
                    !(_replacementMap != null && _replacementMap.isReplaceable(obj))) {
                throw new IllegalArgumentException("Cannot replace non-serializable or non-replacable instance of " + obj.getClass());
            }

            if (obj instanceof org.apache.ode.jacob.oo.ChannelProxy) {
                CommChannel commChannel = ChannelFactory.getBackend((Channel)obj);
                _serializedChannels.add(commChannel.getId());
                return new ChannelRef(commChannel.getType(), (Integer) commChannel.getId());
            } else if (_replacementMap != null && _replacementMap.isReplaceable(obj)) {
                Object replacement = _replacementMap.getReplacement(obj);
                LOG.debug("ReplacmentMap: getReplacement({}) = {}", obj, replacement);
                return replacement;
            }

            return obj;
        }
    }

    /**
     */
    public class ExecutionQueueInputStream extends ObjectInputStream {
        private Set<CommChannel> _deserializedChannels = new HashSet<CommChannel>();

        public ExecutionQueueInputStream(InputStream in) throws IOException {
            super(new GZIPInputStream(in));
            enableResolveObject(true);
        }

        public Set<CommChannel> getSerializedChannels() {
            return _deserializedChannels;
        }

        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return Class.forName(desc.getName(), true, _classLoader);
        }

        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            boolean ser = readBoolean();
            if (ser) {
                String clsName = readUTF();
                ObjectStreamClass cached = _classDescriptors.get(clsName);
                if (cached == null) {
                    cached = ObjectStreamClass.lookup(Class.forName(clsName, true, _classLoader));
                    _classDescriptors.put(clsName, cached);
                }
                return cached;
            }
            return super.readClassDescriptor();
        }

        protected Object resolveObject(Object obj) throws IOException {
            Object resolved;

            if (obj instanceof ChannelRef) {
                // We know this is a channel reference, so we have to resolve
                // the channel.
                ChannelRef oref = (ChannelRef) obj;
                CommChannel channel = new CommChannel(oref._type);
                channel.setId(oref._id);
                _deserializedChannels.add(channel);
                resolved = ChannelFactory.createChannel(channel, channel.getType());
            } else if (_replacementMap != null && _replacementMap.isReplacement(obj)) {
                resolved = _replacementMap.getOriginal(obj);
                LOG.debug("ReplacementMap: getOriginal({}) = {}", obj, resolved);
            } else {
                resolved = obj;
            }

            if (resolved != null && resolved instanceof IndexedObject)
                index((IndexedObject) resolved);

            return resolved;
        }
    }

    protected static final class ChannelRef implements Externalizable {
        private Class<?> _type;

        private Integer _id;

        private ChannelRef(Class<?> type, Integer id) {
            _type = type;
            _id = id;
        }

        // Used for deserialization
        public ChannelRef() {
        }

        public boolean equals(Object obj) {
            return ((ChannelRef) obj)._id.equals(_id);
        }

        public int hashCode() {
            return _id.hashCode();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(_type);
            out.writeInt(_id.intValue());
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            _type = (Class<?>)in.readObject();
            _id = Integer.valueOf(in.readInt());
        }
    }
}
