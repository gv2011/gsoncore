package com.github.gv2011.jsoncore.imp.enc;

public class NumberEncoder<T extends Number> extends NonStreamingEncoder<T>{



  private final boolean lenient;

  @Override
  protected String toString(final T value) {
    final String result = super.toString(value);
    checkFiniteStr(result);
    return result;
  }

  public NumberEncoder(final boolean lenient) {
    this.lenient = lenient;
  }

  protected void checkFiniteStr(final String value){
    if (!lenient && (value.equals("-Infinity") || value.equals("Infinity") || value.equals("NaN"))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
  }

  protected void checkFinite(final double value){
    if (!lenient && (Double.isInfinite(value) || Double.isNaN(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
  }


}
