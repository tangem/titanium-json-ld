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
package com.tangem.jsonld.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonException;

import com.tangem.jsonld.api.JsonLdErrorCode;
import com.tangem.jsonld.http.media.MediaType;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfDataset;
import com.tangem.rdf.io.error.RdfReaderException;
import com.tangem.rdf.io.error.UnsupportedContentException;

public final class RdfDocument implements Document {

    private final MediaType contentType;
    private final com.tangem.rdf.RdfDataset dataset;
    private final String profile;

    private URI documentUrl;
    private URI contentUrl;
    
    private RdfDocument(final MediaType type, final String profile, final com.tangem.rdf.RdfDataset dataset) {
        this.contentType = type;
        this.profile = profile;
        this.dataset = dataset;
    }

    /**
     * Create a new document from {@link com.tangem.rdf.RdfDataset}. Sets {@link MediaType#N_QUADS} as the content type.
     *
     * @param dataset representing parsed RDF content
     * @return {@link Document} representing RDF document
     */
    public static final Document of(final com.tangem.rdf.RdfDataset dataset) {
        return of(MediaType.N_QUADS, dataset);
    }

    /**
     * Create a new document from {@link com.tangem.rdf.RdfDataset}.
     *
     * @param contentType reflecting the provided {@link com.tangem.rdf.RdfDataset}, only {@link MediaType#N_QUADS} is supported
     * @param dataset representing parsed RDF content
     * @return {@link Document} representing RDF document
     */
    public static final Document of(final MediaType contentType, final com.tangem.rdf.RdfDataset dataset) {
        
        assertContentType(contentType);
        
        if (dataset == null) {
            throw new IllegalArgumentException("RDF dataset cannot be a null.");
        }
        
        return new RdfDocument(contentType, null, dataset);
    }

    /**
     * Create a new document from content provided by {@link InputStream}. Sets {@link MediaType#N_QUADS} as the content type.
     *
     * @param is representing parsed RDF content
     * @return {@link Document} representing RDF document
     */
    public static final Document of(final InputStream is)  throws com.tangem.jsonld.api.JsonLdError {
        return of(MediaType.N_QUADS, is);
    }
    
    public static final Document of(final MediaType type, final InputStream is)  throws com.tangem.jsonld.api.JsonLdError {
        
        assertContentType(type);
        
        try {

            com.tangem.rdf.RdfDataset dataset  = com.tangem.rdf.Rdf.createReader(type, is).readDataset();

            return new RdfDocument(type, null, dataset);
            
        } catch (JsonException | IOException | com.tangem.rdf.io.error.RdfReaderException | com.tangem.rdf.io.error.UnsupportedContentException e) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
        }
    }

    /**
     * Create a new document from content provided by {@link Reader}. Sets {@link MediaType#N_QUADS} as the content type.
     *
     * @param reader providing RDF content
     * @return {@link Document} representing RDF document
     */
    public static final Document of(final Reader reader)  throws com.tangem.jsonld.api.JsonLdError {
        return of(MediaType.N_QUADS, reader);
    }
    
    public static final Document of(final MediaType type, final Reader reader)  throws com.tangem.jsonld.api.JsonLdError {
        
        assertContentType(type);
        
        try {

            com.tangem.rdf.RdfDataset dataset  = com.tangem.rdf.Rdf.createReader(type, reader).readDataset();

            return new RdfDocument(type, null, dataset);
            
        } catch (JsonException | IOException | RdfReaderException | UnsupportedContentException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
        }
    }
    
    public static final boolean accepts(final MediaType contentType) {
        return com.tangem.rdf.Rdf.canRead().contains(contentType);
    }
    
    private static final void assertContentType(final MediaType contentType) {
        if (!accepts(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported media type '" + contentType 
                    + "'. Supported content types are [" 
                    + (Rdf.canRead().stream().map(MediaType::toString).collect(Collectors.joining(", ")))
                    + "]");
        }
    }
    
    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public URI getContextUrl() {
        return contentUrl;
    }

    @Override
    public void setContextUrl(URI contextUrl) {
        this.contentUrl = contextUrl;
    }

    @Override
    public URI getDocumentUrl() {
        return documentUrl;
    }

    @Override
    public void setDocumentUrl(URI documentUrl) {
        this.documentUrl = documentUrl;
    }

    @Override
    public Optional<String> getProfile() {
        return Optional.ofNullable(profile);
    }

    @Override
    public Optional<RdfDataset> getRdfContent() {
        return Optional.of(dataset);
    }
}
