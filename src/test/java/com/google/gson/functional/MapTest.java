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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.common.TestTypes;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

/**
 * Functional test for Json serialization and deserialization for Maps
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class MapTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testMapSerialization() {
    final Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":2"));
  }

  public void testMapDeserialization() {
    final String json = "{\"a\":1,\"b\":2}";
    final Type typeOfMap = new TypeToken<Map<String,Integer>>(){}.getType();
    final Map<String, Integer> target = gson.fromJson(json, typeOfMap);
    assertEquals(1, target.get("a").intValue());
    assertEquals(2, target.get("b").intValue());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testRawMapSerialization() {
    final Map map = new LinkedHashMap();
    map.put("a", 1);
    map.put("b", "string");
    final String json = gson.toJson(map);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":\"string\""));
  }

  public void testMapSerializationEmpty() {
    final Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);
    assertEquals("{}", json);
  }

  public void testMapDeserializationEmpty() {
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final Map<String, Integer> map = gson.fromJson("{}", typeOfMap);
    assertTrue(map.isEmpty());
  }

  public void testMapSerializationWithNullValue() {
    final Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", null);
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);

    // Maps are represented as JSON objects, so ignoring null field
    assertEquals("{}", json);
  }

  public void testMapDeserializationWithNullValue() {
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final Map<String, Integer> map = gson.fromJson("{\"abc\":null}", typeOfMap);
    assertEquals(1, map.size());
    assertNull(map.get("abc"));
  }

  public void testMapSerializationWithNullValueButSerializeNulls() {
    gson = new GsonBuilder().serializeNulls().create();
    final Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", null);
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"abc\":null}", json);
  }

  public void testMapSerializationWithNullKey() {
    final Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put(null, 123);
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"null\":123}", json);
  }

  public void testMapDeserializationWithNullKey() {
    final Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{\"null\":123}", typeOfMap);
    assertEquals(1, map.size());
    assertEquals(123, map.get("null").intValue());
    assertNull(map.get(null));

    map = gson.fromJson("{null:123}", typeOfMap);
    assertEquals(1, map.size());
    assertEquals(123, map.get("null").intValue());
    assertNull(map.get(null));
  }

  public void testMapSerializationWithIntegerKeys() {
    final Map<Integer, String> map = new LinkedHashMap<Integer, String>();
    map.put(123, "456");
    final Type typeOfMap = new TypeToken<Map<Integer, String>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"123\":\"456\"}", json);
  }

  public void testMapDeserializationWithIntegerKeys() {
    final Type typeOfMap = new TypeToken<Map<Integer, String>>() {}.getType();
    final Map<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  public void testHashMapDeserialization() throws Exception {
    final Type typeOfMap = new TypeToken<HashMap<Integer, String>>() {}.getType();
    final HashMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  public void testSortedMap() throws Exception {
    final Type typeOfMap = new TypeToken<SortedMap<Integer, String>>() {}.getType();
    final SortedMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  public void testConcurrentMap() throws Exception {
    final Type typeOfMap = new TypeToken<ConcurrentMap<Integer, String>>() {}.getType();
    final ConcurrentMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    final String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

  public void testConcurrentHashMap() throws Exception {
    final Type typeOfMap = new TypeToken<ConcurrentHashMap<Integer, String>>() {}.getType();
    final ConcurrentHashMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    final String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

  public void testConcurrentNavigableMap() throws Exception {
    final Type typeOfMap = new TypeToken<ConcurrentNavigableMap<Integer, String>>() {}.getType();
    final ConcurrentNavigableMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    final String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

  public void testConcurrentSkipListMap() throws Exception {
    final Type typeOfMap = new TypeToken<ConcurrentSkipListMap<Integer, String>>() {}.getType();
    final ConcurrentSkipListMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    final String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

  public void testParameterizedMapSubclassSerialization() {
    final MyParameterizedMap<String, String> map = new MyParameterizedMap<String, String>(10);
    map.put("a", "b");
    final Type type = new TypeToken<MyParameterizedMap<String, String>>() {}.getType();
    final String json = gson.toJson(map, type);
    assertTrue(json.contains("\"a\":\"b\""));
  }

  @SuppressWarnings({ "unused", "serial" })
  private static class MyParameterizedMap<K, V> extends LinkedHashMap<K, V> {
    final int foo;
    MyParameterizedMap(final int foo) {
      this.foo = foo;
    }
  }

  public void testMapSubclassSerialization() {
    final MyMap map = new MyMap();
    map.put("a", "b");
    final String json = gson.toJson(map, MyMap.class);
    assertTrue(json.contains("\"a\":\"b\""));
  }

  public void testMapStandardSubclassDeserialization() {
    final String json = "{a:'1',b:'2'}";
    final Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
    final LinkedHashMap<String, Integer> map = gson.fromJson(json, type);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  public void testMapSubclassDeserialization() {
    final Gson gson = new GsonBuilder().registerTypeAdapter(MyMap.class, new InstanceCreator<MyMap>() {
      @Override
      public MyMap createInstance(final Type type) {
        return new MyMap();
      }
    }).create();
    final String json = "{\"a\":1,\"b\":2}";
    final MyMap map = gson.fromJson(json, MyMap.class);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  public void testCustomSerializerForSpecificMapType() {
    final Type type = $Gson$Types.newParameterizedTypeWithOwner(
        null, Map.class, String.class, Long.class);
    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(type, new JsonSerializer<Map<String, Long>>() {
          @Override
          public JsonElement serialize(final Map<String, Long> src, final Type typeOfSrc,
              final JsonSerializationContext context) {
            final JsonArray array = new JsonArray();
            for (final long value : src.values()) {
              array.add(new JsonPrimitive(value));
            }
            return array;
          }
        }).create();

    final Map<String, Long> src = new LinkedHashMap<String, Long>();
    src.put("one", 1L);
    src.put("two", 2L);
    src.put("three", 3L);

    assertEquals("[1,2,3]", gson.toJson(src, type));
  }

  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=99
   */
  private static class ClassWithAMap {
    Map<String, String> map = new TreeMap<String, String>();
  }

  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=99
   */
  public void testMapSerializationWithNullValues() {
    final ClassWithAMap target = new ClassWithAMap();
    target.map.put("name1", null);
    target.map.put("name2", "value2");
    final String json = gson.toJson(target);
    assertFalse(json.contains("name1"));
    assertTrue(json.contains("name2"));
  }

  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=99
   */
  public void testMapSerializationWithNullValuesSerialized() {
    final Gson gson = new GsonBuilder().serializeNulls().create();
    final ClassWithAMap target = new ClassWithAMap();
    target.map.put("name1", null);
    target.map.put("name2", "value2");
    final String json = gson.toJson(target);
    assertTrue(json.contains("name1"));
    assertTrue(json.contains("name2"));
  }

  public void testMapSerializationWithWildcardValues() {
    final Map<String, ? extends Collection<? extends Integer>> map =
        new LinkedHashMap<String, Collection<Integer>>();
    map.put("test", null);
    final Type typeOfMap =
        new TypeToken<Map<String, ? extends Collection<? extends Integer>>>() {}.getType();
    final String json = gson.toJson(map, typeOfMap);

    assertEquals("{}", json);
  }

  public void testMapDeserializationWithWildcardValues() {
    final Type typeOfMap = new TypeToken<Map<String, ? extends Long>>() {}.getType();
    final Map<String, ? extends Long> map = gson.fromJson("{\"test\":123}", typeOfMap);
    assertEquals(1, map.size());
    assertEquals((Long)123L, map.get("test"));
  }


  private static class MyMap extends LinkedHashMap<String, String> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    int foo = 10;
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=95
   */
  public void testMapOfMapSerialization() {
    final Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
    final Map<String, String> nestedMap = new HashMap<String, String>();
    nestedMap.put("1", "1");
    nestedMap.put("2", "2");
    map.put("nestedMap", nestedMap);
    final String json = gson.toJson(map);
    assertTrue(json.contains("nestedMap"));
    assertTrue(json.contains("\"1\":\"1\""));
    assertTrue(json.contains("\"2\":\"2\""));
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=95
   */
  public void testMapOfMapDeserialization() {
    final String json = "{nestedMap:{'2':'2','1':'1'}}";
    final Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
    final Map<String, Map<String, String>> map = gson.fromJson(json, type);
    final Map<String, String> nested = map.get("nestedMap");
    assertEquals("1", nested.get("1"));
    assertEquals("2", nested.get("2"));
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=178
   */
  public void testMapWithQuotes() {
    final Map<String, String> map = new HashMap<String, String>();
    map.put("a\"b", "c\"d");
    final String json = gson.toJson(map);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

  /**
   * From issue 227.
   */
  public void testWriteMapsWithEmptyStringKey() {
    final Map<String, Boolean> map = new HashMap<String, Boolean>();
    map.put("", true);
    assertEquals("{\"\":true}", gson.toJson(map));

  }

  public void testReadMapsWithEmptyStringKey() {
    final Map<String, Boolean> map = gson.fromJson("{\"\":true}", new TypeToken<Map<String, Boolean>>() {}.getType());
    assertEquals(Boolean.TRUE, map.get(""));
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=204
   */
  public void testSerializeMaps() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", 12);
    map.put("b", null);

    final LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
    innerMap.put("test", 1);
    innerMap.put("TestStringArray", new String[] { "one", "two" });
    map.put("c", innerMap);

    assertEquals("{\"a\":12,\"b\":null,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"]}}",
        new GsonBuilder().serializeNulls().create().toJson(map));
    assertEquals("{\n  \"a\": 12,\n  \"b\": null,\n  \"c\": "
  		+ "{\n    \"test\": 1,\n    \"TestStringArray\": "
  		+ "[\n      \"one\",\n      \"two\"\n    ]\n  }\n}",
        new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(map));
    assertEquals("{\"a\":12,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"]}}",
        new GsonBuilder().create().toJson(map));
    assertEquals("{\n  \"a\": 12,\n  \"c\": "
        + "{\n    \"test\": 1,\n    \"TestStringArray\": "
        + "[\n      \"one\",\n      \"two\"\n    ]\n  }\n}",
        new GsonBuilder().setPrettyPrinting().create().toJson(map));

    innerMap.put("d", "e");
    assertEquals("{\"a\":12,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"],\"d\":\"e\"}}",
        new Gson().toJson(map));
  }

  public final void testInterfaceTypeMap() {
    final MapClass element = new MapClass();
    final TestTypes.Sub subType = new TestTypes.Sub();
    element.addBase("Test", subType);
    element.addSub("Test", subType);

    final String subTypeJson = new Gson().toJson(subType);
    final String expected = "{\"bases\":{\"Test\":" + subTypeJson + "},"
      + "\"subs\":{\"Test\":" + subTypeJson + "}}";

    final Gson gsonWithComplexKeys = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();
    String json = gsonWithComplexKeys.toJson(element);
    assertEquals(expected, json);

    final Gson gson = new Gson();
    json = gson.toJson(element);
    assertEquals(expected, json);
  }

  public final void testInterfaceTypeMapWithSerializer() {
    final MapClass element = new MapClass();
    final TestTypes.Sub subType = new TestTypes.Sub();
    element.addBase("Test", subType);
    element.addSub("Test", subType);

    final Gson tempGson = new Gson();
    final String subTypeJson = tempGson.toJson(subType);
    final JsonElement baseTypeJsonElement = tempGson.toJsonTree(subType, TestTypes.Base.class);
    final String baseTypeJson = tempGson.toJson(baseTypeJsonElement);
    final String expected = "{\"bases\":{\"Test\":" + baseTypeJson + "},"
        + "\"subs\":{\"Test\":" + subTypeJson + "}}";

    final JsonSerializer<TestTypes.Base> baseTypeAdapter = new JsonSerializer<TestTypes.Base>() {
      @Override
      public JsonElement serialize(final TestTypes.Base src, final Type typeOfSrc,
          final JsonSerializationContext context) {
        return baseTypeJsonElement;
      }
    };

    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TestTypes.Base.class, baseTypeAdapter)
        .create();
    String json = gson.toJson(element);
    assertEquals(expected, json);

    gson = new GsonBuilder()
        .registerTypeAdapter(TestTypes.Base.class, baseTypeAdapter)
        .create();
    json = gson.toJson(element);
    assertEquals(expected, json);
  }

  public void testGeneralMapField() throws Exception {
    final MapWithGeneralMapParameters map = new MapWithGeneralMapParameters();
    map.map.put("string", "testString");
    map.map.put("stringArray", new String[]{"one", "two"});
    map.map.put("objectArray", new Object[]{1, 2L, "three"});

    final String expected = "{\"map\":{\"string\":\"testString\",\"stringArray\":"
        + "[\"one\",\"two\"],\"objectArray\":[1,2,\"three\"]}}";
    assertEquals(expected, gson.toJson(map));

    gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();
    assertEquals(expected, gson.toJson(map));
  }

  public void testComplexKeysSerialization() {
    final Map<Point, String> map = new LinkedHashMap<Point, String>();
    map.put(new Point(2, 3), "a");
    map.put(new Point(5, 7), "b");
    final String json = "{\"2,3\":\"a\",\"5,7\":\"b\"}";
    assertEquals(json, gson.toJson(map, new TypeToken<Map<Point, String>>() {}.getType()));
    assertEquals(json, gson.toJson(map, Map.class));
  }

  public void testComplexKeysDeserialization() {
    final String json = "{'2,3':'a','5,7':'b'}";
    try {
      gson.fromJson(json, new TypeToken<Map<Point, String>>() {}.getType());
      fail();
    } catch (final JsonParseException expected) {
    }
  }

  public void testStringKeyDeserialization() {
    final String json = "{'2,3':'a','5,7':'b'}";
    final Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("2,3", "a");
    map.put("5,7", "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
  }

  public void testNumberKeyDeserialization() {
    final String json = "{'2.3':'a','5.7':'b'}";
    final Map<Double, String> map = new LinkedHashMap<Double, String>();
    map.put(2.3, "a");
    map.put(5.7, "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<Double, String>>() {}.getType()));
  }

  public void testBooleanKeyDeserialization() {
    final String json = "{'true':'a','false':'b'}";
    final Map<Boolean, String> map = new LinkedHashMap<Boolean, String>();
    map.put(true, "a");
    map.put(false, "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<Boolean, String>>() {}.getType()));
  }

  public void testMapDeserializationWithDuplicateKeys() {
    try {
      gson.fromJson("{'a':1,'a':2}", new TypeToken<Map<String, Integer>>() {}.getType());
      fail();
    } catch (final JsonSyntaxException expected) {
    }
  }

  public void testSerializeMapOfMaps() {
    final Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
    final Map<String, Map<String, String>> map = newMap(
        "a", newMap("ka1", "va1", "ka2", "va2"),
        "b", newMap("kb1", "vb1", "kb2", "vb2"));
    assertEquals("{'a':{'ka1':'va1','ka2':'va2'},'b':{'kb1':'vb1','kb2':'vb2'}}",
        gson.toJson(map, type).replace('"', '\''));
  }

  public void testDeerializeMapOfMaps() {
    final Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
    final Map<String, Map<String, String>> map = newMap(
        "a", newMap("ka1", "va1", "ka2", "va2"),
        "b", newMap("kb1", "vb1", "kb2", "vb2"));
    final String json = "{'a':{'ka1':'va1','ka2':'va2'},'b':{'kb1':'vb1','kb2':'vb2'}}";
    assertEquals(map, gson.fromJson(json, type));
  }

  private <K, V> Map<K, V> newMap(final K key1, final V value1, final K key2, final V value2) {
    final Map<K, V> result = new LinkedHashMap<K, V>();
    result.put(key1, value1);
    result.put(key2, value2);
    return result;
  }

  public void testMapNamePromotionWithJsonElementReader() {
    final String json = "{'2.3':'a'}";
    final Map<Double, String> map = new LinkedHashMap<Double, String>();
    map.put(2.3, "a");
    final JsonElement tree = new JsonParser().parse(json);
    assertEquals(map, gson.fromJson(tree, new TypeToken<Map<Double, String>>() {}.getType()));
  }

  static class Point {
    private final int x;
    private final int y;

    Point(final int x, final int y) {
      this.x = x;
      this.y = y;
    }

    @Override public boolean equals(final Object o) {
      return o instanceof Point && x == ((Point) o).x && y == ((Point) o).y;
    }

    @Override public int hashCode() {
      return x * 37 + y;
    }

    @Override public String toString() {
      return x + "," + y;
    }
  }

  static final class MapClass {
    private final Map<String, TestTypes.Base> bases = new HashMap<String, TestTypes.Base>();
    private final Map<String, TestTypes.Sub> subs = new HashMap<String, TestTypes.Sub>();

    public final void addBase(final String name, final TestTypes.Base value) {
      bases.put(name, value);
    }

    public final void addSub(final String name, final TestTypes.Sub value) {
      subs.put(name, value);
    }
  }

  static final class MapWithGeneralMapParameters {
    @SuppressWarnings({"rawtypes", "unchecked"})
    final Map<String, Object> map = new LinkedHashMap();
  }
}
