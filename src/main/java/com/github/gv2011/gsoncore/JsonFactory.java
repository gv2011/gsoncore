package com.github.gv2011.gsoncore;

import java.io.Reader;
import java.io.Writer;

public interface JsonFactory {

  JsonParser newJsonParser(Reader reader, JsonOption... jsonOptions);

  JsonSerializer newJsonSerializer(Writer out, JsonOption... jsonOptions);

  <T> JsonEncoder<T> newJsonEncoder(Class<T> primitive, JsonOption... jsonOptions);

  JsonReader newJsonReader();

}
