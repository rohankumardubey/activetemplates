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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.google.code.activetemplates.Template;
import com.google.code.activetemplates.TemplateBuilder;
import com.google.code.activetemplates.tiles.TemplateImpl.Access;
import com.google.code.activetemplates.util.deps.DependencyNode;
import com.google.code.activetemplates.util.deps.DependencyTree;
import com.google.code.activetemplates.xml.XmlCache;
import com.google.code.activetemplates.xml.XmlResult;
import com.google.code.activetemplates.xml.XmlSource;

public class TileBasedTemplateBuilder implements TemplateBuilder {

    private XmlCache xmlCache;
    private Map<String, TileSource> tileSources;
    private List<TemplateDefinitionSource> definitionSources;
    
    /**
     * Returns XmlCache implementation which is used by template builder
     * 
     * @return
     */
    public XmlCache getXmlCache() {
        return xmlCache;
    }

    /**
     * Sets XmlCache implementation to be used by template builder
     * 
     * @param xmlCache
     */
    public void setXmlCache(XmlCache xmlCache) {
        this.xmlCache = xmlCache;
    }

    /**
     * Returns tile sources which are used by template builder
     * @return
     */
    public Map<String, TileSource> getTileSources() {
        return tileSources;
    }

    /**
     * Sets tile sources (tileSource prefixes are mapped to tile sources)
     * to be used by template builder 
     * @param tileSources
     */
    public void setTileSources(Map<String, TileSource> tileSources) {
        this.tileSources = tileSources;
    }
    
    /**
     * Adds a new tile source to the tileSource map
     * 
     * @param key
     * @param tileSource
     */
    public void addTileSource(String key, TileSource tileSource) {
        if(tileSources == null) tileSources = new LinkedHashMap<String, TileSource>();
        tileSources.put(key, tileSource);
    }
    
    /**
     * Returns a list of template definition sources which are used by template builder
     * 
     * @return
     */
    public List<TemplateDefinitionSource> getDefinitionSources() {
        return definitionSources;
    }

    /**
     * Sets a list of template definition sources to be used by template builder
     * 
     * @param definitionSources
     */
    public void setDefinitionSources(List<TemplateDefinitionSource> definitionSources) {
        this.definitionSources = definitionSources;
    }
    
    /**
     * Adds a new template definition source to the list
     * 
     * @param definitionSource
     */
    public void addDefinitionSource(TemplateDefinitionSource definitionSource) {
        if(definitionSources == null) definitionSources = new ArrayList<TemplateDefinitionSource>();
        definitionSources.add(definitionSource);
    }

    @Override
    public List<Template> build() {
        
        List<TemplateNode> tnodes = new ArrayList<TemplateNode>();
        
        for(TemplateDefinitionSource ds: definitionSources) {
            
            for(TemplateDefinition def: ds.getDefinitions()){
                tnodes.add(new TemplateNode(def));
            }
            
        }
        
        DependencyTree<TemplateNode> dt = new DependencyTree<TemplateNode>();
        dt.addAll(tnodes);
        
        Map<String, TemplateImpl> templates = new LinkedHashMap<String, TemplateImpl>();
        
        List<Template> l = new ArrayList<Template>();
        for(TemplateNode tn: dt.getRootChain()) {
            TemplateDefinition def = tn.getDefinition();
            
            TemplateImpl t = buildTemplate(def, templates);
            templates.put(t.getName(), t);
            
            if(t.getAccess() == Access.CONCRETE) {
                l.add(t);
            }
        }
        
        return l;
    }
    
    private TemplateImpl buildTemplate(TemplateDefinition td, Map<String, TemplateImpl> templates) {
        
        TemplateImpl t = new TemplateImpl(xmlCache);
        t.setName(td.getName());
        if(td.isInternal()) {
            t.setAccess(Access.INTERNAL);
        } else if(td.isAbstract()) {
            t.setAccess(Access.ABSTRACT);
        } else {
            t.setAccess(Access.CONCRETE);
        }

        if(!td.isEmpty()) {
            t.setInclusions(new HashMap<String, String>());
            if(td.getSuperTemplate() != null) {
                // inherit source name and inclusions from parent template
                TemplateImpl st = templates.get(td.getSuperTemplate());
                t.setSourceName(st.getSourceName());
                t.getInclusions().putAll(st.getInclusions());
            } else if(td.isEmpty()) {
                
                t.setSourceName(null);
                
            } else {
                t.setSourceName(td.getSource().replaceAll(":", "-"));

                // read tile source if not done already
                if(!t.hasRawSource()) {
                    XmlResult res = t.createRawResult();
                    try {
                        readTile(td.getSource(), res);
                    } finally {
                        res.close();
                    }
                }
                
            }
            
            // override inclusions with those in definition
            t.getInclusions().putAll(td.getInclusions());
            
            // merge source with inclusions
            if(t.getAccess() != Access.ABSTRACT) {
                Map<String, XmlSource> incSources = new HashMap<String, XmlSource>();
                XmlSource s = t.getRawSource();
                XmlResult r = t.createResult();
                try {
                    
                    for(Map.Entry<String, String> e: t.getInclusions().entrySet()) {
                        TemplateImpl incTemplate = templates.get(e.getValue());
                        incSources.put(e.getKey(), incTemplate.createSource());
                    }
                    new TemplateMerger(t.getName(), r, s, incSources).merge();
                    
                } catch (XMLStreamException xe) {
                    
                    throw new IllegalStateException(xe);
                    
                } finally {
                    
                    s.close();
                    r.close();
                    for(Map.Entry<String, XmlSource> e: incSources.entrySet()) {
                        if(e.getValue() != null) {
                            TemplateImpl incTemplate = templates.get(t.getInclusions().get(e.getKey()));
                            if(incTemplate == null) throw new IllegalStateException("Template " + e.getKey() + " not found in the cache");
                            e.getValue().close();
                        }
                    }
                    
                }
            }
            
        }
        
        return t;
    }
    
    
    private void readTile(String name, XmlResult res) {
        
        String[] sn = name.split(":", 2);
        if(sn.length != 2) throw new IllegalArgumentException("Tile named " + name + " does not contain tileSource name");
        
        TileSource ts = tileSources.get(sn[0]);
        if(ts == null) throw new IllegalArgumentException("No such tileSource: " + sn[0]);
        
        if(!ts.readTile(sn[1], res)) throw new IllegalArgumentException("No such tile: " + name);
    }
    
    private static class TemplateNode implements DependencyNode {
        
        private TemplateDefinition definition;
        
        public TemplateNode(TemplateDefinition definition) {
            this.definition = definition;
        }
        
        public TemplateDefinition getDefinition(){
            return definition;
        }

        public Set<String> getDependencies() {
            Set<String> s = new HashSet<String>();
            if(definition.getSuperTemplate() != null) {
                s.add(definition.getSuperTemplate());
            }
            s.addAll(definition.getInclusions().values());
            
            return s;
        }

        public String getId() {
            return definition.getName();
        }
        
    }

}
