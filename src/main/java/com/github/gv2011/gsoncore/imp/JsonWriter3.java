/*
 * Copyright (C) 2016 Vinz (https://github.com/gv2011)
 * Copyright (C) 2010 Google Inc.
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

package com.github.gv2011.gsoncore.imp;

import static com.github.gv2011.gsoncore.imp.JsonScope.DANGLING_NAME;
import static com.github.gv2011.gsoncore.imp.JsonScope.EMPTY_ARRAY;
import static com.github.gv2011.gsoncore.imp.JsonScope.EMPTY_DOCUMENT;
import static com.github.gv2011.gsoncore.imp.JsonScope.EMPTY_OBJECT;
import static com.github.gv2011.gsoncore.imp.JsonScope.NONEMPTY_ARRAY;
import static com.github.gv2011.gsoncore.imp.JsonScope.NONEMPTY_DOCUMENT;
import static com.github.gv2011.gsoncore.imp.JsonScope.NONEMPTY_OBJECT;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.gv2011.gsoncore.JsonEncoder;
import com.github.gv2011.gsoncore.JsonFactory;
import com.github.gv2011.gsoncore.JsonOption;
import com.github.gv2011.gsoncore.JsonSerializer;


public class JsonWriter3 implements JsonSerializer {


  /** The output data, containing at most one top-level array or object. */
  private final Writer out;

  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    push(EMPTY_DOCUMENT);
  }

  /**
   * A string containing a full set of spaces for a single level of
   * indentation, or null for no pretty printing.
   */
  private String indent;

  /**
   * The name/value separator; either ":" or ": ".
   */
  private String separator = ":";

  private final boolean lenient;

  private final boolean htmlSafe;

  private String deferredName;

  private final boolean serializeNulls;
  private final Set<JsonOption> optList;

  private final JsonEncoder<String> stringEncoder;
  private final JsonEncoder<Long> longEncoder;

  /**
   * Creates a new instance that writes a JSON-encoded stream to {@code out}.
   * For best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   */
  public JsonWriter3(final Writer out, final JsonFactory factory, final JsonOption... options) {
    this.out = out;
    optList = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(options)));
    lenient = optList.contains(JsonOption.LENIENT);
    htmlSafe = optList.contains(JsonOption.HTML_SAFE);
    serializeNulls = true;
    stringEncoder = factory.newJsonEncoder(String.class, options);
    longEncoder = factory.newJsonEncoder(Long.class, options);
  }

  /**
   * Sets the indentation string to be repeated for each level of indentation
   * in the encoded document. If {@code indent.isEmpty()} the encoded document
   * will be compact. Otherwise the encoded document will be more
   * human-readable.
   *
   * @param indent a string containing only whitespace.
   */
  public final void setIndent(final String indent) {
    if (indent.length() == 0) {
      this.indent = null;
      separator = ":";
    } else {
      this.indent = indent;
      separator = ": ";
    }
  }

  /**
   * Configure this writer to relax its syntax rules. By default, this writer
   * only emits well-formed JSON as specified by <a
   * href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>. Setting the writer
   * to lenient permits the following:
   * <ul>
   *   <li>Top-level values of any type. With strict writing, the top-level
   *       value must be an object or an array.
   *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
   *       Double#isInfinite() infinities}.
   * </ul>
   */
//  public final void setLenient(final boolean lenient) {
//    this.lenient = lenient;
//  }

  /**
   * Returns true if this writer has relaxed syntax rules.
   */
  public boolean isLenient() {
    return lenient;
  }

  /**
   * Configure this writer to emit JSON that's safe for direct inclusion in HTML
   * and XML documents. This escapes the HTML characters {@code <}, {@code >},
   * {@code &} and {@code =} before writing them to the stream. Without this
   * setting, your XML/HTML encoder should replace these characters with the
   * corresponding escape sequences.
   */
