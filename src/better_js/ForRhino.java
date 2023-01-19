package better_js;

import arc.files.Fi;
import arc.func.Func2;
import arc.struct.ObjectMap;
import better_js.BetterJSRhino.DelegatingScope;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.ByteCodeTools.*;
import better_js.utils.MyReflect;
import mindustry.Vars;
import rhino.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

import static better_js.BetterJSRhino.status;
import static better_js.Main.*;
import static rhino.Context.*;

public class ForRhino {
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
		ContextFactory global = ContextFactory.getGlobal();
		MyClass<? extends ContextFactory> factoryMyClass = new MyClass<>(global.getClass().getName() + "$1", global.getClass());
		factoryMyClass.addInterface(MyContextFactory.class);
		factoryMyClass.visit(ForRhino.class);

		factoryMyClass.setFunc("<init>", null, Modifier.PUBLIC, true,Void.TYPE, Vars.mobile ? new Class[]{Fi.class} : new Class[0]);

		factoryMyClass.writer.write(Vars.tmpDirectory.child(factoryMyClass.adapterName + ".class").write());

		Constructor<?> cons = factoryMyClass.define(Vars.mods.mainLoader()).getDeclaredConstructors()[0];
		factory = (ContextFactory) (Vars.mobile ? cons.newInstance(Vars.tmpDirectory.child("factory"))
				: cons.newInstance());
		// 设置全局的factory
		if (!ContextFactory.hasExplicitGlobal()) {
			ContextFactory.getGlobalSetter().setContextFactoryGlobal(factory);
		} else {
			MyReflect.setValue(null,
					ContextFactory.class.getDeclaredField("global"),
					factory);
		}
		return factory;
	}

	/**
	 * Create new {@link Context} instance to be associated with the current
	 * thread.
	 * This is a callback method used by Rhino to create {@link Context}
	 * instance when it is necessary to associate one with the current
	 * execution thread. <tt>makeContext()</tt> is allowed to call
	 * {@link Context#seal(Object)} on the result to prevent
	 * {@link Context} changes by hostile scripts orThrow applets.
	 */
	@Include(buildSuper = true)
	public static Context makeContext(ContextFactory self) {
		Context cx = ((MyContextFactory) self).super$_makeContext();
		cx.setWrapFactory(wrapFactory);
		return cx;
	}

	/**
	 * Execute top call to script orThrow function.
	 * When the runtime is about to execute a script orThrow function that will
	 * create the first stack frame with scriptable code, it calls this method
	 * to perform the real call. In this way execution of any script
	 * happens inside this function.
	 */
	@Include(buildSuper = true)
	public static Object doTopCall(ContextFactory self, Callable callable,
	                               Context cx, Scriptable scope,
	                               Scriptable thisObj, Object[] args) {
		// try {
		Object result = callable.call(cx, status == Status.normal ? scope : new DelegatingScope(scope), thisObj, args);
		return result instanceof ConsString ? result.toString() : result;
		/*} catch (Throwable e) {
			Log.err(e);
			return null;
		}*/
	}

	/**
	 * Implementation of {@link Context#hasFeature(int featureIndex)}.
	 * This can be used to customize {@link Context} without introducing
	 * additional subclasses.
	 */
	@Include(buildSuper = true)
	public static boolean hasFeature(ContextFactory self, Context cx, int featureIndex) {
		switch (featureIndex) {
			case FEATURE_ENHANCED_JAVA_ACCESS:
				return status != Status.normal;
			case FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE:
				return true;
		}
		return ((MyContextFactory) self).super$_hasFeature(cx, featureIndex);
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
			return new MyNativeJavaClass(scope, javaClass, true);
		}

		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
			// if (staticType == null || javaObject == null) return new MyNativeJavaObject(scope, javaObject, staticType);
			// return objCache.get(staticType, ObjectMap::new).get(javaObject, () -> new MyNativeJavaObject(scope, javaObject, staticType));
			return new MyNativeJavaObject(scope, javaObject, staticType, true);
		}
	}

	public interface MyContextFactory {
		Context super$_makeContext();

		Object super$_doTopCall(Callable callable,
		                        Context cx, Scriptable scope,
		                        Scriptable thisObj, Object[] args);

		boolean super$_hasFeature(Context cx, int featureIndex);
	}
}

