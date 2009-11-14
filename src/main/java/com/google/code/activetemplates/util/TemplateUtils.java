/*
 * Copyright 2009 Anton Tanasenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.activetemplates.util;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.google.code.activetemplates.events.Action;
import com.google.code.activetemplates.events.StartElementEvent;
import com.google.code.activetemplates.events.TemplateEvent;

public final class TemplateUtils {

    /**
     * Skip all elements until current elements's end tag is reached.
     * 
     * @param te
     * @param skipEnd - whether to skip end tag itself, useful from attribute events
     * @throws XMLStreamException
     */
    public static void skipChildren(TemplateEvent te, boolean skipEnd) throws XMLStreamException {
        readElements(te, 1, skipEnd, null);
    }

    /**
     * Skip all elements until parent tag's end is encountered
     * 
     * @param te
     * @throws XMLStreamException
     */
    public static void skipSiblings(TemplateEvent te) throws XMLStreamException {
        readElements(te, 2, true, null);
    }
    
    /**
     * Read all elements into a queue until current elements's end tag is reached.
     * 
     * @param te
     * @param readEnd - whether to read end tag itself, useful from attribute events
     * @return queue
     * @throws XMLStreamException
     */
    public static Queue<XMLEvent> readChildren(TemplateEvent te, boolean readEnd) throws XMLStreamException {
        Queue<XMLEvent> q = new ArrayDeque<XMLEvent>();
        readElements(te, 1, readEnd, q);
        return q;
    }

    /**
     * Read all elements into a queue until parent tag's end is encountered
     * 
     * @param te
     * @return queue
     * @throws XMLStreamException
     */
    public static Queue<XMLEvent> readSiblings(TemplateEvent te) throws XMLStreamException {
        Queue<XMLEvent> q = new ArrayDeque<XMLEvent>();
        readElements(te, 2, true, q);
        return q;
    }

    // skip elements until level reaches 0
    private static void readElements(TemplateEvent te, int initialLevel, boolean readEnd, Queue<XMLEvent> q) throws XMLStreamException {
        
        while(te.hasNextEvent()) {
            XMLEvent e = te.peekEvent();
            
            if(e.isStartElement()) {
                initialLevel++;
            } else if(e.isEndElement()) {
                initialLevel--;
            }
            
            if(initialLevel == 0) {
                // do not remove the event if we need to process it later
                if(readEnd) {
                    e = te.nextEvent();
                    if(q != null) q.offer(e);
                }
                break;
            }
            e = te.nextEvent();
            if(q != null) q.offer(e);
            
        }
        
    }
    
    /**
     * Returns value of the specified attribute or defValue, 
     * if no such attribute is specified
     * 
     * @param se
     * @param attribute
     * @param defValue
     * @return
     */
    public static String getAttribute(StartElementEvent se, QName attribute, String defValue) {
        Attribute a = se.getEvent().getAttributeByName(attribute);
        if(a == null) return defValue;
        return a.getValue();
    }
    
    /**
     * Returns value of the specified attribute, or throws an IllegalArgumentException
     * if no such attribute is specified
     * 
     * @param se
     * @param attribute
     * @return
     */
    public static String getAttribute(StartElementEvent se, QName attribute) {
        Attribute a = se.getEvent().getAttributeByName(attribute);
        if(a == null) throw new IllegalArgumentException("Attribute " + attribute.getLocalPart() + " not specified");
        return a.getValue();
    }
    
    public static final Action END_SCOPE_ACTION = new Action() {
        public void execute(TemplateEvent e) {
            e.endScope();
        }
    };
}