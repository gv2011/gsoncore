package com.github.gv2011.gsoncore.imp.enc;

import static com.github.gv2011.util.ex.Exceptions.run;

import com.github.gv2011.gsoncore.JsonEncoder;
import com.github.gv2011.util.ann.Nullable;

abstract class NonStreamingEncoder<T> implements JsonEncoder<T>{


  protected NonStreamingEncoder() {}

  @Override
  public final String encode(final @Nullable T value) {
    return toString(value);
  }

  protected String toString(final @Nullable T value) {
    return String.valueOf(value);
  }

  @Override
  public final void encode(final @Nullable T value, final Appendable out) {
    write(out, toString(value));
  }


  protected final void write(final Appendable out, final String str) {
    run(()->out.append(str));
  }

  protected final void write(final Appendable out, final String cbuf, final int off, final int len) {
    run(()->out.append(cbuf, off, off+len));
  }


}
