package com.gf.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GFcache<T> implements Cache<T>{
	private static final long serialVersionUID = -145114832439641933L;

	private final long keepFor;
	
	private final ConcurrentLinkedQueue<ValueBundle<T>> queue;
	private final ConcurrentHashMap<String, ValueBundle<T>> map;
	
	public GFcache(final long keepFor){
		this.keepFor = keepFor;
		this.queue = new ConcurrentLinkedQueue<ValueBundle<T>>();
		this.map = new ConcurrentHashMap<String, ValueBundle<T>>();
	}
	
	@Override
	public final int size() {
		return map.size();
	}
	
	@Override
	public final void clearExpired(){
		final long time = System.currentTimeMillis();
		for(;;){
			final ValueBundle<T> val = queue.peek();
			if (val == null)
				return;
			
			final long kept = time - val.setTime;
			if (kept > keepFor){
				final ValueBundle<T> val1 = queue.poll();
				if (val1 != null){
					map.remove(val1.key);
				}
			}else{
				return;
			}
		}
	}
	
	@Override
	public final T get(final String key, final ValueLoader<T> loader){
		final ValueBundle<T> res = map.get(key);
		if (res == null){
			final ValueBundle<T> val = new ValueBundle<T>(key, loader);
			final ValueBundle<T> can = map.putIfAbsent(key, val);
			if (can == null){
				queue.add(val);
				return val.value;
			}else{
				return can.value;
			}
		}
		return res.value;
	}
	
	
	private static final class ValueBundle<T>{
		public final String key;
		public final T value;
		public final long setTime;
		
		public ValueBundle(final String key, final ValueLoader<T> loader){
			this.key = key;
			value = loader.load();
			setTime = System.currentTimeMillis();
		}
		@Override
		public final int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + (int) (setTime ^ (setTime >>> 32));
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		@Override
		public final boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("rawtypes")
			final ValueBundle other = (ValueBundle) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (setTime != other.setTime)
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
