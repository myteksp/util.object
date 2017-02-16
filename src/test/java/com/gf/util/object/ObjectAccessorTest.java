package com.gf.util.object;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public final class ObjectAccessorTest {
	@Test
	public final void accessorSanityTest(){
		final HashMap<String, String> map = new HashMap<String, String>();
		final ObjectAccessor accessor = ObjectAccessor.createAccessor(map.getClass());
		map.put("param", "hello");
		assertEquals("hello", accessor.getValue("param", map));
	}
}
