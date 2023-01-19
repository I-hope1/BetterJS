package better_js.reflect;

import arc.util.OS;
import jdk.internal.misc.Unsafe;

/**
 * 一些jdk包下的实例
 **/
public class JDKVars {
	public static final Unsafe unsafe = Unsafe.getUnsafe();
	public static final Class<?> MagicAccessorImpl;
	static {
		try {
			MagicAccessorImpl = Class.forName(getJavaSpecificationVersion() <= 8 ? "sun.reflect.MagicAccessorImpl": "jdk.internal.reflect.MagicAccessorImpl");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static final int version = getJavaSpecificationVersion();
	private static int getJavaSpecificationVersion() {
        String value = System.getProperty("java.specification.version");
        if (value.startsWith("1.")) {
            value = value.substring(2);
        }

        return Integer.parseInt(value);
    }
}
