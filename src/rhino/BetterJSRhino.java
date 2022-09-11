package rhino;

import arc.func.Prov;
import arc.struct.*;
import better_js.myrhino.*;
import better_js.myrhino.MyJavaMembers;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static better_js.ForRhino.*;
import static better_js.Main.*;

public class BetterJSRhino {
	public static boolean methodsPriority = false;

	public static NativeJavaMethod getMethods(Class<?> cls, String name) {
		Seq<MemberBox> seq = new Seq<>();
		for (var m : cls.getDeclaredMethods()) {
			if (m.getName().equals(name)) seq.add(new MemberBox(m));
		}

		return new NativeJavaMethod(seq.toArray(MemberBox.class));
	}

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
		Object last = unsafe.getObject(cache, classCacheLong);
		unsafe.putObject(cache, classCacheLong, newCache ? new ConcurrentHashMap<>(16, 0.75f, 1) : NORMAL_PRIVATE_MAP);
		var myLast = MyJavaMembers.myJavaMembersCaches;
		MyJavaMembers.myJavaMembersCaches = newCache ? new ObjectMap<>() : MY_PRIVATE_MAP;
		try {
			return prov.get();
		} finally {
			// 恢复原来的状态
			unsafe.putObject(cache, classCacheLong, last);
			MyJavaMembers.myJavaMembersCaches = myLast;
		}
	}

	public static Object wrapAccess(Context cx, Scriptable scope, Object obj) throws IllegalAccessException {
		if (!(obj instanceof MyNativeJavaObject)) obj = Context.javaToJS(obj, scope);
		MyNativeJavaObject object = (MyNativeJavaObject) obj;
		final MyNativeJavaObject[] result = new MyNativeJavaObject[1];
		if (enableAccess) {
			if (object instanceof MyNativeJavaClass)
				return new MyNativeJavaClass(scope, object.unwrap().getClass(), true);
			return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		}

		enableAccess = true;
		result[0] = (MyNativeJavaObject) clearCacheRun(scope, () -> {
			if (object instanceof MyNativeJavaClass)
				return new MyNativeJavaClass(scope, object.unwrap().getClass(), true);
			return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		}, false);
		enableAccess = false;

		return result[0];
	}
}
