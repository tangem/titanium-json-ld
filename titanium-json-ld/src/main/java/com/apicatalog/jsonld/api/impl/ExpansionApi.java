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
package com.apicatalog.jsonld.api.impl;

import java.net.URI;

import javax.json.JsonArray;
import javax.json.JsonStructure;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.lang.Version;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.processor.ExpansionProcessor;
import com.apicatalog.jsonld.uri.UriUtils;

public final class ExpansionApi implements CommonApi<ExpansionApi>, LoaderApi<ExpansionApi>, ContextApi<ExpansionApi> {

    // required
    private final URI documentUri;
    private final Document document;
    
    // optional
    private JsonLdOptions options;
    
    public ExpansionApi(URI documentUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.options = new JsonLdOptions();
    }

    public ExpansionApi(Document document) {
        this.document = document;
        this.documentUri = null;
        this.options = new JsonLdOptions();
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
            
            if (UriUtils.isNotURI(contextLocation)) {
                throw new IllegalArgumentException("Context location must be valid URI or null but is [" + contextLocation + ".");
            }
            return context(UriUtils.create(contextLocation));    
        }
        
        return context((Document) null);
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
            
            if (UriUtils.isNotURI(baseLocation)) {
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
     * @throws JsonLdError
     */
    public JsonArray get() throws JsonLdError {
        if (document != null) {
            return ExpansionProcessor.expand(document, options, false);
            
        } else if (documentUri != null) {
            return ExpansionProcessor.expand(documentUri, options);
        }
        throw new IllegalStateException();
    }
}
