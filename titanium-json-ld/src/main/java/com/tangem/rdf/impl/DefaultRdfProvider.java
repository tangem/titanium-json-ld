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
package com.tangem.rdf.impl;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import com.tangem.JavaOver8Utils;
import com.tangem.jsonld.http.media.MediaType;
import com.tangem.rdf.RdfDataset;
import com.tangem.rdf.RdfGraph;
import com.tangem.rdf.RdfLiteral;
import com.tangem.rdf.RdfNQuad;
import com.tangem.rdf.RdfResource;
import com.tangem.rdf.RdfTriple;
import com.tangem.rdf.RdfValue;
import com.tangem.rdf.impl.RdfResourceImpl;
import com.tangem.rdf.io.RdfReader;
import com.tangem.rdf.io.RdfWriter;
import com.tangem.rdf.io.error.UnsupportedContentException;
import com.tangem.rdf.io.nquad.NQuadsWriter;
import com.tangem.rdf.io.nquad.NQuadsReader;
import com.tangem.rdf.spi.RdfProvider;

public final class DefaultRdfProvider extends com.tangem.rdf.spi.RdfProvider {

    public static final RdfProvider INSTANCE = new DefaultRdfProvider();
    
    private static final Collection<MediaType> CAN_READWRITE = Arrays.asList(MediaType.N_QUADS);
    
    @Override
    public RdfDataset createDataset() {
        return new RdfDatasetImpl();
    }

    @Override
    public RdfReader createReader(final MediaType contentType, final Reader reader) throws UnsupportedContentException {
        
        if (reader == null || contentType == null) {
            throw new IllegalArgumentException();
        }
        
        if (MediaType.N_QUADS.match(contentType)) {
            return new NQuadsReader(reader);
        }
        throw new UnsupportedContentException(contentType.toString());
    }

    @Override
    public RdfWriter createWriter(final MediaType contentType, final Writer writer) throws UnsupportedContentException {

        if (writer == null || contentType == null) {
            throw new IllegalArgumentException();
        }

        if (MediaType.N_QUADS.match(contentType)) {
            return new NQuadsWriter(writer);            
        }
        
        throw new UnsupportedContentException(contentType.toString());
    }

    @Override
    public RdfGraph createGraph() {
        return new RdfGraphImpl();
    }

    @Override
    public RdfTriple createTriple(RdfResource subject, RdfResource predicate, RdfValue object) {
        
        if (subject == null || predicate == null || object == null) {
            throw new IllegalArgumentException();
        }

        return new RdfTripleImpl(subject, predicate, object);
    }

    @Override
    public RdfNQuad createNQuad(RdfResource subject, RdfResource predicate, RdfValue object, RdfResource graphName) {
        
        if (subject == null || predicate == null || object == null) {
            throw new IllegalArgumentException();
        }
        
        return new RdfNQuadImpl(subject, predicate, object, graphName);
    }

    @Override
    public RdfResource createBlankNode(String value) {
        if (value == null || isBlank(value)) {
            throw new IllegalArgumentException();
        }
        
        return new com.tangem.rdf.impl.RdfResourceImpl(value, true);
    }

    @Override
    public RdfResource createIRI(String value) {
        if (value == null || isBlank(value)) {
            throw new IllegalArgumentException();
        }
        
        return new com.tangem.rdf.impl.RdfResourceImpl(value, false);
    }

    @Override
    public RdfLiteral createLangString(String lexicalForm, String langTag) {
        if (lexicalForm == null) {
            throw new IllegalArgumentException();
        }
        
        return new RdfLiteralImpl(lexicalForm, langTag, null);
    }

    @Override
    public RdfLiteral createTypedString(String lexicalForm, String datatype) {
        if (lexicalForm == null) {
            throw new IllegalArgumentException();
        }
        
        return new RdfLiteralImpl(lexicalForm, null, datatype);
    }
    
    @Override
    public Collection<MediaType> canRead() {
        return CAN_READWRITE;
    }

    @Override
    public Collection<MediaType> canWrite() {
        return CAN_READWRITE;
    }
    
    private static final boolean isBlank(String value) {
        return value.isEmpty() 
                || JavaOver8Utils.isBlank(value) && value.chars().noneMatch(ch -> ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f');
    }
}