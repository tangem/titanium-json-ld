package com.tangem

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tangem.jsonld.JsonLd
import com.tangem.jsonld.document.JsonDocument
import com.tangem.jsonld.document.RdfDocument
import com.tangem.rdf.normalization.NQuadSerializer
import com.tangem.rdf.normalization.RdfNormalize
import com.tangem.rdf.normalization.SerializedQuad
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val handler = Handler(HandlerThread("HandlerThread").apply { start() }.looper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        helloWorld.setOnClickListener {
            handler.post {
                val jsonInputStream = assets.open("test_json_ld.json")
                val document = JsonDocument.of(jsonInputStream)
//                val result = JsonLd.expand(document)
//                val result = JsonLd.flatten(document)
                val result = JsonLd.toRdf(document).get()
                Log.d("testExample", result.toList().toString())
                val normalized = RdfNormalize.normalize(result)

                val rdfDocument = RdfDocument.of(normalized)

                var normalizedString = ""
                normalized.toList().forEach { normalizedString = normalizedString + NQuadSerializer.write(it) }
                normalizedString.removeSuffix("\n") // TODO: check if needed

                print(normalizedString)

//                Expansion
//                JsonLd.expand("https://github.com/w3c/json-ld-api/blob/master/tests/expand/0001-in.jsonld")
//                    .ordered()
//                    .get()

//                JsonLd.expand("file:/home/filip/document.json")    // HTTP(S) and File schemes supported
//                .context("file:/home/filip/context.jsonld")  // external context
//                .get();

//                Compaction
//                JsonLd.compact("https://example/expanded.jsonld", "https://example/context.jsonld")
//                .compactToRelative(false)
//                .get();

//                Flattening
//                JsonLd.flatten("https://example/document.jsonld").get();

//                JSON - LD to RDF
//                JsonLd.toRdf("https://example/document.jsonld").get();

//                RDF to JSON - LD
//                JsonLd.fromRdf("https://example/document.nq").options(options).get();

//                Framing
//                JsonLd.frame("https://example/document.jsonld", "https://example/frame.jsonld").get();
            }
        }
    }
}