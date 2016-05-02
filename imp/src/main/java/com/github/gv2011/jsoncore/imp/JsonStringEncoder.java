package com.github.gv2011.jsoncore.imp;

import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.StringWriter;

import com.github.gv2011.jsoncore.JsonEncoder;

class JsonStringEncoder implements JsonEncoder<String>{

    /*
   * From RFC 7159, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. http://code.google.com/p/google-gson/issues/detail?id=341
   */
  static final String[] REPLACEMENT_CHARS;
  static final String[] HTML_SAFE_REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }


  private final boolean htmlSafe;

  JsonStringEncoder() {
    this(false);
  }

  JsonStringEncoder(final boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
  }

  @Override
  public String encode(final String value) {
    final StringWriter w = new StringWriter();
    encode(value, w);
    return w.toString();
  }

  @Override
  public void encode(final String value, final Appendable out) {
    final String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
    write(out, "\"");
    int last = 0;
    final int length = value.length();
    for (int i = 0; i < length; i++) {
      final char c = value.charAt(i);
      String replacement;
      if (c < 128) {
        replacement = replacements[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      } else {
        continue;
      }
      if (last < i) {
        write(out, value, last, i - last);
      }
      write(out, replacement);
      last = i + 1;
    }
    if (last < length) {
      write(out, value, last, length - last);
    }
    write(out, "\"");
  }

  private void write(final Appendable out, final String str) {
    run(()->out.append(str));
  }

  private void write(final Appendable out, final String cbuf, final int off, final int len) {
    run(()->out.append(cbuf, off, off+len));
  }

}
