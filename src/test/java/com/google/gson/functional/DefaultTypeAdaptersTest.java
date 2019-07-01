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
package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import junit.framework.TestCase;

/**
 * Functional test for Json serialization and deserialization for common classes for which default
 * support is provided in Gson. The tests for Map types are available in {@link MapTest}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class DefaultTypeAdaptersTest extends TestCase {
  private Gson gson;
  private TimeZone oldTimeZone;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
    gson = new Gson();
  }

  @Override
  protected void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    super.tearDown();
  }

  public void testClassSerialization() {
    try {
      gson.toJson(String.class);
    } catch (final UnsupportedOperationException expected) {}
    // Override with a custom type adapter for class.
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals("\"java.lang.String\"", gson.toJson(String.class));
  }

  public void testClassDeserialization() {
    try {
      gson.fromJson("String.class", String.class.getClass());
    } catch (final UnsupportedOperationException expected) {}
    // Override with a custom type adapter for class.
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals(String.class, gson.fromJson("java.lang.String", Class.class));
  }

  public void testUrlSerialization() throws Exception {
    final String urlValue = "http://google.com/";
    final URL url = new URL(urlValue);
    assertEquals("\"http://google.com/\"", gson.toJson(url));
  }

  public void testUrlDeserialization() {
    final String urlValue = "http://google.com/";
    final String json = "'http:\\/\\/google.com\\/'";
    final URL target = gson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());

    gson.fromJson('"' + urlValue + '"', URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

  public void testUrlNullSerialization() throws Exception {
    final ClassWithUrlField target = new ClassWithUrlField();
    assertEquals("{}", gson.toJson(target));
  }

  public void testUrlNullDeserialization() {
    final String json = "{}";
    final ClassWithUrlField target = gson.fromJson(json, ClassWithUrlField.class);
    assertNull(target.url);
  }

  private static class ClassWithUrlField {
    URL url;
  }

  public void testUriSerialization() throws Exception {
    final String uriValue = "http://google.com/";
    final URI uri = new URI(uriValue);
    assertEquals("\"http://google.com/\"", gson.toJson(uri));
  }

  public void testUriDeserialization() {
    final String uriValue = "http://google.com/";
    final String json = '"' + uriValue + '"';
    final URI target = gson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }

  public void testNullSerialization() throws Exception {
    testNullSerializationAndDeserialization(Boolean.class);
    testNullSerializationAndDeserialization(Byte.class);
    testNullSerializationAndDeserialization(Short.class);
    testNullSerializationAndDeserialization(Integer.class);
    testNullSerializationAndDeserialization(Long.class);
    testNullSerializationAndDeserialization(Double.class);
    testNullSerializationAndDeserialization(Float.class);
    testNullSerializationAndDeserialization(Number.class);
    testNullSerializationAndDeserialization(Character.class);
    testNullSerializationAndDeserialization(String.class);
    testNullSerializationAndDeserialization(StringBuilder.class);
    testNullSerializationAndDeserialization(StringBuffer.class);
    testNullSerializationAndDeserialization(BigDecimal.class);
    testNullSerializationAndDeserialization(BigInteger.class);
    testNullSerializationAndDeserialization(TreeSet.class);
    testNullSerializationAndDeserialization(ArrayList.class);
    testNullSerializationAndDeserialization(HashSet.class);
    testNullSerializationAndDeserialization(Properties.class);
    testNullSerializationAndDeserialization(URL.class);
    testNullSerializationAndDeserialization(URI.class);
    testNullSerializationAndDeserialization(UUID.class);
    testNullSerializationAndDeserialization(Locale.class);
    testNullSerializationAndDeserialization(InetAddress.class);
    testNullSerializationAndDeserialization(BitSet.class);
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(GregorianCalendar.class);
    testNullSerializationAndDeserialization(Calendar.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
    testNullSerializationAndDeserialization(java.sql.Date.class);
//    testNullSerializationAndDeserialization(Enum.class); //fails, makes field accessible
    testNullSerializationAndDeserialization(Class.class);
  }

  private void testNullSerializationAndDeserialization(final Class<?> c) {
    assertEquals("null", gson.toJson(null, c));
    assertEquals(null, gson.fromJson("null", c));
  }

  public void testUuidSerialization() throws Exception {
    final String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    final UUID uuid = UUID.fromString(uuidValue);
    assertEquals('"' + uuidValue + '"', gson.toJson(uuid));
  }

  public void testUuidDeserialization() {
    final String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    final String json = '"' + uuidValue + '"';
    final UUID target = gson.fromJson(json, UUID.class);
    assertEquals(uuidValue, target.toString());
  }

  public void testLocaleSerializationWithLanguage() {
    final Locale target = new Locale("en");
    assertEquals("\"en\"", gson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguage() {
    final String json = "\"en\"";
    final Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

  public void testLocaleSerializationWithLanguageCountry() {
    final Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", gson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguageCountry() {
    final String json = "\"fr_CA\"";
    final Locale locale = gson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

  public void testLocaleSerializationWithLanguageCountryVariant() {
    final Locale target = new Locale("de", "DE", "EURO");
    final String json = gson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

  public void testLocaleDeserializationWithLanguageCountryVariant() {
    final String json = "\"de_DE_EURO\"";
    final Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

  public void testBigDecimalFieldSerialization() {
    final ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    final String json = gson.toJson(target);
    final String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

  public void testBigDecimalFieldDeserialization() {
    final ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    final String json = expected.getExpectedJson();
    final ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (final JsonParseException expected) { }
  }

  public void testBigIntegerFieldSerialization() {
    final ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    final String json = gson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  public void testBigIntegerFieldDeserialization() {
    final ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    final String json = expected.getExpectedJson();
    final ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }

  public void testOverrideBigIntegerTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigInteger.class, new NumberAsStringAdapter(BigInteger.class))
        .create();
    assertEquals("\"123\"", gson.toJson(new BigInteger("123"), BigInteger.class));
    assertEquals(new BigInteger("123"), gson.fromJson("\"123\"", BigInteger.class));
  }

  public void testOverrideBigDecimalTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigDecimal.class, new NumberAsStringAdapter(BigDecimal.class))
        .create();
    assertEquals("\"1.1\"", gson.toJson(new BigDecimal("1.1"), BigDecimal.class));
    assertEquals(new BigDecimal("1.1"), gson.fromJson("\"1.1\"", BigDecimal.class));
  }

  public void testSetSerialization() throws Exception {
    final Gson gson = new Gson();
    final HashSet<String> s = new HashSet<String>();
    s.add("blah");
    String json = gson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = gson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

  public void testBitSetSerialization() throws Exception {
    final Gson gson = new Gson();
    final BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    final String json = gson.toJson(bits);
    assertEquals("[0,1,0,1,1,1,0,0,0,1]", json);
  }

  public void testBitSetDeserialization() throws Exception {
    final BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);

    final Gson gson = new Gson();
    String json = gson.toJson(expected);
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[1,0,1,1,1,1,0,0,1,0,0,0]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[\"1\",\"0\",\"1\",\"1\",\"1\",\"1\",\"0\",\"0\",\"1\"]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[true,false,true,true,true,true,false,false,true,false,false]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));
  }

//  public void testDefaultDateSerialization() {
//    final Date now = new Date(1315806903103L);
//    final String json = gson.toJson(now);
//    assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
//  }

//  public void testDefaultDateDeserialization() {
//    final String json = "'Dec 13, 2009 07:18:02 AM'";
//    final Date extracted = gson.fromJson(json, Date.class);
//    assertEqualsDate(extracted, 2009, 11, 13);
//    assertEqualsTime(extracted, 7, 18, 2);
//  }

  // Date can not directly be compared with another instance since the deserialization loses the
  // millisecond portion.
  @SuppressWarnings("deprecation")
  private void assertEqualsDate(final Date date, final int year, final int month, final int day) {
    assertEquals(year-1900, date.getYear());
    assertEquals(month, date.getMonth());
    assertEquals(day, date.getDate());
  }

  @SuppressWarnings("deprecation")
  private void assertEqualsTime(final Date date, final int hours, final int minutes, final int seconds) {
    assertEquals(hours, date.getHours());
    assertEquals(minutes, date.getMinutes());
    assertEquals(seconds, date.getSeconds());
  }

  public void testDefaultJavaSqlDateSerialization() {
    final java.sql.Date instant = new java.sql.Date(1259875082000L);
    final String json = gson.toJson(instant);
    assertEquals("\"Dec 3, 2009\"", json);
  }

  public void testDefaultJavaSqlDateDeserialization() {
    final String json = "'Dec 3, 2009'";
    final java.sql.Date extracted = gson.fromJson(json, java.sql.Date.class);
    assertEqualsDate(extracted, 2009, 11, 3);
  }

//  public void testDefaultJavaSqlTimestampSerialization() {
//    final Timestamp now = new java.sql.Timestamp(1259875082000L);
//    final String json = gson.toJson(now);
//    assertEquals("\"Dec 3, 2009 1:18:02 PM\"", json);
//  }

//  public void testDefaultJavaSqlTimestampDeserialization() {
//    final String json = "'Dec 3, 2009 1:18:02 PM'";
//    final Timestamp extracted = gson.fromJson(json, Timestamp.class);
//    assertEqualsDate(extracted, 2009, 11, 3);
//    assertEqualsTime(extracted, 13, 18, 2);
//  }

  public void testDefaultJavaSqlTimeSerialization() {
    final Time now = new Time(1259875082000L);
    final String json = gson.toJson(now);
    assertEquals("\"01:18:02 PM\"", json);
  }

  public void testDefaultJavaSqlTimeDeserialization() {
    final String json = "'1:18:02 PM'";
    final Time extracted = gson.fromJson(json, Time.class);
    assertEqualsTime(extracted, 13, 18, 2);
  }

//  public void testDefaultDateSerializationUsingBuilder() throws Exception {
//    final Gson gson = new GsonBuilder().create();
//    final Date now = new Date(1315806903103L);
//    final String json = gson.toJson(now);
//    assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
//  }

  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    final Gson gson = new GsonBuilder().create();
    final Date now = new Date(1315806903103L);
    final String json = gson.toJson(now);
    final Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.toString(), extracted.toString());
  }

  public void testDefaultCalendarSerialization() throws Exception {
    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(Calendar.getInstance());
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  public void testDefaultCalendarDeserialization() throws Exception {
    final Gson gson = new GsonBuilder().create();
    final String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    final Calendar cal = gson.fromJson(json, Calendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  public void testDefaultGregorianCalendarSerialization() throws Exception {
    final Gson gson = new GsonBuilder().create();
    final GregorianCalendar cal = new GregorianCalendar();
    final String json = gson.toJson(cal);
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  public void testDefaultGregorianCalendarDeserialization() throws Exception {
    final Gson gson = new GsonBuilder().create();
    final String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    final GregorianCalendar cal = gson.fromJson(json, GregorianCalendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  public void testDateSerializationWithPattern() throws Exception {
    final String pattern = "yyyy-MM-dd";
    final Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    final Date now = new Date(1315806903103L);
    final String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  @SuppressWarnings("deprecation")
  public void testDateDeserializationWithPattern() throws Exception {
    final String pattern = "yyyy-MM-dd";
    final Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    final Date now = new Date(1315806903103L);
    final String json = gson.toJson(now);
    final Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() throws Exception {
    final String pattern = "yyyy-MM-dd";
    final Gson gson = new GsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          @Override
          public Date deserialize(final JsonElement json, final Type typeOfT,
              final JsonDeserializationContext context)
              throws JsonParseException {
            return new Date(1315806903103L);
          }
        })
        .create();

    final Date now = new Date(1315806903103L);
    final String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  public void testDateSerializationInCollection() throws Exception {
    final Type listOfDates = new TypeToken<List<Date>>() {}.getType();
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    final Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      final List<Date> dates = Arrays.asList(new Date(0));
      final String json = gson.toJson(dates, listOfDates);
      assertEquals("[\"1970-01-01\"]", json);
      assertEquals(0L, gson.<List<Date>>fromJson("[\"1970-01-01\"]", listOfDates).get(0).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  public void testTimestampSerialization() throws Exception {
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    final Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      final Timestamp timestamp = new Timestamp(0L);
      final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      final String json = gson.toJson(timestamp, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", Timestamp.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  public void testSqlDateSerialization() throws Exception {
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    final Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      final java.sql.Date sqlDate = new java.sql.Date(0L);
      final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      final String json = gson.toJson(sqlDate, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", java.sql.Date.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testJsonPrimitiveSerialization() {
    assertEquals("5", gson.toJson(new JsonPrimitive(5), JsonElement.class));
    assertEquals("true", gson.toJson(new JsonPrimitive(true), JsonElement.class));
    assertEquals("\"foo\"", gson.toJson(new JsonPrimitive("foo"), JsonElement.class));
    assertEquals("\"a\"", gson.toJson(new JsonPrimitive('a'), JsonElement.class));
  }

  public void testJsonPrimitiveDeserialization() {
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonElement.class));
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonPrimitive.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonElement.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonPrimitive.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonElement.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonPrimitive.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonElement.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonPrimitive.class));
  }

  public void testJsonNullSerialization() {
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonElement.class));
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonNull.class));
  }

  public void testNullJsonElementSerialization() {
    assertEquals("null", gson.toJson(null, JsonElement.class));
    assertEquals("null", gson.toJson(null, JsonNull.class));
  }

  public void testJsonArraySerialization() {
    final JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));
    assertEquals("[1,2,3]", gson.toJson(array, JsonElement.class));
  }

  public void testJsonArrayDeserialization() {
    final JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));

    final String json = "[1,2,3]";
    assertEquals(array, gson.fromJson(json, JsonElement.class));
    assertEquals(array, gson.fromJson(json, JsonArray.class));
  }

  public void testJsonObjectSerialization() {
    final JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));
    assertEquals("{\"foo\":1,\"bar\":2}", gson.toJson(object, JsonElement.class));
  }

  public void testJsonObjectDeserialization() {
    final JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));

    final String json = "{\"foo\":1,\"bar\":2}";
    final JsonElement actual = gson.fromJson(json, JsonElement.class);
    assertEquals(object, actual);

    final JsonObject actualObj = gson.fromJson(json, JsonObject.class);
    assertEquals(object, actualObj);
  }

  public void testJsonNullDeserialization() {
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonElement.class));
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonNull.class));
  }

  public void testJsonElementTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (final JsonSyntaxException expected) {
      assertEquals("Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive",
          expected.getMessage());
    }
  }

  private static class ClassWithBigDecimal {
    BigDecimal value;
    ClassWithBigDecimal(final String value) {
      this.value = new BigDecimal(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value.toEngineeringString() + "}";
    }
  }

  private static class ClassWithBigInteger {
    BigInteger value;
    ClassWithBigInteger(final String value) {
      this.value = new BigInteger(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }

  public void testPropertiesSerialization() {
    final Properties props = new Properties();
    props.setProperty("foo", "bar");
    final String json = gson.toJson(props);
    final String expected = "{\"foo\":\"bar\"}";
    assertEquals(expected, json);
  }

  public void testPropertiesDeserialization() {
    final String json = "{foo:'bar'}";
    final Properties props = gson.fromJson(json, Properties.class);
    assertEquals("bar", props.getProperty("foo"));
  }

  public void testTreeSetSerialization() {
    final TreeSet<String> treeSet = new TreeSet<String>();
    treeSet.add("Value1");
    final String json = gson.toJson(treeSet);
    assertEquals("[\"Value1\"]", json);
  }

  public void testTreeSetDeserialization() {
    final String json = "['Value1']";
    final Type type = new TypeToken<TreeSet<String>>() {}.getType();
    final TreeSet<String> treeSet = gson.fromJson(json, type);
    assertTrue(treeSet.contains("Value1"));
  }

  public void testStringBuilderSerialization() {
    final StringBuilder sb = new StringBuilder("abc");
    final String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  public void testStringBuilderDeserialization() {
    final StringBuilder sb = gson.fromJson("'abc'", StringBuilder.class);
    assertEquals("abc", sb.toString());
  }

  public void testStringBufferSerialization() {
    final StringBuffer sb = new StringBuffer("abc");
    final String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  public void testStringBufferDeserialization() {
    final StringBuffer sb = gson.fromJson("'abc'", StringBuffer.class);
    assertEquals("abc", sb.toString());
  }

  @SuppressWarnings("rawtypes")
  private static class MyClassTypeAdapter extends TypeAdapter<Class> {
    @Override
    public void write(final JsonWriter out, final Class value) throws IOException {
      out.value(value.getName());
    }
    @Override
    public Class read(final JsonReader in) throws IOException {
      final String className = in.nextString();
      try {
        return Class.forName(className);
      } catch (final ClassNotFoundException e) {
        throw new IOException(e);
      }
    }
  }

  static class NumberAsStringAdapter extends TypeAdapter<Number> {
    private final Constructor<? extends Number> constructor;
    NumberAsStringAdapter(final Class<? extends Number> type) throws Exception {
      constructor = type.getConstructor(String.class);
    }
    @Override public void write(final JsonWriter out, final Number value) throws IOException {
      out.value(value.toString());
    }
    @Override public Number read(final JsonReader in) throws IOException {
      try {
        return constructor.newInstance(in.nextString());
      } catch (final Exception e) {
        throw new AssertionError(e);
      }
    }
  }
}
