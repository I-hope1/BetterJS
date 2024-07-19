package better_js;

import arc.util.OS;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class HopeVars {
	public static boolean isAndroid;
	public static Unsafe unsafe;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		try {
			isAndroid = OS.isAndroid;
		} catch (NoClassDefFoundError error) {
			isAndroid = false;
		}
	}
}
