package com.github.gv2011.gsoncore;

public interface JsonEncoder<T> {

  String encode(T value);

  void encode(T value, Appendable out);

}
