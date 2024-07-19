package better_js.utils;

import static better_js.utils.MyReflect.unsafe;

import arc.func.Prov;
import arc.struct.Seq;
import arc.util.*;
import arc.util.Timer.Task;
import hope_android.FieldUtils;

import java.lang.reflect.*;
import java.util.function.*;

public class Tools {

	public static void clone(Object from, Object to, Class<?> cls) {
		clone(from, to, cls, null);
	}

	public static boolean display = false;

	public static void clone(Object from, Object to, Class<?> cls, Seq<String> blackList) {
		if (from == to) throw new IllegalArgumentException("from == to");
		while (cls != Object.class && Object.class.isAssignableFrom(cls)) {
			Field[] fields = cls.getDeclaredFields();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers()) && (blackList == null || !blackList.contains(f.getName()))) {
					if (display) Log.info(f);
					setValue(f, from, to);
				}
			}
			cls = cls.getSuperclass();
		}
	}

	public static void setValue(Field f, Object from, Object to) {
		Class<?> type   = f.getType();
		long     offset = OS.isAndroid ? FieldUtils.getFieldOffset(f) : unsafe.objectFieldOffset(f);
		if (int.class.equals(type)) {
			unsafe.putInt(to, offset, unsafe.getInt(from, offset));
		} else if (float.class.equals(type)) {
			unsafe.putFloat(to, offset, unsafe.getFloat(from, offset));
		} else if (double.class.equals(type)) {
			unsafe.putDouble(to, offset, unsafe.getDouble(from, offset));
		} else if (long.class.equals(type)) {
			unsafe.putLong(to, offset, unsafe.getLong(from, offset));
		} else if (char.class.equals(type)) {
			unsafe.putChar(to, offset, unsafe.getChar(from, offset));
		} else if (byte.class.equals(type)) {
			unsafe.putByte(to, offset, unsafe.getByte(from, offset));
		} else if (short.class.equals(type)) {
			unsafe.putShort(to, offset, unsafe.getShort(from, offset));
		} else if (boolean.class.equals(type)) {
			unsafe.putBoolean(to, offset, unsafe.getBoolean(from, offset));
		} else {
			Object o = unsafe.getObject(from, offset);
			/*if (f.getType().isArray()) {
				o = Arrays.copyOf((Object[]) o, Array.getLength(o));
			}*/
			unsafe.putObject(to, offset, o);
		}
	}

	public static Field getField(Class<?> cls, String name) {
		while (cls != Object.class && Object.class.isAssignableFrom(cls)) {
			try {
				// Log.debug(Seq.with(cls.getDeclaredFields()));
				return cls.getDeclaredField(name);
			} catch (Throwable e) {
				cls = cls.getSuperclass();
			}
		}
		return null;
	}

	public static Method getMethod(Class<?> cls, String name, Class<?>... args) {
		Method[] methods;
		while (cls != Object.class && Object.class.isAssignableFrom(cls)) {
			try {
				// Log.debug(Seq.with(cls.getDeclaredFields()));
				methods = cls.getDeclaredMethods();
				for (Method m : methods) {
					if (m.getName().equals(name)) return m;
				}
				// return cls.getDeclaredMethod(name);
			} catch (Throwable e) {
				cls = cls.getSuperclass();
			}
		}
		return null;
	}

	public static <T> T newInstance(Class<T> cls) {
		try {
			// Log.debug("create");
			return (T) unsafe.allocateInstance(cls);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void forceRun(Runnable toRun) {
		new Task() {
			@Override
			public void run() {
				try {
					toRun.run();
				} catch (NotTimeException e) {
					Time.runTask(0, this);
				}
			}
		}.run();
	}

	public static class NotTimeException extends RuntimeException {
	}


	@SafeVarargs
	public static <R> R orThrow(ProvT<R>... provTSeq) {
		for (ProvT<R> p : provTSeq) {
			try {
				return p.get();
			} catch (Throwable ignored) {}
		}
		return null;
	}


	public static <R> R or(Prov<R> prov1, Prov<R> prov2) {
		R r = prov1.get();
		if (r == null) return prov2.get();
		return r;
	}

	// a ?? b ?? c ?? d
	@SafeVarargs
	public static <R> R or(Prov<R>... provTSeq) {
		R r;
		for (Prov<R> p : provTSeq) {
			r = p.get();
			if (r != null) return r;
		}
		return null;
	}

	public static final class SR<T> {
		T value;

		public SR(T value) {
			this.value = value;
		}

		public SR<T> cons(Consumer<T> consumer) {
			consumer.accept(value);
			return this;
		}

		public SR<T> reset(Supplier<T> value) {
			this.value = value.get();
			return this;
		}

		public SR<T> ifeq(Object obj, Runnable run) {
			if (value.equals(obj)) {
				run.run();
			}
			return this;
		}

		public SR<T> ifneq(Object obj, Runnable run) {
			if (!value.equals(obj)) {
				run.run();
			}
			return this;
		}

		public T get() {
			return value;
		}
	}


	public interface ProvT<R> {
		R get() throws Throwable;
	}
}
