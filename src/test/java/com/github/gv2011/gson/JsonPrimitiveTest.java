/*
 * Copyright (C) 2008 Google Inc.
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

package com.github.gv2011.gson;

import com.github.gv2011.gson.JsonPrimitive;
import com.github.gv2011.gson.common.MoreAsserts;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Unit test for the {@link JsonPrimitive} class.
 *
 * @author Joel Leitch
 */
public class JsonPrimitiveTest extends TestCase {

  public void testBoolean() throws Exception {
    JsonPrimitive json = new JsonPrimitive(Boolean.TRUE);

    assertTrue(json.isBoolean());
    assertTrue(json.getAsBoolean());

    // Extra support for booleans
    json = new JsonPrimitive(1);
    assertFalse(json.getAsBoolean());

    json = new JsonPrimitive("1");
    assertFalse(json.getAsBoolean());

    json = new JsonPrimitive("true");
    assertTrue(json.getAsBoolean());

    json = new JsonPrimitive("TrUe");
    assertTrue(json.getAsBoolean());

    json = new JsonPrimitive("1.3");
    assertFalse(json.getAsBoolean());
  }

  public void testParsingStringAsBoolean() throws Exception {
    final JsonPrimitive json = new JsonPrimitive("true");

    assertFalse(json.isBoolean());
    assertTrue(json.getAsBoolean());
  }

  public void testParsingStringAsNumber() throws Exception {
    final JsonPrimitive json = new JsonPrimitive("1");

    assertFalse(json.isNumber());
    assertEquals(1D, json.getAsDouble(), 0.00001);
    assertEquals(1F, json.getAsFloat(), 0.00001);
    assertEquals(1, json.getAsInt());
    assertEquals(1L, json.getAsLong());
    assertEquals((short) 1, json.getAsShort());
    assertEquals((byte) 1, json.getAsByte());
    assertEquals(new BigInteger("1"), json.getAsBigInteger());
    assertEquals(new BigDecimal("1"), json.getAsBigDecimal());
  }

  public void testStringsAndChar() throws Exception {
    JsonPrimitive json = new JsonPrimitive("abc");
    assertTrue(json.isString());
    assertEquals('a', json.getAsCharacter());
    assertEquals("abc", json.getAsString());

    json = new JsonPrimitive('z');
    assertTrue(json.isString());
    assertEquals('z', json.getAsCharacter());
    assertEquals("z", json.getAsString());
  }

  public void testExponential() throws Exception {
    final JsonPrimitive json = new JsonPrimitive("1E+7");

    assertEquals(new BigDecimal("1E+7"), json.getAsBigDecimal());
    assertEquals(Double.parseDouble("1E+7"), json.getAsDouble(), 0.00001);
    assertEquals(Float.parseFloat("1E+7"), json.getAsDouble(), 0.00001);

    try {
      json.getAsInt();
      fail("Integers can not handle exponents like this.");
    } catch (final NumberFormatException expected) { }
  }

  public void testByteEqualsShort() {
    final JsonPrimitive p1 = new JsonPrimitive((byte)10);
    final JsonPrimitive p2 = new JsonPrimitive((short)10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testByteEqualsInteger() {
    final JsonPrimitive p1 = new JsonPrimitive((byte)10);
    final JsonPrimitive p2 = new JsonPrimitive(10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testByteEqualsLong() {
    final JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    final JsonPrimitive p2 = new JsonPrimitive(10L);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testByteEqualsBigInteger() {
    final JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    final JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testShortEqualsInteger() {
    final JsonPrimitive p1 = new JsonPrimitive((short)10);
    final JsonPrimitive p2 = new JsonPrimitive(10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testShortEqualsLong() {
    final JsonPrimitive p1 = new JsonPrimitive((short)10);
    final JsonPrimitive p2 = new JsonPrimitive(10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testShortEqualsBigInteger() {
    final JsonPrimitive p1 = new JsonPrimitive((short)10);
    final JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testIntegerEqualsLong() {
    final JsonPrimitive p1 = new JsonPrimitive(10);
    final JsonPrimitive p2 = new JsonPrimitive(10L);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testIntegerEqualsBigInteger() {
    final JsonPrimitive p1 = new JsonPrimitive(10);
    final JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testLongEqualsBigInteger() {
    final JsonPrimitive p1 = new JsonPrimitive(10L);
    final JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testFloatEqualsDouble() {
    final JsonPrimitive p1 = new JsonPrimitive(10.25F);
    final JsonPrimitive p2 = new JsonPrimitive(10.25D);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testFloatEqualsBigDecimal() {
    final JsonPrimitive p1 = new JsonPrimitive(10.25F);
    final JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testDoubleEqualsBigDecimal() {
    final JsonPrimitive p1 = new JsonPrimitive(10.25D);
    final JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  public void testValidJsonOnToString() throws Exception {
    JsonPrimitive json = new JsonPrimitive("Some\nEscaped\nValue");
    assertEquals("\"Some\\nEscaped\\nValue\"", json.toString());

    json = new JsonPrimitive(new BigDecimal("1.333"));
    assertEquals("1.333", json.toString());
  }

  public void testEquals() {
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive("A"), new JsonPrimitive("A"));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(true), new JsonPrimitive(true));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(5L), new JsonPrimitive(5L));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive('a'), new JsonPrimitive('a'));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NaN), new JsonPrimitive(Float.NaN));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NEGATIVE_INFINITY),
        new JsonPrimitive(Float.NEGATIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.POSITIVE_INFINITY),
        new JsonPrimitive(Float.POSITIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.NaN), new JsonPrimitive(Double.NaN));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.NEGATIVE_INFINITY),
        new JsonPrimitive(Double.NEGATIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.POSITIVE_INFINITY),
        new JsonPrimitive(Double.POSITIVE_INFINITY));
    assertFalse(new JsonPrimitive("a").equals(new JsonPrimitive("b")));
    assertFalse(new JsonPrimitive(true).equals(new JsonPrimitive(false)));
    assertFalse(new JsonPrimitive(0).equals(new JsonPrimitive(1)));
  }

  public void testEqualsAcrossTypes() {
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive("a"), new JsonPrimitive('a'));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(0), new JsonPrimitive(0L));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NaN), new JsonPrimitive(Double.NaN));
  }

  public void testEqualsIntegerAndBigInteger() {
    final JsonPrimitive a = new JsonPrimitive(5L);
    final JsonPrimitive b = new JsonPrimitive(new BigInteger("18446744073709551621")); // 2^64 + 5
    // Ideally, the following assertion should have failed but the price is too much to pay
    // assertFalse(a + " equals " + b, a.equals(b));
    assertTrue(a + " equals " + b, a.equals(b));
  }

  public void testEqualsDoesNotEquateStringAndNonStringTypes() {
    assertFalse(new JsonPrimitive("true").equals(new JsonPrimitive(true)));
    assertFalse(new JsonPrimitive("0").equals(new JsonPrimitive(0)));
    assertFalse(new JsonPrimitive("NaN").equals(new JsonPrimitive(Float.NaN)));
  }

  public void testDeepCopy() {
    final JsonPrimitive a = new JsonPrimitive("a");
    assertSame(a, a.deepCopy()); // Primitives are immutable!
  }
}
