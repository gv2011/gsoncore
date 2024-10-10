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

import static com.github.gv2011.gsoncore.JsonScope.DANGLING_NAME;
import static com.github.gv2011.gsoncore.JsonScope.EMPTY_ARRAY;
import static com.github.gv2011.gsoncore.JsonScope.EMPTY_DOCUMENT;
import static com.github.gv2011.gsoncore.JsonScope.EMPTY_OBJECT;
import static com.github.gv2011.gsoncore.JsonScope.NONEMPTY_ARRAY;
import static com.github.gv2011.gsoncore.JsonScope.NONEMPTY_DOCUMENT;
import static com.github.gv2011.gsoncore.JsonScope.NONEMPTY_OBJECT;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.json.JsonWriter;
import com.github.gv2011.util.num.Decimal;

/**
 * Writes a JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new {@code JsonWriter}. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 * <ul>
 *   <li>To write <strong>arrays</strong>, first call {@link #beginArray()}.
 *       Write each of the array's elements with the appropriate {@link #value}
 *       methods or by nesting other arrays and objects. Finally close the array
 *       using {@link #endArray()}.
 *   <li>To write <strong>objects</strong>, first call {@link #beginObject()}.
 *       Write each of the object's properties by alternating calls to
 *       {@link #name} with the property's value. Write property values with the
 *       appropriate {@link #value} method or by nesting other objects or arrays.
 *       Finally close the object using {@link #endObject()}.
 * </ul>
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I stream JSON in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonWriter!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code encodes the above structure: <pre>   {@code
 *   public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 *     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 *     writer.setIndent("    ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }}</pre>
 *
 * <p>Each {@code JsonWriter} may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an {@link IllegalStateException}.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
public class GsonWriter implements AutoCloseableNt, Flushable, JsonWriter {

  public static final String DEFAULT_INDENT = "  ";

  /*
   * From RFC 7159, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. http://code.google.com/p/google-gson/issues/detail?id=341
   */
  private static final String[] REPLACEMENT_CHARS;
  private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }

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
  private final String indent;

  /**
   * The name/value separator; either ":" or ": ".
   */
  private final String separator;

  private final boolean lenient;

  private final boolean htmlSafe;

  private String deferredName;

  private boolean serializeNulls = false;


  /**
   * Creates a new instance that writes a JSON-encoded stream to {@code out}.
   * For best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   */
  public GsonWriter(final Writer out) {
    this(out, DEFAULT_INDENT, false, false, false);
  }

  public GsonWriter(final Writer out, final String indent) {
    this(out, indent, false, false, false);
  }


  GsonWriter(final Writer out, final String indent, final boolean lenient, final boolean htmlSafe, final boolean serializeNulls) {
    assert out != null;
    this.out = out;
    if (indent.length() == 0) {
      this.indent = null;
      this.separator = ":";
    } else {
      this.indent = indent;
      this.separator = ": ";
    }
    this.lenient = lenient;
    this.htmlSafe = htmlSafe;
    this.serializeNulls = serializeNulls;
  }

  /**
   * Returns true if this writer has relaxed syntax rules.
   */
  public boolean isLenient() {
    return lenient;
  }

  /**
   * Returns true if this writer writes JSON that's safe for inclusion in HTML
   * and XML documents.
   */
  public final boolean isHtmlSafe() {
    return htmlSafe;
  }

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
  public void beginArray() {
    writeDeferredName();
    open(EMPTY_ARRAY, "[");
  }

  /**
   * Ends encoding the current array.
   *
   * @return this writer.
   */
  @Override
  public void endArray(){
    close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
  }

  /**
   * Begins encoding a new object. Each call to this method must be paired
   * with a call to {@link #endObject}.
   *
   * @return this writer.
   */
  @Override
  public void beginObject(){
    writeDeferredName();
    open(EMPTY_OBJECT, "{");
  }

  /**
   * Ends encoding the current object.
   *
   * @return this writer.
   */
  @Override
  public void endObject(){
    close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private void open(final int empty, final String openBracket){
    beforeValue();
    push(empty);
    call(()->out.write(openBracket));
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private GsonWriter close(final int empty, final int nonempty, final String closeBracket){
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
    call(()->out.write(closeBracket));
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
  public void name(final String name){
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
  }

  private void writeDeferredName(){
    if (deferredName != null) {
      beforeName();
      string(deferredName);
      deferredName = null;
    }
  }

  /**
   * Encodes {@code value}.
   *
   * @param value the literal string value, or null to encode a null literal.
   */
  @Override
  public void writeString(final String value){
    assert value!=null;
    writeDeferredName();
    beforeValue();
    string(value);
  }

  /**
   * Writes {@code value} directly to the writer without quoting or
   * escaping.
   *
   * @param value the literal string value, or null to encode a null literal.
   */
  void jsonValue(final String value) throws IOException {
    if (value == null) {
      nullValue();
    }
    else{
      writeDeferredName();
      beforeValue();
      out.append(value);
    }
  }

  /**
   * Encodes {@code null}.
   */
  @Override
  public void nullValue(){
    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
        beforeValue();
        call(()->out.write("null"));
      } else {
        deferredName = null;
      }
    }
    else{
      beforeValue();
      call(()->out.write("null"));
    }
  }

  @Override
  public void writeBoolean(final boolean value){
    writeDeferredName();
    beforeValue();
    call(()->out.write(Boolean.toString(value)));
  }

  @Override
  public void writeDecimal(final Decimal value){
    assert value!=null;
    writeDeferredName();
    beforeValue();
    call(()->out.append(value.toEcmaString()));
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Writer}
   * and flushes that writer.
   */
  @Override
  public void flush(){
    if (stackSize == 0) {
      throw new IllegalStateException("Closed.");
    }
    call(out::flush);
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   *
   * @throws IOException if the JSON document is incomplete.
   */
  @Override
  public void close(){
    call(out::close);

    final int size = stackSize;
    if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT) {
      throw new RuntimeException("Incomplete document");
    }
    stackSize = 0;
  }

  private void string(final String value){
    call(()->{
      final String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
      out.write("\"");
      int last = 0;
      final int length = value.length();
      for (int i = 0; i < length; i++) {
        final char c = value.charAt(i);
        final String replacement;
        if (c < 128) {
          replacement = replacements[c];
          if (replacement == null) {
            continue;
          }
        } else if (c == '\u2028') {
          replacement = "\\u2028";
        } else if (c == '\u2029') {
          replacement = "\\u2029";
        } else {
          continue;
        }
        if (last < i) {
          out.write(value, last, i - last);
        }
        out.write(replacement);
        last = i + 1;
      }
      if (last < length) {
        out.write(value, last, length - last);
      }
      out.write("\"");
    });
  }

  private void newline(){
    if (indent == null) {
      return;
    }
    call(()->{
      out.write("\n");
      for (int i = 1, size = stackSize; i < size; i++) {
        out.write(indent);
      }
    });
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  private void beforeName(){
    final int context = peek();
    if (context == NONEMPTY_OBJECT) { // first in object
      call(()->out.write(','));
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
      call(()->out.append(','));
      newline();
      break;

    case DANGLING_NAME: // value for name
      call(()->out.append(separator));
      replaceTop(NONEMPTY_OBJECT);
      break;

    default:
      throw new IllegalStateException("Nesting problem.");
    }
  }
}
