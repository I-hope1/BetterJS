package better_js.utils;

import arc.util.OS;
import better_js.Main;
import better_js.android.DexLoader.*;
import better_js.reflect.JDKVars;
import hope_android.FieldUtils;
import ihope_lib.*;
import mindustry.Vars;
import mindustry.android.AndroidRhinoContext.AndroidContextFactory;
import rhino.*;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.security.ProtectionDomain;

import static better_js.Desktop.lookup;

public class MyReflect {
	public static final Unsafe unsafe = Main.unsafe;

	static Field accessFlagsCls, accessFlagsExec;

	static {
		if (OS.isAndroid) try {
			accessFlagsCls = Class.class.getDeclaredField("accessFlags");
			accessFlagsCls.setAccessible(true);
			accessFlagsExec = Executable.class.getDeclaredField("accessFlags");
			accessFlagsExec.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * only for android
	 **/
	public static void setPublic(Class<?> exec) {
		if (OS.isAndroid) try {
			int flags = accessFlagsCls.getInt(exec);
			flags &= 0xFFFF;
			flags &= ~Modifier.FINAL;
			flags &= ~Modifier.PRIVATE;
			flags |= Modifier.PUBLIC;
			accessFlagsCls.setInt(exec, flags & 0xFFFF);
		} catch (Exception ignored) {}
	}
	/**
	 * only for android
	 **/
	public static void setPublic(Executable cls) {
		if (OS.isAndroid) try {
			int flags = accessFlagsExec.getInt(cls);
			flags &= 0xFFFF;
			flags &= ~Modifier.FINAL;
			flags &= ~Modifier.PRIVATE;
			flags |= Modifier.PUBLIC;
			accessFlagsExec.setInt(cls, flags);
		} catch (Exception ignored) {}
	}

	public static ClassLoader loader;
	public static ClassLoader dexClassLoader;
	public static Class<?> defineClass(String name, Class<?> superClass, byte[] bytes) {
		if (OS.isAndroid) {
			int mod = superClass.getModifiers();
			if (/*Modifier.isFinal(mod) || */!Modifier.isPublic(mod)) {
				setPublic(superClass);
			}
			try {
				if (dexClassLoader == null) dexClassLoader = new InMemoryAndroidClassLoader(null);
				((BaseAndroidClassLoader)dexClassLoader).definingContext = superClass.getClassLoader();
				return ((GeneratedClassLoader)dexClassLoader).defineClass(name, bytes);
				/* return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new InMemoryAndroidClassLoader(parent)
                : new FileAndroidClassLoader(parent, ((AndroidApplication)Vars.platform).getCacheDir())
				).defineClass(name, bytes); */
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return JDKVars.junsafe.defineClass0(name, bytes, 0, bytes.length, superClass.getClassLoader(), null);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		// return unsafe.defineAnonymousClass(superClass, bytes, null);
	}

	public static Class<?> defineClass(String name, ClassLoader loader, byte[] bytes) {
		if (OS.isAndroid) {
			try {
				return ((GeneratedClassLoader) ((AndroidContextFactory) ContextFactory.getGlobal())
						.createClassLoader(loader))
						.defineClass(name, bytes);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return JDKVars.junsafe.defineClass0(null, bytes, 0, bytes.length, loader, null);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}


	public static Class<?> defineClass(ClassLoader loader, byte[] bytes, ProtectionDomain pd) {
		try {
			return JDKVars.junsafe.defineClass0(null, bytes, 0, bytes.length,
					loader, pd);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Class<?> defineClass(ClassLoader loader, byte[] bytes) {
		return defineClass(loader, bytes, null);
	}

	public static Class<?> getCaller() {
		Thread              thread = Thread.currentThread();
		StackTraceElement[] trace  = thread.getStackTrace();
		try {
			return Class.forName(trace[3].getClassName(), false, Vars.mods.mainLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static void setValue(Object obj, Field f, Object value, boolean force) {
		boolean isStatic = Modifier.isStatic(f.getModifiers());
		if (isStatic) {
			obj = f.getDeclaringClass();
		}
		long offset = OS.isAndroid ? FieldUtils.getFieldOffset(f)
				: isStatic ? unsafe.staticFieldOffset(f)
				: unsafe.objectFieldOffset(f);
		if (force) {
			unsafe.putObject(obj, offset, value);
			return;
		}
		setValue(obj, offset, value, f.getType());
	}

	public static void setValue(Object o, long off, Object value, Class<?> type) {
		if (int.class == type) {
			unsafe.putInt(o, off, ((Number) value).intValue());
		} else if (float.class == type) {
			unsafe.putFloat(o, off, ((Number) value).floatValue());
		} else if (double.class == type) {
			unsafe.putDouble(o, off, ((Number) value).doubleValue());
		} else if (long.class == type) {
			unsafe.putLong(o, off, ((Number) value).longValue());
		} else if (char.class == type) {
			unsafe.putChar(o, off, (char) value);
		} else if (byte.class == type) {
			unsafe.putByte(o, off, ((Number) value).byteValue());
		} else if (short.class == type) {
			unsafe.putShort(o, off, ((Number) value).shortValue());
		} else if (boolean.class == type) {
			unsafe.putBoolean(o, off, (boolean) value);
		} else {
			unsafe.putObject(o, off, value);
		}
	}
	private static void setInt(Object o, long off, Object value) {
		if (value instanceof Byte) {
			unsafe.putByte(o, off, (Byte) value);
			return;
		}
		if (value instanceof Short) {
			unsafe.putShort(o, off, (Short) value);
			return;
		}
		if (value instanceof Character) {
			unsafe.putChar(o, off, (Character) value);
			return;
		}
		unsafe.putInt(o, off, (Integer) value);
	}
	private static void setFloat(Object o, long off, Object value) {
		if (value instanceof Byte) {
			unsafe.putFloat(o, off, (Byte) value);
			return;
		}
		if (value instanceof Short) {
			unsafe.putFloat(o, off, (Short) value);
			return;
		}
		if (value instanceof Character) {
			unsafe.putFloat(o, off, (Character) value);
			return;
		}
		if (value instanceof Integer) {
			unsafe.putFloat(o, off, (Integer) value);
			return;
		}
		if (value instanceof Long) {
			unsafe.putFloat(o, off, (Long) value);
			return;
		}
		unsafe.putFloat(o, off, (float) value);
	}
	private static void setLong(Object o, long off, Object value) {
		if (value instanceof Byte) {
			unsafe.putLong(o, off, (Byte) value);
			return;
		}
		if (value instanceof Short) {
			unsafe.putLong(o, off, (Short) value);
			return;
		}
		if (value instanceof Character) {
			unsafe.putLong(o, off, (Character) value);
			return;
		}
		if (value instanceof Integer) {
			unsafe.putLong(o, off, (Integer) value);
			return;
		}
		if (value instanceof Long) {
			unsafe.putLong(o, off, (Long) value);
			return;
		}
		unsafe.putLong(o, off, (long) value);
	}
	private static void setDouble(Object o, long off, Object value) {
		if (value instanceof Byte) {
			unsafe.putDouble(o, off, (Byte) value);
			return;
		}
		if (value instanceof Short) {
			unsafe.putDouble(o, off, (Short) value);
			return;
		}
		if (value instanceof Character) {
			unsafe.putDouble(o, off, (Character) value);
			return;
		}
		if (value instanceof Integer) {
			unsafe.putDouble(o, off, (Integer) value);
			return;
		}
		if (value instanceof Long) {
			unsafe.putDouble(o, off, (Long) value);
			return;
		}
		if (value instanceof Float) {
			unsafe.putDouble(o, off, (Float) value);
			return;
		}
		if (value instanceof Double) {
			unsafe.putDouble(o, off, (Double) value);
			return;
		}
		unsafe.putDouble(o, off, (double) value);
	}
	
	

	/*static ObjectMap<Class<?>, ObjectLongCons> getter = new ObjectMap<>() {
		@Override
		public ObjectLongCons get(Class<?> key) {
			if (!containsKey(key)) return unsafe::getObject;
			return super.get(key);
		}
	};
	static {
		getter.put(int.class, unsafe::getInt);
		getter.put(float.class, unsafe::getFloat);
		getter.put(double.class, unsafe::getDouble);
		getter.put(long.class, unsafe::getLong);
		getter.put(char.class, unsafe::getChar);
		getter.put(byte.class, unsafe::getByte);
		getter.put(short.class, unsafe::getShort);
		getter.put(boolean.class, unsafe::getBoolean);
	};
	public interface ObjectLongCons {
		Object get(Object o, long off);
	}*/

	public static Object getValue(Object o, long off, Class<?> type) {
		if (int.class == type) {
			return unsafe.getInt(o, off);
		} else if (float.class == type) {
			return unsafe.getFloat(o, off);
		} else if (double.class == type) {
			return unsafe.getDouble(o, off);
		} else if (long.class == type) {
			return unsafe.getLong(o, off);
		} else if (char.class == type) {
			return unsafe.getChar(o, off);
		} else if (byte.class == type) {
			return unsafe.getByte(o, off);
		} else if (short.class == type) {
			return unsafe.getShort(o, off);
		} else if (boolean.class == type) {
			return unsafe.getBoolean(o, off);
		} else {
			return unsafe.getObject(o, off);
		}
	}
	public static native void extend();
	/** 是否只占一个字节 */
	public static boolean isSingle(Class<?> cls) {
		return cls != long.class && cls != double.class;
	}

	public static void setValue(Object obj, Field f, Object value) {
		setValue(obj, f, value, false);
	}
}
