package com.gf.util.cache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;





public final class GfSizeCache<T> implements Cache<T>{
	private static final long serialVersionUID = -5556396108097208144L;
	
	private final ConcurrentHashMap<String, ValueBundle<T>> map;
	private final int maxSize;
	
	public GfSizeCache(final int maxSize) {
		this.maxSize = maxSize < 10?10:maxSize;
		this.map = new ConcurrentHashMap<String, ValueBundle<T>>(maxSize);
	}

	@Override
	public final int size() {
		return map.size();
	}

	@Override
	public final void clearExpired() {
		if (size() < maxSize)
			return;
		
		final ArrayList<Entry<String, ValueBundle<T>>> list = new ArrayList<Entry<String, ValueBundle<T>>>(map.size() + 10);
		for(final Entry<String, ValueBundle<T>> e : map.entrySet()) 
			list.add(e);
		
		list.sort(new Comparator<Entry<String, ValueBundle<T>>>() {
			@Override
			public final int compare(final Entry<String, ValueBundle<T>> o1, final Entry<String, ValueBundle<T>> o2) {
				return Long.compare(o1.getValue().setTime.get(), o2.getValue().setTime.get());
			}
		});
		
		final int toRemoveCount = (list.size() / 2);
		for (int i = 0; i < toRemoveCount; i++) 
			map.remove(list.get(i).getKey());
	}

	@Override
	public final T get(String key, ValueLoader<T> loader) {
		final ValueBundle<T> res = map.get(key);
		if (res == null){
			final ValueBundle<T> val = new ValueBundle<T>(key, loader);
			final ValueBundle<T> can = map.putIfAbsent(key, val);
			if (can == null){
				if (this.size() >= maxSize) {
					clearExpired();
				}
				return val.value;
			}else{
				can.setTime.set(System.currentTimeMillis());
				return can.value;
			}
		}
		res.setTime.set(System.currentTimeMillis());
		return res.value;
	}

	private static final class ValueBundle<T>{
		public final String key;
		public final T value;
		public final AtomicLong setTime;
		
		public ValueBundle(final String key, final ValueLoader<T> loader){
			this.key = key;
			value = loader.load();
			setTime = new AtomicLong(System.currentTimeMillis());
		}
		
		@Override
		public final int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((setTime == null) ? 0 : setTime.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ValueBundle<?> other = (ValueBundle<?>) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (setTime == null) {
				if (other.setTime != null)
					return false;
			} else if (!setTime.equals(other.setTime))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		@Override
		public final String toString() {
			return "ValueBundle [key=" + key + ", value=" + value + ", setTime=" + setTime + "]";
		}
	}
}
