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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Ignore;

import com.github.gv2011.gsoncore.JsonOption;
import com.github.gv2011.gsoncore.imp.JsonWriter;
import com.github.gv2011.gsoncore.imp.enc.EncoderSelector;

import junit.framework.TestCase;

@Ignore//TODO WIP
public final class JsonWriterTest extends TestCase {

  private JsonWriter newJsonWriter(final Writer w, final JsonOption... jsonOptions){
    return newJsonWriter(w, 0, jsonOptions);
  }

  private JsonWriter newJsonWriter(final Writer w, final int indent, final JsonOption... jsonOptions){
    final EncoderSelector encoders = new EncoderSelector(
      Arrays.asList(jsonOptions).contains(JsonOption.LENIENT)
    );
    return new JsonWriter(w, encoders, indent, jsonOptions);
  }

  public void testTopLevelValueTypes() throws IOException {
    final StringWriter string1 = new StringWriter();
    final JsonWriter writer1 = newJsonWriter(string1);
    writer1.serializeElementary(true);
    writer1.endDocument();
    assertEquals("true", string1.toString());

    final StringWriter string2 = new StringWriter();
    final JsonWriter writer2 = newJsonWriter(string2);
    writer2.nullValue();
    writer2.endDocument();
    assertEquals("null", string2.toString());

    final StringWriter string3 = new StringWriter();
    final JsonWriter writer3 = newJsonWriter(string3);
    writer3.serializeElementary(123);
    writer3.endDocument();
    assertEquals("123", string3.toString());

    final StringWriter string4 = new StringWriter();
    final JsonWriter writer4 = newJsonWriter(string4);
    writer4.serializeElementary(123.4);
    writer4.endDocument();
    assertEquals("123.4", string4.toString());

    final StringWriter string5 = new StringWriter();
    final JsonWriter writert = newJsonWriter(string5);
    writert.serializeElementary("a");
    writert.endDocument();
    assertEquals("\"a\"", string5.toString());
  }

  public void testInvalidTopLevelTypes() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.name("hello");
    try {
      jsonWriter.serializeElementary("world");
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testTwoNames() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.name("a");
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testNameWithoutValue() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.endObject();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testValueWithoutName() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    try {
      jsonWriter.serializeElementary(true);
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testMultipleTopLevelValues() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginArray().endArray();
    try {
      jsonWriter.startList();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testBadNestingObject() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.beginObject();
    try {
      jsonWriter.endArray();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testBadNestingArray() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.startList();
    try {
      jsonWriter.endObject();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testNullName() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    try {
      jsonWriter.name(null);
      fail();
    } catch (final NullPointerException expected) {
    }
  }

//  public void testJsonValue() throws IOException {
//    final StringWriter stringWriter = new StringWriter();
//    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
//    jsonWriter.beginObject();
//    jsonWriter.name("a");
//    jsonWriter.jsonValue("{\"b\":true}");
//    jsonWriter.name("c");
//    jsonWriter.serializeElementary(1);
//    jsonWriter.endObject();
//    assertEquals("{\"a\":{\"b\":true},\"c\":1}", stringWriter.toString());
//  }

  public void testNonFiniteDoubles() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    try {
      jsonWriter.serializeElementary(Double.NaN);
      fail();
    } catch (final IllegalArgumentException expected) {
    }
    try {
      jsonWriter.serializeElementary(Double.NEGATIVE_INFINITY);
      fail();
    } catch (final IllegalArgumentException expected) {
    }
    try {
      jsonWriter.serializeElementary(Double.POSITIVE_INFINITY);
      fail();
    } catch (final IllegalArgumentException expected) {
    }
  }

  public void testNonFiniteBoxedDoubles() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    try {
      jsonWriter.serializeElementary(new Double(Double.NaN));
      fail();
    } catch (final IllegalArgumentException expected) {
    }
    try {
      jsonWriter.serializeElementary(new Double(Double.NEGATIVE_INFINITY));
      fail();
    } catch (final IllegalArgumentException expected) {
    }
    try {
      jsonWriter.serializeElementary(new Double(Double.POSITIVE_INFINITY));
      fail();
    } catch (final IllegalArgumentException expected) {
    }
  }

  public void testDoubles() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary(-0.0);
    jsonWriter.serializeElementary(1.0);
    jsonWriter.serializeElementary(Double.MAX_VALUE);
    jsonWriter.serializeElementary(Double.MIN_VALUE);
    jsonWriter.serializeElementary(0.0);
    jsonWriter.serializeElementary(-0.5);
    jsonWriter.serializeElementary(2.2250738585072014E-308);
    jsonWriter.serializeElementary(Math.PI);
    jsonWriter.serializeElementary(Math.E);
    jsonWriter.endArray();
    jsonWriter.endDocument();
    assertEquals("[-0.0,"
        + "1.0,"
        + "1.7976931348623157E308,"
        + "4.9E-324,"
        + "0.0,"
        + "-0.5,"
        + "2.2250738585072014E-308,"
        + "3.141592653589793,"
        + "2.718281828459045]", stringWriter.toString());
  }

  public void testLongs() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary(0);
    jsonWriter.serializeElementary(1);
    jsonWriter.serializeElementary(-1);
    jsonWriter.serializeElementary(Long.MIN_VALUE);
    jsonWriter.serializeElementary(Long.MAX_VALUE);
    jsonWriter.endArray();
    jsonWriter.endDocument();
    assertEquals("[0,"
        + "1,"
        + "-1,"
        + "-9223372036854775808,"
        + "9223372036854775807]", stringWriter.toString());
  }

