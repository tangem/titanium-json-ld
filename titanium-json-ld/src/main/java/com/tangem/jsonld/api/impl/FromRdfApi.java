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

import com.tangem.jsonld.api.impl.LoaderApi;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.lang.Version;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.processor.FromRdfProcessor;
import com.tangem.rdf.RdfDataset;

public final class FromRdfApi implements CommonApi<FromRdfApi>, LoaderApi<FromRdfApi> {

    // required
    private final com.tangem.jsonld.document.Document document;
    private final URI documentUri;
    
    // optional
    private com.tangem.jsonld.api.JsonLdOptions options;
    
    public FromRdfApi(Document document) {
        this.document = document;
        this.documentUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public FromRdfApi(URI documentUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }
    
    @Override
    public FromRdfApi options(JsonLdOptions options) {
        
        if (options == null) {
            throw new IllegalArgumentException("Parameter 'options' is null.");
        }

        this.options = options;
        return this;
    }
    
    @Override
    public FromRdfApi mode(Version processingMode) {
        options.setProcessingMode(processingMode);
        return this;
    }

    @Override
    public FromRdfApi base(URI baseUri) {
        options.setBase(baseUri);
        return this;
    }

    @Override
    public FromRdfApi base(String baseUri) {
        return base(URI.create(baseUri));
    }

    @Override
    public FromRdfApi ordered(boolean enable) {
        options.setOrdered(enable);
        return this;
    }
    
    @Override
    public FromRdfApi ordered() {
        return ordered(true);
    }
    
    @Override
    public FromRdfApi loader(DocumentLoader loader) {
        options.setDocumentLoader(loader);
        return this;
    }
    
    public FromRdfApi nativeTypes() {
        return nativeTypes(true);
    }

    public FromRdfApi nativeTypes(boolean useNativeTypes) {
        options.setUseNativeTypes(useNativeTypes);
        return this;
    }
    
    /**
     * Get <code>JSON-LD</code> representation of the provided {@link RdfDataset}.
     * 
     * @return {@link JsonArray} representing <code>JSON-LD</code> document
     * @throws com.tangem.jsonld.api.JsonLdError
     */
    public JsonArray get() throws JsonLdError {
        
        if (document != null) {
            return com.tangem.jsonld.processor.FromRdfProcessor.fromRdf(document, options);
        }
        
        if (documentUri != null) {
            return FromRdfProcessor.fromRdf(documentUri, options);
        }
        
        throw new IllegalStateException();
    }
}
