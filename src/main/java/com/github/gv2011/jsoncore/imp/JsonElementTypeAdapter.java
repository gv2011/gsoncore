/*
 * Copyright (C) 2011 Google Inc.
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

package com.github.gv2011.jsoncore.imp;

import java.io.IOException;
import java.util.Map;

import com.github.gv2011.jsoncore.JsonElement;
import com.github.gv2011.jsoncore.JsonPrimitive;
import com.github.gv2011.jsoncore.JsonWriter;

/**
 * Type adapters for basic types.
 */
public final class JsonElementTypeAdapter {

  public static final JsonElementTypeAdapter JSON_ELEMENT = new JsonElementTypeAdapter();

  public void write(final JsonWriter out, final JsonElement value) throws IOException {
    if (value == null || value.isJsonNull()) {
      out.nullValue();
    } else if (value.isJsonPrimitive()) {
      final JsonPrimitive primitive = value.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        out.value(primitive.getAsNumber());
      } else if (primitive.isBoolean()) {
        out.value(primitive.getAsBoolean());
      } else {
        out.value(primitive.getAsString());
      }

    } else if (value.isJsonArray()) {
      out.beginArray();
      for (final JsonElement e : value.getAsJsonArray()) {
        write(out, e);
      }
      out.endArray();

    } else if (value.isJsonObject()) {
      out.beginObject();
      for (final Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
        out.name(e.getKey());
        write(out, e.getValue());
      }
      out.endObject();

    } else {
      throw new IllegalArgumentException("Couldn't write " + value.getClass());
    }
  }


}
