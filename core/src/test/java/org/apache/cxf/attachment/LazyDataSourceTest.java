/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;

import org.junit.Test;

import static org.junit.Assert.*;

public class LazyDataSourceTest {

    private static final String ID_1 = "id1";
    private static final String ID_2 = "id2";

    private static final String CONTENT_TYPE = "contentType";

    private static final DataHandler DATA_HANDLER = new DataHandler((DataSource) null) {
        @Override
        public DataSource getDataSource() {
            return new DataSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    return null;
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return null;
                }

                @Override
                public String getContentType() {
                    return CONTENT_TYPE;
                }

                @Override
                public String getName() {
                    return null;
                }
            };
        }
    };

    @Test
    public void testInternalLoadOK() throws Exception {
        DataSource ds = new LazyDataSource(ID_1,
                Collections.singleton(new AttachmentImpl(ID_1, DATA_HANDLER)));

        assertEquals(CONTENT_TYPE, ds.getContentType());
    }

    @Test
    public void testUrlEncodedId() throws Exception {
        String id = "tes123_123@org:apache:cxf";
        String urlEncodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        DataSource ds = new LazyDataSource(id, Collections.singleton(new AttachmentImpl(urlEncodedId, DATA_HANDLER)));
        ds.getContentType(); // No exception thrown
    }

    @Test
    public void testNoDataSource() throws Exception {
        DataSource ds = new LazyDataSource(ID_1,
                Collections.singleton(new AttachmentImpl(ID_1, new DataHandler((DataSource) null) {
                    @Override
                    public DataSource getDataSource() {
                        return null;
                    }
                })));
        try {
            ds.getContentType();
            fail();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            assertTrue(message, message.contains(ID_1));
        }
    }

    @Test
    public void testNoAttachment() throws Exception {
        DataSource ds = new LazyDataSource(ID_1, Collections.singleton(new AttachmentImpl(ID_2)));
        try {
            ds.getName();
            fail();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            assertTrue(message, message.contains(ID_1));
            assertTrue(message, message.contains(ID_2));
        }
    }

}
