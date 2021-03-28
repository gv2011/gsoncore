// * Copyright (C) 2021 Vinz (https://github.com/gv2011)
module com.github.gv2011.gsoncore{
  requires transitive com.github.gv2011.util;
  exports com.github.gv2011.gsoncore to com.github.gv2011.util;
  provides com.github.gv2011.util.json.Adapter with com.github.gv2011.gsoncore.GsoncoreAdapter;

}