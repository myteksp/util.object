package com.gf.util.cache;

import java.io.Serializable;

public interface Cache<T> extends Serializable{
	int size();
	void clearExpired();
	T get(final String key, final ValueLoader<T> loader);
}
