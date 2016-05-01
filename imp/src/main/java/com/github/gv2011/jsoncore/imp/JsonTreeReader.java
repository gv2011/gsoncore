/*
 * Copyright (C) 2016 Vinz (https://github.com/gv2011)
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.gv2011.jsoncore.JsonToken;

/**
 * This reader walks the elements of a JsonElement as if it was coming from a
 * character stream.
 *
 * @author Jesse Wilson
 */
public final class JsonTreeReader extends JsonReader {
  private static final Reader UNREADABLE_READER = new Reader() {
    @Override public int read(final char[] buffer, final int offset, final int count){
      throw new AssertionError();
    }
    @Override public void close(){
      throw new AssertionError();
    }
  };
  private static final Object SENTINEL_CLOSED = new Object();

  private final List<Object> stack = new ArrayList<Object>();

  public JsonTreeReader(final JsonElement element) {
    super(UNREADABLE_READER);
    stack.add(element);
  }

  @Override public void beginArray(){
    expect(JsonToken.BEGIN_ARRAY);
    final JsonArray array = (JsonArray) peekStack();
    stack.add(array.iterator());
  }

  @Override public void endArray(){
    expect(JsonToken.END_ARRAY);
    popStack(); // empty iterator
    popStack(); // array
  }

  @Override public void beginObject(){
    expect(JsonToken.BEGIN_OBJECT);
    final JsonObject object = (JsonObject) peekStack();
    stack.add(object.entrySet().iterator());
  }

  @Override public void endObject(){
    expect(JsonToken.END_OBJECT);
    popStack(); // empty iterator
    popStack(); // object
  }

  @Override public boolean hasNext(){
    final JsonToken token = peek();
    return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY;
  }

  @Override public JsonToken peek(){
    if (stack.isEmpty()) {
      return JsonToken.END_DOCUMENT;
    }

    final Object o = peekStack();
    if (o instanceof Iterator) {
      final boolean isObject = stack.get(stack.size() - 2) instanceof JsonObject;
      final Iterator<?> iterator = (Iterator<?>) o;
      if (iterator.hasNext()) {
        if (isObject) {
          return JsonToken.NAME;
        } else {
          stack.add(iterator.next());
          return peek();
        }
      } else {
        return isObject ? JsonToken.END_OBJECT : JsonToken.END_ARRAY;
      }
    } else if (o instanceof JsonObject) {
      return JsonToken.BEGIN_OBJECT;
    } else if (o instanceof JsonArray) {
      return JsonToken.BEGIN_ARRAY;
    } else if (o instanceof JsonPrimitive) {
      final JsonPrimitive primitive = (JsonPrimitive) o;
      if (primitive.isString()) {
        return JsonToken.STRING;
      } else if (primitive.isBoolean()) {
        return JsonToken.BOOLEAN;
      } else if (primitive.isNumber()) {
        return JsonToken.NUMBER;
      } else {
        throw new AssertionError();
      }
    } else if (o instanceof JsonNull) {
      return JsonToken.NULL;
    } else if (o == SENTINEL_CLOSED) {
      throw new IllegalStateException("JsonReader is closed");
    } else {
      throw new AssertionError();
    }
  }

  private Object peekStack() {
    return stack.get(stack.size() - 1);
  }

  private Object popStack() {
    return stack.remove(stack.size() - 1);
  }

  private void expect(final JsonToken expected){
    if (peek() != expected) {
      throw new IllegalStateException("Expected " + expected + " but was " + peek());
    }
  }

  @Override public String nextName(){
    expect(JsonToken.NAME);
    final Iterator<?> i = (Iterator<?>) peekStack();
    final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
    stack.add(entry.getValue());
    return (String) entry.getKey();
  }

  @Override public String nextString(){
    final JsonToken token = peek();
    if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
      throw new IllegalStateException("Expected " + JsonToken.STRING + " but was " + token);
    }
    return ((JsonPrimitive) popStack()).getAsString();
  }

  @Override public boolean nextBoolean(){
    expect(JsonToken.BOOLEAN);
    return ((JsonPrimitive) popStack()).getAsBoolean();
  }

  @Override public void nextNull(){
    expect(JsonToken.NULL);
    popStack();
  }

  @Override public double nextDouble(){
    final JsonToken token = peek();
    if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
      throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
    }
    final double result = ((JsonPrimitive) peekStack()).getAsDouble();
    if (!isLenient() && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw new NumberFormatException("JSON forbids NaN and infinities: " + result);
    }
    popStack();
    return result;
  }

  @Override public long nextLong(){
    final JsonToken token = peek();
    if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
      throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
    }
    final long result = ((JsonPrimitive) peekStack()).getAsLong();
    popStack();
    return result;
  }

  @Override public int nextInt(){
    final JsonToken token = peek();
    if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
      throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
    }
    final int result = ((JsonPrimitive) peekStack()).getAsInt();
    popStack();
    return result;
  }

  @Override public void close(){
    stack.clear();
    stack.add(SENTINEL_CLOSED);
  }

  @Override public void skipValue(){
    if (peek() == JsonToken.NAME) {
      nextName();
    } else {
      popStack();
    }
  }

  @Override public String toString() {
    return getClass().getSimpleName();
  }

  public void promoteNameToValue(){
    expect(JsonToken.NAME);
    final Iterator<?> i = (Iterator<?>) peekStack();
    final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
    stack.add(entry.getValue());
    stack.add(new JsonPrimitive((String)entry.getKey()));
  }
}
