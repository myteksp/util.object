package com.gf.util.object;

import org.junit.Test;

import static org.junit.Assert.*;

public final class ObjectMetaUtilTest {
	@Test
	public final void metaSanityTest(){
		assertTrue(22.4 == (double)ObjectMetaUtil.toPrimitive("22.4", double.class));
	}
}
