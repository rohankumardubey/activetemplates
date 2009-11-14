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

package com.google.code.activetemplates.impl;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.google.code.activetemplates.bind.BindingContext;
import com.google.code.activetemplates.bind.Bindings;
import com.google.code.activetemplates.events.Action;
import com.google.code.activetemplates.events.TemplateEvent;
import com.google.code.activetemplates.script.ScriptingProvider;

abstract class TemplateEventImpl implements TemplateEvent {
    
    private CompileContext cc;
    private XMLEvent e;
    
    void init(CompileContext cc, XMLEvent e) {
        this.cc = cc;
        this.e = e;
    }

    public void dispose() {
        cc = null;
        e = null;
    }

    public BindingContext getBindingContext() {
        return cc.getBindingContext();
    }
    
    public void startScope(boolean topLevel) {
        Bindings b;
        if(topLevel) {
            b = cc.getTopLevelBindings();
        } else {
            b = getBindingContext().getBindings();
        }
        b = cc.getScriptingProvider().createBindings(b);
        cc.pushBindings(b);
    }

    public void endScope() {
        cc.popBindings();
    }

    public XMLEvent getEvent(){
        return e;
    }

    public XMLEventFactory getEventFactory() {
        return cc.getElementFactory();
    }
    
    public ScriptingProvider getScriptingProvider(){
        return cc.getScriptingProvider();
    }
    
    public boolean hasNextEvent() {
        return cc.hasNextEvent();
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        return cc.nextEvent();
    }

    public XMLEvent peekEvent() throws XMLStreamException {
        return cc.peekEvent();
    }

    public void queueEvent(XMLEvent e) {
        cc.queueEvent(e);
    }

    public void queueAction(Action a) {
        
        String aid = cc.getActionRegistry().registerAction(a);
        System.out.println("Registering action: " + aid);
        
        cc.queueEvent(com.google.code.activetemplates.lib.elements.Action.createActionStartEvent(
                cc.getElementFactory(), aid));
        cc.queueEvent(com.google.code.activetemplates.lib.elements.Action.createActionEndEvent(
                cc.getElementFactory()));
    }

    public void executeAction(String aid) {
        Action a = cc.getActionRegistry().removeAction(aid);
        
        if(a == null) throw new IllegalStateException("No such action: " + aid);
        
        a.execute(this);
    }

}