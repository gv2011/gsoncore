package com.github.gv2011.gsoncore;

import static com.github.gv2011.util.CollectionUtils.pair;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Iterator;

import com.github.gv2011.util.Pair;
import com.github.gv2011.util.XStream;
import com.github.gv2011.util.ex.ThrowingFunction;
import com.github.gv2011.util.json.JsonFactory;
import com.github.gv2011.util.json.JsonList;
import com.github.gv2011.util.json.JsonNode;
import com.github.gv2011.util.json.JsonObject;
import com.github.gv2011.util.json.JsonReader;
import com.github.gv2011.util.json.JsonWriter;
import com.github.gv2011.util.json.Adapter;

public final class GsoncoreAdapter implements Adapter{

  @Override
  public JsonWriter newJsonWriter(final Writer out) {
    return new GsonWriter(out);
  }

  @Override
  public JsonReader newJsonReader(final JsonFactory jf, final Reader in) {
	  return new GsonReader(in);
  }

  @Override
  public JsonNode deserialize(final JsonFactory jf, final String json) {
    return callWithCloseable(()->new GsonReader(new StringReader(json)),
    	(ThrowingFunction<GsonReader,JsonNode>)r->deserialize(jf, r)
    );
  }

  private JsonNode deserialize(final JsonFactory jf, final GsonReader in) {
    return call(()->{
      switch (in.peek()) {
      case STRING:
        return jf.primitive(in.readStringRaw());
      case NUMBER:
        return jf.primitive(new BigDecimal(in.readStringRaw()));
      case BOOLEAN:
        return jf.primitive(in.readBooleanRaw());
      case NULL:
        in.readNullRaw();
        return jf.jsonNull();
      case BEGIN_ARRAY:
        in.readArrayStart();
        final JsonList list = XStream.fromIterator(new It(jf, in)).collect(jf.toJsonList());
        in.readArrayEnd();
        return list;
      case BEGIN_OBJECT:
        in.readObjectStart();
        final JsonObject obj = XStream.fromIterator(new Itm(jf, in)).collect(jf.toJsonObject());
        in.readObjectEnd();
        return obj;
      case NAME:
      case END_DOCUMENT:
      case END_OBJECT:
      case END_ARRAY:
      default:
        throw new IllegalArgumentException();
      }
    });
  }


  private final class It implements Iterator<JsonNode> {
    private final GsonReader in;
    private final JsonFactory jf;

    private It(final JsonFactory jf, final GsonReader in) {
        this.jf = jf;
        this.in = in;
    }

    @Override
    public boolean hasNext() {
        return call(in::hasNext);
    }

    @Override
    public JsonNode next() {
        return deserialize(jf, in);
    }
  }


  private final class Itm implements Iterator<Pair<String,JsonNode>> {
    private final GsonReader in;
    private final JsonFactory jf;

    private Itm(final JsonFactory jf, final GsonReader in) {
        this.jf = jf;
        this.in = in;
    }

    @Override
    public boolean hasNext() {
        return call(in::hasNext);
    }

    @Override
    public Pair<String,JsonNode> next() {
        final String key = in.readName();
        final JsonNode value = deserialize(jf, in);
        return pair(key, value);
    }
  }
}
