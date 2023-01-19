package interfaces;

import arc.util.Log;
import better_js.Desktop;
import jdk.internal.reflect.*;

import java.lang.invoke.*;
import java.lang.reflect.*;

import static jdk.internal.reflect.ReflectionFactory.*;

public class InterfaceMain {
	static {
		try {
			init();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	static final Method m;
	static final MethodHandle handle;
	static MethodAccessor nativeAcc;

	static {
		try {
            m = InterfaceMain.class.getDeclaredMethod("apiofqi");
			handle = Desktop.lookup.unreflect(m);
			nativeAcc = getReflectionFactory().newMethodAccessor(m);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void init() throws Throwable {
		Desktop.main(null);
		// ReflectionFactory.class.getDeclaredField("inflationThreshold").setInt(null, 10_0000_0000);
		// var acc = MagicReflect.getFieldAccess(m);
	}

	public static void main(String[] args) throws Throwable {
		// invoke();

		Release.main(args);
	}
	static Runnable r;

	// @Benchmark
	public static void invoke() throws Throwable {
		// new Fi("F:/aaa").writeBytes(bytes);

		r = InterfaceMain::apiofqi;
		for (int i = 0; i < 1_0000; i++) {
			invokeEach();
		}
		Object[] ARGS = new Object[0];

		// final Object[] Null = null;
		long last = System.nanoTime();
		for (int i = 0; i < 1_0000_0000; i++) {
			// m.invoke(null, ARGS);
			// handle.invokeExact();
			nativeAcc.invoke(null, null);
			// r.run();
			// acc.invoke(null, null);
			// apiofqi();
		}
		Log.info((System.nanoTime() - last) / 1_0000_0000f);
	}

	static void invokeEach() throws Throwable {
		// m.invoke(null, (Object[]) null);
		handle.invokeExact();
		r.run();
		apiofqi();
		nativeAcc.invoke(null, null);
	}

	static int count = 0;

	// @Benchmark
	public static void apiofqi() {
		count++;
	}

}
