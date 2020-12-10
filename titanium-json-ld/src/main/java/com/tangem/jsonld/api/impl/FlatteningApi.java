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
package com.tangem.jsonld.api.impl;

import java.net.URI;

import javax.json.JsonStructure;

import com.tangem.jsonld.api.impl.LoaderApi;
import com.tangem.jsonld.uri.UriUtils;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.document.JsonDocument;
import com.tangem.jsonld.lang.Version;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.processor.FlatteningProcessor;

public final class FlatteningApi implements CommonApi<FlatteningApi>, LoaderApi<FlatteningApi>, ContextApi<FlatteningApi> {

    // required
    private final URI documentUri;
    private final com.tangem.jsonld.document.Document document;
    
    // optional
    private com.tangem.jsonld.document.Document context;
    private URI contextUri;
    private com.tangem.jsonld.api.JsonLdOptions options;
    
    public FlatteningApi(URI documentUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.context = null;
        this.contextUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public FlatteningApi(com.tangem.jsonld.document.Document document) {
        this.document = document;
        this.documentUri = null;
        this.context = null;
        this.contextUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    @Override
    public FlatteningApi options(JsonLdOptions options) {
        
        if (options == null) {
            throw new IllegalArgumentException("Parameter 'options' is null.");
        }

        this.options = options;
        return this;
    }

    @Override
    public FlatteningApi mode(Version processingMode) {
        options.setProcessingMode(processingMode);
        return this;
    }

    @Override
    public FlatteningApi base(URI baseUri) {
        options.setBase(baseUri);
        return this;
    }

    @Override
    public FlatteningApi base(String baseLocation) {
        
        if (baseLocation != null) {
            
            if (UriUtils.isNotURI(baseLocation)) {
                throw new IllegalArgumentException("Base location must be valid URI or null but is [" + baseLocation + ".");
            }
            return base(UriUtils.create(baseLocation));
        }
        
        return base((URI) null);
    }

    public FlatteningApi compactArrays(boolean enable) {
        options.setCompactArrays(enable);
        return this;
    }

    public FlatteningApi compactArrays() {
        return compactArrays(true);
    }

    @Override
    public FlatteningApi loader(DocumentLoader loader) {
        options.setDocumentLoader(loader);
        return this;
    }

    @Override
    public FlatteningApi ordered(boolean enable) {
        options.setOrdered(enable);
        return this;
    }

    @Override
    public FlatteningApi ordered() {
        return ordered(true);
    }

    @Override
    public FlatteningApi context(URI contextUri) {
        this.contextUri = contextUri;
        return this;
    }
    
    @Override
    public FlatteningApi context(final String contextLocation) {
        
        if (contextLocation != null) {
            
            if (UriUtils.isNotURI(contextLocation)) {
                throw new IllegalArgumentException("Context location must be valid URI or null but is [" + contextLocation + ".");
            }
            
            return context(UriUtils.create(contextLocation));
        }
        
        return context((URI) null);
    }

    @Override
    public FlatteningApi context(JsonStructure context) {
        this.context = context != null ?  JsonDocument.of(context) : null;
        return this;
    }

    @Override
    public FlatteningApi context(com.tangem.jsonld.document.Document context) {
        this.context = context;
        return this;
    }

    /**
     * Get the result of flattening.
     * 
     * @return {@link JsonStructure} representing flattened document
     * @throws com.tangem.jsonld.api.JsonLdError
     */
    public JsonStructure get() throws JsonLdError {

        if (document != null && context != null) {
            return com.tangem.jsonld.processor.FlatteningProcessor.flatten(document, context, options);
        }

        if (document != null && contextUri != null) {
            return com.tangem.jsonld.processor.FlatteningProcessor.flatten(document, contextUri, options);
        }

        if (document != null) {
            return com.tangem.jsonld.processor.FlatteningProcessor.flatten(document, (com.tangem.jsonld.document.Document)null, options);
        }

        if (documentUri != null && context != null) {
            return com.tangem.jsonld.processor.FlatteningProcessor.flatten(documentUri, context, options);
        }

        if (documentUri != null && contextUri != null) {
            return com.tangem.jsonld.processor.FlatteningProcessor.flatten(documentUri, contextUri, options);
        }

        if (documentUri != null) {
            return FlatteningProcessor.flatten(documentUri, (Document)null, options);
        }

        throw new IllegalStateException();
    }
}
