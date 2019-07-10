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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.GsonBuilder;
import com.github.gv2011.gson.InstanceCreator;
import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonObject;
import com.github.gv2011.gson.JsonParseException;
import com.github.gv2011.gson.JsonSerializationContext;
import com.github.gv2011.gson.JsonSerializer;
import com.github.gv2011.gson.common.TestTypes.ArrayOfObjects;
import com.github.gv2011.gson.common.TestTypes.BagOfPrimitiveWrappers;
import com.github.gv2011.gson.common.TestTypes.BagOfPrimitives;
import com.github.gv2011.gson.common.TestTypes.ClassWithArray;
import com.github.gv2011.gson.common.TestTypes.ClassWithNoFields;
import com.github.gv2011.gson.common.TestTypes.ClassWithObjects;
import com.github.gv2011.gson.common.TestTypes.ClassWithTransientFields;
import com.github.gv2011.gson.common.TestTypes.Nested;
import com.github.gv2011.gson.common.TestTypes.PrimitiveArray;
import com.github.gv2011.gson.reflect.TypeToken;

import junit.framework.TestCase;

/**
 * Functional tests for Json serialization and deserialization of regular classes.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ObjectTest extends TestCase {
  private Gson gson;
  private final TimeZone oldTimeZone = TimeZone.getDefault();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();

    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
  }

  @Override
  protected void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    super.tearDown();
  }
  public void testJsonInSingleQuotesDeserialization() {
    final String json = "{'stringValue':'no message','intValue':10,'longValue':20}";
    final BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testJsonInMixedQuotesDeserialization() {
    final String json = "{\"stringValue\":'no message','intValue':10,'longValue':20}";
    final BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testBagOfPrimitivesSerialization() throws Exception {
    final BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitivesDeserialization() throws Exception {
    final BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    final String json = src.getExpectedJson();
    final BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testBagOfPrimitiveWrappersSerialization() throws Exception {
    final BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitiveWrappersDeserialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    final String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

  public void testClassWithTransientFieldsSerialization() throws Exception {
    final ClassWithTransientFields<Long> target = new ClassWithTransientFields<Long>(1L);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserialization() throws Exception {
    final String json = "{\"longValue\":[1]}";
    final ClassWithTransientFields target = gson.fromJson(json, ClassWithTransientFields.class);
    assertEquals(json, target.getExpectedJson());
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored()
      throws Exception {
    final String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    final ClassWithTransientFields target = gson.fromJson(json, ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

  public void testClassWithNoFieldsSerialization() throws Exception {
    assertEquals("{}", gson.toJson(new ClassWithNoFields()));
  }

  public void testClassWithNoFieldsDeserialization() throws Exception {
    final String json = "{}";
    final ClassWithNoFields target = gson.fromJson(json, ClassWithNoFields.class);
    final ClassWithNoFields expected = new ClassWithNoFields();
    assertEquals(expected, target);
  }

  public void testNestedSerialization() throws Exception {
    final Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"),
       new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testNestedDeserialization() throws Exception {
    final String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    final Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }
  public void testNullSerialization() throws Exception {
    assertEquals("null", gson.toJson(null));
  }

  public void testEmptyStringDeserialization() throws Exception {
    final Object object = gson.fromJson("", Object.class);
    assertNull(object);
  }

  public void testTruncatedDeserialization() {
    try {
      gson.fromJson("[\"a\", \"b\",", new TypeToken<List<String>>() {}.getType());
      fail();
    } catch (final JsonParseException expected) {
    }
  }

  public void testNullDeserialization() throws Exception {
    final String myNullObject = null;
    final Object object = gson.fromJson(myNullObject, Object.class);
    assertNull(object);
  }

  public void testNullFieldsSerialization() throws Exception {
    final Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testNullFieldsDeserialization() throws Exception {
    final String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    final Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsSerialization() throws Exception {
    final ArrayOfObjects target = new ArrayOfObjects();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfObjectsDeserialization() throws Exception {
    final String json = new ArrayOfObjects().getExpectedJson();
    final ArrayOfObjects target = gson.fromJson(json, ArrayOfObjects.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfArraysSerialization() throws Exception {
    final ArrayOfArrays target = new ArrayOfArrays();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfArraysDeserialization() throws Exception {
    final String json = new ArrayOfArrays().getExpectedJson();
    final ArrayOfArrays target = gson.fromJson(json, ArrayOfArrays.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsAsFields() throws Exception {
    final ClassWithObjects classWithObjects = new ClassWithObjects();
    final BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    final String stringValue = "someStringValueInArray";
    final String classWithObjectsJson = gson.toJson(classWithObjects);
    final String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    final ClassWithArray classWithArray = new ClassWithArray(
        new Object[] { stringValue, classWithObjects, bagOfPrimitives });
    final String json = gson.toJson(classWithArray);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
    assertTrue(json.contains("\"" + stringValue + "\""));
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullArraysDeserialization() throws Exception {
    final String json = "{\"array\": null}";
    final ClassWithArray target = gson.fromJson(json, ClassWithArray.class);
    assertNull(target.array);
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullObjectFieldsDeserialization() throws Exception {
    final String json = "{\"bag\": null}";
    final ClassWithObjects target = gson.fromJson(json, ClassWithObjects.class);
    assertNull(target.bag);
  }

  public void testEmptyCollectionInAnObjectDeserialization() throws Exception {
    final String json = "{\"children\":[]}";
    final ClassWithCollectionField target = gson.fromJson(json, ClassWithCollectionField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  private static class ClassWithCollectionField {
    Collection<String> children = new ArrayList<String>();
  }

  public void testPrimitiveArrayInAnObjectDeserialization() throws Exception {
    final String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    final PrimitiveArray target = gson.fromJson(json, PrimitiveArray.class);
    assertEquals(json, target.getExpectedJson());
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullPrimitiveFieldsDeserialization() throws Exception {
    final String json = "{\"longValue\":null}";
    final BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

  public void testEmptyCollectionInAnObjectSerialization() throws Exception {
    final ClassWithCollectionField target = new ClassWithCollectionField();
    assertEquals("{\"children\":[]}", gson.toJson(target));
  }

  public void testPrivateNoArgConstructorDeserialization() throws Exception {
    final ClassWithPrivateNoArgsConstructor target =
      gson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

  public void testAnonymousLocalClassesSerialization() throws Exception {
    assertEquals("null", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testAnonymousLocalClassesCustomSerialization() throws Exception {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(ClassWithNoFields.class,
            new JsonSerializer<ClassWithNoFields>() {
              @Override
              public JsonElement serialize(
                  final ClassWithNoFields src, final Type typeOfSrc, final JsonSerializationContext context) {
                return new JsonObject();
              }
            }).create();

    assertEquals("null", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testPrimitiveArrayFieldSerialization() {
    final PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  /**
   * Tests that a class field with type Object can be serialized properly.
   * See issue 54
   */
  public void testClassWithObjectFieldSerialization() {
    final ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    final String json = gson.toJson(obj);
    assertTrue(json.contains("abc"));
  }

  private static class ClassWithObjectField {
    @SuppressWarnings("unused")
    Object member;
  }

  public void testInnerClassSerialization() {
    final Parent p = new Parent();
    final Parent.Child c = p.new Child();
    final String json = gson.toJson(c);
    assertTrue(json.contains("value2"));
    assertFalse(json.contains("value1"));
  }

  public void testInnerClassDeserialization() {
    final Parent p = new Parent();
    final Gson gson = new GsonBuilder().registerTypeAdapter(
        Parent.Child.class, new InstanceCreator<Parent.Child>() {
      @Override
      public Parent.Child createInstance(final Type type) {
        return p.new Child();
      }
    }).create();
    final String json = "{'value2':3}";
    final Parent.Child c = gson.fromJson(json, Parent.Child.class);
    assertEquals(3, c.value2);
  }

  private static class Parent {
    @SuppressWarnings("unused")
    int value1 = 1;
    private class Child {
      int value2 = 2;
    }
  }

  private static class ArrayOfArrays {
    private final BagOfPrimitives[][] elements;
    public ArrayOfArrays() {
      elements = new BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        final BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new BagOfPrimitives(i+j, i*j, false, i+"_"+j);
        }
      }
    }
    public String getExpectedJson() {
      final StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (final BagOfPrimitives[] row : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        boolean firstOfRow = true;
        sb.append("[");
        for (final BagOfPrimitives element : row) {
          if (firstOfRow) {
            firstOfRow = false;
          } else {
            sb.append(",");
          }
          sb.append(element.getExpectedJson());
        }
        sb.append("]");
      }
      sb.append("]}");
      return sb.toString();
    }
  }

  private static class ClassWithPrivateNoArgsConstructor {
    public int a;
    private ClassWithPrivateNoArgsConstructor() {
      a = 10;
    }
  }

  /**
   * In response to Issue 41 http://code.google.com/p/google-gson/issues/detail?id=41
   */
  public void testObjectFieldNamesWithoutQuotesDeserialization() {
    final String json = "{longValue:1,'booleanValue':true,\"stringValue\":'bar'}";
    final BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(1, bag.longValue);
    assertTrue(bag.booleanValue);
    assertEquals("bar", bag.stringValue);
  }

  public void testStringFieldWithNumberValueDeserialization() {
    String json = "{\"stringValue\":1}";
    BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("1", bag.stringValue);

    json = "{\"stringValue\":1.5E+6}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("1.5E+6", bag.stringValue);

    json = "{\"stringValue\":true}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("true", bag.stringValue);
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueSerialization() {
    final ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    final String json = gson.toJson(target);
    assertTrue(json.contains("\"a\":\"5794749\""));
    assertTrue(json.contains("\"b\":\"\""));
    assertTrue(json.contains("\"c\":\"\""));
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueDeserialization() {
    final String json = "{a:\"5794749\",b:\"\",c:\"\"}";
    final ClassWithEmptyStringFields target = gson.fromJson(json, ClassWithEmptyStringFields.class);
    assertEquals("5794749", target.a);
    assertEquals("", target.b);
    assertEquals("", target.c);
  }

  private static class ClassWithEmptyStringFields {
    String a = "";
    String b = "";
    String c = "";
  }

  public void testJsonObjectSerialization() {
    final Gson gson = new GsonBuilder().serializeNulls().create();
    final JsonObject obj = new JsonObject();
    final String json = gson.toJson(obj);
    assertEquals("{}", json);
  }

  /**
   * Test for issue 215.
   */
  public void testSingletonLists() {
    final Gson gson = new Gson();
    final Product product = new Product();
    assertEquals("{\"attributes\":[],\"departments\":[]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.departments.add(new Department());
    assertEquals("{\"attributes\":[],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.attributes.add("456");
    assertEquals("{\"attributes\":[\"456\"],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=270
//  public void testDateAsMapObjectField() {
//    final HasObjectMap a = new HasObjectMap();
//    a.map.put("date", new Date(0));
//    assertEquals("{\"map\":{\"date\":\"Dec 31, 1969 4:00:00 PM\"}}", gson.toJson(a));
//  }

  public class HasObjectMap {
    Map<String, Object> map = new HashMap<String, Object>();
  }

  static final class Department {
    public String name = "abc";
    public String code = "123";
  }

  static final class Product {
    private final List<String> attributes = new ArrayList<String>();
    private final List<Department> departments = new ArrayList<Department>();
  }
}
