package com.github.gv2011.gsoncore.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class GsonTest {

  @Test
  public void test() {
    final Map<Object,Object> map = new HashMap<>();
    final List<Object> list = new ArrayList<>();
    list.add("i");
    list.add(7);
//    map.put("a", 2);
    map.put(4, 3);
//    map.put(null, 4);
    System.out.println(new Gson().toJson(map));
  }

}
