package better_js;

import apzmagic.MAGICIMPL;
import arc.struct.ObjectMap;
import arc.util.*;
import better_js.reflect.*;
import better_js.utils.*;
import interfaces.AO_MyInterface;
import jdk.internal.loader.*;
import jdk.internal.module.Modules;
import jdk.internal.reflect.*;
import mindustry.Vars;
import mindustry.mod.*;
import rhino.classfile.ByteCode;
import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

import static better_js.utils.ByteCodeTools.*;
import static java.lang.reflect.Modifier.*;

/**
 * only for window
 **/
public class Desktop {
	public static final Unsafe unsafe = Main.unsafe;
	public static final Lookup lookup;

	public static AO_MyInterface myInterface;

	static {
		try {
			/*Constructor<?> cons = Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
			Lookup _lookup = (Lookup) ReflectionFactory.getReflectionFactory().newConstructorForSerialization(
					Lookup.class, cons
			).newInstance(Lookup.class, null, -1);
			_lookup = (Lookup) _lookup.findStaticGetter(Lookup.class, "IMPL_LOOKUP", Lookup.class).invoke();
			lookup = _lookup;*/
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));
			// mh = lookup.unreflectSetter(Test2.class.getDeclaredField("IAAA"));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> reflect;

	static {
		try {
			reflect = Class.forName("jdk.internal.reflect.Reflection");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] ___) throws Throwable {
		((Map) lookup.findStaticGetter(reflect, "fieldFilterMap", Map.class)
				.invokeExact()).clear();
		((Map) lookup.findStaticGetter(reflect, "methodFilterMap", Map.class)
				.invokeExact()).clear();
		Field moduleSetter = Class.class.getDeclaredField("module");
		unsafe.putObject(Desktop.class, unsafe.objectFieldOffset(moduleSetter), Object.class.getModule());

		// MethodHandle m = lookup.findStatic(Module.class, "addExportsToAll0", MethodType.methodType(void.class, Module.class, String.class));
		// m.setAccessible(true);
		Module java_base = Object.class.getModule();
		Module everyone  = (Module) unsafe.getObject(Module.class, unsafe.staticFieldOffset(Module.class.getDeclaredField("EVERYONE_MODULE")));
		lookup.findVirtual(Module.class, "implAddOpens", MethodType.methodType(void.class, String.class))
				.invokeExact(java_base, "jdk.internal.module");
		Modules.addOpens(java_base, "jdk.internal.module", everyone);
		Modules.addOpens(java_base, "jdk.internal.misc", everyone);
		Modules.addOpens(java_base, "jdk.internal.reflect", everyone);
		Modules.addOpens(java_base, "jdk.internal.loader", everyone);
		// Modules.addOpens(java_base, "jdk.internal.misc", everyone);
		Modules.addOpens(java_base, "jdk.internal.platform", everyone);
		try {
			Modules.addOpens(java_base, "jdk.internal.access", everyone);
		} catch (Throwable ignored) {}

		lookup.findStatic(Module.class, "addReads0", MethodType.methodType(void.class, Module.class, Module.class))
				.invokeExact(java_base, everyone);

		byte[] bytes;
		// Func2
		// bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 12, 7, 0, 2, 1, 0, 27, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 49, 47, 70, 117, 110, 99, 50, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 3, 103, 101, 116, 1, 0, 56, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 9, 83, 105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 13, 40, 84, 80, 49, 59, 84, 80, 50, 59, 41, 84, 82, 59, 1, 0, 82, 60, 80, 49, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 80, 50, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 82, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 62, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 10, 70, 117, 110, 99, 50, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 4, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 2, 0, 8, 0, 2, 0, 7, 0, 0, 0, 2, 0, 9, 0, 10, 0, 0, 0, 2, 0, 11};
		// MyReflect.defineClass(null, bytes);
		// AO_MyInterface
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 11, 7, 0, 2, 1, 0, 25, 105, 110, 116, 101, 114, 102, 97, 99, 101, 115, 47, 65, 79, 95, 77, 121, 73, 110, 116, 101, 114, 102, 97, 99, 101, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 11, 115, 101, 116, 79, 118, 101, 114, 114, 105, 100, 101, 1, 0, 39, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 114, 101, 102, 108, 101, 99, 116, 47, 65, 99, 99, 101, 115, 115, 105, 98, 108, 101, 79, 98, 106, 101, 99, 116, 59, 41, 86, 1, 0, 17, 103, 101, 116, 77, 101, 116, 104, 111, 100, 65, 99, 99, 101, 115, 115, 111, 114, 1, 0, 65, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 101, 116, 104, 111, 100, 59, 41, 76, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 101, 116, 104, 111, 100, 65, 99, 99, 101, 115, 115, 111, 114, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 19, 65, 79, 95, 77, 121, 73, 110, 116, 101, 114, 102, 97, 99, 101, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 4, 1, 0, 5, 0, 6, 0, 0, 4, 1, 0, 7, 0, 8, 0, 0, 0, 1, 0, 9, 0, 0, 0, 2, 0, 10};
		MyReflect.defineClass(null, bytes);
		// MagicPublic
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 1, 0, 45, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 95, 80, 85, 66, 76, 73, 67, 7, 0, 1, 1, 0, 38, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 7, 0, 3, 1, 0, 13, 95, 95, 66, 89, 84, 69, 95, 67, 108, 97, 115, 115, 48, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 12, 0, 6, 0, 7, 10, 0, 4, 0, 8, 1, 0, 4, 67, 111, 100, 101, 1, 0, 13, 83, 116, 97, 99, 107, 77, 97, 112, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 0, 1, 0, 2, 0, 4, 0, 0, 0, 0, 0, 1, 0, 1, 0, 6, 0, 7, 0, 1, 0, 10, 0, 0, 0, 25, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 1, 0, 11, 0, 0, 0, 2, 0, 0, 0, 1, 0, 12, 0, 0, 0, 2, 0, 5};
		MyReflect.defineClass(null, bytes);
		// MAGICIMPL
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 1, 0, 18, 97, 112, 122, 109, 97, 103, 105, 99, 47, 77, 65, 71, 73, 67, 73, 77, 80, 76, 7, 0, 1, 1, 0, 45, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 95, 80, 85, 66, 76, 73, 67, 7, 0, 3, 1, 0, 13, 95, 95, 66, 89, 84, 69, 95, 67, 108, 97, 115, 115, 48, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 12, 0, 6, 0, 7, 10, 0, 4, 0, 8, 1, 0, 4, 67, 111, 100, 101, 1, 0, 13, 83, 116, 97, 99, 107, 77, 97, 112, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 0, 1, 0, 2, 0, 4, 0, 0, 0, 0, 0, 1, 0, 1, 0, 6, 0, 7, 0, 1, 0, 10, 0, 0, 0, 25, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 1, 0, 11, 0, 0, 0, 2, 0, 0, 0, 1, 0, 12, 0, 0, 0, 2, 0, 5};
		MyReflect.defineClass(null, bytes);
		// Log.info(AO_MyInterface.class);

		myInterface = defineInterfaceClass();
		defineReflectionFactory();

		// defineJavaLangAccess();
		Time.runTask(0, () -> {
			try {
				Class.forName("ihope_lib.MyReflect", false, Vars.mods.mainLoader());
			} catch (ClassNotFoundException e) {
				try {
					definePlatformClassLoader();
				} catch (Throwable x) {
					Log.err(x);
				}
			}
		});
		/*MyClass<?> javaLangClass = new MyClass<>(Private.class.getName() + "$qwq", Private.class);
		javaLangClass.setFunc("<init>", null, PUBLIC, Void.TYPE, Private.class.getDeclaredConstructors()[0].getParameterTypes());

		var cls = javaLangClass.define();
		// unsafe.putObject(cls, unsafe.staticFieldOffset(cls.getDeclaredField("instance")), ao);
		Log.info(cls.getDeclaredConstructors()[0].newInstance());*/
	}

	private static final String interfaceName = "I_HOPE_MAGIC_INTERFACE";

	/**
	 * 定义一个支持接口类<br>
	 * 可以随意设置override<br>
	 * 可以获取方法的MethodAccessor<br>
	 **/
	public static AO_MyInterface defineInterfaceClass() throws Throwable {
		MyClass<?> javaLangClass = new MyClass<>(interfaceName, MAGICIMPL.class);
		javaLangClass.addInterface(AO_MyInterface.class);
		javaLangClass.setFuncSelf("setOverride", cfw -> {
			cfw.addALoad(1);
			cfw.addPush(true);
			cfw.add(ByteCode.PUTFIELD, nativeName(AccessibleObject.class), "override", "Z");
			cfw.add(ByteCode.RETURN);
			return 2;
		}, PUBLIC, void.class, AccessibleObject.class);
		javaLangClass.setFuncSelf("getMethodAccessor", cfw -> {
			// cfw.addInvoke(ByteCode.INVOKESTATIC, nativeName(Tools.class), "a", "()V");
			cfw.addALoad(1);
			cfw.add(ByteCode.GETFIELD, "java/lang/reflect/Method", "methodAccessor", typeToNative(MethodAccessor.class));
			cfw.add(ByteCode.ARETURN);
			return 2;
		}, PUBLIC, MethodAccessor.class, Method.class);
		javaLangClass.setField(PUBLIC | STATIC | FINAL, AO_MyInterface.class, "instance");

		byte[]         bytes = javaLangClass.writer.toByteArray();
		Class<?>       cls   = MyReflect.defineClass(null, bytes);
		AO_MyInterface ao    = (AO_MyInterface) unsafe.allocateInstance(cls);
		unsafe.putObject(cls, unsafe.staticFieldOffset(cls.getDeclaredField("instance")), ao);

		return ao;
		// return (SetOverride) unsafe.allocateInstance(javaLangClass.define());
	}

	/**
	 * 创建一个自己的反射工厂
	 **/
	public static void defineReflectionFactory() throws Throwable {
		Class<?> factory = Class.forName("jdk.internal.reflect.ReflectionFactory");
		// TestMagic.init();

		MyClass<?> myFactoryClass = new MyClass<>(factory.getName() + "AOA_PAPA", factory);
		myFactoryClass.setFuncSelf("copyMethod", cfw -> {
			cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", typeToNative(AO_MyInterface.class));
			cfw.addALoad(1);
			cfw.addInvoke(ByteCode.INVOKEINTERFACE, nativeName(AO_MyInterface.class), "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
			cfw.addALoad(1);
			cfw.add(ByteCode.ARETURN);
			return 2;
		}, PUBLIC, Method.class, Method.class);
		myFactoryClass.setFuncSelf("copyField", cfw -> {
			cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", typeToNative(AO_MyInterface.class));
			cfw.addALoad(1);
			cfw.addInvoke(ByteCode.INVOKEINTERFACE, nativeName(AO_MyInterface.class), "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
			cfw.addALoad(1);
			cfw.add(ByteCode.ARETURN);
			return 2;
		}, PUBLIC, Field.class, Field.class);
		myFactoryClass.setFuncSelf("copyConstructor", cfw -> {
			cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", typeToNative(AO_MyInterface.class));
			cfw.addALoad(1);
			cfw.addInvoke(ByteCode.INVOKEINTERFACE, nativeName(AO_MyInterface.class), "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
			cfw.addALoad(1);
			cfw.add(ByteCode.ARETURN);
			return 2;
		}, PUBLIC, Constructor.class, Constructor.class);
		myFactoryClass.setFunc("<init>", null, PUBLIC, void.class);

		// SetMethod.init(myFactoryClass);
		SetField.init(myFactoryClass);

		Object ins = unsafe.allocateInstance(myFactoryClass.define());

		Tools.clone(ReflectionFactory.getReflectionFactory(),
		            ins, factory);
		unsafe.putObject(AccessibleObject.class, JDKVars.unsafe
				.objectFieldOffset(AccessibleObject.class, "reflectionFactory"), ins);
		unsafe.putObject(factory, JDKVars.unsafe
				.objectFieldOffset(factory, "soleInstance"), ins);
		unsafe.putObject(Class.class, JDKVars.unsafe
				.objectFieldOffset(Class.class, "reflectionFactory"), ins);
	}


	/*private static final ObjectMap<String, Class<?>> nameClassMap = ObjectMap.of(
			"better_js.Desktop", Desktop.class,
			"better_js.utils.MyReflect", MyReflect.class
	);*/

	public static ObjectMap<String, Object> hashMap = new ObjectMap<>();

	static {
		hashMap.put("mindustry.core.Platform$1", null);
		hashMap.put("mindustry.mod.ModClassLoader", null);
	}

	/**
	 * 创建一个自己的AppClassLoader
	 **/
	public static void definePlatformClassLoader() throws Throwable {
		ClassLoader platformLoader = ClassLoaders.platformClassLoader();
		Class<?>    superClass     = platformLoader.getClass();

		MyClass<?>  myFactoryClass = new MyClass<>(superClass.getName() + "AOA_PAPA", superClass);
		boolean[]   inChild        = {false};
		ClassLoader loader         = Vars.mods.mainLoader();
		myFactoryClass.setFunc("loadClassOrNull", (self, args) -> {
			Class<?> c = (Class<?>) args.get(args.size() - 1);
			if (c != null) return c;
			if (inChild[0]) return null;
			// try {
			// 	Time.mark();
			for (var trace : Thread.currentThread().getStackTrace()) {
				if (hashMap.containsKey(trace.getClassName())) return null;
			}
			// } finally {
			// 	Log.info(Time.elapsed());
			// }
			String name = (String) args.get(0);
			if (name.startsWith("lll")) {
				name = name.substring(3, name.length() - 1);
			}
			try {
				inChild[0] = true;
				return loader.loadClass(name);
			} catch (ClassNotFoundException e) {
				return null;
			} finally {
				inChild[0] = false;
			}
		}, PUBLIC, true, Class.class, String.class, boolean.class);
		myFactoryClass.setFunc("<init>", null, PUBLIC, Void.TYPE,
		                       superClass.getDeclaredConstructors()[0].getParameterTypes());
		// myFactoryClass.writeTo(new Fi("F:/classes/"));

		long off = JDKVars.unsafe.objectFieldOffset(
				Class.forName("jdk.internal.loader.BuiltinClassLoader"), "parent"
		);
		Object ins = myFactoryClass.define().getDeclaredConstructors()[0].newInstance(unsafe.getObject(platformLoader, off));
		unsafe.putObject(ClassLoaders.platformClassLoader(), off, ins);
		try {
			Log.info("start");
			Log.info(new ClassLoader() {}.loadClass("llljava.lang.Classk"));
		} finally {
			// unsafe.park(false, Long.MAX_VALUE);
		}
		/*unsafe.putObject(cloader, JDKVars.unsafe
				.objectFieldOffset(cloader, "APP_LOADER"), ins);
		unsafe.putObject(ClassLoader.class, JDKVars.unsafe
				.objectFieldOffset(ClassLoader.class, "scl"), ins);*/

		// Log.info(Class.forName("better_js.Desktop", false, ClassLoader.getSystemClassLoader()));
	}

	/**
	 * 创建一个自己的JavaLangAccess
	 **/
	public static void defineJavaLangAccess() throws Throwable {
		Class<?> secrets = Tools.orThrow(() -> Class.forName("jdk.internal.access.SharedSecrets"),
		                                 () -> Class.forName("jdk.internal.misc.SharedSecrets"));
		assert secrets != null;
		Object superAccess = unsafe.getObject(secrets, JDKVars.unsafe
				.objectFieldOffset(secrets, "javaLangAccess"));
		Class<?> superClass = superAccess.getClass();
		// TestMagic.init();

		MyClass<?> myFactoryClass = new MyClass<>(superClass.getName() + "AOA_PAPA", superClass);
		myFactoryClass.setField(PUBLIC | STATIC | FINAL, ClassLoader.class, "loader", Vars.mods.mainLoader());
		ModClassLoader loader = (ModClassLoader) Vars.mods.mainLoader();
		// ThreadLocal<Boolean> inChild = (ThreadLocal<Boolean>) unsafe.getObject(loader, JDKVars.unsafe.objectFieldOffset(ModClassLoader.class, "inChild"));
		// Class<?> cl = Desktop.class;
		myFactoryClass.setFunc("findBootstrapClassOrNull", (self, args) -> {
			Class<?> c = (Class<?>) args.get(args.size() - 1);
			if (c != null) return c;
			// if (Thread.currentThread().getStackTrace().length > lastLength[0]) return null;
			String name = (String) args.get(1);
			if (name == null) return null;
			// Log.info(name);
			if (name.equals("better_js.Desktop")) return Desktop.class;
			return null;
		}, PUBLIC, true, Class.class, ClassLoader.class, String.class);
		myFactoryClass.setFunc("<init>", null, PUBLIC, Void.TYPE);
		// myFactoryClass.writeTo(new Fi("F:/" + myFactoryClass.adapterName + ".class"));

		Object ins = myFactoryClass.define().getDeclaredConstructor().newInstance();

		// Tools.clone(ReflectionFactory.getReflectionFactory(),
		// 		ins, factory);
		unsafe.putObject(secrets, JDKVars.unsafe
				.objectFieldOffset(secrets, "javaLangAccess"), ins);
		Class<?> loaders = Class.forName("jdk.internal.loader.ClassLoaders");
		unsafe.putObject(loaders, JDKVars.unsafe
				.objectFieldOffset(loaders, "JLA"), ins);
	}


	public static void clearReflectionFilter() throws Throwable {
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

	@Deprecated
	public static final class MyMethodAccessor implements MethodAccessor {
		// public static final Lookup lookup = Test.lookup;
		private final MethodHandle handle;
		private final FuncInvoke   func;

		public MyMethodAccessor(Method method) {
			boolean isStatic = Modifier.isStatic(method.getModifiers());
			int     len      = method.getParameterCount();
			try {
				handle = lookup.unreflect(method);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			func = getFunc(isStatic, len, handle);
		}

		private static FuncInvoke getFunc(boolean isStatic, int len, final MethodHandle handle) {
			if (isStatic) {
				switch (len) {
					case 0:
						return (obj, args) -> handle.invoke();
					case 1:
						return (obj, args) -> handle.invoke(args[0]);
					case 2:
						return (obj, args) -> handle.invoke(args[0], args[1]);
					case 3:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2]);
					case 4:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3]);
					case 5:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4]);
					case 6:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5]);
					case 7:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
					case 8:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
					case 9:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
					case 10:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
					case 11:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
					case 12:
						return (obj, args) -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
					default:
						throw new IllegalArgumentException("" + len);
				}
			}
			switch (len) {
				case 0:
					return (obj, args) -> handle.invoke(obj);
				case 1:
					return (obj, args) -> handle.invoke(obj, args[0]);
				case 2:
					return (obj, args) -> handle.invoke(obj, args[0], args[1]);
				case 3:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2]);
				case 4:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3]);
				case 5:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4]);
				case 6:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5]);
				case 7:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
				case 8:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
				case 9:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
				case 10:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
				case 11:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
				case 12:
					return (obj, args) -> handle.invoke(obj, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
				default:
					throw new IllegalArgumentException("" + len);
			}
		}

		@Override
		public Object invoke(Object obj, Object[] args) {
			try {
				return func.invoke(obj, args);
				// return (Integer) Test.handle.invokeExact((Integer) args[0]);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	private interface FuncInvoke {
		Object invoke(Object obj, Object[] args) throws Throwable;
	}
}