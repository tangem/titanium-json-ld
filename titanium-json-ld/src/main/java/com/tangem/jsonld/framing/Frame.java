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
package com.tangem.jsonld.framing;

import java.util.Collection;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import com.tangem.jsonld.api.JsonLdEmbed;
import com.tangem.jsonld.api.JsonLdErrorCode;
import com.tangem.jsonld.framing.FramingState;
import com.tangem.jsonld.framing.ValuePatternMatcher;
import com.tangem.jsonld.json.JsonUtils;
import com.tangem.jsonld.lang.DefaultObject;
import com.tangem.jsonld.lang.Keywords;
import com.tangem.jsonld.lang.ListObject;
import com.tangem.jsonld.lang.NodeObject;
import com.tangem.jsonld.lang.ValueObject;
import com.tangem.jsonld.uri.UriUtils;
import com.tangem.jsonld.api.JsonLdError;

public final class Frame {

    private final JsonObject frameObject;
    
    private Frame(final JsonObject frameObject) {
        this.frameObject = frameObject;
    }
    
    public static final Frame of(final JsonStructure structure) throws com.tangem.jsonld.api.JsonLdError {

        final JsonObject frameObject;

        // 1.
        if (JsonUtils.isArray(structure)) {

            if (structure.asJsonArray().size() != 1  
                    || JsonUtils.isNotObject(structure.asJsonArray().get(0))
                    ) {
                throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_FRAME, "Frame is not JSON object nor an array containing JSON object [" + structure + "]");
            }

            frameObject = structure.asJsonArray().getJsonObject(0);

            
        } else if (JsonUtils.isObject(structure)) {
            
            frameObject = structure.asJsonObject();

        } else {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_FRAME, "Frame is not JSON object. [" + structure + "]");
        }
        
        // 1.2.
        if (frameObject.containsKey(Keywords.ID) && !validateFrameId(frameObject)) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_FRAME, "Frame @id value is not valid [" + frameObject.get(Keywords.ID) + "].");
        }
        
        // 1.3.
        if (frameObject.containsKey(Keywords.TYPE) && !validateFrameType(frameObject)) {
            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_FRAME, "Frame @type value i not valid [" + frameObject.get(Keywords.TYPE) + "].");
        }
        return new Frame(frameObject);
    }
        
    public JsonLdEmbed getEmbed(final JsonLdEmbed defaultValue) throws com.tangem.jsonld.api.JsonLdError {
        
        if (frameObject.containsKey(Keywords.EMBED)) {

            JsonValue embed = frameObject.get(Keywords.EMBED);

            if (embed == null || JsonUtils.isNull(embed)) {
                return defaultValue;
            }
            
            if (ValueObject.isValueObject(embed)) {
                embed = ValueObject.getValue(embed);
            }
            
            if (JsonUtils.isString(embed)) {

                String stringValue = ((JsonString)embed).getString();
             
                if (Keywords.noneMatch(stringValue, Keywords.ALWAYS, Keywords.ONCE, Keywords.NEVER)) {
                    throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_KEYWORD_EMBED_VALUE);
                }
                
                return JsonLdEmbed.valueOf(stringValue.substring(1).toUpperCase());
                
            } else if (JsonUtils.isFalse(embed)) {
                return JsonLdEmbed.NEVER;
                
            } else if (JsonUtils.isTrue(embed)) {
                return JsonLdEmbed.ONCE;
            }

            throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_KEYWORD_EMBED_VALUE);
         }
        
        return defaultValue;
    }
    
    public boolean getExplicit(boolean defaultValue) throws com.tangem.jsonld.api.JsonLdError {
        return getBoolean(frameObject, Keywords.EXPLICIT, defaultValue);
    }
    
    public boolean getRequireAll(boolean defaultValue) throws com.tangem.jsonld.api.JsonLdError {
        return getBoolean(frameObject, Keywords.REQUIRE_ALL, defaultValue);
    }
    
    public static final boolean getBoolean(JsonObject frame, String key, boolean defaultValue) throws com.tangem.jsonld.api.JsonLdError {
        
        if (frame.containsKey(key)) {

            JsonValue value = frame.get(key);

            if (JsonUtils.isNull(value)) {
                return defaultValue;
            }
            
            if (ValueObject.isValueObject(value)) {
                value = ValueObject.getValue(value);
            }

            if (JsonUtils.isString(value)) {
                if ("true".equalsIgnoreCase(((JsonString)value).getString())) {
                    return true;
                    
                } else if ("false".equalsIgnoreCase(((JsonString)value).getString())) {
                    return false;
                }
            }
            
            if (JsonUtils.isNotBoolean(value)) {
                throw new com.tangem.jsonld.api.JsonLdError(JsonLdErrorCode.INVALID_FRAME);
            }
      
            return JsonUtils.isTrue(value);
        }
        return defaultValue; 
    }
    
    private static final boolean validateFrameId(JsonObject frame) {
        
        final JsonValue idValue = frame.get(Keywords.ID);
        
        if (JsonUtils.isArray(idValue) && JsonUtils.isNotEmptyArray(idValue)) {
            
            if (idValue.asJsonArray().size() == 1
                   && JsonUtils.isEmptyObject(idValue.asJsonArray().get(0))) {
                return true;
            } 
            
            for (final JsonValue item : idValue.asJsonArray()) {
                if (JsonUtils.isNotString(item) || UriUtils.isNotAbsoluteUri(((JsonString)item).getString())) {
                    return false;
                }
            }
            return true;
            
        }
        return JsonUtils.isString(idValue) && UriUtils.isAbsoluteUri(((JsonString)idValue).getString());
    }
    
    private static final boolean validateFrameType(JsonObject frame) {

        final JsonValue typeValue = frame.get(Keywords.TYPE);

        if (JsonUtils.isArray(typeValue) && JsonUtils.isNotEmptyArray(typeValue)) {
            
            if (typeValue.asJsonArray().size() == 1
                   && (JsonUtils.isEmptyObject(typeValue.asJsonArray().get(0))
                           || (JsonUtils.isObject(typeValue.asJsonArray().get(0))
                                   && typeValue.asJsonArray().get(0).asJsonObject().containsKey(Keywords.DEFAULT)
                                   )
                    )) {

                return true;
            } 
            
            for (final JsonValue item : typeValue.asJsonArray()) {
                if (JsonUtils.isNotString(item) || UriUtils.isNotAbsoluteUri(((JsonString)item).getString())) {
                    return false;
                }
            }
            return true;
            
        }
        return 
                JsonUtils.isEmptyArray(typeValue)
                || JsonUtils.isEmptyObject(typeValue)
                || JsonUtils.isString(typeValue) && UriUtils.isAbsoluteUri(((JsonString)typeValue).getString());
    }
    
    public Set<String> keys() {
        return frameObject.keySet();
    }

    public JsonValue get(String property) {
        return frameObject.get(property);
    }

    public boolean contains(String property) {
        return frameObject.containsKey(property);
    }

    public boolean containsOnly(String property) {
        return frameObject.containsKey(property) && com.tangem.jsonld.framing.ValuePatternMatcher.isWildcard(frameObject, property);
    }

    public boolean isWildCard() {
        return com.tangem.jsonld.framing.ValuePatternMatcher.isWildcard(frameObject);
    }
    
    public boolean isWildCard(String property) {
        return frameObject.containsKey(property) 
                    && com.tangem.jsonld.framing.ValuePatternMatcher.isWildcard(frameObject.get(property));
    }

    public boolean isNone(String property) {
        return frameObject.containsKey(property) 
                    && com.tangem.jsonld.framing.ValuePatternMatcher.isNone(frameObject.get(property));
    }

    public Collection<JsonValue> getCollection(String property) {
        return frameObject.containsKey(property)
                    ? JsonUtils.toJsonArray(frameObject.get(property))
                    : JsonValue.EMPTY_JSON_ARRAY;
    }
    
    @Override
    public String toString() {
        return frameObject.toString();
    }

    public boolean isValuePattern() {
        return ValueObject.isValueObject(frameObject);
    }
    
    public boolean matchValue(JsonValue value) {
        return JsonUtils.isObject(value) && ValuePatternMatcher.with(frameObject, value.asJsonObject()).match();
    }

    public boolean isDefaultObject(String property) {
        return DefaultObject.isDefaultObject(frameObject.get(property))
                || JsonUtils.isArray(frameObject.get(property))
                    && frameObject.get(property).asJsonArray().size() == 1
                    && DefaultObject.isDefaultObject(frameObject.get(property).asJsonArray().get(0))
                ;
    }

    public boolean isNodePattern() {
        return NodeObject.isNodeObject(frameObject);
    }

    public boolean isNodeReference() {
        return NodeObject.isNodeReference(frameObject);
    }
    
    public boolean matchNode(FramingState state, JsonValue value, boolean requireAll) throws JsonLdError {
        if (JsonUtils.isNotObject(value) || !value.asJsonObject().containsKey(Keywords.ID)) {
            return false;
        }
        
        JsonValue valueObject = state.getGraphMap().get(state.getGraphName())
                .get(value.asJsonObject().getString(Keywords.ID));
     
        if (JsonUtils.isNotObject(valueObject)) {
            return false;
        }
        
        return FrameMatcher.with(state, this, requireAll).match(valueObject.asJsonObject());
    }

    public boolean isListObject() {
        return ListObject.isListObject(frameObject);
    }
}
