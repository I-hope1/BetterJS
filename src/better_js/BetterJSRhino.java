package better_js;

import arc.func.Prov;
import arc.struct.*;
import arc.util.Log;
import better_js.myrhino.*;
import better_js.myrhino.MyJavaMembers;
import rhino.*;

import java.util.concurrent.ConcurrentHashMap;

import static better_js.ForRhino.*;
import static better_js.myrhino.MyJavaMembers.*;

public class BetterJSRhino {
	public static boolean methodsPriority = false;
	private static void test() {
		System.out.println("aoack");
	}

	public static Object evalFunc(Context cx, Scriptable scope, Object object) {
		if (!(object instanceof Callable)) throw new IllegalArgumentException(object + " isn't function");
		// Context cx = Context.getCurrentContext();
		// Scriptable scope = Vars.mods.getScripts().scope;
		if (enableAccess) {
			return ((Callable) object).call(cx, scope, scope, new Object[0]);
		}
		enableAccess = true;
		return clearCacheRun(scope, () -> {
			// return Context.call(factory, (Callable) object, TOP_LEVEL, TOP_LEVEL, new Object[0]);
			try {
				return ((Callable) object).call(cx, scope, scope, new Object[0]);
			} finally {
				enableAccess = false;
			}
		}, false);
	}

	public static Object evalFuncWithoutCache(Context cx, Scriptable scope, Object object) {
		if (!(object instanceof Callable)) throw new IllegalArgumentException(object + " isn't function");
		// Context cx = Context.getCurrentContext();
		// Scriptable scope = Vars.mods.getScripts().scope;
		if (enableAccess) {
			return ((Callable) object).call(cx, scope, scope, new Object[0]);
		}
		enableAccess = true;
		try {
			return clearCacheRun(scope, () -> {
				// return Context.call(factory, (Callable) object, TOP_LEVEL, TOP_LEVEL, new Object[0]);
				return ((Callable) object).call(cx, scope, scope, new Object[0]);
			}, true);
		} finally {
			enableAccess = false;
		}
	}

	public static Object clearCacheRun(Scriptable scope, Prov<Object> prov, boolean newCache) {
		ClassCache cache = ClassCache.get(scope);
		// 临时设置
		Object last = Main.unsafe.getObject(cache, classCacheLong);
		Main.unsafe.putObject(cache, classCacheLong, newCache ? new ConcurrentHashMap<>(16, 0.75f, 1) : NORMAL_PRIVATE_MAP);
		var myLast = myJavaMembersCaches;
		myJavaMembersCaches = newCache ? new ObjectMap<>() : MY_PRIVATE_MAP;
		try {
			return prov.get();
		} finally {
			// 恢复原来的状态
			Main.unsafe.putObject(cache, classCacheLong, last);
			myJavaMembersCaches = myLast;
		}
	}

	public static Object wrapAccess(Context cx, Scriptable scope, Object obj) throws IllegalAccessException {
		if (!(obj instanceof NativeJavaObject)) obj = Context.javaToJS(obj, scope);
		NativeJavaObject object = (NativeJavaObject) obj;
		final NativeJavaObject[] result = new NativeJavaObject[1];
		if (enableAccess) {
			if (object instanceof NativeJavaClass || object instanceof MyNativeJavaClass)
				return new MyNativeJavaClass(scope, (Class<?>) object.unwrap(), true);
			return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		}

		enableAccess = true;
		result[0] = (NativeJavaObject) clearCacheRun(scope, () -> {
			if (object instanceof NativeJavaClass || object instanceof MyNativeJavaClass)
				return new MyNativeJavaClass(scope, (Class<?>) object.unwrap(), true);
			return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		}, false);
		enableAccess = false;

		return result[0];
	}
}
