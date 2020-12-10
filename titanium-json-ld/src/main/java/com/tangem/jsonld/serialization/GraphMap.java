/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tangem.jsonld.serialization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.json.JsonValue;

import com.tangem.jsonld.lang.Keywords;
import com.tangem.jsonld.serialization.RdfToJsonld.Reference;

final class GraphMap {

    //                 graph,     subject,  predicate,  object
    private final Map<String, Map<String, Map<String, JsonValue>>> index;
    
    private final Map<String, Map<String, List<RdfToJsonld.Reference>>> usages;
    
    public GraphMap() {
        this.index = new LinkedHashMap<>();
        this.index.put(Keywords.DEFAULT, new LinkedHashMap<>());
        
        this.usages = new LinkedHashMap<>();
    }

    public boolean contains(final String graphName, final String subject) {
        return index.containsKey(graphName) && index.get(graphName).containsKey(subject);
    }

    public void set(final String graphName, final String subject, final String property, final JsonValue value) {
        index
            .computeIfAbsent(graphName, e -> new LinkedHashMap<>())
            .computeIfAbsent(subject, e -> new LinkedHashMap<>())
            .put(property, value);        
    }

    public Optional<Map<String, JsonValue>> get(final String graphName, final String subject) {
        
        final Map<String, Map<String, JsonValue>> graphMap = index.get(graphName);
        
        if (graphMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(graphMap.get(subject));
    }

    public Optional<JsonValue> get(final String graphName, final String subject, final String property) {
        
        final Map<String, Map<String, JsonValue>> graphMap = index.get(graphName);
        
        if (graphMap == null) {
            return Optional.empty();
        }
        
        final Map<String, JsonValue> subjectMap = graphMap.get(subject);
        
        if (subjectMap == null) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(subjectMap.get(property));
    }

    public Set<String> keys(String graphName) {
        return index.get(graphName).keySet();
    }

    public boolean contains(String graphName) {
        return index.containsKey(graphName);
    }

    public Set<String> keys() {
        return index.keySet();
    }

    public List<RdfToJsonld.Reference> getUsages(String graphName, String subject) {
        return usages.containsKey(graphName) && usages.get(graphName).containsKey(subject)
                    ? usages.get(graphName).get(subject) 
                    : Collections.emptyList();
    }

    public void addUsage(String graphName, String subject, RdfToJsonld.Reference reference) {
        usages.computeIfAbsent(graphName, e -> new LinkedHashMap<>())
            .computeIfAbsent(subject, e -> new LinkedList<>())
            .add(reference)
            ;
    }

    public void remove(String graphName, String subject) {
        index.get(graphName).remove(subject);
    }    
}