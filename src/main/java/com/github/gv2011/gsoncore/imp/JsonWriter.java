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
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.notYetImplementedException;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.gv2011.gsoncore.JsonEncoder;
import com.github.gv2011.gsoncore.JsonOption;
import com.github.gv2011.gsoncore.JsonSerializer;
import com.github.gv2011.gsoncore.imp.enc.EncoderSelector;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.ser.ElementarySerializer;


public class JsonWriter implements ElementarySerializer<Object,String,Appendable>, JsonSerializer {


  /** The output data, containing at most one top-level array or object. */
  private final Appendable out;

  private byte[] stack = new byte[32];
  private int stackSize = 0;
  {
    push(EMPTY_DOCUMENT);
  }

  /**
   * A string containing a full set of spaces for a single level of
   * indentation, or an empty string for no pretty printing.
   */
  private final String indent;

  /**
   * The name/value separator; either ":" or ": ".
   */
  private final String separator;

  private final boolean lenient;

  private final boolean htmlSafe;

  private final Set<JsonOption> optList;

  private final JsonEncoder<Void> nullEncoder;
  private final JsonEncoder<String> stringEncoder;

  private final EncoderSelector encoderSelector;

  private boolean inKey;


  /**
   * Creates a new instance that writes a JSON-encoded stream to {@code out}.
   * For best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   *
   * Sets the indentation string to be repeated for each level of indentation
   * in the encoded document. If {@code indent.isEmpty()} the encoded document
   * will be compact. Otherwise the encoded document will be more
   * human-readable.
   *
   * @param indent the width of indentation (number of blanks).
   *   If indent==0 the encoded document will be compact. Otherwise the encoded document will be more
   *   human-readable.
   *
   */
  public JsonWriter(
    final Writer out, final EncoderSelector encoderSelector, final int indent, final JsonOption... options
  ) {
    this.out = out;
    optList = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(options)));
    lenient = optList.contains(JsonOption.LENIENT);
    htmlSafe = optList.contains(JsonOption.HTML_SAFE);
    this.encoderSelector = encoderSelector;
    nullEncoder = encoderSelector.tryGetEncoder(Void.class).get();
    stringEncoder = encoderSelector.tryGetEncoder(String.class).get();

    this.indent = StringUtils.multiply(" ",indent);
    separator = indent<=0?":":": ";
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


  @Override
  public void startList(){
    checkNotInKey();
    open(EMPTY_ARRAY, "[");
  }


  private void checkNotInKey() {
    if(inKey) throw new IllegalStateException("Only strings may be used as keys.");
  }


  @Override
  public void endList(){
    close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
  }

  @Override
  public void startMap(){
    checkNotInKey();
    open(EMPTY_OBJECT, "{");
  }


  @Override
  public void endMap(){
    close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private JsonWriter open(final byte empty, final String openBracket){
    beforeValue();
    push(empty);
    write(openBracket);
    return this;
  }

  private void write(final String str) {
    run(()->out.append(str));
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private JsonWriter close(final int empty, final int nonempty, final String closeBracket)
     {
    final int context = peek();
    if (context != nonempty && context != empty) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (inKey) {
      throw new IllegalStateException("In key.");
    }

    stackSize--;
    if (context == nonempty) {
      newline();
    }
    write(closeBracket);
    return this;
  }

  private void push(final byte newTop) {
    if (stackSize == stack.length) {
      final byte[] newStack = new byte[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      stack = newStack;
    }
    stack[stackSize++] = newTop;
  }

  /**
   * Returns the value on the top of the stack.
   */
  private int peek() {
    if (isClosed()) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    return stack[stackSize - 1];
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(final byte topOfStack) {
    stack[stackSize - 1] = topOfStack;
  }



  public boolean isClosed() {
    return stackSize == 0;
  }

  private void checkNotClosed(){
    if (isClosed()) throw new IllegalStateException("JsonWriter is closed.");
  }


  private void append(final String str) {
    run(()->out.append(str));
  }

  private void append(final char c) {
    run(()->out.append(c));
  }



  @Override
  public void serializeElementary(final Object value){
    if(inKey){
      inKey=false;
      if(value.getClass()!=String.class) throw new IllegalArgumentException(
        format("Only strings allowed as keys.", value.getClass())
      );
    }
    beforeValue();
    encode(value, out);
  }

  private String encode(final Object value) {
    return encoderSelector.selectEncoder(value).encode(value);
  }

  private void encode(final Object value, final Appendable out) {
    encoderSelector.selectEncoder(value).encode(value, out);
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Appendable}.
   */
  @Override
  public void flush(){
    if (isClosed()) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   *
   * @throws IOException if the JSON document is incomplete.
   */
  @Override
  public void endDocument(){
    final int size = stackSize;
    if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IllegalStateException("Incomplete document");
    }
    stackSize = 0;
  }


  private void newline(){
    if (indent.isEmpty()) {
      return;
    }

    write("\n");
    for (int i = 1, size = stackSize; i < size; i++) {
      write(indent);
    }
  }

  @Override
  public void startBean(){
    open(EMPTY_OBJECT, "{");
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

    case EMPTY_OBJECT: // first in object
      replaceTop(DANGLING_NAME);
      newline();
      break;

    case NONEMPTY_OBJECT: // another in object
      replaceTop(DANGLING_NAME);
      append(',');
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


  @Override
  public void startDocument() {
  }



  @Override
  public void endBean() {
    close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
  }


  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  @Override
  public void startBeanEntry() {
    final int context = peek();
    if (context == NONEMPTY_OBJECT) { // first in object
      this.
      append(',');
    } else if (context != EMPTY_OBJECT) { // not in an object!
      throw new IllegalStateException("Nesting problem.");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }



  @Override
  public void startBeanValue() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void endBeanEntry() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void startMapEntry() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void startMapValue() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void endMapEntry() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void serializeNull() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer beginArray() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer endArray() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer beginObject() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer endObject() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer name(final String name) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final String value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer nullValue() {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final boolean value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final Boolean value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final double value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final long value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


  @Override
  public JsonSerializer value(final Number value) {
    // TODO Auto-generated method stub
    throw notYetImplementedException();
  }


}
