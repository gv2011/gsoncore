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

package com.github.gv2011.gson.stream;

import static com.github.gv2011.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.github.gv2011.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.github.gv2011.gson.stream.JsonToken.BOOLEAN;
import static com.github.gv2011.gson.stream.JsonToken.END_ARRAY;
import static com.github.gv2011.gson.stream.JsonToken.END_OBJECT;
import static com.github.gv2011.gson.stream.JsonToken.NAME;
import static com.github.gv2011.gson.stream.JsonToken.NULL;
import static com.github.gv2011.gson.stream.JsonToken.NUMBER;
import static com.github.gv2011.gson.stream.JsonToken.STRING;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import junit.framework.TestCase;

public final class GsonReaderTest extends TestCase {
  public void testReadArray() {
    GsonReader reader = new GsonReader(reader("[true, true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadEmptyArray() {
    GsonReader reader = new GsonReader(reader("[]"));
    reader.beginArray();
    assertFalse(reader.hasNext());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadObject() {
    GsonReader reader = new GsonReader(reader(
        "{\"a\": \"android\", \"b\": \"banana\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("android", reader.nextString());
    assertEquals("b", reader.nextName());
    assertEquals("banana", reader.nextString());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadEmptyObject() {
    GsonReader reader = new GsonReader(reader("{}"));
    reader.beginObject();
    assertFalse(reader.hasNext());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipArray() {
    GsonReader reader = new GsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    assertEquals(123, reader.nextInt());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipArrayAfterPeek() throws Exception {
    GsonReader reader = new GsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(BEGIN_ARRAY, reader.peek());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    assertEquals(123, reader.nextInt());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipTopLevelObject() throws Exception {
    GsonReader reader = new GsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipObject() {
    GsonReader reader = new GsonReader(reader(
        "{\"a\": { \"c\": [], \"d\": [true, true, {}] }, \"b\": \"banana\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipObjectAfterPeek() throws Exception {
    String json = "{" + "  \"one\": { \"num\": 1 }"
        + ", \"two\": { \"num\": 2 }" + ", \"three\": { \"num\": 3 }" + "}";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginObject();
    assertEquals("one", reader.nextName());
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.skipValue();
    assertEquals("two", reader.nextName());
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.skipValue();
    assertEquals("three", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipInteger() {
    GsonReader reader = new GsonReader(reader(
        "{\"a\":123456789,\"b\":-123456789}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipDouble() {
    GsonReader reader = new GsonReader(reader(
        "{\"a\":-123.456e-789,\"b\":123456789.0}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testHelloWorld() {
    String json = "{\n" +
        "   \"hello\": true,\n" +
        "   \"foo\": [\"world\"]\n" +
        "}";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginObject();
    assertEquals("hello", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    assertEquals("foo", reader.nextName());
    reader.beginArray();
    assertEquals("world", reader.nextString());
    reader.endArray();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testEmptyString() {
    try {
      new GsonReader(reader("")).beginArray();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      new GsonReader(reader("")).beginObject();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testCharacterUnescaping() {
    String json = "[\"a\","
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
        + "\"\\u0019\","
        + "\"\\u20AC\""
        + "]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    assertEquals("a", reader.nextString());
    assertEquals("a\"", reader.nextString());
    assertEquals("\"", reader.nextString());
    assertEquals(":", reader.nextString());
    assertEquals(",", reader.nextString());
    assertEquals("\b", reader.nextString());
    assertEquals("\f", reader.nextString());
    assertEquals("\n", reader.nextString());
    assertEquals("\r", reader.nextString());
    assertEquals("\t", reader.nextString());
    assertEquals(" ", reader.nextString());
    assertEquals("\\", reader.nextString());
    assertEquals("{", reader.nextString());
    assertEquals("}", reader.nextString());
    assertEquals("[", reader.nextString());
    assertEquals("]", reader.nextString());
    assertEquals("\0", reader.nextString());
    assertEquals("\u0019", reader.nextString());
    assertEquals("\u20AC", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testUnescapingInvalidCharacters() {
    String json = "[\"\\u000g\"]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  public void testUnescapingTruncatedCharacters() {
    String json = "[\"\\u000";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testUnescapingTruncatedSequence() {
    String json = "[\"\\";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testIntegersWithFractionalPartSpecified() {
    GsonReader reader = new GsonReader(reader("[1.0,1.0,1.0]"));
    reader.beginArray();
    assertEquals(1.0, reader.nextDouble());
    assertEquals(1, reader.nextInt());
    assertEquals(1L, reader.nextLong());
  }

  public void testDoubles() {
    String json = "[-0.0,"
        + "1.0,"
        + "1.7976931348623157E308,"
        + "4.9E-324,"
        + "0.0,"
        + "-0.5,"
        + "2.2250738585072014E-308,"
        + "3.141592653589793,"
        + "2.718281828459045]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    assertEquals(-0.0, reader.nextDouble());
    assertEquals(1.0, reader.nextDouble());
    assertEquals(1.7976931348623157E308, reader.nextDouble());
    assertEquals(4.9E-324, reader.nextDouble());
    assertEquals(0.0, reader.nextDouble());
    assertEquals(-0.5, reader.nextDouble());
    assertEquals(2.2250738585072014E-308, reader.nextDouble());
    assertEquals(3.141592653589793, reader.nextDouble());
    assertEquals(2.718281828459045, reader.nextDouble());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictNonFiniteDoubles() {
    String json = "[NaN]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictQuotedNonFiniteDoubles() {
    String json = "[\"NaN\"]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientNonFiniteDoubles() {
    String json = "[NaN, -Infinity, Infinity]";
    GsonReader reader = new GsonReader(reader(json), true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testLenientQuotedNonFiniteDoubles() {
    String json = "[\"NaN\", \"-Infinity\", \"Infinity\"]";
    GsonReader reader = new GsonReader(reader(json), true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testStrictNonFiniteDoublesWithSkipValue() {
    String json = "[NaN]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLongs() {
    String json = "[0,0,0,"
        + "1,1,1,"
        + "-1,-1,-1,"
        + "-9223372036854775808,"
        + "9223372036854775807]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    assertEquals(0L, reader.nextLong());
    assertEquals(0, reader.nextInt());
    assertEquals(0.0, reader.nextDouble());
    assertEquals(1L, reader.nextLong());
    assertEquals(1, reader.nextInt());
    assertEquals(1.0, reader.nextDouble());
    assertEquals(-1L, reader.nextLong());
    assertEquals(-1, reader.nextInt());
    assertEquals(-1.0, reader.nextDouble());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void disabled_testNumberWithOctalPrefix() {
    String json = "[01]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextInt();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextLong();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
    assertEquals("01", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testBooleans() {
    GsonReader reader = new GsonReader(reader("[true,false]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testPeekingUnquotedStringsPrefixedWithBooleans() {
    GsonReader reader = new GsonReader(reader("[truey]"), true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("truey", reader.nextString());
    reader.endArray();
  }

  public void testMalformedNumbers() {
    assertNotANumber("-");
    assertNotANumber(".");

    // exponent lacks digit
    assertNotANumber("e");
    assertNotANumber("0e");
    assertNotANumber(".e");
    assertNotANumber("0.e");
    assertNotANumber("-.0e");

    // no integer
    assertNotANumber("e1");
    assertNotANumber(".e1");
    assertNotANumber("-e1");

    // trailing characters
    assertNotANumber("1x");
    assertNotANumber("1.1x");
    assertNotANumber("1e1x");
    assertNotANumber("1ex");
    assertNotANumber("1.1ex");
    assertNotANumber("1.1e1x");

    // fraction has no digit
    assertNotANumber("0.");
    assertNotANumber("-0.");
    assertNotANumber("0.e1");
    assertNotANumber("-0.e1");

    // no leading digit
    assertNotANumber(".0");
    assertNotANumber("-.0");
    assertNotANumber(".0e1");
    assertNotANumber("-.0e1");
  }

  private void assertNotANumber(String s) {
    GsonReader reader = new GsonReader(reader("[" + s + "]"), true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(s, reader.nextString());
    reader.endArray();
  }

  public void testPeekingUnquotedStringsPrefixedWithIntegers() {
    GsonReader reader = new GsonReader(reader("[12.34e5x]"), true);
   reader.beginArray();
    assertEquals(STRING, reader.peek());
    try {
      reader.nextInt();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("12.34e5x", reader.nextString());
  }

  public void testPeekLongMinValue() {
    GsonReader reader = new GsonReader(reader("[-9223372036854775808]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    assertEquals(-9223372036854775808L, reader.nextLong());
  }

  public void testPeekLongMaxValue() {
    GsonReader reader = new GsonReader(reader("[9223372036854775807]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    assertEquals(9223372036854775807L, reader.nextLong());
  }

  public void testLongLargerThanMaxLongThatWrapsAround() {
    GsonReader reader = new GsonReader(reader("[22233720368547758070]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  public void testLongLargerThanMinLongThatWrapsAround() {
    GsonReader reader = new GsonReader(reader("[-22233720368547758070]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  /**
   * This test fails because there's no double for 9223372036854775808, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testPeekLargerThanLongMaxValue() {
    GsonReader reader = new GsonReader(reader("[9223372036854775808]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException e) {
    }
  }

  /**
   * This test fails because there's no double for -9223372036854775809, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testPeekLargerThanLongMinValue() {
    GsonReader reader = new GsonReader(reader("[-9223372036854775809]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-9223372036854775809d, reader.nextDouble());
  }

  /**
   * This test fails because there's no double for 9223372036854775806, and
   * our long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testHighPrecisionLong() {
    String json = "[9223372036854775806.000]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    assertEquals(9223372036854775806L, reader.nextLong());
    reader.endArray();
  }

  public void testPeekMuchLargerThanLongMinValue() {
    GsonReader reader = new GsonReader(reader("[-92233720368547758080]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-92233720368547758080d, reader.nextDouble());
  }

  public void testQuotedNumberWithEscape() {
    GsonReader reader = new GsonReader(reader("[\"12\u00334\"]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    assertEquals(1234, reader.nextInt());
  }

  public void testMixedCaseLiterals() {
    GsonReader reader = new GsonReader(reader("[True,TruE,False,FALSE,NULL,nulL]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testMissingValue() {
    GsonReader reader = new GsonReader(reader("{\"a\":}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testPrematureEndOfInput() {
    GsonReader reader = new GsonReader(reader("{\"a\":true,"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testPrematurelyClosed() {
    try {
      GsonReader reader = new GsonReader(reader("{\"a\":[]}"));
      reader.beginObject();
      reader.close();
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      GsonReader reader = new GsonReader(reader("{\"a\":[]}"));
      reader.close();
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      GsonReader reader = new GsonReader(reader("{\"a\":true}"));
      reader.beginObject();
      reader.nextName();
      reader.peek();
      reader.close();
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testNextFailuresDoNotAdvance() {
    GsonReader reader = new GsonReader(reader("{\"a\":true}"));
    reader.beginObject();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("a", reader.nextName());
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    reader.close();
  }

  public void testIntegerMismatchFailuresDoNotAdvance() {
    GsonReader reader = new GsonReader(reader("[1.5]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(1.5d, reader.nextDouble());
    reader.endArray();
  }

  public void testStringNullIsNotNull() {
    GsonReader reader = new GsonReader(reader("[\"null\"]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testNullLiteralIsNotAString() {
    GsonReader reader = new GsonReader(reader("[null]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testStrictNameValueSeparator() {
    GsonReader reader = new GsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientNameValueSeparator() {
    GsonReader reader = new GsonReader(reader("{\"a\"=true}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());

    reader = new GsonReader(reader("{\"a\"=>true}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictNameValueSeparatorWithSkipValue() {
    GsonReader reader = new GsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testCommentsInStringValue() throws Exception {
    GsonReader reader = new GsonReader(reader("[\"// comment\"]"));
    reader.beginArray();
    assertEquals("// comment", reader.nextString());
    reader.endArray();

    reader = new GsonReader(reader("{\"a\":\"#someComment\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("#someComment", reader.nextString());
    reader.endObject();

    reader = new GsonReader(reader("{\"#//a\":\"#some //Comment\"}"));
    reader.beginObject();
    assertEquals("#//a", reader.nextName());
    assertEquals("#some //Comment", reader.nextString());
    reader.endObject();
  }

  public void testStrictComments() {
    GsonReader reader = new GsonReader(reader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientComments() {
    GsonReader reader = new GsonReader(reader("[// comment \n true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new GsonReader(reader("[# comment \n true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new GsonReader(reader("[/* comment */ true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictCommentsWithSkipValue() {
    GsonReader reader = new GsonReader(reader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedNames() {
    GsonReader reader = new GsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientUnquotedNames() {
    GsonReader reader = new GsonReader(reader("{a:true}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictUnquotedNamesWithSkipValue() {
    GsonReader reader = new GsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictSingleQuotedNames() {
    GsonReader reader = new GsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientSingleQuotedNames() {
    GsonReader reader = new GsonReader(reader("{'a':true}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictSingleQuotedNamesWithSkipValue() {
    GsonReader reader = new GsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedStrings() {
    GsonReader reader = new GsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedStringsWithSkipValue() {
    GsonReader reader = new GsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientUnquotedStrings() {
    GsonReader reader = new GsonReader(reader("[a]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStrings() {
    GsonReader reader = new GsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientSingleQuotedStrings() {
    GsonReader reader = new GsonReader(reader("['a']"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStringsWithSkipValue() {
    GsonReader reader = new GsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictSemicolonDelimitedArray() {
    GsonReader reader = new GsonReader(reader("[true;true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientSemicolonDelimitedArray() {
    GsonReader reader = new GsonReader(reader("[true;true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictSemicolonDelimitedArrayWithSkipValue() {
    GsonReader reader = new GsonReader(reader("[true;true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictSemicolonDelimitedNameValuePair() {
    GsonReader reader = new GsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientSemicolonDelimitedNameValuePair() {
    GsonReader reader = new GsonReader(reader("{\"a\":true;\"b\":true}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    assertEquals("b", reader.nextName());
  }

  public void testStrictSemicolonDelimitedNameValuePairWithSkipValue() {
    GsonReader reader = new GsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnnecessaryArraySeparators() {
    GsonReader reader = new GsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientUnnecessaryArraySeparators() {
    GsonReader reader = new GsonReader(reader("[true,,true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new GsonReader(reader("[,true]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new GsonReader(reader("[true,]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    reader.endArray();

    reader = new GsonReader(reader("[,]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  public void testStrictUnnecessaryArraySeparatorsWithSkipValue() {
    GsonReader reader = new GsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new GsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictMultipleTopLevelValues() {
    GsonReader reader = new GsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientMultipleTopLevelValues() {
    GsonReader reader = new GsonReader(reader("[] true {}"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(true, reader.nextBoolean());
    reader.beginObject();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictMultipleTopLevelValuesWithSkipValue() {
    GsonReader reader = new GsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testTopLevelValueTypes() {
    GsonReader reader1 = new GsonReader(reader("true"));
    assertTrue(reader1.nextBoolean());
    assertEquals(JsonToken.END_DOCUMENT, reader1.peek());

    GsonReader reader2 = new GsonReader(reader("false"));
    assertFalse(reader2.nextBoolean());
    assertEquals(JsonToken.END_DOCUMENT, reader2.peek());

    GsonReader reader3 = new GsonReader(reader("null"));
    assertEquals(JsonToken.NULL, reader3.peek());
    reader3.nextNull();
    assertEquals(JsonToken.END_DOCUMENT, reader3.peek());

    GsonReader reader4 = new GsonReader(reader("123"));
    assertEquals(123, reader4.nextInt());
    assertEquals(JsonToken.END_DOCUMENT, reader4.peek());

    GsonReader reader5 = new GsonReader(reader("123.4"));
    assertEquals(123.4, reader5.nextDouble());
    assertEquals(JsonToken.END_DOCUMENT, reader5.peek());

    GsonReader reader6 = new GsonReader(reader("\"a\""));
    assertEquals("a", reader6.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader6.peek());
  }

  public void testTopLevelValueTypeWithSkipValue() {
    GsonReader reader = new GsonReader(reader("true"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictNonExecutePrefix() {
    GsonReader reader = new GsonReader(reader(")]}'\n []"));
    try {
      reader.beginArray();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictNonExecutePrefixWithSkipValue() {
    GsonReader reader = new GsonReader(reader(")]}'\n []"));
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientNonExecutePrefix() {
    GsonReader reader = new GsonReader(reader(")]}'\n []"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientNonExecutePrefixWithLeadingWhitespace() {
    GsonReader reader = new GsonReader(reader("\r\n \t)]}'\n []"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientPartialNonExecutePrefix() {
    GsonReader reader = new GsonReader(reader(")]}' []"), true);
    //reader.setLenient(true);
    try {
      assertEquals(")", reader.nextString());
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testBomIgnoredAsFirstCharacterOfDocument() {
    GsonReader reader = new GsonReader(reader("\ufeff[]"));
    reader.beginArray();
    reader.endArray();
  }

  public void testBomForbiddenAsOtherCharacterInDocument() {
    GsonReader reader = new GsonReader(reader("[\ufeff]"));
    reader.beginArray();
    try {
      reader.endArray();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testFailWithPosition() {
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n\n\n\n\"a\",}]");
  }

  public void testFailWithPositionGreaterThanBufferSize() {
    String spaces = repeat(' ', 8192);
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n" + spaces + "\n\n\n\"a\",}]");
  }

  public void testFailWithPositionOverSlashSlashEndOfLineComment() {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n// foo\n\n//bar\r\n[\"a\",}");
  }

  public void testFailWithPositionOverHashEndOfLineComment() {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n# foo\n\n#bar\r\n[\"a\",}");
  }

  public void testFailWithPositionOverCStyleComment() {
    testFailWithPosition("Expected value at line 6 column 12 path $[1]",
        "\n\n/* foo\n*\n*\r\nbar */[\"a\",}");
  }

  public void testFailWithPositionOverQuotedString() {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]",
        "[\"foo\nbar\r\nbaz\n\",\n  }");
  }

  public void testFailWithPositionOverUnquotedString() {
    testFailWithPosition("Expected value at line 5 column 2 path $[1]", "[\n\nabcd\n\n,}");
  }

  public void testFailWithEscapedNewlineCharacter() {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]", "[\n\n\"\\\n\n\",}");
  }

  public void testFailWithPositionIsOffsetByBom() {
    testFailWithPosition("Expected value at line 1 column 6 path $[1]",
        "\ufeff[\"a\",}]");
  }

  private void testFailWithPosition(String message, String json) {
    // Validate that it works reading the string normally.
    GsonReader reader1 = new GsonReader(reader(json), true);
    //reader1.setLenient(true);
    reader1.beginArray();
    reader1.nextString();
    try {
      reader1.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(message, expected.getMessage());
    }

    // Also validate that it works when skipping.
    GsonReader reader2 = new GsonReader(reader(json), true);
    //reader2.setLenient(true);
    reader2.beginArray();
    reader2.skipValue();
    try {
      reader2.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(message, expected.getMessage());
    }
  }

  public void testFailWithPositionDeepPath() {
    GsonReader reader = new GsonReader(reader("[1,{\"a\":[2,3,}"));
    reader.beginArray();
    reader.nextInt();
    reader.beginObject();
    reader.nextName();
    reader.beginArray();
    reader.nextInt();
    reader.nextInt();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 14 path $[1].a[2]", expected.getMessage());
    }
  }

  public void testStrictVeryLongNumber() {
    GsonReader reader = new GsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.beginArray();
    try {
      assertEquals(1d, reader.nextDouble());
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientVeryLongNumber() {
    GsonReader reader = new GsonReader(reader("[0." + repeat('9', 8192) + "]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(1d, reader.nextDouble());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testVeryLongUnquotedLiteral() {
    String literal = "a" + repeat('b', 8192) + "c";
    GsonReader reader = new GsonReader(reader("[" + literal + "]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(literal, reader.nextString());
    reader.endArray();
  }

  public void testDeeplyNestedArrays() {
    // this is nested 40 levels deep; Gson is tuned for nesting is 30 levels deep or fewer
    GsonReader reader = new GsonReader(reader(
        "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    assertEquals("$[0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0]"
        + "[0][0][0][0][0][0][0][0][0][0][0][0][0][0]", reader.getPath());
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testDeeplyNestedObjects() {
    // Build a JSON document structured like {"a":{"a":{"a":{"a":true}}}}, but 40 levels deep
    String array = "{\"a\":%s}";
    String json = "true";
    for (int i = 0; i < 40; i++) {
      json = String.format(array, json);
    }

    GsonReader reader = new GsonReader(reader(json));
    for (int i = 0; i < 40; i++) {
      reader.beginObject();
      assertEquals("a", reader.nextName());
    }
    assertEquals("$.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a"
        + ".a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a", reader.getPath());
    assertEquals(true, reader.nextBoolean());
    for (int i = 0; i < 40; i++) {
      reader.endObject();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // http://code.google.com/p/google-gson/issues/detail?id=409
  public void testStringEndingInSlash() {
    GsonReader reader = new GsonReader(reader("/"), true);
    //reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testDocumentWithCommentEndingInSlash() {
    GsonReader reader = new GsonReader(reader("/* foo *//"), true);
    //reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStringWithLeadingSlash() {
    GsonReader reader = new GsonReader(reader("/x"), true);
    //reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testUnterminatedObject() {
    GsonReader reader = new GsonReader(reader("{\"a\":\"android\"x"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("android", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testVeryLongQuotedString() {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[\"" + string + "\"]";
    GsonReader reader = new GsonReader(reader(json));
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongUnquotedString() {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    GsonReader reader = new GsonReader(reader(json), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongUnterminatedString() {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string;
    GsonReader reader = new GsonReader(reader(json), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testSkipVeryLongUnquotedString() {
    GsonReader reader = new GsonReader(reader("[" + repeat('x', 8192) + "]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  public void testSkipTopLevelUnquotedString() {
    GsonReader reader = new GsonReader(reader(repeat('x', 8192)), true);
    //reader.setLenient(true);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipVeryLongQuotedString() {
    GsonReader reader = new GsonReader(reader("[\"" + repeat('x', 8192) + "\"]"));
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  public void testSkipTopLevelQuotedString() {
    GsonReader reader = new GsonReader(reader("\"" + repeat('x', 8192) + "\""), true);
    //reader.setLenient(true);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStringAsNumberWithTruncatedExponent() {
    GsonReader reader = new GsonReader(reader("[123e]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testStringAsNumberWithDigitAndNonDigitExponent() {
    GsonReader reader = new GsonReader(reader("[123e4b]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testStringAsNumberWithNonDigitExponent() {
    GsonReader reader = new GsonReader(reader("[123eb]"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testEmptyStringName() {
    GsonReader reader = new GsonReader(reader("{\"\":true}"), true);
    //reader.setLenient(true);
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(NAME, reader.peek());
    assertEquals("", reader.nextName());
    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertEquals(true, reader.nextBoolean());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictExtraCommasInMaps() {
    GsonReader reader = new GsonReader(reader("{\"a\":\"b\",}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("b", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientExtraCommasInMaps() {
    GsonReader reader = new GsonReader(reader("{\"a\":\"b\",}"), true);
    //reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("b", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  private String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
  }

  public void testMalformedDocuments() {
    assertDocument("{]", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{,", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{{", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{[", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{:", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\":}", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\"::", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\":,", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\"=}", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\"=>}", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\"=>\"string\":", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\"=>\"string\"=", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\"=>\"string\"=>", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\"=>\"string\",", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\"=>\"string\",\"name\"", BEGIN_OBJECT, NAME, STRING, NAME);
    assertDocument("[}", BEGIN_ARRAY, MalformedJsonException.class);
    assertDocument("[,]", BEGIN_ARRAY, NULL, NULL, END_ARRAY);
    assertDocument("{", BEGIN_OBJECT, MalformedJsonException.class);
    assertDocument("{\"name\"", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{'name'", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{'name',", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("{name", BEGIN_OBJECT, NAME, MalformedJsonException.class);
    assertDocument("[", BEGIN_ARRAY, MalformedJsonException.class);
    assertDocument("[string", BEGIN_ARRAY, STRING, MalformedJsonException.class);
    assertDocument("[\"string\"", BEGIN_ARRAY, STRING, MalformedJsonException.class);
    assertDocument("['string'", BEGIN_ARRAY, STRING, MalformedJsonException.class);
    assertDocument("[123", BEGIN_ARRAY, NUMBER, MalformedJsonException.class);
    assertDocument("[123,", BEGIN_ARRAY, NUMBER, MalformedJsonException.class);
    assertDocument("{\"name\":123", BEGIN_OBJECT, NAME, NUMBER, MalformedJsonException.class);
    assertDocument("{\"name\":123,", BEGIN_OBJECT, NAME, NUMBER, MalformedJsonException.class);
    assertDocument("{\"name\":\"string\"", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\":\"string\",", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\":'string'", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\":'string',", BEGIN_OBJECT, NAME, STRING, MalformedJsonException.class);
    assertDocument("{\"name\":false", BEGIN_OBJECT, NAME, BOOLEAN, MalformedJsonException.class);
    assertDocument("{\"name\":false,,", BEGIN_OBJECT, NAME, BOOLEAN, MalformedJsonException.class);
  }

  /**
   * This test behave slightly differently in Gson 2.2 and earlier. It fails
   * during peek rather than during nextString().
   */
  public void testUnterminatedStringFailure() {
    GsonReader reader = new GsonReader(reader("[\"string"), true);
    //reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  private void assertDocument(String document, Object... expectations) {
    GsonReader reader = new GsonReader(reader(document), true);
    //reader.setLenient(true);
    for (Object expectation : expectations) {
      if (expectation == BEGIN_OBJECT) {
        reader.beginObject();
      } else if (expectation == BEGIN_ARRAY) {
        reader.beginArray();
      } else if (expectation == END_OBJECT) {
        reader.endObject();
      } else if (expectation == END_ARRAY) {
        reader.endArray();
      } else if (expectation == NAME) {
        assertEquals("name", reader.nextName());
      } else if (expectation == BOOLEAN) {
        assertEquals(false, reader.nextBoolean());
      } else if (expectation == STRING) {
        assertEquals("string", reader.nextString());
      } else if (expectation == NUMBER) {
        assertEquals(123, reader.nextInt());
      } else if (expectation == NULL) {
        reader.nextNull();
      } else if (expectation == MalformedJsonException.class) {
        try {
          reader.peek();
          fail();
        } catch (MalformedJsonException expected) {
        }
      } else {
        throw new AssertionError();
      }
    }
  }

  /**
   * Returns a reader that returns one character at a time.
   */
  private Reader reader(final String s) {
    /* if (true) */ return new StringReader(s);
    /* return new Reader() {
      int position = 0;
      @Override public int read(char[] buffer, int offset, int count) {
        if (position == s.length()) {
          return -1;
        } else if (count > 0) {
          buffer[offset] = s.charAt(position++);
          return 1;
        } else {
          throw new IllegalArgumentException();
        }
      }
      @Override public void close() {
      }
    }; */
  }
}
