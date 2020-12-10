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

import javax.json.JsonObject;
import javax.json.JsonStructure;

import com.tangem.jsonld.api.impl.LoaderApi;
import com.tangem.jsonld.api.JsonLdEmbed;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.document.JsonDocument;
import com.tangem.jsonld.lang.Version;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.processor.FramingProcessor;
import com.tangem.jsonld.uri.UriUtils;

public final class FramingApi implements CommonApi<FramingApi>, LoaderApi<FramingApi>, ContextApi<FramingApi> {

    // required
    private final com.tangem.jsonld.document.Document document;
    private final URI documentUri;
    private final com.tangem.jsonld.document.Document frame;
    private final URI frameUri;
    
    // optional
    private com.tangem.jsonld.api.JsonLdOptions options;
    
    public FramingApi(URI documentUri, URI frameUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.frame = null;
        this.frameUri = frameUri;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public FramingApi(com.tangem.jsonld.document.Document document, com.tangem.jsonld.document.Document frame) {
        this.document = document;
        this.documentUri = null;
        this.frame = frame;
        this.frameUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    @Override
    public FramingApi options(JsonLdOptions options) {
        
        if (options == null) {
            throw new IllegalArgumentException("Parameter 'options' is null.");
        }

        this.options = options;
        return this;
    }
    
    @Override
    public FramingApi context(URI contextUri) {
        options.setExpandContext(contextUri);
        return this;
    }

    @Override
    public FramingApi context(String contextLocation) {

        if (contextLocation != null) {
            
            if (com.tangem.jsonld.uri.UriUtils.isNotURI(contextLocation)) {
                throw new IllegalArgumentException("Context location must be valid URI or null but is [" + contextLocation + ".");
            }
            
            return context(UriUtils.create(contextLocation));
        }
        
        return context((URI) null);
    }

    @Override
    public FramingApi context(JsonStructure context) {
        options.setExpandContext(context != null ?  JsonDocument.of(context) : null);
        return this;
    }

    @Override
    public FramingApi context(Document context) {
        options.setExpandContext(context);
        return this;
    }

    @Override
    public FramingApi mode(Version processingMode) {
        options.setProcessingMode(processingMode);
        return this;
    }

    @Override
    public FramingApi base(URI baseUri) {
        options.setBase(baseUri);
        return this;
    }

    @Override
    public FramingApi base(String baseUri) {
        return base(URI.create(baseUri));
    }

    @Override
    public FramingApi loader(DocumentLoader loader) {
        options.setDocumentLoader(loader);
        return this;
    }

    @Override
    public FramingApi ordered(boolean enable) {
        options.setOrdered(enable);
        return this;
    }
    
    @Override
    public FramingApi ordered() {
        return ordered(true);
    }

    public FramingApi embed(JsonLdEmbed value) {
        options.setEmbed(value);
        return this;
    }

    public FramingApi explicit(boolean enable) {
        options.setExplicit(enable);
        return this;
    }

    public FramingApi explicit() {
        return explicit(true);
    }

    public FramingApi omitDefault(boolean enable) {
        options.setOmitDefault(enable);
        return this;
    }

    public FramingApi omitDefault() {
        return omitDefault(true);
    }

    public FramingApi omitGraph(boolean enable) {
        options.setOmitGraph(enable);
        return this;
    }

    public FramingApi omitGraph() {
        return omitGraph(true);
    }

    public FramingApi requiredAll(boolean enable) {
        options.setRequiredAll(enable);
        return this;
    }

    public FramingApi requiredAll() {
        return requiredAll(true);
    }
    
    /**
     * Get the result of framing.
     * 
     * @return {@link JsonObject} representing framed document
     * @throws com.tangem.jsonld.api.JsonLdError
     */
    public JsonObject get() throws JsonLdError {
        if (documentUri != null && frameUri != null) {
            return com.tangem.jsonld.processor.FramingProcessor.frame(documentUri, frameUri, options);
        }
        if (document != null && frame != null) {
            return FramingProcessor.frame(document, frame, options);
        }

        throw new IllegalStateException();
    }
}