/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.common.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * @author Christian Bremer
 */
public class BufferSchemaOutputResolver extends SchemaOutputResolver {

    protected Map<String, StreamResult> buffers = new HashMap<String, StreamResult>();

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        StringWriter out = new StringWriter();
        StreamResult res = new StreamResult(out);
        res.setSystemId(suggestedFileName);
        buffers.put(namespaceUri, res);
        return res;
    }

    public Set<String> getNamespaces() {
        return buffers.keySet();
    }

    public String getSchema(String namespaceUri) {
        return buffers.get(namespaceUri).getWriter().toString();
    }

    public String getSystemId(String namespaceUri) {
        return buffers.get(namespaceUri).getSystemId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : buffers.keySet()) {
            sb.append("***** Begin schema ").append(s).append(", system-id=").append(getSystemId(s)).append(" *****");
            sb.append(System.getProperty("line.separator"));
            sb.append(getSchema(s));
            sb.append("***** End schema ").append(s).append(" *****");
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public void clear() {
        buffers.clear();
    }

}
