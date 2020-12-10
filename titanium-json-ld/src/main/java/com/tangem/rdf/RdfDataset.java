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
package com.tangem.rdf;

import com.tangem.rdf.RdfGraph;
import com.tangem.rdf.RdfNQuad;
import com.tangem.rdf.RdfResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RdfDataset {

    com.tangem.rdf.RdfGraph getDefaultGraph();

    void add(com.tangem.rdf.RdfNQuad nquad);
        
    List<RdfNQuad> toList();
    
    Set<com.tangem.rdf.RdfResource> getGraphNames();
    
    Optional<RdfGraph> getGraph(RdfResource graphName);

    /**
     * 
     * @return total number of N-Quads in the dataset 
     */    
    int size();
}
