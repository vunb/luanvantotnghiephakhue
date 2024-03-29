/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jorphan.util;

import org.apache.commons.collections.ArrayStack;

// @see org.apache.jorphan.util.TestXMLBuffer for unit tests

/**
 * Provides XML string building methods.
 * Not synchronised.
 *
 */
public class XMLBuffer{
    private StringBuilder sb = new StringBuilder(); // the string so far

    private ArrayStack tags = new ArrayStack(); // opened tags

    public XMLBuffer(){

    }

    private void startTag(String t){
        sb.append("<");
        sb.append(t);
        sb.append(">");
    }

    private void endTag(String t){
        sb.append("</");
        sb.append(t);
        sb.append(">");
        sb.append("\n");
    }

    private void emptyTag(String t){
        sb.append("<");
        sb.append(t);
        sb.append("/>");
        sb.append("\n");
    }

    /**
     * Open a tag; save on stack.
     *
     * @param tagname
     * @return this
     */
    public XMLBuffer openTag(String tagname){
        tags.push(tagname);
        startTag(tagname);
        return this;
    }

    /**
     * Close top tag from stack.
     *
     * @param tagname
     *
     * @return this
     *
     * @throws IllegalArgumentException if the tag names do not match
     */
    public XMLBuffer closeTag(String tagname){
        String tag = (String) tags.pop();
        if (!tag.equals(tagname)) {
            throw new IllegalArgumentException("Trying to close tag: "+tagname+" ; should be "+tag);
        }
        endTag(tag);
        return this;
    }

    /**
     * Add a complete tag with content.
     *
     * @param tagname
     * @param content
     * @return this
     */
    public XMLBuffer tag(String tagname, String content){
        if (content.length() == 0) {
            emptyTag(tagname);
        } else {
            startTag(tagname);
            sb.append(content);
            endTag(tagname);
        }
        return this;
    }

    /**
     * Add a complete tag with content.
     *
     * @param tagname
     * @param content
     * @return this
     */
    public XMLBuffer tag(String tagname,StringBuilder content){
        if (content.length() == 0) {
            emptyTag(tagname);
        } else {
            startTag(tagname);
            sb.append(content);
            endTag(tagname);
        }
        return this;
    }

    /**
     * Convert the buffer to a string, closing any open tags
     */
    @Override
    public String toString(){
        while(!tags.isEmpty()){
            endTag((String)tags.pop());
        }
        return sb.toString();
    }
}