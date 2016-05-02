package com.github.gv2011.jsoncore.imp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JsonStringEncoderTest {

  @Test
  public void testEncodeString() {
    final JsonStringEncoder encoder = new JsonStringEncoder();
    assertThat(encoder.encode("abc\"d"), is("\"abc\\\"d\""));
  }

}
