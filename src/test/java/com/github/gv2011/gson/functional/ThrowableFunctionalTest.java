// Copyright (C) 2014 Trymph Inc.
package com.github.gv2011.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public final class ThrowableFunctionalTest // extends TestCase TODO fails, makes private fields accesible
{
  private final Gson gson = new Gson();

  public void testExceptionWithoutCause() {
    RuntimeException e = new RuntimeException("hello");
    final String json = gson.toJson(e);
    assertTrue(json.contains("hello"));

    e = gson.fromJson("{'detailMessage':'hello'}", RuntimeException.class);
    assertEquals("hello", e.getMessage());
  }

  public void testExceptionWithCause() {
    Exception e = new Exception("top level", new IOException("io error"));
    final String json = gson.toJson(e);
    assertTrue(json.contains("{\"detailMessage\":\"top level\",\"cause\":{\"detailMessage\":\"io error\""));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Exception.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  public void testSerializedNameOnExceptionFields() {
    final MyException e = new MyException();
    final String json = gson.toJson(e);
    assertTrue(json.contains("{\"my_custom_name\":\"myCustomMessageValue\""));
  }

  public void testErrorWithoutCause() {
    OutOfMemoryError e = new OutOfMemoryError("hello");
    final String json = gson.toJson(e);
    assertTrue(json.contains("hello"));

    e = gson.fromJson("{'detailMessage':'hello'}", OutOfMemoryError.class);
    assertEquals("hello", e.getMessage());
  }

  public void testErrornWithCause() {
    Error e = new Error("top level", new IOException("io error"));
    final String json = gson.toJson(e);
    assertTrue(json.contains("top level"));
    assertTrue(json.contains("io error"));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Error.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  private static final class MyException extends Throwable {
    @SerializedName("my_custom_name") String myCustomMessage = "myCustomMessageValue";
  }
}
