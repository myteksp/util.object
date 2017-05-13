package com.gf.util.cache;

import java.io.Serializable;

public interface ValueLoader<T> extends Serializable{
	T load();
}
