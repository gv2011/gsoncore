/*
 * Copyright (C) 2010 Google Inc.
 * Copyright (C) 2016-2021 Vinz (https://github.com/gv2011)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gv2011.gsoncore;

import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.json.JsonNodeType;

/**
 * A structure, name or value type in a JSON-encoded string.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
enum JsonToken {

  /**
   * The opening of a JSON array. Written using {@link JsonWriter#beginArray}
   * and read using {@link JsonReader#beginArray}.
   */
  BEGIN_ARRAY(JsonNodeType.LIST),

  /**
   * The closing of a JSON array. Written using {@link JsonWriter#endArray}
   * and read using {@link JsonReader#endArray}.
   */
  END_ARRAY,

  /**
   * The opening of a JSON object. Written using {@link JsonWriter#beginObject}
   * and read using {@link JsonReader#beginObject}.
   */
  BEGIN_OBJECT(JsonNodeType.OBJECT),

  /**
   * The closing of a JSON object. Written using {@link JsonWriter#endObject}
   * and read using {@link JsonReader#endObject}.
   */
  END_OBJECT,

  /**
   * A JSON property name. Within objects, tokens alternate between names and
   * their values. Written using {@link JsonWriter#name} and read using {@link
   * JsonReader#nextName}
   */
  NAME,

  /**
   * A JSON string.
   */
  STRING(JsonNodeType.STRING),

  /**
   * A JSON number represented in this API by a Java {@code double}, {@code
   * long}, or {@code int}.
   */
  NUMBER(JsonNodeType.NUMBER),

  /**
   * A JSON {@code true} or {@code false}.
   */
  BOOLEAN(JsonNodeType.BOOLEAN),

  /**
   * A JSON {@code null}.
   */
  NULL(JsonNodeType.NULL),

  /**
   * The end of the JSON stream. This sentinel value is returned by {@link
   * JsonReader#peek()} to signal that the JSON-encoded value has no more
   * tokens.
   */
  END_DOCUMENT;
  
  private final Opt<JsonNodeType> nodeType;
  
  private JsonToken(){
    nodeType = Opt.empty();
  }
  
  private JsonToken(JsonNodeType nodeType){
    this.nodeType = Opt.of(nodeType);
  }
  
  Opt<JsonNodeType> nodeType(){
    return nodeType;
  }
}
