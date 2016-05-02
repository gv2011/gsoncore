package com.github.gv2011.jsoncore.imp;

public class ToStringEncoder<T> extends AbstractJsonEncoder<T>{

  @Override
  public void encode(final T value, final Appendable out) {
    write(out, value.toString());
  }

}
