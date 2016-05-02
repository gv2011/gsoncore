package com.github.gv2011.jsoncore;

public interface JsonEncoder<T> {

  String encode(T value);

  void encode(T value, Appendable out);

}
