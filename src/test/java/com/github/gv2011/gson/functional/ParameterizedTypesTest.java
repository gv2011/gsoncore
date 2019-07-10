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

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.GsonBuilder;
import com.github.gv2011.gson.ParameterizedTypeFixtures.MyParameterizedType;
import com.github.gv2011.gson.ParameterizedTypeFixtures.MyParameterizedTypeAdapter;
import com.github.gv2011.gson.ParameterizedTypeFixtures.MyParameterizedTypeInstanceCreator;
import com.github.gv2011.gson.common.TestTypes.BagOfPrimitives;
import com.github.gv2011.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Functional tests for the serialization and deserialization of parameterized types in Gson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypesTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testParameterizedTypesSerialization() throws Exception {
    final MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    final Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    final String json = gson.toJson(src, typeOfSrc);
    assertEquals(src.getExpectedJson(), json);
  }

  public void testParameterizedTypeDeserialization() throws Exception {
    final BagOfPrimitives bag = new BagOfPrimitives();
    final MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    final Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    final BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    final Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    final String json = expected.getExpectedJson();
    final MyParameterizedType<BagOfPrimitives> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

  public void testTypesWithMultipleParametersSerialization() throws Exception {
    final MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src =
        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
            "abc", new BagOfPrimitives());
    final Type typeOfSrc = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    final String json = gson.toJson(src, typeOfSrc);
    final String expected = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    assertEquals(expected, json);
  }

  public void testTypesWithMultipleParametersDeserialization() throws Exception {
    final Type typeOfTarget = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    final String json = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    final MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target =
        gson.fromJson(json, typeOfTarget);
    final MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected =
        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
            "abc", new BagOfPrimitives());
    assertEquals(expected, target);
  }

  public void testParameterizedTypeWithCustomSerializer() {
    final Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    final Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .create();
    final MyParameterizedType<Integer> intTarget = new MyParameterizedType<Integer>(10);
    String json = gson.toJson(intTarget, ptIntegerType);
    assertEquals(MyParameterizedTypeAdapter.<Integer>getExpectedJson(intTarget), json);

    final MyParameterizedType<String> stringTarget = new MyParameterizedType<String>("abc");
    json = gson.toJson(stringTarget, ptStringType);
    assertEquals(MyParameterizedTypeAdapter.<String>getExpectedJson(stringTarget), json);
  }

  public void testParameterizedTypesWithCustomDeserializer() {
    final Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    final Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    final Gson gson = new GsonBuilder().registerTypeAdapter(
        ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeInstanceCreator<String>(""))
        .registerTypeAdapter(ptIntegerType,
            new MyParameterizedTypeInstanceCreator<Integer>(0))
        .create();

    final MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    String json = MyParameterizedTypeAdapter.<Integer>getExpectedJson(src);
    final MyParameterizedType<Integer> intTarget = gson.fromJson(json, ptIntegerType);
    assertEquals(10, intTarget.value.intValue());

    final MyParameterizedType<String> srcStr = new MyParameterizedType<String>("abc");
    json = MyParameterizedTypeAdapter.<String>getExpectedJson(srcStr);
    final MyParameterizedType<String> stringTarget = gson.fromJson(json, ptStringType);
    assertEquals("abc", stringTarget.value);
  }

  public void testParameterizedTypesWithWriterSerialization() throws Exception {
    final Writer writer = new StringWriter();
    final MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    final Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    gson.toJson(src, typeOfSrc, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }

  public void testParameterizedTypeWithReaderDeserialization() throws Exception {
    final BagOfPrimitives bag = new BagOfPrimitives();
    final MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    final Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    final BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    final Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    final Reader json = new StringReader(expected.getExpectedJson());
    final MyParameterizedType<Integer> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

  @SuppressWarnings("unchecked")
  public void testVariableTypeFieldsAndGenericArraysSerialization() throws Exception {
    final Integer obj = 0;
    final Integer[] array = { 1, 2, 3 };
    final List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    final List<Integer>[] arrayOfLists = new List[] { list, list };

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    final String json = gson.toJson(objToSerialize, typeOfSrc);

    assertEquals(objToSerialize.getExpectedJson(), json);
  }

  @SuppressWarnings("unchecked")
  public void testVariableTypeFieldsAndGenericArraysDeserialization() throws Exception {
    final Integer obj = 0;
    final Integer[] array = { 1, 2, 3 };
    final List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    final List<Integer>[] arrayOfLists = new List[] { list, list };

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    final ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  public void testVariableTypeDeserialization() throws Exception {
    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(0, null, null, null, null, null);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    final ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  public void testVariableTypeArrayDeserialization() throws Exception {
    final Integer[] array = { 1, 2, 3 };

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, array, null, null, null, null);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    final ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  public void testParameterizedTypeWithVariableTypeDeserialization() throws Exception {
    final List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, list, null, null, null);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    final ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  @SuppressWarnings("unchecked")
  public void testParameterizedTypeGenericArraysSerialization() throws Exception {
    final List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    final List<Integer>[] arrayOfLists = new List[] { list, list };

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    assertEquals("{\"arrayOfListOfTypeParameters\":[[1,2],[1,2]]}", json);
  }

  @SuppressWarnings("unchecked")
  public void testParameterizedTypeGenericArraysDeserialization() throws Exception {
    final List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    final List<Integer>[] arrayOfLists = new List[] { list, list };

    final Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    final ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    final String json = gson.toJson(objToSerialize, typeOfSrc);
    final ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  /**
   * An test object that has fields that are type variables.
   *
   * @param <T> Enforce T to be a string to make writing the "toExpectedJson" method easier.
   */
  public static class ObjectWithTypeVariables<T extends Number> {
    private final T typeParameterObj;
    private final T[] typeParameterArray;
    private final List<T> listOfTypeParameters;
    private final List<T>[] arrayOfListOfTypeParameters;
    private final List<? extends T> listOfWildcardTypeParameters;
    private final List<? extends T>[] arrayOfListOfWildcardTypeParameters;

    // For use by Gson
    public ObjectWithTypeVariables() {
      this(null, null, null, null, null, null);
    }

    public ObjectWithTypeVariables(final T obj, final T[] array, final List<T> list, final List<T>[] arrayOfList,
        final List<? extends T> wildcardList, final List<? extends T>[] arrayOfWildcardList) {
      this.typeParameterObj = obj;
      this.typeParameterArray = array;
      this.listOfTypeParameters = list;
      this.arrayOfListOfTypeParameters = arrayOfList;
      this.listOfWildcardTypeParameters = wildcardList;
      this.arrayOfListOfWildcardTypeParameters = arrayOfWildcardList;
    }

    public String getExpectedJson() {
      final StringBuilder sb = new StringBuilder().append("{");

      boolean needsComma = false;
      if (typeParameterObj != null) {
        sb.append("\"typeParameterObj\":").append(toString(typeParameterObj));
        needsComma = true;
      }

      if (typeParameterArray != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"typeParameterArray\":[");
        appendObjectsToBuilder(sb, Arrays.asList(typeParameterArray));
        sb.append(']');
        needsComma = true;
      }

      if (listOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (listOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }
      sb.append('}');
      return sb.toString();
    }

    private void appendObjectsToBuilder(final StringBuilder sb, final Iterable<? extends T> iterable) {
      boolean isFirst = true;
      for (final T obj : iterable) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        sb.append(toString(obj));
      }
    }

    private void appendObjectsToBuilder(final StringBuilder sb, final List<? extends T>[] arrayOfList) {
      boolean isFirst = true;
      for (final List<? extends T> list : arrayOfList) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        if (list != null) {
          sb.append('[');
          appendObjectsToBuilder(sb, list);
          sb.append(']');
        } else {
          sb.append("null");
        }
      }
    }

    public String toString(final T obj) {
      return obj.toString();
    }
  }

  public static class MultiParameters<A, B, C, D, E> {
    A a;
    B b;
    C c;
    D d;
    E e;
    // For use by Gson
    public MultiParameters() {
    }
    MultiParameters(final A a, final B b, final C c, final D d, final E e) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.e = e;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((a == null) ? 0 : a.hashCode());
      result = prime * result + ((b == null) ? 0 : b.hashCode());
      result = prime * result + ((c == null) ? 0 : c.hashCode());
      result = prime * result + ((d == null) ? 0 : d.hashCode());
      result = prime * result + ((e == null) ? 0 : e.hashCode());
      return result;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MultiParameters<A, B, C, D, E> other = (MultiParameters<A, B, C, D, E>) obj;
      if (a == null) {
        if (other.a != null) {
          return false;
        }
      } else if (!a.equals(other.a)) {
        return false;
      }
      if (b == null) {
        if (other.b != null) {
          return false;
        }
      } else if (!b.equals(other.b)) {
        return false;
      }
      if (c == null) {
        if (other.c != null) {
          return false;
        }
      } else if (!c.equals(other.c)) {
        return false;
      }
      if (d == null) {
        if (other.d != null) {
          return false;
        }
      } else if (!d.equals(other.d)) {
        return false;
      }
      if (e == null) {
        if (other.e != null) {
          return false;
        }
      } else if (!e.equals(other.e)) {
        return false;
      }
      return true;
    }
  }

  // Begin: tests to reproduce issue 103
  private static class Quantity {
    @SuppressWarnings("unused")
    int q = 10;
  }
  private static class MyQuantity extends Quantity {
    @SuppressWarnings("unused")
    int q2 = 20;
  }
  private interface Measurable<T> {
  }
  private interface Field<T> {
  }
  private interface Immutable {
  }

  public static final class Amount<Q extends Quantity>
      implements Measurable<Q>, Field<Amount<?>>, Serializable, Immutable {
    private static final long serialVersionUID = -7560491093120970437L;

    int value = 30;
  }

  public void testDeepParameterizedTypeSerialization() {
    final Amount<MyQuantity> amount = new Amount<MyQuantity>();
    final String json = gson.toJson(amount);
    assertTrue(json.contains("value"));
    assertTrue(json.contains("30"));
  }

  public void testDeepParameterizedTypeDeserialization() {
    final String json = "{value:30}";
    final Type type = new TypeToken<Amount<MyQuantity>>() {}.getType();
    final Amount<MyQuantity> amount = gson.fromJson(json, type);
    assertEquals(30, amount.value);
  }
  // End: tests to reproduce issue 103
}