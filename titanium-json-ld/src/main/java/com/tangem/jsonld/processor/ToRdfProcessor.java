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
package com.tangem.jsonld.processor;

import java.net.URI;

import javax.json.JsonArray;

import com.tangem.jsonld.api.JsonLdErrorCode;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.loader.DocumentLoaderOptions;
import com.tangem.jsonld.processor.ExpansionProcessor;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.deseralization.JsonLdToRdf;
import com.tangem.jsonld.flattening.NodeMap;
import com.tangem.jsonld.flattening.NodeMapBuilder;
import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfDataset;

/**
 * 
 * @see <a href="https://w3c.github.io/json-ld-api/#dom-jsonldprocessor-tordf">JsonLdProcessor.toRdf()</a>
 *
 */
public final class ToRdfProcessor {

    private ToRdfProcessor() {
    }

    public static final com.tangem.rdf.RdfDataset toRdf(final URI input, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {

        if (options.getDocumentLoader() == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + input + "].");
        }
        
        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

        final Document remoteDocument = options.getDocumentLoader().loadDocument(input, loaderOptions);

        if (remoteDocument == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }
        
        return toRdf(remoteDocument, options);
    }

    public static final RdfDataset toRdf(Document input, final com.tangem.jsonld.api.JsonLdOptions options) throws JsonLdError {

        final com.tangem.jsonld.api.JsonLdOptions expansionOptions = new JsonLdOptions(options);
        
        expansionOptions.setProcessingMode(options.getProcessingMode());
        expansionOptions.setBase(options.getBase());
        expansionOptions.setExpandContext(options.getExpandContext());
        
        final JsonArray expandedInput = ExpansionProcessor.expand(input, expansionOptions, false);

        return JsonLdToRdf
                        .with(
                            NodeMapBuilder.with(expandedInput, new NodeMap()).build(),
                            Rdf.createDataset()
                            )
                        .produceGeneralizedRdf(options.isProduceGeneralizedRdf())
                        .rdfDirection(options.getRdfDirection())
                        .build();     
    }
}
