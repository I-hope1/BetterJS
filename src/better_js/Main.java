package better_js;

import arc.func.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.mod.*;
import rhino.*;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static rhino.Context.*;

public class Main extends Mod {
	// public static Scripts scripts;
	public static Unsafe unsafe;

	public Main() throws Exception {
		Log.info("load BetterJS constructor");
		initScripts();
	}

	public static boolean enableAccess = false;
	public static ContextFactory factory;
	public static final Map map = new ConcurrentHashMap<>(16, 0.75f, 1);
	public static Field classCache;

	static {
		try {
			classCache = ClassCache.class.getDeclaredField("classTable");
			classCache.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private static void initScripts() throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		unsafe = (Unsafe) unsafeField.get(null);

		if (!Vars.mobile) {
			Field moduleF = Class.class.getDeclaredField("module");
			// 设置模块，使 JavaMembers 可以 setAccessible。
			// 参阅java.lang.reflect.AccessibleObject#checkCanSetAccessible(java.lang.Class<?>, java.lang.Class<?>, boolean)
			unsafe.putObject(Class.forName("rhino.JavaMembers"), unsafe.objectFieldOffset(moduleF), Object.class.getModule());
		}


		Boolf2<Context, Integer> boolf2 = (cx, featureIndex) -> {
			int version;
			switch (featureIndex) {
				case FEATURE_NON_ECMA_GET_YEAR:
					/*
					 * During the great date rewrite of 1.3, we tried to track the
					 * evolving ECMA standard, which then had a definition of
					 * getYear which always subtracted 1900.  Which we
					 * implemented, not realizing that it was incompatible with
					 * the old behavior...  now, rather than thrash the behavior
					 * yet again, we've decided to leave it with the - 1900
					 * behavior and point people to the getFullYear method.  But
					 * we try to protect existing scripts that have specified a
					 * version...
					 */
					version = cx.getLanguageVersion();
					return (version == VERSION_1_0
							|| version == VERSION_1_1
							|| version == VERSION_1_2);

				case FEATURE_ENHANCED_JAVA_ACCESS:
					Log.debug(enableAccess);
					return enableAccess;

				case FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
				case FEATURE_LITTLE_ENDIAN:
				case FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE:
				case FEATURE_THREAD_SAFE_OBJECTS:
				case FEATURE_WARNING_AS_ERROR:
				case FEATURE_STRICT_MODE:
				case FEATURE_LOCATION_INFORMATION_IN_ERROR:
				case FEATURE_STRICT_EVAL:
				case FEATURE_STRICT_VARS:
				case FEATURE_DYNAMIC_SCOPE:
					return false;

				case FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
				case FEATURE_V8_EXTENSIONS:
				case FEATURE_PARENT_PROTO_PROPERTIES:
					return true;

				case FEATURE_TO_STRING_AS_SOURCE:
					version = cx.getLanguageVersion();
					return version == VERSION_1_2;

				case FEATURE_E4X:
					version = cx.getLanguageVersion();
					return version >= VERSION_1_6;

				case FEATURE_OLD_UNDEF_NULL_THIS:
					return cx.getLanguageVersion() <= VERSION_1_7;

				case FEATURE_ENUMERATE_IDS_FIRST:
					return cx.getLanguageVersion() >= VERSION_ES6;
			}
			// It is a bug to call the method with unknown featureIndex
			throw new IllegalArgumentException(String.valueOf(featureIndex));
		};
		Object factory = Vars.mobile ? new mindustry.android.AndroidRhinoContext.AndroidContextFactory(Vars.tmpDirectory.child("caches").file()) {
			@Override
			protected boolean hasFeature(Context cx, int featureIndex) {
				return boolf2.get(cx, featureIndex);
			}
		} : new ContextFactory() {
			@Override
			protected boolean hasFeature(Context cx, int featureIndex) {
				return boolf2.get(cx, featureIndex);
			}
		};
		Main.factory = (ContextFactory) factory;
		// 设置全局的factory
		if (!ContextFactory.hasExplicitGlobal()) {
			ContextFactory.getGlobalSetter().setContextFactoryGlobal((ContextFactory) factory);
		} else {
			Field globalF = ContextFactory.class.getDeclaredField("global");
			globalF.setAccessible(true);
			globalF.set(null, factory);
		}

		Scriptable scope = Vars.mods.getScripts().scope;
		ScriptableObject.putProperty(scope, "evalFunc", new NativeJavaMethod(Main.class.getMethod("evalFunc", Object.class), "evalFunc"));
	}
	public static Object evalFunc(Object object) throws IllegalAccessException {
		if (!(object instanceof Callable)) throw new IllegalArgumentException(object + " isn't function");
		Context cx = Context.getCurrentContext();
		Scriptable scope = Vars.mods.getScripts().scope;
		ClassCache cache = ClassCache.get(scope);
		Object last = classCache.get(cache);
		classCache.set(cache, map);
		try {
			enableAccess = true;
			// return Context.call(factory, (Callable) object, TOP_LEVEL, TOP_LEVEL, new Object[0]);
			return ((Callable) object).call(cx, scope, scope, new Object[0]);
		} finally {
			enableAccess = false;
			classCache.set(cache, last);
		}
	}

	/*public static Object evalAccess(String string) throws IllegalAccessException {
		Object result;
		Scriptable scope = Vars.mods.getScripts().scope;
		ClassCache cache = ClassCache.get(scope);
		Object last = classCache.get(cache);
		classCache.set(cache, map);
		enableAccess = true;
		result = Vars.mods.getScripts().context.evaluateString(scope, string, "a.js", 1);
		enableAccess = false;
		classCache.set(cache, last);
		return result;
	}

	public static Object wrapAccessObject(Object obj) throws IllegalAccessException {
		if (!(obj instanceof NativeJavaObject)) obj = Context.javaToJS(obj, Vars.mods.getScripts().scope);
		NativeJavaObject object = (NativeJavaObject) obj;
		NativeJavaObject result;
		Scriptable parent = object.getParentScope();
		ClassCache cache = ClassCache.get(parent);
		Object last = classCache.get(cache);
		classCache.set(cache, map);
		enableAccess = true;
		if (object instanceof NativeJavaClass)
			result = new NativeJavaClass(parent, object.unwrap().getClass(), true);
		else result = new NativeJavaObject(parent, object.unwrap(), object.getClass(), false);
		enableAccess = false;
		classCache.set(cache, last);
		return result;
	}*/
}
