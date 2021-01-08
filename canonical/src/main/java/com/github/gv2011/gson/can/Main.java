package com.github.gv2011.gson.can;

import com.github.gv2011.gson.Gson;
import com.github.gv2011.gson.JsonElement;
import com.github.gv2011.gson.JsonParser;

public final class Main {
	
	public static void main(String[] args){
	  if(args.length==0) args = new String[]{"a"};
		try {
      final JsonElement tree = new JsonParser(true).parse(args[0]);
      final Gson gson = new Gson();
      System.out.print(gson.toJson(tree));
    } catch (Exception e) {
      System.exit(1);
    }
	}
	
}
