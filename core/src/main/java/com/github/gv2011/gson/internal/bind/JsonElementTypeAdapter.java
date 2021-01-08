package com.github.gv2011.gson.internal.bind;

import java.io.IOException;
import java.util.Map;

import com.github.gv2011.gson.JsonArray;
import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonNull;
import com.github.gv2011.gson.JsonObject;
import com.github.gv2011.gson.JsonPrimitive;
import com.github.gv2011.gson.TypeAdapter;
import com.github.gv2011.gson.internal.LazilyParsedNumber;
import com.github.gv2011.gson.stream.JsonReader;
import com.github.gv2011.gson.stream.JsonWriter;

class JsonElementTypeAdapter extends TypeAdapter<JsonElement>{

  @Override
  public final JsonElement read(JsonReader in) throws IOException {
    switch (in.peek()) {
    case STRING:
      return new JsonPrimitive(in.nextString());
    case NUMBER:
      String number = in.nextString();
      return new JsonPrimitive(new LazilyParsedNumber(number));
    case BOOLEAN:
      return new JsonPrimitive(in.nextBoolean());
    case NULL:
      in.nextNull();
      return JsonNull.INSTANCE;
    case BEGIN_ARRAY:
      JsonArray array = new JsonArray();
      in.beginArray();
      while (in.hasNext()) {
        array.add(read(in));
      }
      in.endArray();
      return array;
    case BEGIN_OBJECT:
      return handleObject(in);
    case END_DOCUMENT:
    case NAME:
    case END_OBJECT:
    case END_ARRAY:
    default:
      throw new IllegalArgumentException();
    }
  }

  JsonElement handleObject(JsonReader in) throws IOException {
    JsonObject object = new JsonObject();
    in.beginObject();
    while (in.hasNext()) {
      object.add(in.nextName(), read(in));
    }
    in.endObject();
    return object;
  }

  @Override
  public final void write(JsonWriter out, JsonElement value) throws IOException {
    if (value == null || value.isJsonNull()) {
      out.nullValue();
    } else if (value.isJsonPrimitive()) {
      JsonPrimitive primitive = value.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        out.value(primitive.getAsNumber());
      } else if (primitive.isBoolean()) {
        out.value(primitive.getAsBoolean());
      } else {
        out.value(primitive.getAsString());
      }

    } else if (value.isJsonArray()) {
      out.beginArray();
      for (JsonElement e : value.getAsJsonArray()) {
        write(out, e);
      }
      out.endArray();

    } else if (value.isJsonObject()) {
      out.beginObject();
      for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
        out.name(e.getKey());
        write(out, e.getValue());
      }
      out.endObject();

    } else {
      throw new IllegalArgumentException("Couldn't write " + value.getClass());
    }
  }
}

