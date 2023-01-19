package better_js;

import arc.func.Func3;
import arc.util.*;
import arc.util.serialization.Json;
import better_js.myrhino.MyJavaMembers;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.MyReflect;
import dalvik.system.VMRuntime;
import mindustry.Vars;
import mindustry.mod.Mod;
import rhino.*;
import sun.misc.Unsafe;

import java.lang.reflect.*;

import static better_js.BetterJSRhino.*;
import static better_js.myrhino.MyJavaAdapter.*;
import static mindustry.Vars.mods;

public class Main extends Mod {
	// public static Scripts scripts;
	public static final Unsafe unsafe;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		if (!Vars.mobile) {
			try {
				Field moduleF = Class.class.getDeclaredField("module");
				// 设置模块，使 JavaMembers 可以 setAccessible。
				// 参阅java.lang.reflect.AccessibleObject#checkCanSetAccessible(java.lang.Class<?>,
				// java.lang.Class<?>, boolean)
				long off = unsafe.objectFieldOffset(moduleF);
				Module java_base = Object.class.getModule();
				unsafe.putObject(ForRhino.class, off, java_base);
				unsafe.putObject(MyMemberBox.class, off, java_base);
				unsafe.putObject(MyJavaMembers.class, off, java_base);
				unsafe.putObject(MyInterfaceAdapter.class, off, java_base);
				unsafe.putObject(MyReflect.class, off, java_base);
				unsafe.putObject(Class.forName("rhino.JavaMembers"), off, java_base);
				unsafe.putObject(Class.forName("rhino.VMBridge"), off, java_base);
				// 使json更快
				unsafe.putObject(Json.class, off, java_base);
				unsafe.putObject(Reflect.class, off, java_base);
			} catch (Exception ignored) {}
		}
	}

	public Main() throws Throwable {
		try {
			if (Class.forName(Main.class.getName(), false, mods.mainLoader()) != Main.class)
				return;
		} catch (ClassNotFoundException ignored) {
		}
		Log.info("load BetterJS constructor");
		initScripts();
	}

	public static ContextFactory factory;

	static Function wa, ef, efm, interfaceAdapter;

	static void initScripts() throws Throwable {
		wa = new MyFunc((cx, scope, args) ->
				wrapAccess(cx, scope, args[0])
		);
		ef = new MyFunc((cx, scope, args) ->
				evalFunc(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0])
		);
		efm = new MyFunc((cx, scope, args) ->
				evalFuncWithoutCache(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0], Status.accessMethod)
		);
		interfaceAdapter = new MyFunc((cx, scope, args) ->
				MyInterfaceAdapter.create(cx, (Class<?>) (
						args[0] instanceof NativeJavaObject ? ((NativeJavaObject) args[0]).unwrap() : args[0]
				), (ScriptableObject) args[1])
		);

		if (!Vars.mobile) {
			try {
				Desktop.main(new String[]{});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		try {
			clearReflectionFilter();
		} catch (Throwable ex) {
			Log.err("can't clear reflection filter.", ex);
		}

		factory = ForRhino.createFactory();

		if (mods.hasScripts()) {
			installScripts();
		} else {
			Time.runTask(0, Main::installScripts);
		}
	}

	static void installScripts() {
		if (Context.getCurrentContext() != null) {
			/*VMBridge.setContext(VMBridge.getThreadContextHelper(), null);
			// Context last = Context.getCurrentContext();
			Context cx = Context.enter();
			try {
				MyReflect.setValue(cx,
						Context.class.getDeclaredField("topCallScope"),
						mods.getScripts().scope);
				// Tools.clone(last, cx, last.getClass());
				MyReflect.setValue(mods.getScripts(),
						Scripts.class.getDeclaredField("context"),
						cx);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}*/
			try {
				MyReflect.setValue(Context.getCurrentContext(),
						Context.class.getDeclaredField("factory"),
						factory);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
		Scriptable scope = mods.getScripts().scope;
		Context cx = mods.getScripts().context;
		cx.setWrapFactory(ForRhino.wrapFactory);
		ScriptableObject.putProperty(scope, "a", (Runnable)() -> {});
		// Vars.mods.getScripts().context.setOptimizationLevel(-1);
		// AAO_MyJavaAdapter.init(Vars.mods.getScripts().context, scope, false);
		var obj = new ScriptableObject() {
			final IdFunctionObject ctor = new IdFunctionObject(new MyJavaAdapter(), FTAG, Id_JavaAdapter,
					"MyJavaAdapter", 1, this);

			{
				ctor.markAsConstructor(null);
			}

			@Override
			public String getClassName() {
				return "$AX";
			}

			public Object get(String key, Scriptable obj) {
				switch (key) {
					case "wa":
						return wa;
					case "ef":
						return ef;
					case "efm":
						return efm;
					case "in":
						return interfaceAdapter;
					case "extend":
						return ctor;
					default:
						return NOT_FOUND;
				}
			}
		};
		ScriptableObject.putConstProperty(scope, "$AX", obj);
	}

	/*
	 * public static Object evalAccess(String string) throws IllegalAccessException
	 * {
	 * Object result;
	 * Scriptable scope = Vars.mods.getScripts().scope;
	 * ClassCache cache = ClassCache.get(scope);
	 * Object last = classCache.get(cache);
	 * classCache.set(cache, map);
	 * enableAccess = true;
	 * result = Vars.mods.getScripts().context.evaluateString(scope, string, "a.js",
	 * 1);
	 * enableAccess = false;
	 * classCache.set(cache, last);
	 * return result;
	 * }
	 */

	public static class MyFunc implements Function {
		Func3<Context, Scriptable, Object[], Object> func3;

		public MyFunc(Func3<Context, Scriptable, Object[], Object> func3) {
			this.func3 = func3;
		}

		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length == 0) throw new IllegalArgumentException("wrong args length");
			return func3.get(cx, scope, args);
		}

		public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
			throw new RuntimeException();
		}

		public String getClassName() {
			return "aaa";
		}

		public Object get(String name, Scriptable start) {
			return null;
		}

		public Object get(int index, Scriptable start) {
			return null;
		}

		public boolean has(String name, Scriptable start) {
			return false;
		}

		public boolean has(int index, Scriptable start) {
			return false;
		}

		public void put(String name, Scriptable start, Object value) {
		}

		public void put(int index, Scriptable start, Object value) {
		}

		public void delete(String name) {
		}

		public void delete(int index) {
		}

		public Scriptable getPrototype() {
			return null;
		}

		public void setPrototype(Scriptable prototype) {
		}

		public Scriptable getParentScope() {
			return null;
		}

		public void setParentScope(Scriptable parent) {
		}

		public Object[] getIds() {
			return new Object[0];
		}

		public Object getDefaultValue(Class<?> hint) {
			return null;
		}

		public boolean hasInstance(Scriptable instance) {
			return false;
		}
	}


	static void clearReflectionFilter() throws Throwable {
		if (!OS.isAndroid) {
			Desktop.clearReflectionFilter();
			return;
		}
		Method methodM = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
		methodM.setAccessible(true);
		Method m2 = (Method) methodM.invoke(VMRuntime.class, "setHiddenApiExemptions",
				new Class[]{String[].class});
		m2.setAccessible(true);
		m2.invoke(VMRuntime.getRuntime(), (Object) new String[]{"L"});
	}
}
