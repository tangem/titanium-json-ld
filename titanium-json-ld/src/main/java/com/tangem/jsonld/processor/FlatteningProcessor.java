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
import javax.json.JsonStructure;

import com.tangem.jsonld.api.JsonLdErrorCode;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.document.JsonDocument;
import com.tangem.jsonld.http.media.MediaType;
import com.tangem.jsonld.loader.DocumentLoaderOptions;
import com.tangem.jsonld.processor.CompactionProcessor;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.flattening.Flattening;

/**
 * 
 * @see <a href="https://www.w3.org/TR/json-ld11-api/#dom-jsonldprocessor-compact">JsonLdProcessor.compact()</a>
 *
 */
public final class FlatteningProcessor {

    private FlatteningProcessor() {
    }
    
    public static final JsonStructure flatten(final URI input, final URI context, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {
        
        if (context == null) {
            return flatten(input, (Document)null, options);
        }
        
        assertDocumentLoader(options, input);
        
        final Document contextDocument = options.getDocumentLoader().loadDocument(context, new DocumentLoaderOptions());

        if (contextDocument == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_REMOTE_CONTEXT, "Context[" + context + "] is null.");
        }
                
        return flatten(input, contextDocument, options);        
    }

    public static final JsonStructure flatten(final Document input, final URI context, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {
        
        if (context == null) {
            return flatten(input, (Document)null, options);
        }

        assertDocumentLoader(options, context);
        
        final Document contextDocument = options.getDocumentLoader().loadDocument(context, new DocumentLoaderOptions());

        if (contextDocument == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_REMOTE_CONTEXT, "Context[" + context + "] is null.");
        }
                
        return flatten(input, contextDocument, options);        
    }

    public static final JsonStructure flatten(final URI input, final Document context, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {

        assertDocumentLoader(options, input);

        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

        final Document remoteDocument = options.getDocumentLoader().loadDocument(input, loaderOptions);

        if (remoteDocument == null) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }
        
        return flatten(remoteDocument, context, options);
    }

    public static final JsonStructure flatten(final Document input, final Document context, final com.tangem.jsonld.api.JsonLdOptions options) throws com.tangem.jsonld.api.JsonLdError {
        
        // 4.
        final com.tangem.jsonld.api.JsonLdOptions expansionOptions = new com.tangem.jsonld.api.JsonLdOptions(options);
        expansionOptions.setOrdered(false);
        
        final JsonArray expandedInput = ExpansionProcessor.expand(input, expansionOptions, false);
        
        // 5.
        // 6.
        JsonStructure flattenedOutput = Flattening.with(expandedInput).ordered(options.isOrdered()).flatten();

        // 6.1.
        if (context != null) {
         
            final Document document = JsonDocument.of(MediaType.JSON_LD, flattenedOutput);
            
            com.tangem.jsonld.api.JsonLdOptions compactionOptions = new com.tangem.jsonld.api.JsonLdOptions(options);
            
            if (options.getBase() != null) {
                compactionOptions.setBase(options.getBase());
                
            } else if (options.isCompactArrays()) {
                compactionOptions.setBase(input.getDocumentUrl());
            }
            
            flattenedOutput = CompactionProcessor.compact(document, context, compactionOptions);
        }
        
        return flattenedOutput;            
    }
    
    private static final void assertDocumentLoader(final JsonLdOptions options, final URI target) throws com.tangem.jsonld.api.JsonLdError {
        if (options.getDocumentLoader() == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + target + "].");
        }
    }
    
}
