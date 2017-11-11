package com.github.gv2011.gsoncore.imp.enc;

import com.github.gv2011.util.ann.Nullable;

public class FloatEncoder extends NumberEncoder<Float>{

  public FloatEncoder(final boolean lenient) {
    super(lenient);
  }

  @Override
  protected String toString(final @Nullable Float value) {
    if(value!=null)checkFinite(value);
    final String result = super.toString(value);
    return result;
  }

}