//  public final void setHtmlSafe(final boolean htmlSafe) {
//    this.htmlSafe = htmlSafe;
//  }

  /**
   * Returns true if this writer writes JSON that's safe for inclusion in HTML
   * and XML documents.
   */
  public final boolean isHtmlSafe() {
    return htmlSafe;
  }

  /**
   * Sets whether object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
//  public final void setSerializeNulls(final boolean serializeNulls) {
//    this.serializeNulls = serializeNulls;
//  }

  /**
   * Returns true if object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
  public final boolean getSerializeNulls() {
    return serializeNulls;
  }

  /**
   * Begins encoding a new array. Each call to this method must be paired with
   * a call to {@link #endArray}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 beginArray(){
    writeDeferredName();
    return open(EMPTY_ARRAY, "[");
  }

  /**
   * Ends encoding the current array.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 endArray(){
    return close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
  }

  /**
   * Begins encoding a new object. Each call to this method must be paired
   * with a call to {@link #endObject}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 beginObject(){
    writeDeferredName();
    return open(EMPTY_OBJECT, "{");
  }

  /**
   * Ends encoding the current object.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 endObject(){
    return close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private JsonWriter3 open(final int empty, final String openBracket){
    beforeValue();
    push(empty);
    write(openBracket);
    return this;
  }

  private void write(final String str) {
    run(()->out.write(str));
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private JsonWriter3 close(final int empty, final int nonempty, final String closeBracket)
     {
    final int context = peek();
    if (context != nonempty && context != empty) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (deferredName != null) {
      throw new IllegalStateException("Dangling name: " + deferredName);
    }

    stackSize--;
    if (context == nonempty) {
      newline();
    }
    write(closeBracket);
    return this;
  }

  private void push(final int newTop) {
    if (stackSize == stack.length) {
      final int[] newStack = new int[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      stack = newStack;
    }
    stack[stackSize++] = newTop;
  }

  /**
   * Returns the value on the top of the stack.
   */
  private int peek() {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    return stack[stackSize - 1];
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(final int topOfStack) {
    stack[stackSize - 1] = topOfStack;
  }

  /**
   * Encodes the property name.
   *
   * @param name the name of the forthcoming value. May not be null.
   * @return this writer.
   */
  @Override
  public JsonWriter3 name(final String name){
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    if (deferredName != null) {
      throw new IllegalStateException();
    }
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    deferredName = name;
    return this;
  }

  private void writeDeferredName(){
    if (deferredName != null) {
      beforeName();
      stringEncoder.encode(deferredName, out);
      deferredName = null;
    }
  }

  /**
   * Encodes {@code value}.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final String value){
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    stringEncoder.encode(value, out);
    return this;
  }

  /**
   * Writes {@code value} directly to the writer without quoting or
   * escaping.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public JsonWriter3 jsonValue(final String value){
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    append(value);
    return this;
  }

  private void append(final String str) {
    run(()->out.append(str));
  }

  /**
   * Encodes {@code null}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 nullValue(){
    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
      } else {
        deferredName = null;
        return this; // skip the name and the value
      }
    }
    beforeValue();
    write("null");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final boolean value){
    writeDeferredName();
    beforeValue();
    write(value ? "true" : "false");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final Boolean value){
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    write(value ? "true" : "false");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final double value){
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    writeDeferredName();
    beforeValue();
    append(Double.toString(value));
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final long value){
    writeDeferredName();
    beforeValue();
    longEncoder.encode(value, out);
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  @Override
  public JsonWriter3 value(final Number value){
    if (value == null) {
      return nullValue();
    }

    writeDeferredName();
    final String string = value.toString();
    if (!lenient
        && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    append(string);
    return this;
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Writer}
   * and flushes that writer.
   */
  @Override
  public void flush(){
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    run(out::flush);
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   *
   * @throws IOException if the JSON document is incomplete.
   */
  @Override
  public void close(){
    run(out::close);

    final int size = stackSize;
    if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IllegalStateException("Incomplete document");
    }
    stackSize = 0;
  }


  private void newline(){
    if (indent == null) {
      return;
    }

    write("\n");
    for (int i = 1, size = stackSize; i < size; i++) {
      write(indent);
    }
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  private void beforeName(){
    final int context = peek();
    if (context == NONEMPTY_OBJECT) { // first in object
      run(()->out.write(','));
    } else if (context != EMPTY_OBJECT) { // not in an object!
      throw new IllegalStateException("Nesting problem.");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }

  /**
   * Inserts any necessary separators and whitespace before a literal value,
   * inline array, or inline object. Also adjusts the stack to expect either a
   * closing bracket or another element.
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue(){
    switch (peek()) {
    case NONEMPTY_DOCUMENT:
      if (!lenient) {
        throw new IllegalStateException(
            "JSON must have only one top-level value.");
      }
      // fall-through
    case EMPTY_DOCUMENT: // first in document
      replaceTop(NONEMPTY_DOCUMENT);
      break;

    case EMPTY_ARRAY: // first in array
      replaceTop(NONEMPTY_ARRAY);
      newline();
      break;

    case NONEMPTY_ARRAY: // another in array
      run(()->out.append(','));
      newline();
      break;

    case DANGLING_NAME: // value for name
      append(separator);
      replaceTop(NONEMPTY_OBJECT);
      break;

    default:
      throw new IllegalStateException("Nesting problem.");
    }
  }
}
