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

package com.google.code.activetemplates.exp;

import com.google.code.activetemplates.bind.BindingContext;

/**
 * Expansion which delegates resolution process to binding resolver and always
 * uses specified prefix
 * 
 * @author sleepless
 * 
 */
public class FixedBindingExpansion implements Expansion {

    private String prefix;
    private CompoundExpansion compound;

    public FixedBindingExpansion(String prefix, CompoundExpansion ce) {
        this.prefix = prefix;
        compound = ce;
    }

    public void resolve(StringBuilder sb, BindingContext bc) {

        StringBuilder sb2 = new StringBuilder();
        compound.resolve(sb2, bc);
        String binding = sb2.toString();

        Object o = bc.getBindingResolver().resolve(prefix, binding, bc);
        if (o != null) {
            sb.append(o);
        }
    }

    public String toString() {
        return "${ " + compound + " }";
    }

}