  public void testNumbers() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary(new BigInteger("0"));
    jsonWriter.serializeElementary(new BigInteger("9223372036854775808"));
    jsonWriter.serializeElementary(new BigInteger("-9223372036854775809"));
    jsonWriter.serializeElementary(new BigDecimal("3.141592653589793238462643383"));
    jsonWriter.endArray();
    jsonWriter.endDocument();
    assertEquals("[0,"
        + "9223372036854775808,"
        + "-9223372036854775809,"
        + "3.141592653589793238462643383]", stringWriter.toString());
  }

  public void testBooleans() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary(true);
    jsonWriter.serializeElementary(false);
    jsonWriter.endArray();
    assertEquals("[true,false]", stringWriter.toString());
  }

  public void testBoxedBooleans() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary(true);
    jsonWriter.serializeElementary(false);
    jsonWriter.serializeElementary((Boolean) null);
    jsonWriter.endArray();
    assertEquals("[true,false,null]", stringWriter.toString());
  }

  public void testNulls() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.nullValue();
    jsonWriter.endArray();
    assertEquals("[null]", stringWriter.toString());
  }

  public void testStrings() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary("a");
    jsonWriter.serializeElementary("a\"");
    jsonWriter.serializeElementary("\"");
    jsonWriter.serializeElementary(":");
    jsonWriter.serializeElementary(",");
    jsonWriter.serializeElementary("\b");
    jsonWriter.serializeElementary("\f");
    jsonWriter.serializeElementary("\n");
    jsonWriter.serializeElementary("\r");
    jsonWriter.serializeElementary("\t");
    jsonWriter.serializeElementary(" ");
    jsonWriter.serializeElementary("\\");
    jsonWriter.serializeElementary("{");
    jsonWriter.serializeElementary("}");
    jsonWriter.serializeElementary("[");
    jsonWriter.serializeElementary("]");
    jsonWriter.serializeElementary("\0");
    jsonWriter.serializeElementary("\u0019");
    jsonWriter.endArray();
    assertEquals("[\"a\","
        + "\"a\\\"\","
        + "\"\\\"\","
        + "\":\","
        + "\",\","
        + "\"\\b\","
        + "\"\\f\","
        + "\"\\n\","
        + "\"\\r\","
        + "\"\\t\","
        + "\" \","
        + "\"\\\\\","
        + "\"{\","
        + "\"}\","
        + "\"[\","
        + "\"]\","
        + "\"\\u0000\","
        + "\"\\u0019\"]", stringWriter.toString());
  }

  public void testUnicodeLineBreaksEscaped() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.serializeElementary("\u2028 \u2029");
    jsonWriter.endArray();
    assertEquals("[\"\\u2028 \\u2029\"]", stringWriter.toString());
  }

  public void testEmptyArray() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.startList();
    jsonWriter.endArray();
    assertEquals("[]", stringWriter.toString());
  }

  public void testEmptyObject() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.endObject();
    assertEquals("{}", stringWriter.toString());
  }

//  public void testObjectsInArrays() throws IOException {
//    final StringWriter stringWriter = new StringWriter();
//    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
//    jsonWriter.startList();
//    jsonWriter.beginObject();
//    jsonWriter.name("a").serializeElementary(5);
//    jsonWriter.name("b").serializeElementary(false);
//    jsonWriter.endObject();
//    jsonWriter.beginObject();
//    jsonWriter.name("c").serializeElementary(6);
//    jsonWriter.name("d").serializeElementary(true);
//    jsonWriter.endObject();
//    jsonWriter.endArray();
//    assertEquals("[{\"a\":5,\"b\":false},"
//        + "{\"c\":6,\"d\":true}]", stringWriter.toString());
//  }

  public void testArraysInObjects() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.startList();
    jsonWriter.serializeElementary(5);
    jsonWriter.serializeElementary(false);
    jsonWriter.endArray();
    jsonWriter.name("b");
    jsonWriter.startList();
    jsonWriter.serializeElementary(6);
    jsonWriter.serializeElementary(true);
    jsonWriter.endArray();
    jsonWriter.endObject();
    assertEquals("{\"a\":[5,false],"
        + "\"b\":[6,true]}", stringWriter.toString());
  }

  public void testDeepNestingArrays() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    for (int i = 0; i < 20; i++) {
      jsonWriter.startList();
    }
    for (int i = 0; i < 20; i++) {
      jsonWriter.endArray();
    }
    assertEquals("[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]", stringWriter.toString());
  }

  public void testDeepNestingObjects() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
    jsonWriter.beginObject();
    for (int i = 0; i < 20; i++) {
      jsonWriter.name("a");
      jsonWriter.beginObject();
    }
    for (int i = 0; i < 20; i++) {
      jsonWriter.endObject();
    }
    jsonWriter.endObject();
    assertEquals("{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":"
        + "{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{"
        + "}}}}}}}}}}}}}}}}}}}}}", stringWriter.toString());
  }

