package better_js.android;

import arc.util.Log;
import better_js.utils.MyReflect;

import java.lang.reflect.*;

public class TestPrivate {
	public static void test() throws Exception {
		Method getter = Class.class.getDeclaredMethod("getDeclaredMethodsUnchecked", boolean.class);
		getter.setAccessible(true);
		Method method = ((Method[]) getter.invoke(CLASS_TEST.class, false))[0];
		MyReflect.setPublic(method);
		Log.info(method == ((Method[]) getter.invoke(CLASS_TEST.class, false))[0]);
		new CLASS_TEST2().print();
	}
	static class CLASS_TEST2 extends CLASS_TEST {
		public int a() {
			return 234;
		}
	}

	static class CLASS_TEST {
		private int a() {
			return 222;
		}
		public void print() {
			Log.info(a());
		}
	}
}
