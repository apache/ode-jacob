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
package org.apache.ode.jacob.soup.jackson;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonGeneratorImpl;

/**
 * No-op Json generator.
 * 
 * @author Tammo van Lessen
 *
 */
public class NullJsonGenerator extends JsonGeneratorImpl {

    public NullJsonGenerator(IOContext ctxt, int features, ObjectCodec codec) {
        super(ctxt, features, codec);
    }

    public void flush() throws IOException {
    }

    protected void _releaseBuffers() {
    }

    protected void _verifyValueWrite(String typeMsg) throws IOException,
            JsonGenerationException {
    }

    public void writeStartArray() throws IOException, JsonGenerationException {
    }

    public void writeEndArray() throws IOException, JsonGenerationException {
    }

    public void writeStartObject() throws IOException, JsonGenerationException {
    }

    public void writeEndObject() throws IOException, JsonGenerationException {
    }

    public void writeFieldName(String name) throws IOException,
            JsonGenerationException {
    }

    public void writeString(String text) throws IOException,
            JsonGenerationException {
    }

    public void writeString(char[] text, int offset, int len)
            throws IOException, JsonGenerationException {
    }

    public void writeRawUTF8String(byte[] text, int offset, int length)
            throws IOException, JsonGenerationException {
    }

    public void writeUTF8String(byte[] text, int offset, int length)
            throws IOException, JsonGenerationException {
    }

    public void writeRaw(String text) throws IOException,
            JsonGenerationException {
    }

    public void writeRaw(String text, int offset, int len) throws IOException,
            JsonGenerationException {
    }

    public void writeRaw(char[] text, int offset, int len) throws IOException,
            JsonGenerationException {
    }

    public void writeRaw(char c) throws IOException, JsonGenerationException {
    }

    public void writeBinary(Base64Variant b64variant, byte[] data, int offset,
            int len) throws IOException, JsonGenerationException {
    }

    public void writeNumber(int v) throws IOException, JsonGenerationException {
    }

    public void writeNumber(long v) throws IOException, JsonGenerationException {
    }

    public void writeNumber(BigInteger v) throws IOException,
            JsonGenerationException {
    }

    public void writeNumber(double d) throws IOException,
            JsonGenerationException {
    }

    public void writeNumber(float f) throws IOException,
            JsonGenerationException {
    }

    public void writeNumber(BigDecimal dec) throws IOException,
            JsonGenerationException {
    }

    public void writeNumber(String encodedValue) throws IOException,
            JsonGenerationException, UnsupportedOperationException {
    }

    public void writeBoolean(boolean state) throws IOException,
            JsonGenerationException {
    }

    public void writeNull() throws IOException, JsonGenerationException {
    }
}
