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
package com.tangem.rdf;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import com.tangem.jsonld.http.media.MediaType;
import com.tangem.jsonld.lang.BlankNode;
import com.tangem.jsonld.uri.UriUtils;
import com.tangem.rdf.RdfLiteral;
import com.tangem.rdf.RdfNQuad;
import com.tangem.rdf.RdfResource;
import com.tangem.JavaOver8Utils;
import com.tangem.rdf.io.RdfReader;
import com.tangem.rdf.io.RdfWriter;
import com.tangem.rdf.io.error.UnsupportedContentException;
import com.tangem.rdf.lang.XsdConstants;
import com.tangem.rdf.spi.RdfProvider;

import static com.tangem.JavaOver8Utils.isBlank;

public final class Rdf {

    private Rdf() {
    }
    
    public static final RdfGraph createGraph() {
        return com.tangem.rdf.spi.RdfProvider.provider().createGraph();
    }

    public static final Collection<MediaType> canRead() {
        return com.tangem.rdf.spi.RdfProvider.provider().canRead();
    }
    
    public static final com.tangem.rdf.io.RdfReader createReader(final MediaType contentType, Reader reader) throws com.tangem.rdf.io.error.UnsupportedContentException {

        if (reader == null || contentType == null) {
            throw new IllegalArgumentException();
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createReader(contentType, reader);
    }
    
    public static final RdfReader createReader(final MediaType contentType, final InputStream is) throws com.tangem.rdf.io.error.UnsupportedContentException {
        
        if (is == null || contentType == null) {
            throw new IllegalArgumentException();
        }

        return createReader(contentType, new InputStreamReader(is));
    }

    public static final Collection<MediaType> canWrite() {
        return com.tangem.rdf.spi.RdfProvider.provider().canWrite();
    }
    
    public static final com.tangem.rdf.io.RdfWriter createWriter(final MediaType contentType, final Writer writer) throws com.tangem.rdf.io.error.UnsupportedContentException {
        
        if (writer == null || contentType == null) {
            throw new IllegalArgumentException();
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createWriter(contentType, writer);
    }

    public static final RdfWriter createWriter(final MediaType contentType, final OutputStream os) throws UnsupportedContentException {
        
        if (os == null || contentType == null) {
            throw new IllegalArgumentException();
        }

        return createWriter(contentType, new OutputStreamWriter(os));
    }

    public static final RdfDataset createDataset() {
        return com.tangem.rdf.spi.RdfProvider.provider().createDataset();
    }

    public static final RdfTriple createTriple(com.tangem.rdf.RdfResource subject, com.tangem.rdf.RdfResource predicate, RdfValue object) {
        
        if (subject == null || predicate == null || object == null) {
            throw new IllegalArgumentException();
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createTriple(subject, predicate, object);
    }

    public static final com.tangem.rdf.RdfNQuad createNQuad(com.tangem.rdf.RdfResource subject, com.tangem.rdf.RdfResource predicate, RdfValue object, com.tangem.rdf.RdfResource graphName) {
        
        if (subject == null) {            
            throw new IllegalArgumentException("Subject cannot be null.");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null.");
        }
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null.");            
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createNQuad(subject, predicate, object, graphName);
    }

    public static final RdfNQuad createNQuad(RdfTriple triple, com.tangem.rdf.RdfResource graphName) {
        
        if (triple == null) {            
            throw new IllegalArgumentException("Triple cannot be null.");
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createNQuad(triple.getSubject(), triple.getPredicate(), triple.getObject(), graphName);
    }

    public static RdfValue createValue(String value) {
        
        if (value == null) {
            throw new IllegalArgumentException();
        }
        
        if (UriUtils.isAbsoluteUri(value)) {
            return com.tangem.rdf.spi.RdfProvider.provider().createIRI(value);
        }
        
        if (BlankNode.isWellFormed(value)) {
            return com.tangem.rdf.spi.RdfProvider.provider().createBlankNode(value);
        }
        
        return com.tangem.rdf.spi.RdfProvider.provider().createTypedString(value, com.tangem.rdf.lang.XsdConstants.STRING);
    }

    public static com.tangem.rdf.RdfLiteral createString(String lexicalForm) {
        
        if (lexicalForm == null) {
            throw new IllegalArgumentException();
        }
        
        return com.tangem.rdf.spi.RdfProvider.provider().createTypedString(lexicalForm, XsdConstants.STRING);
    }

    public static com.tangem.rdf.RdfLiteral createTypedString(String lexicalForm, String dataType) {
        
        if (lexicalForm == null) {
            throw new IllegalArgumentException();
        }
        
        return com.tangem.rdf.spi.RdfProvider.provider().createTypedString(lexicalForm, dataType);
    }
    
    public static RdfLiteral createLangString(String lexicalForm, String langTag) {
        
        if (lexicalForm == null) {
            throw new IllegalArgumentException();
        }
        
        return com.tangem.rdf.spi.RdfProvider.provider().createLangString(lexicalForm, langTag);
    }

    /**
     * Create a new {@link com.tangem.rdf.RdfResource}.
     * 
     * @param resource is an absolute IRI or blank node identifier
     * @return RDF resource 
     * @throws IllegalArgumentException if the resource is not an absolute IRI or blank node identifier
     */
    public static com.tangem.rdf.RdfResource createResource(String resource) {
        
        if (resource == null) {
            throw new IllegalArgumentException("The resource value cannot be null.");
        }
        
        if (UriUtils.isAbsoluteUri(resource)) {
            return com.tangem.rdf.spi.RdfProvider.provider().createIRI(resource);
        }
        
        if (BlankNode.isWellFormed(resource)) {
            return com.tangem.rdf.spi.RdfProvider.provider().createBlankNode(resource);
        }
        
        throw new IllegalArgumentException("The resource must be an absolute IRI or blank node identifier, but was [" + resource + "].");        
    }
    
    public static com.tangem.rdf.RdfResource createBlankNode(final String value) {
        
        if (value == null || JavaOver8Utils.isBlank(value)) {
            throw new IllegalArgumentException();
        }

        return com.tangem.rdf.spi.RdfProvider.provider().createBlankNode(value);
    }
    
    public static RdfResource createIRI(final String value) {
        
        if (value == null || JavaOver8Utils.isBlank(value)) {
            throw new IllegalArgumentException();
        }

        return RdfProvider.provider().createIRI(value);
    }
}