package better_js;

import arc.util.*;
import arc.util.serialization.Json;
import better_js.myrhino.*;
import better_js.mytest.TestAndroid;
import better_js.utils.*;
import dalvik.system.*;
import mindustry.Vars;
import mindustry.mod.*;
import rhino.*;
import rhino.classfile.ClassFileWriter;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.*;

import static rhino.BetterJSRhino.*;

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
			Field moduleF;
			try {
				moduleF = Class.class.getDeclaredField("module");
				// 设置模块，使 JavaMembers 可以 setAccessible。
				// 参阅java.lang.reflect.AccessibleObject#checkCanSetAccessible(java.lang.Class<?>,
				// java.lang.Class<?>, boolean)
				long off = unsafe.objectFieldOffset(moduleF);
				Module java_base = Object.class.getModule();
				unsafe.putObject(ForRhino.class, off, java_base);
				unsafe.putObject(MyMemberBox.class, off, java_base);
				unsafe.putObject(MyReflect.class, off, java_base);
				unsafe.putObject(Class.forName("rhino.JavaMembers"), off, java_base);
				unsafe.putObject(Class.forName("rhino.VMBridge"), off, java_base);
				// 使json更快
				unsafe.putObject(Json.class, off, java_base);
				unsafe.putObject(Reflect.class, off, java_base);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Main() throws Throwable {
		Log.info("load BetterJS constructor");
		initScripts();
	}

	public static ContextFactory factory;

	static void initScripts() throws Throwable {
		factory = ForRhino.createFactory();
		var wa = new MyFunc() {
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
				try {
					return wrapAccess(cx, scope, args[0]);
				} catch (IllegalAccessException e) {throw new RuntimeException(e);}
			}
		};
		var ef = new MyFunc() {
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
				methodsPriority = false;
				return evalFunc(cx, scope, args[0]);
			}
		};
		var efm = new MyFunc() {
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
				try {
					methodsPriority = true;
					return evalFuncWithoutCache(cx, scope, args[0]);
				} finally {
					methodsPriority = false;
				}
			}
		};
		Time.runTask(1, () -> {
			Scriptable scope = Vars.mods.getScripts().scope;
			// Vars.mods.getScripts().context.setOptimizationLevel(-1);
			// AAO_MyJavaAdapter.init(Vars.mods.getScripts().context, scope, false);
			var obj = new ScriptableObject() {
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
						default:
							return NOT_FOUND;
					}
				}
			};
			ScriptableObject.putConstProperty(scope, "$AX", obj);

			/*ScriptableObject.putProperty(scope, "evalFuncOnlyMethods", new MyFunc() {
				public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
					try {
						fieldsPriority = false;
						return evalFuncWithoutCache(cx, scope, args[0]);
					} finally {
						fieldsPriority = true;
					}
				}
			});*/
		});

		clearReflectionFilter();
		if (Vars.mobile) {
			TestAndroid.main(new String[0]);
			Field f = Class.class.getDeclaredField("accessFlags");
			f.setAccessible(true);
			int flags = f.getInt(ATest.class);
			flags &= 0xFFFF;
			flags &= ~Modifier.FINAL;
			flags &= ~Modifier.PRIVATE;
			flags |= Modifier.PUBLIC;
			f.setInt(ATest.class, flags & 0xFFFF);
			String name = ATest.class.getName() + "$1";

			ClassFileWriter cfw = new ClassFileWriter(name, ATest.class.getName(),
					"<adapter>");
			VMRuntime.getRuntime().startJitCompilation();

			/*Tools.forceRun(() -> {
				if (Vars.mods.getMod(Main.class) == null) throw new RuntimeException();
				Fi target = Vars.mods.getMod(Main.class).root.child("invokeFunc.jar");
				Fi toFile = Vars.tmpDirectory.child("invokeFunc.jar");
				target.copyTo(toFile);
				try {
					ClassLoader loader = Vars.platform.loadJar(toFile, loader);
					Class.forName(, true, loader);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
								BaseDexClassLoader loader = (BaseDexClassLoader) Context.class.getClassLoader();

				// loader.addDexPath(toFile.path(), true);
			});*/
			Time.runTask(0, () -> {
				Class<?> cls = Vars.mods.getScripts().context.createClassLoader(ATest.class.getClassLoader())
						.defineClass(name, cfw.toByteArray());
				try {
					Log.info(unsafe.allocateInstance(cls));
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				}
				/*Vars.mods.getScripts().context.createClassLoader(JavaMembersClass.getClassLoader()).defineClass("rhino.MyJavaMembers", Bytecodes.javaMembers);*/
			});
		} else {
			try {
				Test.main(new String[]{});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static final class ATest {
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

	public static abstract class MyFunc implements Function {
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
		if (Vars.mobile) {
			Method methodM = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
			methodM.setAccessible(true);
			Method m2 = (Method) methodM.invoke(VMRuntime.class, "setHiddenApiExemptions",
					new Class[]{String[].class});
			m2.setAccessible(true);
			m2.invoke(VMRuntime.getRuntime(), (Object) new String[]{"L"});
			return;
		}
		Class<?> reflect = Class.forName("jdk.internal.reflect.Reflection");
		// System.out.println(Arrays.toString(reflect.getDeclaredFields()));
		Lookup lookup = (Lookup) ReflectionFactory.getReflectionFactory().newConstructorForSerialization(
						Lookup.class, Lookup.class.getDeclaredConstructor(
								Class.class))
				.newInstance(reflect);
		// Class.forName("jdk.internal.reflect.ConstantPool");
		MethodHandle handle = lookup.findStaticGetter(reflect, "fieldFilterMap", Map.class);
		((Map) handle.invokeExact()).clear();

		handle = lookup.findStaticGetter(reflect, "methodFilterMap", Map.class);
		((Map) handle.invokeExact()).clear();
	}
}
