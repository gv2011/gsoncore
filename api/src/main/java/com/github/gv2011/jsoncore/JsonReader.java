package com.github.gv2011.jsoncore;

import java.io.InputStream;

import com.github.gv2011.util.ex.ThrowingSupplier;

public interface JsonReader {

	JsonNode parse(ThrowingSupplier<InputStream> object);

}
