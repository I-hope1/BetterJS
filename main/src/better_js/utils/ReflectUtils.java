package better_js.utils;

import java.lang.reflect.*;

public class ReflectUtils {
	public static Field findField(Object obj, String name) throws NoSuchFieldException {
		Class<?> cls = obj.getClass();
		while (cls != Object.class) {
			try {
				Field field = cls.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				cls = cls.getSuperclass();
			}
		}
		throw new NoSuchFieldException(obj.getClass().getSimpleName() + " not find " + name);
	}

	public static Method findMethod(Object obj, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
		Class<?> cls = obj.getClass();
		while (cls != Object.class) {
			try {
				Method method = cls.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				cls = cls.getSuperclass();
			}
		}
		throw new NoSuchMethodException(obj.getClass().getSimpleName() + " not find " + name);
	}
}
