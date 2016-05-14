package com.github.gv2011.jsoncore.imp.enc;

import static com.github.gv2011.util.ex.Exceptions.notYetImplementedException;

import com.github.gv2011.jsoncore.JsonEncoder;
import com.github.gv2011.util.bytes.Bytes;

public class BytesEncoder extends AbstractJsonEncoder<Bytes>{

  public BytesEncoder(final JsonEncoder<String> stringEncoder) {
  }

  @Override
  public void encode(final Bytes value, final Appendable out) {
    throw notYetImplementedException();
  }

}
