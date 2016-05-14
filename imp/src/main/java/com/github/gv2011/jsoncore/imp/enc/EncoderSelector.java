package com.github.gv2011.jsoncore.imp.enc;

import static com.github.gv2011.util.ex.Exceptions.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.gv2011.jsoncore.JsonEncoder;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.ser.TypeSupport;

public class EncoderSelector implements TypeSupport<String,Object>{

  private final Map<Class<?>,JsonEncoder<?>> encoders;

  public EncoderSelector(final boolean lenient) {
    encoders = buildEncoderMap(lenient);
  }


  @SuppressWarnings("unchecked")
  public <T> JsonEncoder<T> selectEncoder(final T obj){
    final Class<? extends Object> clazz = obj.getClass();
    return (JsonEncoder<T>) tryGetEncoder(clazz)
      .orElseThrow(()->new IllegalArgumentException(format("{} is not supported.", clazz)))
    ;
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<JsonEncoder<T>> tryGetEncoder(final Class<T> type){
    return Optional.ofNullable((JsonEncoder<T>) encoders.get(type));
  }

  private static Map<Class<?>, JsonEncoder<?>> buildEncoderMap(final boolean lenient) {
    final Map<Class<?>,JsonEncoder<?>> encoders = new HashMap<>();
    put(encoders, Void.class, new ToStringEncoder<>());
    put(encoders, Boolean.class, new ToStringEncoder<>());
    final JsonEncoder<String> stringEncoder = new StringEncoder();
    put(encoders, String.class, stringEncoder);
    put(encoders, Bytes.class, new BytesEncoder(stringEncoder));
    put(encoders, Byte.class, new ToStringEncoder<>());
    put(encoders, Short.class, new ToStringEncoder<>());
    put(encoders, Integer.class, new ToStringEncoder<>());
    put(encoders, Long.class, new ToStringEncoder<>());
    put(encoders, BigInteger.class, new ToStringEncoder<>());
    put(encoders, Float.class, new FloatEncoder(lenient));
    put(encoders, Double.class, new DoubleEncoder(lenient));
    put(encoders, BigDecimal.class, new ToStringEncoder<>());
    put(encoders, BigInteger.class, new ToStringEncoder<>());
    return Collections.unmodifiableMap(encoders);
  }


  private static <T> void put(
    final Map<Class<?>, JsonEncoder<?>> encoders, final Class<T> clazz, final JsonEncoder<T> encoder
  ) {
    encoders.put(clazz, encoder);
  }



  @Override
  public boolean isMap(final Object obj) {
    return obj instanceof Map;
  }

  @Override
  public Map<?,?> asMap(final Object obj) {
    return (Map<?, ?>) obj;
  }

  @Override
  public boolean isList(final Object obj) {
    return obj instanceof Iterable;
  }

  @Override
  public Iterable<?> asList(final Object obj) {
    return (Iterable<?>) obj;
  }


  @Override
  public boolean isBean(final Object obj) {
    if(!(obj instanceof Map)) return false;
    else return ((Map<?,?>)obj).keySet().parallelStream().allMatch(k->k.getClass()==String.class);
  }


  @SuppressWarnings("unchecked")
  @Override
  public Map<String,?> asBean(final Object obj) {
    return (Map<String,?>) obj;
  }


  @Override
  public Object asElementary(final Object obj) {
    return obj;
  }


  @Override
  public boolean isElementary(final Object obj) {
    return tryGetEncoder(obj.getClass()).isPresent();
  }

}
