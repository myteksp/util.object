package com.gf.util.object;

import org.junit.Test;

import com.gf.util.cache.GFcache;
import com.gf.util.cache.ValueLoader;

import static org.junit.Assert.*;

public final class CacheTest {
	@Test
	public final void cacheTest(){
		final GFcache<String> cache = new GFcache<String>(100);
		cache.get("k1", new ValueLoader<String>() {
			private static final long serialVersionUID = 979854666728852687L;

			@Override
			public final String load() {
				return "s1";
			}
		});
		cache.get("k2", new ValueLoader<String>() {
			private static final long serialVersionUID = -6802882647375156047L;

			@Override
			public final String load() {
				return "s2";
			}
		});
		cache.get("k3", new ValueLoader<String>() {
			private static final long serialVersionUID = 57674381728873344L;

			@Override
			public final String load() {
				return "s3";
			}
		});
		cache.clearExpired();
		assertEquals(3, cache.size());
		try {Thread.sleep(300);} catch (InterruptedException e) {}
		cache.clearExpired();
		assertEquals(0, cache.size());
	}
}
