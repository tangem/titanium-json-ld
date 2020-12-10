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
package com.tangem.jsonld.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import com.tangem.jsonld.api.JsonLdErrorCode;
import com.tangem.jsonld.http.media.MediaType;
import com.tangem.jsonld.loader.DocumentLoader;
import com.tangem.jsonld.loader.DocumentLoaderOptions;
import com.tangem.JavaOver8Utils;
import com.tangem.jsonld.api.JsonLdError;
import com.tangem.jsonld.document.Document;
import com.tangem.jsonld.document.DocumentParser;

import static com.tangem.JavaOver8Utils.isBlank;

public final class FileLoader implements DocumentLoader {

    @Override
    public Document loadDocument(final URI url, final DocumentLoaderOptions options) throws com.tangem.jsonld.api.JsonLdError {
        
        if (!"file".equalsIgnoreCase(url.getScheme())) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Unsupported URL scheme [" + url.getScheme() + "]. FileLoader accepts only file scheme.");
        }
        
        final File file = new File(url);
        
        if (!file.canRead()) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "File [" + url + "] is not accessible to read.");
        }
        
        final MediaType contentType =
                                detectedContentType(url.getPath().toLowerCase())
                                .orElseThrow(() -> new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Unknown media type of the file [" + url + "]."));
                        
        try (final InputStream is = new FileInputStream(file)) {
            
            return DocumentParser.parse(contentType, is);
            
        } catch (FileNotFoundException e) {
            
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "File not found [" + url + "].");
            
        } catch (IOException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
        }
    }

    private static final Optional<MediaType> detectedContentType(String name) {
        
        if (name == null || JavaOver8Utils.isBlank(name)) {
            return Optional.empty();
        }
        
        if (name.endsWith(".nq")) {
            return Optional.of(MediaType.N_QUADS);
        }
        if (name.endsWith(".json")) {
            return Optional.of(MediaType.JSON);
        }
        if (name.endsWith(".jsonld")) {
            return Optional.of(MediaType.JSON_LD);
        }
        if (name.endsWith(".html")) {
            return Optional.of(MediaType.HTML);
        }
        
        return Optional.empty();
    }
}
