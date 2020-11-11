package com.apicatalog.jsonld.loader

import com.apicatalog.jsonld.api.JsonLdError
import com.apicatalog.jsonld.api.JsonLdErrorCode
import com.apicatalog.jsonld.document.Document
import com.apicatalog.jsonld.document.DocumentParser
import com.apicatalog.jsonld.http.ProfileConstants
import com.apicatalog.jsonld.http.link.Link
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.uri.UriResolver
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URI
import java.util.stream.Collectors

/**
 * Created by Anton Zhilenkov on 10/11/2020.
 */
class HttpLoader(
    private val httpClient: OkHttpClient,
    private val maxRedirections: Int = 10
) : DocumentLoader {

    override fun loadDocument(uri: URI, options: DocumentLoaderOptions): Document? {
        return try {
            var redirection = 0
            var done = false
            var targetUri: URI = uri
            var contentType: MediaType? = null
            var contextUri: URI? = null
            var response: Response? = null

            while (!done) {
                // 2.
                val request = Request.Builder()
                    .get()
                    .url(targetUri.toURL())
                    .header("Accept", getAcceptHeader(options.requestProfile))
                    .build()
                response = httpClient.newCall(request).execute()

                // 3.
                if (response.code == 301 || response.code == 302 || response.code == 303 || response.code == 307) {
                    val location: String? = response.headers.values("Location").firstOrNull()
                    targetUri = if (location != null) {
                        URI.create(UriResolver.resolve(targetUri, location))
                    } else {
                        throw JsonLdError(
                            JsonLdErrorCode.LOADING_DOCUMENT_FAILED,
                            "Header location is required for code [${response.code}]."
                        )
                    }
                    redirection++
                    if (maxRedirections > 0 && redirection >= maxRedirections) {
                        throw JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Too many redirections")
                    }
                    continue
                }
                if (response.code != 200) {
                    throw JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Unexpected response code [${response.code}]")
                }
                val contentTypeValue: String? = response.headers.values("Content-Type").firstOrNull()
                if (contentTypeValue != null) {
                    contentType = MediaType.of(contentTypeValue)
                }
                val linkValues: List<String>? = response.headers.get("link") as? List<String>
                if (linkValues != null && linkValues.isNotEmpty()) {

                    // 4.
                    if (contentType == null || (!MediaType.JSON.match(contentType)
                                && !contentType.subtype().toLowerCase().endsWith(PLUS_JSON))
                    ) {
                        val baseUri = targetUri
                        val alternate = linkValues.stream()
                            .flatMap { l: String? -> Link.of(l, baseUri).stream() }
                            .filter { l: Link ->
                                (l.relations().contains("alternate")
                                        && l.type().isPresent && MediaType.JSON_LD.match(l.type().get()))
                            }
                            .findFirst()

                        if (alternate.isPresent) {
                            targetUri = alternate.get().target()
                            redirection++
                            if (maxRedirections > 0 && redirection >= maxRedirections) {
                                throw JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Too many redirections")
                            }
                            continue
                        }
                    }

                    // 5.
                    if (contentType != null && !MediaType.JSON_LD.match(contentType)
                        && (MediaType.JSON.match(contentType) || contentType.subtype().toLowerCase().endsWith(PLUS_JSON))
                    ) {

                        val baseUri = targetUri
                        val contextUris = linkValues.stream()
                            .flatMap { l: String? -> Link.of(l, baseUri).stream() }
                            .filter { l: Link -> l.relations().contains(ProfileConstants.CONTEXT) }
                            .collect(Collectors.toList())
                        if (contextUris.size > 1) {
                            throw JsonLdError(JsonLdErrorCode.MULTIPLE_CONTEXT_LINK_HEADERS)
                        } else if (contextUris.size == 1) {
                            contextUri = contextUris[0].target()
                        }
                    }
                }
                done = true
            }
            createDocument(contentType, targetUri, contextUri, response!!)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e)
        } catch (e: IOException) {
            throw JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e)
        }
    }

    companion object {
        private const val PLUS_JSON = "+json"

        val INSTANCE: HttpLoader = HttpLoader(OkHttpClient())

        fun defaultInstance(): DocumentLoader = HttpLoader.INSTANCE
        fun getAcceptHeader(): String = getAcceptHeader(null)

        fun getAcceptHeader(profiles: Collection<String?>?): String {
            val builder = StringBuilder()
            builder.append(MediaType.JSON_LD.toString())
            if (profiles != null && !profiles.isEmpty()) {
                builder.append(";profile=\"")
                builder.append(java.lang.String.join(" ", profiles))
                builder.append("\"")
            }
            builder.append(',')
            builder.append(MediaType.JSON.toString())
            builder.append(";q=0.9,*/*;q=0.8")
            return builder.toString()
        }

        @Throws(JsonLdError::class, IOException::class)
        fun createDocument(
            type: MediaType?,
            targetUri: URI?,
            contextUrl: URI?,
            response: Response
        ): Document? {
            val byteStream = response.body?.byteStream() ?: return null

            val remoteDocument: Document = DocumentParser.parse(type, byteStream)
            remoteDocument.documentUrl = targetUri
            remoteDocument.contextUrl = contextUrl
            return remoteDocument
        }
    }
}