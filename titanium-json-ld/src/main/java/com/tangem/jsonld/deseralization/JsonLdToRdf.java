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

import javax.json.JsonString;
import javax.json.JsonValue;

import com.tangem.jsonld.api.JsonLdOptions.RdfDirection;
import com.tangem.jsonld.flattening.NodeMap;
import com.tangem.jsonld.json.JsonUtils;
import com.tangem.jsonld.lang.BlankNode;
import com.tangem.jsonld.lang.Keywords;
import com.tangem.jsonld.uri.UriUtils;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfDataset;
import com.tangem.rdf.RdfResource;
import com.tangem.rdf.RdfTriple;
import com.tangem.rdf.RdfValue;
import com.tangem.rdf.lang.RdfConstants;

public final class JsonLdToRdf {

    // required
    private final NodeMap nodeMap;
    private final com.tangem.rdf.RdfDataset dataset;
    
    // optional
    private boolean produceGeneralizedRdf;
    private JsonLdOptions.RdfDirection rdfDirection;
    
    private JsonLdToRdf(NodeMap nodeMap, com.tangem.rdf.RdfDataset dataset) {
        this.nodeMap = nodeMap;
        this.dataset = dataset;
        
        this.produceGeneralizedRdf = false;
        this.rdfDirection = null;
    }
    
    public static final JsonLdToRdf with(NodeMap nodeMap, com.tangem.rdf.RdfDataset dataset) {
        return new JsonLdToRdf(nodeMap, dataset);
    }
    
    public JsonLdToRdf produceGeneralizedRdf(boolean enable) {
        this.produceGeneralizedRdf = enable;
        return this;
    }

    public JsonLdToRdf rdfDirection(JsonLdOptions.RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }

    public RdfDataset build() throws JsonLdError {
        
        // 1.
        for (final String graphName : nodeMap.graphs(true)) {

            // 1.2.
            final com.tangem.rdf.RdfResource rdfGraphName;
            
            if (Keywords.DEFAULT.equals(graphName)) {
                rdfGraphName = null;
                
            } else {

                // 1.1.
                if (BlankNode.isWellFormed(graphName)) {
                    
                    rdfGraphName = com.tangem.rdf.Rdf.createBlankNode(graphName);
                    
                } else if (UriUtils.isAbsoluteUri(graphName)) {
                 
                    rdfGraphName = com.tangem.rdf.Rdf.createIRI(graphName);
                    
                } else {
                    continue;
                }
            }
            
            // 1.3.
            for (final String subject : nodeMap.subjects(graphName, true)) {
                
                final RdfResource rdfSubject;

                // 1.3.1.
                if (BlankNode.isWellFormed(subject)) {
                    rdfSubject = com.tangem.rdf.Rdf.createBlankNode(subject);
                    
                } else if (UriUtils.isAbsoluteUri(subject)) {
                    rdfSubject = com.tangem.rdf.Rdf.createIRI(subject);
                    
                } else {
                    continue;                    
                }
                
                // 1.3.2.
                for (final String property : nodeMap.properties(graphName, subject, true)) {

                    // 1.3.2.1.
                    if (Keywords.TYPE.equals(property)) {
                        
                        for (JsonValue type : nodeMap.get(graphName, subject, property).asJsonArray()) { 
                        
                            if (JsonUtils.isNotString(type)) {
                                continue;
                            }
                            
                            final String typeString = ((JsonString)type).getString();

                            final RdfValue rdfObject;
                            
                            if (BlankNode.isWellFormed(typeString)) {
                                rdfObject = com.tangem.rdf.Rdf.createBlankNode(typeString);
                                
                            } else if (UriUtils.isAbsoluteUri(typeString)) {
                                rdfObject = com.tangem.rdf.Rdf.createIRI(typeString);
                                
                            } else {
                                continue;
                            }

                            dataset.add(com.tangem.rdf.Rdf.createNQuad(
                                                rdfSubject,
                                                com.tangem.rdf.Rdf.createIRI(RdfConstants.TYPE),
                                                rdfObject,
                                                rdfGraphName
                                            ));
                        }

                    // 1.3.2.2.
                    } else if (!Keywords.contains(property) 
                                    && ((BlankNode.isWellFormed(property) && !produceGeneralizedRdf) 
                                     || UriUtils.isAbsoluteUri(property))) {
                        
                        // 1.3.2.5.
                        for (JsonValue item : nodeMap.get(graphName, subject, property).asJsonArray()) {
                        
                            // 1.3.2.5.1.
                            final List<RdfTriple> listTriples = new ArrayList<>();

                            // 1.3.2.5.2.                            
                            ObjectToRdf
                                    .with(item.asJsonObject(), listTriples, nodeMap)
                                    .rdfDirection(rdfDirection)
                                    .build()
                                    .ifPresent(rdfObject ->
                                                        dataset.add(com.tangem.rdf.Rdf.createNQuad(
                                                                    rdfSubject,
                                                                    com.tangem.rdf.Rdf.createResource(property),
                                                                    rdfObject,
                                                                    rdfGraphName
                                                                    )));
                            // 1.3.2.5.3.
                            listTriples.stream()
                                        .map(triple -> Rdf.createNQuad(triple, rdfGraphName))
                                        .forEach(dataset::add);
                        }
                    }   
                }   
            }
        }
        return dataset;
    }
}
