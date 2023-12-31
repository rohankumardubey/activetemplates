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

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import com.google.code.activetemplates.xml.XmlResult;
import com.google.code.activetemplates.xml.XmlSource;
import com.google.code.activetemplates.xml.XmlStreamSource;

public class DirectoryTileSource implements TileSource {
    
    private File dir;
    
    public DirectoryTileSource(String dir) {
        this(new File(dir));
    }

    public DirectoryTileSource(File dir) {
        this.dir = dir;
    }

    @Override
    public XmlSource getTile(String name) {
        return new XmlStreamSource(new StreamSource(new File(dir, name)));
    }

    @Override
    public boolean readTile(String name, XmlResult res) {
        
        XmlSource s = getTile(name);
        if(s == null) return false;
        
        try {
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer t = tFactory.newTransformer();
            t.transform(s.getSource(), res.getResult());
            
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        } finally {
            s.close();
        }
        
        return true;
    }

}
