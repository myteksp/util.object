package com.gf.util.object;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gf.util.object.ObjectMetaUtil.PrimitiveParser;
import com.gf.util.string.MacroCompiler;
import com.gf.util.string.Splitter;

public final class ObjectAccessor {
	private final Class<?> clz;

	private final HashMap<String, List<Method>> methodsMap;
	private final HashMap<String, Field> fieldsMap;
	private final ConcurrentHashMap<String, List<ObjectAccessor>> accessorsCache;

	private ObjectAccessor(final Class<?> clz){
		this.clz = clz;
		this.methodsMap = new HashMap<String, List<Method>>();
		this.fieldsMap = new HashMap<String, Field>();
		this.accessorsCache = new ConcurrentHashMap<String, List<ObjectAccessor>>();

		for(final Method method : clz.getMethods()){
			List<Method> methods = methodsMap.get(method.getName());
			if (methods == null){
				methods = new ArrayList<Method>();
				methodsMap.put(method.getName(), methods);
			}
			methods.add(method);
		}

		for(final Field field : clz.getFields())
			fieldsMap.put(field.getName(), field);
	}

	private final Object getField(final String name, final Object object){
		if (name == null)
			return object;

		switch(name.length()){
		case 0:
			return object;
		}

		final Field field = fieldsMap.get(name);

		if (field == null){
			try{
				return ((Map<?, ?>)object).get(name);
			}catch(final Throwable tt){}
			throw new RuntimeException("Field '" + name + "' not found in class '" + clz.getName() + "'");
		}

		try {
			return field.get(object);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static final class MethodSignatureParseResult{
		public final String methodName;
		public final String[] values;
		public MethodSignatureParseResult(final String methodName, final String[] values){
			this.methodName = methodName;
			this.values = values;
		}
	}

	private static final int findArrStartSignatureIndex(final String path){
		int openIndex = path.length() - 1;
		int escapePointer = 0;
		for(;;)
			switch(path.charAt(openIndex)){
			case '(':
				escapePointer--;
				switch(escapePointer){
				case 0:
					return openIndex;
				default:
					openIndex--;
				}
				break;
			case ')':
				escapePointer++;
				openIndex--;
				break;
			default:
				openIndex--;
				switch(openIndex){
				case -1:
					throw new RuntimeException("'(' start closure not found.");
				}
			}
	}

	private static final int findArrStartIndex(final String path){
		int openIndex = path.length() - 1;
		int escapePointer = 0;
		for(;;)
			switch(path.charAt(openIndex)){
			case '[':
				escapePointer--;
				switch(escapePointer){
				case 0:
					return openIndex;
				default:
					openIndex--;
				}
				break;
			case ']':
				escapePointer++;
				openIndex--;
				break;
			default:
				openIndex--;
				switch(openIndex){
				case -1:
					throw new RuntimeException("'[' start closure not found.");
				}
			}
	}

	private static final char[] escapeChars = new char[]{'"', "'".charAt(0)};

	public static final MethodSignatureParseResult methodSignatureParse(final String signature){
		final int openIndex = findArrStartSignatureIndex(signature);
		final String val = signature.substring(openIndex + 1, signature.length() - 1);
		return new MethodSignatureParseResult(signature.replace("(" + val + ")", ""), Splitter.splitIgnoreEscapes(val, ',', escapeChars));
	}

	private final Object getFromMethod(final String signature, final Object object, final Map<String, Object> args){
		MethodSignatureParseResult parsed = methodSignatureParse(signature);
		final String methodName = parsed.methodName;
		final String[] splited = parsed.values;
		final List<Method> methods = methodsMap.get(methodName);
		if (methods == null){
			throw new RuntimeException("No methods with name '" + methodName + "' found in class '"  + clz.getName() + "'");
		}
		for(final Method method : methods){
			final Class<?>[] params = method.getParameterTypes();
			try{
				final Object[] parsedParam = new Object[splited.length];
				for (int i = 0; i < splited.length; i++) {
					final String strValue = MacroCompiler.compile(splited[i], args, defaultValueSerializer);
					Object preSetValue = args.get(strValue);
					if (preSetValue == null){
						final Class<?> type = params[i];
						parsedParam[i] = parse(strValue, type);
					}else{
						parsedParam[i] = preSetValue;
					}
				}
				return method.invoke(object, parsedParam);
			}catch(final Throwable t){
				continue;
			}
		}
		throw new RuntimeException("No methods with name '" + methodName + "' found in class '"  + clz.getName() + "' with suitable signature.");
	}

	private static final Object parse(final String strValue, final Class<?> type){
		final PrimitiveParser parser = ObjectMetaUtil.getPrimitiveParser(type);
		if (parser == null)
			throw new RuntimeException("Type '" + type.getName() + "' is not primitive.");
		return parser.parse(strValue);
	}

	private static final class ArraParseSuffixResult{
		public final String value;
		public final String string;
		public ArraParseSuffixResult(final String value, final String string){
			this.value = value;
			this.string = string;
		}
	}



	private static final ArraParseSuffixResult parseArraySuffix(final String path){
		final int openIndex = findArrStartIndex(path);
		final String val = path.substring(openIndex + 1, path.length() - 1);
		return new ArraParseSuffixResult(val, path.replace("[" + val + "]", ""));
	}

	public final Object getZeroDepthValue(final String fieldOrMethod, 
			final Object object,  
			final Map<String, Object> args){
		final char lastChar = fieldOrMethod.charAt(fieldOrMethod.length() - 1);
		switch(lastChar){
		case ')':
			return getFromMethod(fieldOrMethod, object, args);
		case ']':
			final ArraParseSuffixResult pasr = parseArraySuffix(fieldOrMethod);
			final Object retVal = getZeroDepthValue(pasr.string, object, args);
			try{
				final int index = Integer.parseInt(pasr.value);
				if (retVal instanceof Iterable<?>){
					int counter = 0;
					for(final Object val : (Iterable<?>)retVal){
						if (counter == index){
							return val;
						}else{
							counter++;
						}
					}
				}
				if (retVal instanceof String[]){
					return ((String[])retVal)[index];
				}
				if (retVal instanceof Object[]){
					return ((Object[])retVal)[index];
				}
				if (retVal instanceof int[]){
					return ((int[])retVal)[index];
				}
				if (retVal instanceof byte[]){
					return ((byte[])retVal)[index];
				}
				if (retVal instanceof boolean[]){
					return ((boolean[])retVal)[index];
				}
				if (retVal instanceof short[]){
					return ((short[])retVal)[index];
				}
				if (retVal instanceof float[]){
					return ((float[])retVal)[index];
				}
				if (retVal instanceof double[]){
					return ((double[])retVal)[index];
				}
				if (retVal instanceof char[]){
					return ((char[])retVal)[index];
				}
			}catch(final NumberFormatException nfe){
				if (retVal instanceof Map){
					try{
						return ((Map<?, ?>)retVal).get(pasr.value);
					}catch(final Throwable t){}
				}
			}
			throw new RuntimeException("Returned value was of type: " + retVal.getClass().getName());
		default:
			return getField(fieldOrMethod, object);
		}
	}

	private static final HashMap<String, Object> emptyMap = new HashMap<String, Object>();

	public final Object getValue(final String path, final Object object){
		return getValue(path, object, emptyMap);
	}


	public final Object getValue(final String path, final Object object,  
			final Object[] args){
		if (args.length > 0){
			final StringBuilder sb = new StringBuilder();
			final HashMap<String, Object> params = new HashMap<String, Object>();
			final int lastIndex = args.length - 1;
			sb.append("(");
			for (int i = 0; i < lastIndex; i++) {
				sb.append("${").append(i).append("},");
				params.put(Long.toString(i), args[i]);
			}
			sb.append("${").append(lastIndex).append("})");
			params.put(Long.toString(lastIndex), args[lastIndex]);
			return getValue(path.replace("()", sb.toString()), object, params);
		}else{
			return getValue(path, object, emptyMap);
		}
	}

	public final Object getValue(final String path, final Object object,  
			final Map<String, Object> args){
		if (path.endsWith(")")){
			String[] splitted = path.split("\\.");
			switch(splitted.length){
			case 0:
				return getZeroDepthValue(path, object, args);
			case 1:
				return getZeroDepthValue(path, object, args);
			}
			final int lastIndex = splitted.length - 1;
			final MethodSignatureParseResult sig = methodSignatureParse(path);
			List<ObjectAccessor> accessorsList = accessorsCache.get(sig.methodName);
			if (accessorsList == null){
				accessorsList = new ArrayList<ObjectAccessor>();
				accessorsCache.put(sig.methodName, accessorsList);
				accessorsList.add(this);
				ObjectAccessor currentAccessor = this;
				Object currentAccessedValue = object;
				for (int i = 0; i < lastIndex; i++) {
					currentAccessedValue = currentAccessor.getZeroDepthValue(splitted[i], currentAccessedValue, args);
					currentAccessor = createAccessor(currentAccessedValue.getClass());
					accessorsList.add(currentAccessor);
				}
				return accessorsList.get(accessorsList.size() - 1).getZeroDepthValue(splitted[lastIndex], currentAccessedValue, args);
			}
			Object curObj = object;
			for (int i = 0; i < lastIndex; i++) {
				final ObjectAccessor acc = accessorsList.get(i);
				curObj = acc.getZeroDepthValue(splitted[i], curObj, args);
			}
			return accessorsList.get(lastIndex).getZeroDepthValue(splitted[lastIndex], curObj, args);
		}else{
			String[] splitted = path.split("\\.");
			switch(splitted.length){
			case 0:
				return getZeroDepthValue(path, object, args);
			case 1:
				return getZeroDepthValue(path, object, args);
			}
			final int lastIndex = splitted.length - 1;
			List<ObjectAccessor> accessorsList = accessorsCache.get(path);
			if (accessorsList == null){
				accessorsList = new ArrayList<ObjectAccessor>();
				accessorsCache.put(path, accessorsList);
				accessorsList.add(this);
				ObjectAccessor currentAccessor = this;
				Object currentAccessedValue = object;
				for (int i = 0; i < lastIndex; i++) {
					currentAccessedValue = currentAccessor.getZeroDepthValue(splitted[i], currentAccessedValue, args);
					currentAccessor = createAccessor(currentAccessedValue.getClass());
					accessorsList.add(currentAccessor);
				}
				return accessorsList.get(accessorsList.size() - 1).getZeroDepthValue(splitted[lastIndex], currentAccessedValue, args);
			}
			Object curObj = object;
			for (int i = 0; i < lastIndex; i++) {
				final ObjectAccessor acc = accessorsList.get(i);
				curObj = acc.getZeroDepthValue(splitted[i], curObj, args);
			}
			return accessorsList.get(lastIndex).getZeroDepthValue(splitted[lastIndex], curObj, args);
		}
	}

	@Override
	public final String toString() {
		return "ObjectAccessor [forClass=" + clz.getName() + "]";
	}

	public static final ObjectAccessor createAccessor(final Class<?> clz){
		return new ObjectAccessor(clz);
	}

	public static final Map<String, Object> toArgs(final Object ...args){
		final HashMap<String, Object> result = new HashMap<String, Object>(Math.max(args.length/2, 2));
		boolean isKey = true;
		String key = null;

		for (int i = 0; i < args.length; i++) 
			if (isKey){
				key = args[i].toString();
				isKey = false;
			}else{
				result.put(key, args[i]);
				isKey = true;
			}

		return result;
	}



	private static final MacroCompiler.ValueSerializer defaultValueSerializer = new MacroCompiler.ValueSerializer() {
		@Override
		public final String serialize(final Object value) {
			if (value == null)
				return "null";
			return value.toString();
		}
	};
}
