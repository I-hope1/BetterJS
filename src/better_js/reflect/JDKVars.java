package better_js.reflect;

import jdk.internal.misc.Unsafe;

/**
 * 一些jdk包下的实例
 **/
public class JDKVars {
	public static final Unsafe unsafe = Unsafe.getUnsafe();
	public static final Class<?> MagicAccessorImpl;
	static {
		try {
			MagicAccessorImpl = Class.forName("jdk.internal.reflect.MagicAccessorImpl");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
