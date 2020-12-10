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
package com.tangem.jsonld.deseralization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonValue;

import com.tangem.jsonld.api.JsonLdOptions.RdfDirection;
import com.tangem.jsonld.flattening.NodeMap;
import com.tangem.jsonld.json.JsonUtils;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfTriple;
import com.tangem.rdf.RdfValue;
import com.tangem.rdf.lang.RdfConstants;

/**
 * 
 * @see <a href="https://w3c.github.io/json-ld-api/#list-to-rdf-conversion">List to RDF Conversion</a>
 *
 */
final class ListToRdf {

    // required
    private JsonArray list;
    private List<com.tangem.rdf.RdfTriple> triples;
    private NodeMap nodeMap;
    
    // optional
    private JsonLdOptions.RdfDirection rdfDirection;
    
    private ListToRdf(final JsonArray list, final List<com.tangem.rdf.RdfTriple> triples, NodeMap nodeMap) {
        this.list = list;
        this.triples = triples;
        this.nodeMap = nodeMap;
    }
    
    public static final ListToRdf with(final JsonArray list, final List<com.tangem.rdf.RdfTriple> triples, NodeMap nodeMap) {
        return new ListToRdf(list, triples, nodeMap);
    }
    
    public ListToRdf rdfDirection(JsonLdOptions.RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }
    
    public com.tangem.rdf.RdfValue build() throws JsonLdError {
        
        // 1.
        if (JsonUtils.isEmptyArray(list)) {
            return com.tangem.rdf.Rdf.createIRI(com.tangem.rdf.lang.RdfConstants.NIL);
        }

        // 2.
        final String[] bnodes = new String[list.size()];

        IntStream.range(0,  bnodes.length).forEach(i -> bnodes[i] = nodeMap.createIdentifier());

        // 3.
        int index = 0;
        for (final JsonValue item : list) {
            
            final String subject = bnodes[index];
            index++;
            
            // 3.1.
            final List<RdfTriple> embeddedTriples = new ArrayList<>();
            
            // 3.2.
            ObjectToRdf
                .with(item.asJsonObject(), embeddedTriples, nodeMap)
                .rdfDirection(rdfDirection)
                .build()
                .ifPresent(object -> 
                                triples.add(com.tangem.rdf.Rdf.createTriple(
                                                com.tangem.rdf.Rdf.createBlankNode(subject),
                                                com.tangem.rdf.Rdf.createIRI(com.tangem.rdf.lang.RdfConstants.FIRST),
                                                object)));

            // 3.4.
            final RdfValue rest = (index < bnodes.length) ? com.tangem.rdf.Rdf.createBlankNode(bnodes[index])
                                        : com.tangem.rdf.Rdf.createIRI(com.tangem.rdf.lang.RdfConstants.NIL)
                                        ;
            
            triples.add(com.tangem.rdf.Rdf.createTriple(
                                    com.tangem.rdf.Rdf.createBlankNode(subject),
                                    com.tangem.rdf.Rdf.createIRI(RdfConstants.REST),
                                    rest
                                    ));
            
            // 3.5.
            triples.addAll(embeddedTriples);
        }
        
        // 4.
        return Rdf.createBlankNode(bnodes[0]);
    }
}
