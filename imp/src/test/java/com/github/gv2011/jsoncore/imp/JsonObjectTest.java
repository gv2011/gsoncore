/*
 * Copyright (C) 2016 Vinz (https://github.com/gv2011)
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

package com.github.gv2011.jsoncore.imp;

import com.github.gv2011.jsoncore.imp.JsonArray;
import com.github.gv2011.jsoncore.imp.JsonElement;
import com.github.gv2011.jsoncore.imp.JsonNull;
import com.github.gv2011.jsoncore.imp.JsonObject;
import com.github.gv2011.jsoncore.imp.JsonPrimitive;

import junit.framework.TestCase;

/**
 * Unit test for the {@link JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest extends TestCase {

  public void testAddingAndRemovingObjectProperties() throws Exception {
    final JsonObject jsonObj = new JsonObject();
    final String propertyName = "property";
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    final JsonPrimitive value = new JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertEquals(value, jsonObj.get(propertyName));

    final JsonElement removedElement = jsonObj.remove(propertyName);
    assertEquals(value, removedElement);
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));
  }

  public void testAddingNullPropertyValue() throws Exception {
    final String propertyName = "property";
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add(propertyName, null);

    assertTrue(jsonObj.has(propertyName));

    final JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.isJsonNull());
  }

  public void testAddingNullOrEmptyPropertyName() throws Exception {
    final JsonObject jsonObj = new JsonObject();
    try {
      jsonObj.add(null, JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (final NullPointerException expected) { }

    jsonObj.add("", JsonNull.INSTANCE);
    jsonObj.add("   \t", JsonNull.INSTANCE);
  }

  public void testAddingBooleanProperties() throws Exception {
    final String propertyName = "property";
    final JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertTrue(jsonObj.has(propertyName));

    final JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.getAsBoolean());
  }

  public void testAddingStringProperties() throws Exception {
    final String propertyName = "property";
    final String value = "blah";

    final JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    final JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(value, jsonElement.getAsString());
  }

  public void testAddingCharacterProperties() throws Exception {
    final String propertyName = "property";
    final char value = 'a';

    final JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    final JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(String.valueOf(value), jsonElement.getAsString());
    assertEquals(value, jsonElement.getAsCharacter());
  }


  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
  }

  public void testEqualsNonEmptyObject() {
    final JsonObject a = new JsonObject();
    final JsonObject b = new JsonObject();

    assertEquals(a, a);

    a.add("foo", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("foo", new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("bar", JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testDeepCopy() {
    final JsonObject original = new JsonObject();
    final JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    final JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get("key").getAsJsonArray().size());
    assertEquals(0, copy.get("key").getAsJsonArray().size());
  }
}
