/*
 * Copyright (C) 2011 Google Inc.
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

package com.github.gv2011.gson.functional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.GsonBuilder;
import com.github.gv2011.gson.JsonDeserializationContext;
import com.github.gv2011.gson.JsonDeserializer;
import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonParseException;
import com.github.gv2011.gson.JsonPrimitive;
import com.github.gv2011.gson.JsonSerializationContext;
import com.github.gv2011.gson.JsonSerializer;
import com.github.gv2011.gson.reflect.TypeToken;

/**
 * Collection of functional tests for DOM tree based type adapters.
 */
public class TreeTypeAdaptersTest extends TestCase {
  private static final Id<Student> STUDENT1_ID = new Id<Student>("5", Student.class);
  private static final Id<Student> STUDENT2_ID = new Id<Student>("6", Student.class);
  private static final Student STUDENT1 = new Student(STUDENT1_ID, "first");
  private static final Student STUDENT2 = new Student(STUDENT2_ID, "second");
  private static final Type TYPE_COURSE_HISTORY =
    new TypeToken<Course<HistoryCourse>>(){}.getType();
  private static final Id<Course<HistoryCourse>> COURSE_ID =
      new Id<Course<HistoryCourse>>("10", TYPE_COURSE_HISTORY);

  private Gson gson;
  private Course<HistoryCourse> course;

  @Override
  protected void setUp() {
    gson = new GsonBuilder()
        .registerTypeAdapter(Id.class, new IdTreeTypeAdapter())
        .create();
    course = new Course<HistoryCourse>(COURSE_ID, 4,
        new Assignment<HistoryCourse>(null, null), createList(STUDENT1, STUDENT2));
  }

  public void testSerializeId() {
    final String json = gson.toJson(course, TYPE_COURSE_HISTORY);
    assertTrue(json.contains(String.valueOf(COURSE_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT1_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT2_ID.getValue())));
  }

  public void testDeserializeId() {
    final String json = "{courseId:1,students:[{id:1,name:'first'},{id:6,name:'second'}],"
      + "numAssignments:4,assignment:{}}";
    final Course<HistoryCourse> target = gson.fromJson(json, TYPE_COURSE_HISTORY);
    assertEquals("1", target.getStudents().get(0).id.getValue());
    assertEquals("6", target.getStudents().get(1).id.getValue());
    assertEquals("1", target.getId().getValue());
  }

  private static final class Id<R> {
    final String value;
    @SuppressWarnings("unused")
    final Type typeOfId;

    private Id(final String value, final Type typeOfId) {
      this.value = value;
      this.typeOfId = typeOfId;
    }
    public String getValue() {
      return value;
    }
  }

  private static final class IdTreeTypeAdapter implements JsonSerializer<Id<?>>,
      JsonDeserializer<Id<?>> {

    @SuppressWarnings("rawtypes")
    @Override
    public Id<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
      if (!(typeOfT instanceof ParameterizedType)) {
        throw new JsonParseException("Id of unknown type: " + typeOfT);
      }
      final ParameterizedType parameterizedType = (ParameterizedType) typeOfT;
      // Since Id takes only one TypeVariable, the actual type corresponding to the first
      // TypeVariable is the Type we are looking for
      final Type typeOfId = parameterizedType.getActualTypeArguments()[0];
      return new Id(json.getAsString(), typeOfId);
    }

    @Override
    public JsonElement serialize(final Id<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
      return new JsonPrimitive(src.getValue());
    }
  }

  @SuppressWarnings("unused")
  private static class Student {
    Id<Student> id;
    String name;

    private Student() {
      this(null, null);
    }
    public Student(final Id<Student> id, final String name) {
      this.id = id;
      this.name = name;
    }
  }

  @SuppressWarnings("unused")
  public static class Course<T> {
    final List<Student> students;
    private final Id<Course<T>> courseId;
    private final int numAssignments;
    private final Assignment<T> assignment;

    public Course() {
      this(null, 0, null, new ArrayList<Student>());
    }

    public Course(final Id<Course<T>> courseId, final int numAssignments,
        final Assignment<T> assignment, final List<Student> players) {
      this.courseId = courseId;
      this.numAssignments = numAssignments;
      this.assignment = assignment;
      this.students = players;
    }
    public Id<Course<T>> getId() {
      return courseId;
    }
    List<Student> getStudents() {
      return students;
    }
  }

  @SuppressWarnings("unused")
  private static class Assignment<T> {
    private final Id<Assignment<T>> id;
    private final T data;

    private Assignment() {
      this(null, null);
    }
    public Assignment(final Id<Assignment<T>> id, final T data) {
      this.id = id;
      this.data = data;
    }
  }

  @SuppressWarnings("unused")
  private static class HistoryCourse {
    int numClasses;
  }

  @SafeVarargs
  private static <T> List<T> createList(final T ...items) {
    return Arrays.asList(items);
  }
}
