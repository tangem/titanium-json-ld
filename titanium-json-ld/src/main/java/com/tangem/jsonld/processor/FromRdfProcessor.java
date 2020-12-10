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
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.serialization.RdfToJsonld;

public final class FromRdfProcessor {

    private FromRdfProcessor() {
    }
    
    public static final JsonArray fromRdf(final Document document, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {

        return RdfToJsonld
                    .with(document.getRdfContent().orElseThrow(() -> new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Expected RDF document but got [" + document.getContentType() + "]")))
                    .ordered(options.isOrdered())
                    .rdfDirection(options.getRdfDirection())
                    .useNativeTypes(options.isUseNativeTypes())
                    .useRdfType(options.isUseRdfType())
                    .processingMode(options.getProcessingMode())
                    .build();
    }

    public static JsonArray fromRdf(URI documentUri, JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {

        if (options.getDocumentLoader() == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + documentUri + "].");
        }

        final Document remoteDocument = 
                                options
                                    .getDocumentLoader()
                                    .loadDocument(documentUri,
                                            new DocumentLoaderOptions()
                                                    );

        if (remoteDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }
        
        return fromRdf(remoteDocument, options);
    }
}
