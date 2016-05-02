package com.github.gv2011.jsoncore.imp;

import java.io.Reader;

import com.github.gv2011.jsoncore.JsonEncoder;
import com.github.gv2011.jsoncore.JsonFactory;
import com.github.gv2011.jsoncore.JsonOption;
import com.github.gv2011.jsoncore.JsonParser;
import com.github.gv2011.jsoncore.JsonSerializer;

public class JsonFactoryImp implements JsonFactory{

  @Override
  public JsonParser newJsonParser(final Reader reader, final JsonOption... jsonOptions) {
    return new JsonReader(reader);
  }

  @Override
  public JsonSerializer newJsonSerializer(final Reader reader, final JsonOption... jsonOptions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> JsonEncoder<T> newJsonEncoder(final Class<T> primitive, final JsonOption... jsonOptions) {
    // TODO Auto-generated method stub
    return null;
  }

}
