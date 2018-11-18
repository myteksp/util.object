package com.gf.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class Caches {
	
	private static final ConcurrentHashMap<String, Cache<?>> caches = new ConcurrentHashMap<String, Cache<?>>();
	
	public static final void clear() {
		caches.clear();
	}
	
	public static final <T> Cache<T> timeCache(final long keepFor){
		return new GFcache<T>(keepFor);
	}
	
	public static final <T> Cache<T> sizeCache(final int maxSize){
		return new GfSizeCache<T>(maxSize);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> Cache<T> timeCache(final String name, final long keepFor){
		final Cache<?> cached = caches.get(name);
		if (cached == null) {
			final Cache<T> newCache = timeCache(keepFor);
			final Cache<?> candidate = caches.putIfAbsent(name, newCache);
			if (candidate == null) {
				return newCache;
			} else {
				return (Cache<T>)candidate;
			}
		}
		return (Cache<T>)cached;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> Cache<T> sizeCache(final String name, final int size){
		final Cache<?> cached = caches.get(name);
		if (cached == null) {
			final Cache<T> newCache = sizeCache(size);
			final Cache<?> candidate = caches.putIfAbsent(name, newCache);
			if (candidate == null) {
				return newCache;
			} else {
				return (Cache<T>)candidate;
			}
		}
		return (Cache<T>)cached;
	}
	
	public static final <T> Cache<T> sizeCache(final String name){
		return sizeCache(name, 2048);
	}
	
	public static final <T> Cache<T> timeCache(final String name){
		return timeCache(TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES));
	}
}
