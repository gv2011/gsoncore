package com.github.gv2011.jsoncore;

import java.io.Reader;

public interface JsonFactory {

  JsonParser newJsonParser(Reader reader, JsonOption... jsonOptions);

  JsonSerializer newJsonSerializer(Reader reader, JsonOption... jsonOptions);

  <T> JsonEncoder<T> newJsonEncoder(Class<T> primitive, JsonOption... jsonOptions);

}
