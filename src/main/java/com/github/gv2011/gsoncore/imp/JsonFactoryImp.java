package com.github.gv2011.gsoncore.imp;

import static com.github.gv2011.util.ex.Exceptions.notYetImplementedException;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import com.github.gv2011.gsoncore.JsonEncoder;
import com.github.gv2011.gsoncore.JsonFactory;
import com.github.gv2011.gsoncore.JsonOption;
import com.github.gv2011.gsoncore.JsonParser;
import com.github.gv2011.gsoncore.JsonSerializer;
import com.github.gv2011.gsoncore.imp.enc.EncoderSelector;
import com.github.gv2011.gsoncore.imp.enc.StringEncoder;
import com.github.gv2011.gsoncore.imp.enc.ToStringEncoder;

public class JsonFactoryImp implements JsonFactory{


  @Override
  public JsonParser newJsonParser(final Reader reader, final JsonOption... jsonOptions) {
    return new JsonReader(reader, jsonOptions);
  }


  @Override
  public JsonSerializer newJsonSerializer(final Writer out, final JsonOption... jsonOptions) {
    final boolean lenient = Arrays.asList(jsonOptions).contains(JsonOption.LENIENT);
    return new JsonWriter(out, new EncoderSelector(lenient), 0, jsonOptions);
  }


  @SuppressWarnings("unchecked")
  @Override
  public <T> JsonEncoder<T> newJsonEncoder(final Class<T> primitive, final JsonOption... jsonOptions) {
    JsonEncoder<T> encoder;
    if(primitive.equals(String.class)){
      encoder = (JsonEncoder<T>)
        new StringEncoder(Arrays.asList(jsonOptions).contains(JsonOption.HTML_SAFE))
      ;
    }
    else if(primitive.equals(Long.class)||primitive.equals(long.class)){
      encoder = new ToStringEncoder<>();
    }
    else throw new UnsupportedOperationException(primitive.getName());
    return encoder;
  }


  @Override
  public com.github.gv2011.gsoncore.JsonReader newJsonReader() {
    throw notYetImplementedException();
  }

}
