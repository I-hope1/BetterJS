package better_js;

import arc.func.*;
import arc.struct.ObjectMap;
import arc.util.*;
import better_js.myrhino.*;
import better_js.utils.ByteCodeTools.MyClass;
import better_js.utils.MyReflect;
import mindustry.Vars;
import mindustry.android.AndroidRhinoContext.AndroidContextFactory;
import rhino.*;
import rhino.classfile.ByteCode;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static better_js.Main.unsafe;
import static rhino.Context.*;

public class ForRhino {
	public static boolean enableAccess = false;
	// public static FieldUtils fieldUtils = null;
	public static final Map NORMAL_PRIVATE_MAP = new ConcurrentHashMap<>(16, 0.75f, 1);
	public static final ObjectMap MY_PRIVATE_MAP = new ObjectMap<>();
	public static final long classCacheLong;

	static {
		try {
			classCacheLong = unsafe.objectFieldOffset(ClassCache.class.getDeclaredField("classTable"));
			// classCache.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	static long wrapFactoryOff;

	static {
		try {
			wrapFactoryOff = unsafe.objectFieldOffset(Context.class.getDeclaredField("wrapFactory"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static MyWrapFactory wrapFactory = new MyWrapFactory();
	public static ContextFactory createFactory() throws Exception {
		Boolf2<Context, Integer> boolf2 = (cx, featureIndex) -> {
			switch (featureIndex) {
				case FEATURE_ENHANCED_JAVA_ACCESS:
					return enableAccess;
				case FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE:
					return true;
			}
			return false;
		};
		Object factory = Vars.mobile ? new AndroidContextFactory(Vars.tmpDirectory.child("caches").file()) {
			protected boolean hasFeature(Context cx, int featureIndex) {
				if (boolf2.get(cx, featureIndex)) return true;
				return super.hasFeature(cx, featureIndex);
			}

			protected Context makeContext() {
				Context cx = super.makeContext();
				cx.setWrapFactory(wrapFactory);
				return cx;
			}
		} : new ContextFactory() {
			protected boolean hasFeature(Context cx, int featureIndex) {
				if (boolf2.get(cx, featureIndex)) return true;
				return super.hasFeature(cx, featureIndex);
			}

			protected Context makeContext() {
				Context cx = super.makeContext();
				cx.setWrapFactory(wrapFactory);
				return cx;
			}
		};
		// 设置全局的factory
		if (!ContextFactory.hasExplicitGlobal()) {
			ContextFactory.getGlobalSetter().setContextFactoryGlobal((ContextFactory) factory);
		} else {
			MyReflect.setValue(null,
					ContextFactory.class.getDeclaredField("global"),
					factory);
		}
		return (ContextFactory) factory;
	}

	public static final Class<?> JavaMembersClass;
	public static final long javaMembersOff, javaStaticMembersOff;

	static {
		try {
			JavaMembersClass = Class.forName("rhino.JavaMembers");
			javaMembersOff = unsafe.objectFieldOffset(JavaMembersClass.getDeclaredField("members"));
			javaStaticMembersOff = unsafe.objectFieldOffset(JavaMembersClass.getDeclaredField("staticMembers"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class MyWrapFactory extends WrapFactory {
		private static final ObjectMap<Class<?>, ObjectMap<Object, Scriptable>> objCache = new ObjectMap<>();
		private static final ObjectMap<Class<?>, Scriptable> classCache = new ObjectMap<>();

		public Scriptable wrapJavaClass(Context cx, Scriptable scope, Class<?> javaClass) {
			// if (enableAccess) return new MyNativeJavaObject(scope, javaObject, staticType);
			// return classCache.get(javaClass, () -> new MyNativeJavaClass(scope, javaClass));
			return new MyNativeJavaClass(scope, javaClass);
		}

		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
			// if (staticType == null || javaObject == null) return new MyNativeJavaObject(scope, javaObject, staticType);
			// return objCache.get(staticType, ObjectMap::new).get(javaObject, () -> new MyNativeJavaObject(scope, javaObject, staticType));
			return new MyNativeJavaObject(scope, javaObject, staticType);
		}
	}

	public static void setAccessible(Map<String, Object> map) {
		map.forEach((k, v) -> {
			if (v instanceof AccessibleObject) {
				((AccessibleObject) v).setAccessible(true);
			}
		});
	}

	/*public interface FieldUtils {
		int fieldOffset(Field field);
	}*/
}
