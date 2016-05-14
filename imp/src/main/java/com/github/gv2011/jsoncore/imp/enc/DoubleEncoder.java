package com.github.gv2011.jsoncore.imp.enc;

import com.github.gv2011.util.ann.Nullable;

public class DoubleEncoder extends NumberEncoder<Double>{

  public DoubleEncoder(final boolean lenient) {
    super(lenient);
  }

  @Override
  protected String toString(final @Nullable Double value) {
    if(value!=null)checkFinite(value);
    final String result = super.toString(value);
    return result;
  }


}
