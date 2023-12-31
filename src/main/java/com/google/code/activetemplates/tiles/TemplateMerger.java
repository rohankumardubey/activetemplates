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

package com.google.code.activetemplates.tiles;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.code.activetemplates.impl.handlers.BuiltinHandlerSPI;
import com.google.code.activetemplates.xml.XmlResult;
import com.google.code.activetemplates.xml.XmlSource;

public class TemplateMerger {
    
    private static final QName TAG_INCLUDE = 
        new QName(BuiltinHandlerSPI.NAMESPACE_STDLIB, "include");
    
    private static final QName ATTR_NAME = 
        new QName("name");
    
    private String tName;
    private XmlResult res;
    private XmlSource src;
    private Map<String, XmlSource> inclusions;

    public TemplateMerger(String tName, XmlResult res, XmlSource src, Map<String, XmlSource> inclusions) {
        this.tName = tName;
        this.res = res;
        this.src = src;
        this.inclusions = inclusions;
    }

    public void merge() throws XMLStreamException {
        
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        
        XMLEventWriter w = outFactory.createXMLEventWriter(res.getResult());
        XMLEventReader r = inFactory.createXMLEventReader(src.getSource());
        
        try {
            while(r.hasNext()) {
                XMLEvent e = r.nextEvent();
                
                if(e.isStartElement()) {
                    StartElement se = e.asStartElement();
                    QName qn = se.getName();
                    
                    if(qn.equals(TAG_INCLUDE)) {
                        
                        Attribute incName = se.getAttributeByName(ATTR_NAME);
                        if(incName == null) {
                            throw new IllegalStateException("Inclusion point must specify inject name");
                        }
                            
                        String v = incName.getValue();
                        
                        if(!inclusions.containsKey(v)) {
                            throw new IllegalArgumentException("Inclusion " + v + " not found for template " + tName);
                        }
                        XmlSource s = inclusions.get(v);
                        if(s != null) {
                            XMLEventReader ir = inFactory.createXMLEventReader(s.getSource());
                            try {
                                include(w, ir);
                            } finally {
                                ir.close();
                            }
                        }
                        
                        int num = 1;
                        
                        while(num > 0) {
                            XMLEvent en = r.nextTag();
                            if(en.isStartElement()) num++;
                            else if(en.isEndElement()) num--;
                        }
                        
                        continue;
                    }
                }
                
                //System.out.println("Writing element " + e);
                w.add(e);
            }
        } finally {
            r.close();
            w.close();
        }
    }
    
    private void include(XMLEventWriter w, XMLEventReader r) throws XMLStreamException {
        while(r.hasNext()) {
            XMLEvent e = r.nextEvent();
            if(e.isStartDocument()) continue;
            if(e.isEndDocument()) continue;
            w.add(e);
        }
    }

}
