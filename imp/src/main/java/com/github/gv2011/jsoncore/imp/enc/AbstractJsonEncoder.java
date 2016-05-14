package com.github.gv2011.jsoncore.imp.enc;

import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.StringWriter;

import com.github.gv2011.jsoncore.JsonEncoder;
import com.github.gv2011.util.ann.Nullable;

abstract class AbstractJsonEncoder<T> implements JsonEncoder<T>{


  protected AbstractJsonEncoder() {}

  @Override
  public String encode(final @Nullable T value) {
    final StringWriter w = new StringWriter();
    encode(value, w);
    return w.toString();
  }

  protected final void write(final Appendable out, final String str) {
    run(()->out.append(str));
  }

  protected final void write(final Appendable out, final String cbuf, final int off, final int len) {
    run(()->out.append(cbuf, off, off+len));
  }


}
