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

import com.google.code.activetemplates.Template;
import com.google.code.activetemplates.xml.XmlCache;
import com.google.code.activetemplates.xml.XmlResult;
import com.google.code.activetemplates.xml.XmlSource;

public class TemplateImpl implements Template {
    
    public enum Access {
        CONCRETE,
        ABSTRACT,
        INTERNAL;
    }

    static final String RAW_PREFIX = "tiles/";
    static final String BUILD_PREFIX = "templates/";

    public XmlCache xmlCache;
    
    private String name;
    private Access access;
    
    private String sourceName;
    private Map<String, String> inclusions;
    
    public TemplateImpl(XmlCache xmlCache) {
        this.xmlCache = xmlCache;
    }
    
    public XmlCache getXmlCache() {
        return xmlCache;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    
    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Map<String, String> getInclusions() {
        return inclusions;
    }

    public void setInclusions(Map<String, String> inclusions) {
        this.inclusions = inclusions;
    }

    public XmlSource createSource(){
        if(sourceName == null) return null;
        return xmlCache.createSource(BUILD_PREFIX + name + ".xml");
    }
    
    public XmlResult createResult() {
        if(sourceName == null) throw new IllegalStateException("Empty source");
        return xmlCache.createResult(BUILD_PREFIX + name + ".xml");
    }
    
    public boolean hasRawSource(){
        if(sourceName == null) return true;
        return xmlCache.contains(RAW_PREFIX + sourceName);
    }

    public XmlSource getRawSource(){
        if(sourceName == null) return null;
        return xmlCache.createSource(RAW_PREFIX + sourceName);
    }
    
    public XmlResult createRawResult() {
        if(sourceName == null) throw new IllegalStateException("Empty source");
        return xmlCache.createResult(RAW_PREFIX + sourceName);
    }

}
