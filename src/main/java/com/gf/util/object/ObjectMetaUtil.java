package com.gf.util.object;

public final class ObjectMetaUtil {
	public static final boolean isPrimitive(final Object object){
		if (object == null)
			throw new NullPointerException("Object can not be null.");

		return isPrimitive(object.getClass());
	}

	private static final Class<?>[] numClasses = new Class<?>[]{
		byte.class,
		Byte.class,
		char.class,
		Character.class,
		short.class,
		Short.class,
		int.class,
		Integer.class,
		double.class,
		Double.class,
		long.class,
		Long.class,
		float.class,
		Float.class
	};

	public static final int compareNumbers(final Object a, final Object b){
		final Class<?> clsA = a.getClass();
		final Class<?> clsB = b.getClass();
		int aIndex = -1;
		int bIndex = -1;
		for (int i = 0; i < numClasses.length; i++)
			if (numClasses[i] == clsA)
				aIndex = i;

		for (int i = 0; i < numClasses.length; i++)
			if (numClasses[i] == clsB)
				bIndex = i;

		switch(aIndex){
		case -1:
			throw new RuntimeException("A object was not a number.");
		}
		switch(bIndex){
		case -1:
			throw new RuntimeException("B object was not a number.");
		}

		final int index = aIndex > bIndex?aIndex:bIndex;
		switch(index){
		case 0:
		case 1:
			return ((Byte)a).compareTo((Byte)b);
		case 2:
		case 3:
			return ((Character)a).compareTo((Character)b);
		case 4:
		case 5:
			return ((Short)a).compareTo((Short)b);
		case 6:
		case 7:
			return ((Integer)a).compareTo((Integer)b);
		case 8:
		case 9:
			return ((Double)a).compareTo((Double)b);
		case 10:
		case 11:
			return ((Long)a).compareTo((Long)b);
		case 12:
		case 13:
			return ((Float)a).compareTo((Float)b);
		default:
			throw new RuntimeException("Unknown number format");
		}
	}

	public static final boolean isNumber(final Class<?> clz){
		if (clz == Long.class){
			return true;
		}else if (clz == Short.class){
			return true;
		}else if (clz == Float.class){
			return true;
		}else if (clz == Integer.class){
			return true;
		}else if (clz == Double.class){
			return true;
		}else if (clz == Byte.class){
			return true;
		}else if (clz == Character.class){
			return true;
		}else if (clz == long.class){
			return true;
		}else if (clz == short.class){
			return true;
		}else if (clz == float.class){
			return true;
		}else if (clz == int.class){
			return true;
		}else if (clz == double.class){
			return true;
		}else if (clz == byte.class){
			return true;
		}else if (clz == char.class){
			return true;
		}else{
			return false;
		}
	}

	public static final boolean isPrimitive(final Class<?> clz){
		if (clz == String.class){
			return true;
		}else if (clz == Boolean.class){
			return true;
		}else if (clz == Long.class){
			return true;
		}else if (clz == Short.class){
			return true;
		}else if (clz == Float.class){
			return true;
		}else if (clz == Integer.class){
			return true;
		}else if (clz == Double.class){
			return true;
		}else if (clz == Byte.class){
			return true;
		}else if (clz == Character.class){
			return true;
		}else if (clz == boolean.class){
			return true;
		}else if (clz == long.class){
			return true;
		}else if (clz == short.class){
			return true;
		}else if (clz == float.class){
			return true;
		}else if (clz == int.class){
			return true;
		}else if (clz == double.class){
			return true;
		}else if (clz == byte.class){
			return true;
		}else if (clz == char.class){
			return true;
		}else{
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <V> V toPrimitive(final String obj, final Class<V> clz){
		return (V) getPrimitiveParser(clz).parse(obj.toString());
	}

	public static final PrimitiveParser getPrimitiveParser(final Class<?> clz){
		if (clz == String.class){
			return stringParser;
		}else if (clz == Boolean.class){
			return booleanParser;
		}else if (clz == Long.class){
			return longParser;
		}else if (clz == Short.class){
			return shortParser;
		}else if (clz == Float.class){
			return floatParser;
		}else if (clz == Integer.class){
			return intParser;
		}else if (clz == Double.class){
			return doubleParser;
		}else if (clz == Byte.class){
			return byteParser;
		}else if (clz == Character.class){
			return charParser;
		}else if (clz == boolean.class){
			return booleanParser;
		}else if (clz == long.class){
			return longParser;
		}else if (clz == short.class){
			return shortParser;
		}else if (clz == float.class){
			return floatParser;
		}else if (clz == int.class){
			return intParser;
		}else if (clz == double.class){
			return doubleParser;
		}else if (clz == byte.class){
			return byteParser;
		}else if (clz == char.class){
			return charParser;
		}else{
			return null;
		}
	}

	public static interface PrimitiveParser{
		Object parse(final String str);
	}

	private static final PrimitiveParser stringParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return str;
		}
	};
	private static final PrimitiveParser booleanParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Boolean.parseBoolean(str);
		}
	};

	private static final PrimitiveParser longParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Long.parseLong(str);
		}
	};

	private static final PrimitiveParser shortParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Short.parseShort(str);
		}
	};

	private static final PrimitiveParser floatParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Float.parseFloat(str);
		}
	};

	private static final PrimitiveParser intParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Integer.parseInt(str);
		}
	};

	private static final PrimitiveParser doubleParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Double.parseDouble(str);
		}
	};

	private static final PrimitiveParser byteParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return Byte.parseByte(str);
		}
	};

	private static final PrimitiveParser charParser = new PrimitiveParser() {
		@Override
		public final Object parse(final String str) {
			return (char)Integer.parseInt(str);
		}
	};
}
