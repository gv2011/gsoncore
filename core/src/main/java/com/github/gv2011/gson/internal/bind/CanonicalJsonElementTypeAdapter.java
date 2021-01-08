package com.github.gv2011.gson.internal.bind;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonObject;
import com.github.gv2011.gson.stream.JsonReader;

final class CanonicalJsonElementTypeAdapter extends JsonElementTypeAdapter{

  @Override
  JsonElement handleObject(JsonReader in) throws IOException {
    JsonObject object = new JsonObject();
    in.beginObject();
    final SortedMap<String,JsonElement> attributes = new TreeMap<>();
    while (in.hasNext()) {
      attributes.put(in.nextName(), read(in));
    }
    attributes.forEach((k,v)->object.add(k, v));
    in.endObject();
    return object;
  }

}