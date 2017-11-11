package com.github.gv2011.gsoncore.imp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.gv2011.gsoncore.imp.enc.StringEncoder;

public class JsonStringEncoderTest {

  @Test
  public void testEncodeString() {
    final StringEncoder encoder = new StringEncoder();
    assertThat(encoder.encode("abc\"d"), is("\"abc\\\"d\""));
  }

}
