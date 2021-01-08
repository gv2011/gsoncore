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

package com.github.gv2011.gson.functional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.GsonBuilder;
import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonPrimitive;
import com.github.gv2011.gson.JsonSerializationContext;
import com.github.gv2011.gson.JsonSerializer;
import com.github.gv2011.gson.common.MoreAsserts;
import com.github.gv2011.gson.common.TestTypes.BagOfPrimitives;
import com.github.gv2011.gson.reflect.TypeToken;

import junit.framework.TestCase;

/**
 * Functional tests for Json serialization and deserialization of collections.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class CollectionTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testTopLevelCollectionOfIntegersSerialization() {
    final Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    final Type targetType = new TypeToken<Collection<Integer>>() {}.getType();
    final String json = gson.toJson(target, targetType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

  public void testTopLevelCollectionOfIntegersDeserialization() {
    final String json = "[0,1,2,3,4,5,6,7,8,9]";
    final Type collectionType = new TypeToken<Collection<Integer>>() { }.getType();
    final Collection<Integer> target = gson.fromJson(json, collectionType);
    final int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    MoreAsserts.assertEquals(expected, toIntArray(target));
  }

  public void testTopLevelListOfIntegerCollectionsDeserialization() throws Exception {
    final String json = "[[1,2,3],[4,5,6],[7,8,9]]";
    final Type collectionType = new TypeToken<Collection<Collection<Integer>>>() {}.getType();
    final List<Collection<Integer>> target = gson.fromJson(json, collectionType);
    final int[][] expected = new int[3][3];
    for (int i = 0; i < 3; ++i) {
      final int start = (3 * i) + 1;
      for (int j = 0; j < 3; ++j) {
        expected[i][j] = start + j;
      }
    }

    for (int i = 0; i < 3; i++) {
      MoreAsserts.assertEquals(expected[i], toIntArray(target.get(i)));
    }
  }

  public void testLinkedListSerialization() {
    final List<String> list = new LinkedList<String>();
    list.add("a1");
    list.add("a2");
    final Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    final String json = gson.toJson(list, linkedListType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

  public void testLinkedListDeserialization() {
    final String json = "['a1','a2']";
    final Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    final List<String> list = gson.fromJson(json, linkedListType);
    assertEquals("a1", list.get(0));
    assertEquals("a2", list.get(1));
  }

  public void testQueueSerialization() {
    final Queue<String> queue = new LinkedList<String>();
    queue.add("a1");
    queue.add("a2");
    final Type queueType = new TypeToken<Queue<String>>() {}.getType();
    final String json = gson.toJson(queue, queueType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

  public void testQueueDeserialization() {
    final String json = "['a1','a2']";
    final Type queueType = new TypeToken<Queue<String>>() {}.getType();
    final Queue<String> queue = gson.fromJson(json, queueType);
    assertEquals("a1", queue.element());
    queue.remove();
    assertEquals("a2", queue.element());
  }

  public void testPriorityQueue() throws Exception {
    final Type type = new TypeToken<PriorityQueue<Integer>>(){}.getType();
    final PriorityQueue<Integer> queue = gson.fromJson("[10, 20, 22]", type);
    assertEquals(3, queue.size());
    final String json = gson.toJson(queue);
    assertEquals(10, queue.remove().intValue());
    assertEquals(20, queue.remove().intValue());
    assertEquals(22, queue.remove().intValue());
    assertEquals("[10,20,22]", json);
  }

  public void testVector() {
    final Type type = new TypeToken<Vector<Integer>>(){}.getType();
    final Vector<Integer> target = gson.fromJson("[10, 20, 31]", type);
    assertEquals(3, target.size());
    assertEquals(10, target.get(0).intValue());
    assertEquals(20, target.get(1).intValue());
    assertEquals(31, target.get(2).intValue());
    final String json = gson.toJson(target);
    assertEquals("[10,20,31]", json);
  }

  public void testStack() {
    final Type type = new TypeToken<Stack<Integer>>(){}.getType();
    final Stack<Integer> target = gson.fromJson("[11, 13, 17]", type);
    assertEquals(3, target.size());
    final String json = gson.toJson(target);
    assertEquals(17, target.pop().intValue());
    assertEquals(13, target.pop().intValue());
    assertEquals(11, target.pop().intValue());
    assertEquals("[11,13,17]", json);
  }

  public void testNullsInListSerialization() {
    final List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    final String expected = "[\"foo\",null,\"bar\"]";
    final Type typeOfList = new TypeToken<List<String>>() {}.getType();
    final String json = gson.toJson(list, typeOfList);
    assertEquals(expected, json);
  }

  public void testNullsInListDeserialization() {
    final List<String> expected = new ArrayList<String>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    final String json = "[\"foo\",null,\"bar\"]";
    final Type expectedType = new TypeToken<List<String>>() {}.getType();
    final List<String> target = gson.fromJson(json, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertEquals(expected.get(i), target.get(i));
    }
  }

  public void testCollectionOfObjectSerialization() {
    final List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));

    final Type type = new TypeToken<List<Object>>() {}.getType();
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target, type));
  }

  public void testCollectionOfObjectWithNullSerialization() {
    final List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add(null);
    target.add("World");
    assertEquals("[\"Hello\",null,\"World\"]", gson.toJson(target));

    final Type type = new TypeToken<List<Object>>() {}.getType();
    assertEquals("[\"Hello\",null,\"World\"]", gson.toJson(target, type));
  }

  public void testCollectionOfStringsSerialization() {
    final List<String> target = new ArrayList<String>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  public void testCollectionOfBagOfPrimitivesSerialization() {
    final List<BagOfPrimitives> target = new ArrayList<BagOfPrimitives>();
    final BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    final BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    final String result = gson.toJson(target);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    for (final BagOfPrimitives obj : target) {
      assertTrue(result.contains(obj.getExpectedJson()));
    }
  }

  public void testCollectionOfStringsDeserialization() {
    final String json = "[\"Hello\",\"World\"]";
    final Type collectionType = new TypeToken<Collection<String>>() { }.getType();
    final Collection<String> target = gson.fromJson(json, collectionType);

    assertTrue(target.contains("Hello"));
    assertTrue(target.contains("World"));
  }

  public void testRawCollectionOfIntegersSerialization() {
    final Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  @SuppressWarnings("rawtypes")
  public void testRawCollectionSerialization() {
    final BagOfPrimitives bag1 = new BagOfPrimitives();
    final Collection target = Arrays.asList(bag1, bag1);
    final String json = gson.toJson(target);
    assertTrue(json.contains(bag1.getExpectedJson()));
  }

  @SuppressWarnings("rawtypes")
  public void testRawCollectionDeserializationNotAlllowed() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    final Collection integers = gson.fromJson(json, Collection.class);
    // JsonReader converts numbers to double by default so we need a floating point comparison
    assertEquals(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0), integers);

    json = "[\"Hello\", \"World\"]";
    final Collection strings = gson.fromJson(json, Collection.class);
    assertTrue(strings.contains("Hello"));
    assertTrue(strings.contains("World"));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    final BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
    final String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
    final Collection target = gson.fromJson(json, Collection.class);
    assertEquals(2, target.size());
    for (final Object bag1 : target) {
      // Gson 2.0 converts raw objects into maps
      final Map<String, Object> values = (Map<String, Object>) bag1;
      assertTrue(values.containsValue(10.0));
      assertTrue(values.containsValue(20.0));
      assertTrue(values.containsValue("stringValue"));
    }
  }

  public void testWildcardPrimitiveCollectionSerilaization() throws Exception {
    final Collection<? extends Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    final Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    String json = gson.toJson(target, collectionType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);

    json = gson.toJson(target);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

  public void testWildcardPrimitiveCollectionDeserilaization() throws Exception {
    final String json = "[1,2,3,4,5,6,7,8,9]";
    final Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    final Collection<? extends Integer> target = gson.fromJson(json, collectionType);
    assertEquals(9, target.size());
    assertTrue(target.contains(1));
    assertTrue(target.contains(9));
  }

  public void testWildcardCollectionField() throws Exception {
    final Collection<BagOfPrimitives> collection = new ArrayList<BagOfPrimitives>();
    final BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    final BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    collection.add(objA);
    collection.add(objB);

    ObjectWithWildcardCollection target = new ObjectWithWildcardCollection(collection);
    final String json = gson.toJson(target);
    assertTrue(json.contains(objA.getExpectedJson()));
    assertTrue(json.contains(objB.getExpectedJson()));

    target = gson.fromJson(json, ObjectWithWildcardCollection.class);
    final Collection<? extends BagOfPrimitives> deserializedCollection = target.getCollection();
    assertEquals(2, deserializedCollection.size());
    assertTrue(deserializedCollection.contains(objA));
    assertTrue(deserializedCollection.contains(objB));
  }

  public void testFieldIsArrayList() {
    final HasArrayListField object = new HasArrayListField();
    object.longs.add(1L);
    object.longs.add(3L);
    final String json = gson.toJson(object, HasArrayListField.class);
    assertEquals("{\"longs\":[1,3]}", json);
    final HasArrayListField copy = gson.fromJson("{\"longs\":[1,3]}", HasArrayListField.class);
    assertEquals(Arrays.asList(1L, 3L), copy.longs);
  }

  public void testUserCollectionTypeAdapter() {
    final Type listOfString = new TypeToken<List<String>>() {}.getType();
    final Object stringListSerializer = new JsonSerializer<List<String>>() {
      @Override
      public JsonElement serialize(final List<String> src, final Type typeOfSrc,
          final JsonSerializationContext context) {
        return new JsonPrimitive(src.get(0) + ";" + src.get(1));
      }
    };
    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfString, stringListSerializer)
        .create();
    assertEquals("\"ab;cd\"", gson.toJson(Arrays.asList("ab", "cd"), listOfString));
  }

  public static class HasArrayListField {
    ArrayList<Long> longs = new ArrayList<Long>();
  }

  @SuppressWarnings("rawtypes")
  private static int[] toIntArray(final Collection collection) {
    final int[] ints = new int[collection.size()];
    int i = 0;
    for (final Iterator iterator = collection.iterator(); iterator.hasNext(); ++i) {
      final Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = ((Integer)obj).intValue();
      } else if (obj instanceof Long) {
        ints[i] = ((Long)obj).intValue();
      }
    }
    return ints;
  }

  private static class ObjectWithWildcardCollection {
    private final Collection<? extends BagOfPrimitives> collection;

    public ObjectWithWildcardCollection(final Collection<? extends BagOfPrimitives> collection) {
      this.collection = collection;
    }

    public Collection<? extends BagOfPrimitives> getCollection() {
      return collection;
    }
  }

  private static class Entry {
    int value;
    Entry(final int value) {
      this.value = value;
    }
  }
  public void testSetSerialization() {
    final Set<Entry> set = new HashSet<Entry>();
    set.add(new Entry(1));
    set.add(new Entry(2));
    final String json = gson.toJson(set);
    assertTrue(json.contains("1"));
    assertTrue(json.contains("2"));
  }
  public void testSetDeserialization() {
    final String json = "[{value:1},{value:2}]";
    final Type type = new TypeToken<Set<Entry>>() {}.getType();
    final Set<Entry> set = gson.fromJson(json, type);
    assertEquals(2, set.size());
    for (final Entry entry : set) {
      assertTrue(entry.value == 1 || entry.value == 2);
    }
  }
}