//  public void testRepeatedName() throws IOException {
//    final StringWriter stringWriter = new StringWriter();
//    final JsonWriter jsonWriter = newJsonWriter(stringWriter);
//    jsonWriter.beginObject();
//    jsonWriter.name("a").serializeElementary(true);
//    jsonWriter.name("a").serializeElementary(false);
//    jsonWriter.endObject();
//    // JsonWriter doesn't attempt to detect duplicate names
//    assertEquals("{\"a\":true,\"a\":false}", stringWriter.toString());
//  }

//  public void testPrettyPrintObject() throws IOException {
//    final StringWriter stringWriter = new StringWriter();
//    final JsonWriter jsonWriter = newJsonWriter(stringWriter, 3);
//
//    jsonWriter.beginObject();
//    jsonWriter.name("a").serializeElementary(true);
//    jsonWriter.name("b").serializeElementary(false);
//    jsonWriter.name("c").serializeElementary(5.0);
//    jsonWriter.name("e").nullValue();
//    jsonWriter.name("f").startList();
//    jsonWriter.serializeElementary(6.0);
//    jsonWriter.serializeElementary(7.0);
//    jsonWriter.endArray();
//    jsonWriter.name("g").beginObject();
//    jsonWriter.name("h").serializeElementary(8.0);
//    jsonWriter.name("i").serializeElementary(9.0);
//    jsonWriter.endObject();
//    jsonWriter.endObject();
//
//    final String expected = "{\n"
//        + "   \"a\": true,\n"
//        + "   \"b\": false,\n"
//        + "   \"c\": 5.0,\n"
//        + "   \"e\": null,\n"
//        + "   \"f\": [\n"
//        + "      6.0,\n"
//        + "      7.0\n"
//        + "   ],\n"
//        + "   \"g\": {\n"
//        + "      \"h\": 8.0,\n"
//        + "      \"i\": 9.0\n"
//        + "   }\n"
//        + "}";
//    assertEquals(expected, stringWriter.toString());
//  }

//  public void testPrettyPrintArray() throws IOException {
//    final StringWriter stringWriter = new StringWriter();
//    final JsonWriter jsonWriter = newJsonWriter(stringWriter, 3);
//
//    jsonWriter.startList();
//    jsonWriter.serializeElementary(true);
//    jsonWriter.serializeElementary(false);
//    jsonWriter.serializeElementary(5.0);
//    jsonWriter.nullValue();
//    jsonWriter.beginObject();
//    jsonWriter.name("a").serializeElementary(6.0);
//    jsonWriter.name("b").serializeElementary(7.0);
//    jsonWriter.endObject();
//    jsonWriter.startList();
//    jsonWriter.serializeElementary(8.0);
//    jsonWriter.serializeElementary(9.0);
//    jsonWriter.endArray();
//    jsonWriter.endArray();
//
//    final String expected = "[\n"
//        + "   true,\n"
//        + "   false,\n"
//        + "   5.0,\n"
//        + "   null,\n"
//        + "   {\n"
//        + "      \"a\": 6.0,\n"
//        + "      \"b\": 7.0\n"
//        + "   },\n"
//        + "   [\n"
//        + "      8.0,\n"
//        + "      9.0\n"
//        + "   ]\n"
//        + "]";
//    assertEquals(expected, stringWriter.toString());
//  }

  public void testLenientWriterPermitsMultipleTopLevelValues() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter, JsonOption.LENIENT);
    writer.startList();
    writer.endArray();
    writer.startList();
    writer.endArray();
    writer.endDocument();
    assertEquals("[][]", stringWriter.toString());
  }

  public void testStrictWriterDoesNotPermitMultipleTopLevelValues() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    try {
      writer.startList();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testClosedWriterThrowsOnStructure() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    writer.endDocument();
    try {
      writer.startList();
      fail();
    } catch (final IllegalStateException expected) {
    }
    try {
      writer.endArray();
      fail();
    } catch (final IllegalStateException expected) {
    }
    try {
      writer.beginObject();
      fail();
    } catch (final IllegalStateException expected) {
    }
    try {
      writer.endObject();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testClosedWriterThrowsOnName() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    writer.endDocument();
    try {
      writer.name("a");
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testClosedWriterThrowsOnValue() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    writer.endDocument();
    try {
      writer.serializeElementary("a");
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testClosedWriterThrowsOnFlush() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    writer.endDocument();
    try {
      writer.flush();
      fail();
    } catch (final IllegalStateException expected) {
    }
  }

  public void testWriterCloseIsIdempotent() throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final JsonWriter writer = newJsonWriter(stringWriter);
    writer.startList();
    writer.endArray();
    writer.endDocument();
    writer.endDocument();
  }
}
