// * Copyright (C) 2021 Vinz (https://github.com/gv2011)
module com.github.gv2011.gson{
  requires transitive com.github.gv2011.util;
  requires com.github.gv2011.util.json.imp;
  exports com.github.gv2011.gsoncore to com.github.gv2011.util;
  provides com.github.gv2011.util.json.imp.Adapter with com.github.gv2011.gsoncore.GsoncoreAdapter;

}