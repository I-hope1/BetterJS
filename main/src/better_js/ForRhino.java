package better_js;

import arc.files.Fi;
import arc.util.OS;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.A_ASM.*;
import better_js.utils.MyReflect;
import mindustry.Vars;
import mindustry.mod.ModClassLoader;
import rhino.*;

import java.io.File;
import java.lang.reflect.*;

import static better_js.BetterJSRhino.status;
import static rhino.Context.*;

public class ForRhino {
	public static final long classCacheOff  = MyReflect.offset(ClassCache.class, "classTable");
	public static final long wrapFactoryOff = MyReflect.offset(Context.class, "wrapFactory");

	public static MyWrapFactory wrapFactory = new MyWrapFactory();


	public static ContextFactory createFactory() throws Exception {
		if (true) return ContextFactory.getGlobal();
		ContextFactory                    global         = ContextFactory.getGlobal();
		MyClass<? extends ContextFactory> factoryMyClass = new MyClass<>(global.getClass().getName().replace('.', '/') + "$1", global.getClass());
		factoryMyClass.addInterface(MyContextFactory.class);
		factoryMyClass.visit(ForRhino.class);

		factoryMyClass.setFunc("<init>", null, Modifier.PUBLIC, true, Void.TYPE, OS.isAndroid ? new Class[]{File.class} : new Class[0]);
		// factoryMyClass.writer.write(Vars.tmpDirectory.child(factoryMyClass.adapterName + ".class").write());
		try {
			Vars.mods.mainLoader().loadClass(factoryMyClass.superClass.getName());
		} catch (ClassNotFoundException e) {
			((ModClassLoader) Vars.mods.mainLoader()).addChild(factoryMyClass.superClass.getClassLoader());
		}

		Constructor<?> cons = factoryMyClass.define(Vars.mods.mainLoader()).getDeclaredConstructors()[0];
		Fi             fi   = Vars.tmpDirectory.child("wprhinokfactorys");
		fi.mkdirs();
		ContextFactory factory = (ContextFactory) (OS.isAndroid ? cons.newInstance(fi.file())
		 : cons.newInstance());
		// 设置全局的factory
		if (!ContextFactory.hasExplicitGlobal()) {
			ContextFactory.getGlobalSetter().setContextFactoryGlobal(factory);
		} else {
			MyReflect.setValue(null,
			 ContextFactory.class.getDeclaredField("global"),
			 factory, true);
		}
		return factory;
	}
	public static <T> T unwrap(Object o) {
		if (o instanceof NativeJavaObject na) return (T) na.unwrap();
		return (T) o;
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
	/* @Include(buildSuper = true)
	public static Context makeContext(ContextFactory self) {
		Context cx = ((MyContextFactory) self).super$_makeContext();
		cx.setGeneratingSource(false);
		cx.setGeneratingDebug(false);
		return cx;
	} */

	/**
	 * Execute top call to script orThrow function.
	 * When the runtime is about to execute a script orThrow function that will
	 * create the first stack frame with scriptable code, it calls this method
	 * to perform the real call. In this way execution of any script
	 * happens inside this function.
	 */
	// @Include(buildSuper = true)
	public static Object doTopCall(ContextFactory self, Callable callable,
																 Context cx, Scriptable scope,
																 Scriptable thisObj, Object[] args) {
		// try {
		Object result = callable.call(cx, scope, thisObj, args);
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
		if (featureIndex == FEATURE_ENHANCED_JAVA_ACCESS) {
			return status != Status.normal;
		}
		// if (featureIndex == FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME) return true;
		return ((MyContextFactory) self).super$_hasFeature(cx, featureIndex);
	}

	// public static final Class<?> JavaMembersClass = MyReflect.klassOrNull("rhino.JavaMembers");
	// public static final long javaMembersOff   = MyReflect.offset(JavaMembersClass, "members"), javaStaticMembersOff = MyReflect.offset(JavaMembersClass, "staticMembers");

	public static class MyWrapFactory extends WrapFactory {
		public Scriptable wrapJavaClass(Context cx, Scriptable scope, Class<?> javaClass) {
			return new MyNativeJavaClass(scope, javaClass);
		}

		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
			return new MyNativeJavaObject(scope, javaObject, staticType);
		}
	}

	public interface MyContextFactory {
		Context super$_makeContext();

		/*Object super$_doTopCall(Callable callable,
		                        Context cx, Scriptable scope,
		                        Scriptable thisObj, Object[] args);*/
		boolean super$_hasFeature(Context cx, int featureIndex);
	}
}

