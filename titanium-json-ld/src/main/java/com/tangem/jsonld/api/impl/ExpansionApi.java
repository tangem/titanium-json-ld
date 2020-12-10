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

import javax.json.JsonArray;
import javax.json.JsonStructure;

import com.tangem.jsonld.api.impl.LoaderApi;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.document.JsonDocument;
import com.tangem.jsonld.lang.Version;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.processor.ExpansionProcessor;
import com.tangem.jsonld.uri.UriUtils;

public final class ExpansionApi implements CommonApi<ExpansionApi>, LoaderApi<ExpansionApi>, ContextApi<ExpansionApi> {

    // required
    private final URI documentUri;
    private final com.tangem.jsonld.document.Document document;
    
    // optional
    private com.tangem.jsonld.api.JsonLdOptions options;
    
    public ExpansionApi(URI documentUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public ExpansionApi(com.tangem.jsonld.document.Document document) {
        this.document = document;
        this.documentUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    @Override
    public ExpansionApi options(JsonLdOptions options) {
        
        if (options == null) {
            throw new IllegalArgumentException("Parameter 'options' is null.");
        }

        this.options = options;
        return this;
    }
    
    @Override
    public ExpansionApi context(URI contextUri) {
        options.setExpandContext(contextUri);
        return this;
    }

    @Override
    public ExpansionApi context(String contextLocation) {
        
        if (contextLocation != null) {
            
            if (com.tangem.jsonld.uri.UriUtils.isNotURI(contextLocation)) {
                throw new IllegalArgumentException("Context location must be valid URI or null but is [" + contextLocation + ".");
            }
            return context(com.tangem.jsonld.uri.UriUtils.create(contextLocation));
        }
        
        return context((com.tangem.jsonld.document.Document) null);
    }

    @Override
    public ExpansionApi context(JsonStructure context) {
        options.setExpandContext(context != null ? JsonDocument.of(context) : null);
        return this;
    }

    @Override
    public ExpansionApi context(Document context) {
        options.setExpandContext(context);
        return this;
    }

    @Override
    public ExpansionApi mode(Version processingMode) {
        options.setProcessingMode(processingMode);
        return this;
    }

    @Override
    public ExpansionApi base(URI baseUri) {
        options.setBase(baseUri);
        return this;
    }

    @Override
    public ExpansionApi base(String baseLocation) {
        
        if (baseLocation != null) {
            
            if (com.tangem.jsonld.uri.UriUtils.isNotURI(baseLocation)) {
                throw new IllegalArgumentException("Base location must be valid URI or null but is [" + baseLocation + ".");
            }
            return base(UriUtils.create(baseLocation));
        }        
        
        return base((URI) null);        
    }

    @Override
    public ExpansionApi loader(DocumentLoader loader) {
        options.setDocumentLoader(loader);
        return this;
    }

    @Override
    public ExpansionApi ordered(boolean enable) {
        options.setOrdered(enable);
        return this;
    }
    
    @Override
    public ExpansionApi ordered() {
        return ordered(true);
    }

    /**
     * Get the result of the document expansion.
     * 
     * @return {@link JsonArray} representing expanded document
     * @throws com.tangem.jsonld.api.JsonLdError
     */
    public JsonArray get() throws JsonLdError {
        if (document != null) {
            return com.tangem.jsonld.processor.ExpansionProcessor.expand(document, options, false);
            
        } else if (documentUri != null) {
            return ExpansionProcessor.expand(documentUri, options);
        }
        throw new IllegalStateException();
    }
}
