package com.github.gv2011.gsoncore;

import java.io.Closeable;

public interface JsonParser extends Closeable{

  void beginObject();

  JsonToken peek();

  String nextName();

  void endObject();

  String nextString();

  boolean nextBoolean();

  void beginArray();

  void endArray();

  void nextNull();

}
