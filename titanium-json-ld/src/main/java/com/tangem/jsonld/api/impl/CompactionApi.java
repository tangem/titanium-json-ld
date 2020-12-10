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

import com.tangem.jsonld.api.impl.LoaderApi;
import com.tangem.jsonld.uri.UriUtils;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.api.JsonLdOptions;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.lang.Version;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.processor.CompactionProcessor;

public final class CompactionApi implements CommonApi<CompactionApi>, LoaderApi<CompactionApi> {

    // required
    private final com.tangem.jsonld.document.Document document;
    private final URI documentUri;
    private final com.tangem.jsonld.document.Document context;
    private final URI contextUri;
    
    // optional
    private com.tangem.jsonld.api.JsonLdOptions options;
    
    public CompactionApi(URI documentUri, com.tangem.jsonld.document.Document context) {
        this.document = null;
        this.documentUri = documentUri;
        this.context = context;
        this.contextUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public CompactionApi(URI documentUri, URI contextUri) {
        this.document = null;
        this.documentUri = documentUri;
        this.context = null;
        this.contextUri = contextUri;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    public CompactionApi(com.tangem.jsonld.document.Document document, Document context) {
        this.document = document;
        this.documentUri = null;
        this.context = context;
        this.contextUri = null;
        this.options = new com.tangem.jsonld.api.JsonLdOptions();
    }

    @Override
    public CompactionApi options(JsonLdOptions options) {
        
        if (options == null) {
            throw new IllegalArgumentException("Parameter 'options' is null.");
        }
        
        this.options = options;
        return this;
    }

    @Override
    public CompactionApi mode(Version processingMode) {
        options.setProcessingMode(processingMode);
        return this;
    }

    @Override
    public CompactionApi base(URI baseUri) {        
        options.setBase(baseUri);
        return this;
    }

    @Override
    public CompactionApi base(String baseUri) {
        return base(baseUri != null ? UriUtils.create(baseUri) : null);
    }

    
    /**
     * If set to <code>true</code>, the processor replaces arrays with just one
     * element  If set to false, all arrays will remain arrays even if they have just one
     * element. <code>true</code> by default.
     *
     * @param enable 
     * @return builder instance
     */
    public CompactionApi compactArrays(boolean enable) {
        options.setCompactArrays(enable);
        return this;
    }

    /**
     * The processor replaces arrays with just one element. 
     * 
     * @return {@link CompactionApi} instance
     */
    public CompactionApi compactArrays() {
        return compactArrays(true);
    }
    
    /**
     * Determines if IRIs are compacted relative to the {@link #base(URI)} or document location . 
     * <code>true</code> by default.
     * 
     * @param enable
     * @return builder instance
     */
    public CompactionApi compactToRelative(boolean enable) {
        options.setCompactToRelative(enable);
        return this;
    }

    /**
     * IRIs are compacted relative to the {@link #base(URI)} or document location. 
     * 
     * @return builder instance
     */
    public CompactionApi compactToRelative() {
        return compactToRelative(true);
    }
    
    @Override
    public CompactionApi loader(DocumentLoader loader) {
        options.setDocumentLoader(loader);
        return this;
    }
    
    @Override
    public CompactionApi ordered(boolean enable) {
        options.setOrdered(enable);
        return this;
    }

    @Override
    public CompactionApi ordered() {
        return ordered(true);
    }

    /**
     * Get the result of compaction.
     * 
     * @return {@link JsonObject} representing compacted document
     * @throws com.tangem.jsonld.api.JsonLdError
     */
    public JsonObject get() throws JsonLdError {
        if (documentUri != null && contextUri != null)  {
            return com.tangem.jsonld.processor.CompactionProcessor.compact(documentUri, contextUri, options);
        }        
        if (documentUri != null && context != null)  {
            return com.tangem.jsonld.processor.CompactionProcessor.compact(documentUri, context, options);
        }
        if (document != null && context != null)  {
            return CompactionProcessor.compact(document, context, options);
        }
        throw new IllegalStateException();
    }
}
