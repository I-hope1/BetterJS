package better_js;

import apzmagic.MAGICIMPL;
import better_js.utils.*;
import interfaces.AO_MyInterface;
import jdk.internal.loader.*;
import jdk.internal.module.Modules;
import jdk.internal.reflect.*;
import rhino.classfile.ByteCode;
import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.*;

import static better_js.reflect.JDKVars.junsafe;
import static better_js.utils.A_ASM.*;
import static java.lang.reflect.Modifier.*;

/**
 * only for window
 **/
public class Desktop {
    public static final Unsafe unsafe = HopeVars.unsafe;
    public static final Lookup lookup;
    private static ClassLoader loader;

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

    public static void main() throws Throwable {
        main(null);
    }

    public static void main(String[] ___) throws Throwable {
        init(Desktop.class.getClassLoader());
    }

    public static void init(ClassLoader loader) throws Throwable {
        if (Desktop.loader != null) throw new RuntimeException("Can't init twice.");
        Desktop.loader = loader;
        clearReflectionFilter();
        Field moduleSetter = Class.class.getDeclaredField("module");
        unsafe.putObject(Desktop.class, unsafe.objectFieldOffset(moduleSetter), Object.class.getModule());

        /** open一些模块  */
        Module java_base = Object.class.getModule();
        Module everyone = (Module) unsafe.getObject(Module.class, unsafe.staticFieldOffset(Module.class.getDeclaredField("EVERYONE_MODULE")));
        lookup.findVirtual(Module.class, "implAddOpens", MethodType.methodType(void.class, String.class))
                .invokeExact(java_base, "jdk.internal.module");
        Modules.addOpens(java_base, "jdk.internal.module", everyone);
        Modules.addOpens(java_base, "jdk.internal.misc", everyone);
        Modules.addOpens(java_base, "jdk.internal.reflect", everyone);
        Modules.addOpens(java_base, "jdk.internal.loader", everyone);
        // Modules.addOpens(java_base, "jdk.internal.misc", everyone);
        Modules.addOpens(java_base, "jdk.internal.platform", everyone);
        try {
            Modules.addOpens(java_base, "java.lang.invoke", everyone);
            Modules.addOpens(java_base, "jdk.internal.access", everyone);
        } catch (Throwable ignored) {
        }

        lookup.findStatic(Module.class, "addReads0", MethodType.methodType(void.class, Module.class, Module.class))
                .invokeExact(java_base, everyone);
    }

    public static void init2() throws Throwable {
        byte[] bytes;
        // Func2
		/* bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 12, 7, 0, 2, 1, 0, 27, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 49, 47, 70, 117, 110, 99, 50, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 3, 103, 101, 116, 1, 0, 56, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 9, 83, 105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 13, 40, 84, 80, 49, 59, 84, 80, 50, 59, 41, 84, 82, 59, 1, 0, 82, 60, 80, 49, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 80, 50, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 82, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 62, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 10, 70, 117, 110, 99, 50, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 4, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 2, 0, 8, 0, 2, 0, 7, 0, 0, 0, 2, 0, 9, 0, 10, 0, 0, 0, 2, 0, 11};
		MyReflect.defineClass(null, bytes); */
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
        // generateRedefineClass();

        // defineBootLoader();
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
            cfw.add(ByteCode.PUTFIELD, dot2slash(AccessibleObject.class), "override", "Z");
            cfw.add(ByteCode.RETURN);
            return 2;
        }, PUBLIC, void.class, AccessibleObject.class);
        javaLangClass.setFuncSelf("getMethodAccessor", cfw -> {
            // cfw.addInvoke(ByteCode.INVOKESTATIC, nativeName(Tools.class), "a", "()V");
            cfw.addALoad(1);
            cfw.add(ByteCode.GETFIELD, "java/lang/reflect/Method", "methodAccessor", LtypeName(MethodAccessor.class));
            cfw.add(ByteCode.ARETURN);
            return 2;
        }, PUBLIC, MethodAccessor.class, Method.class);
        javaLangClass.setField(PUBLIC | STATIC | FINAL, AO_MyInterface.class, "instance");

        byte[] bytes = javaLangClass.writer.toByteArray();
        Class<?> cls = MyReflect.defineClass(null, bytes);
        AO_MyInterface ao = (AO_MyInterface) unsafe.allocateInstance(cls);
        unsafe.putObject(cls, junsafe.objectFieldOffset(cls, "instance"), ao);

        return ao;
        // return (SetOverride) unsafe.allocateInstance(javaLangClass.define());
    }

    /**
     * 创建一个自己的反射工厂
     **/
    public static void defineReflectionFactory() throws Throwable {
        Class<?> factory = Class.forName("jdk.internal.reflect.ReflectionFactory");
        // TestMagic.init();

        final String MY_INTERFACE_CLASS = dot2slash(AO_MyInterface.class);
        MyClass<?> myFactoryClass = new MyClass<>(factory.getName() + "AOA_PAPA", factory);
        myFactoryClass.setFuncSelf("copyMethod", cfw -> {
            cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", LtypeName(AO_MyInterface.class));
            cfw.addALoad(1);
            cfw.addInvoke(ByteCode.INVOKEINTERFACE, MY_INTERFACE_CLASS, "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
            cfw.addALoad(1);
            cfw.add(ByteCode.ARETURN);
            return 2;
        }, PUBLIC, Method.class, Method.class);
        myFactoryClass.setFuncSelf("copyField", cfw -> {
            cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", LtypeName(AO_MyInterface.class));
            cfw.addALoad(1);
            cfw.addInvoke(ByteCode.INVOKEINTERFACE, MY_INTERFACE_CLASS, "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
            cfw.addALoad(1);
            cfw.add(ByteCode.ARETURN);
            return 2;
        }, PUBLIC, Field.class, Field.class);
        myFactoryClass.setFuncSelf("copyConstructor", cfw -> {
            cfw.add(ByteCode.GETSTATIC, interfaceName, "instance", LtypeName(AO_MyInterface.class));
            cfw.addALoad(1);
            cfw.addInvoke(ByteCode.INVOKEINTERFACE, MY_INTERFACE_CLASS, "setOverride", "(Ljava/lang/reflect/AccessibleObject;)V");
            cfw.addALoad(1);
            cfw.add(ByteCode.ARETURN);
            return 2;
        }, PUBLIC, Constructor.class, Constructor.class);
        myFactoryClass.setFunc("<init>", null, PUBLIC, void.class);

        // SetMethod.init(myFactoryClass);
        // SetField.init(myFactoryClass);

        Object ins = unsafe.allocateInstance(myFactoryClass.define());

        // ins.getClass().getDeclaredFields();
        Tools.clone(ReflectionFactory.getReflectionFactory(),
                ins, factory);
        unsafe.putObject(AccessibleObject.class, junsafe
                .objectFieldOffset(AccessibleObject.class, "reflectionFactory"), ins);
        unsafe.putObject(factory, junsafe
                .objectFieldOffset(factory, "soleInstance"), ins);
        unsafe.putObject(Class.class, junsafe
                .objectFieldOffset(Class.class, "reflectionFactory"), ins);
    }

	/*private static final ObjectMap<String, Class<?>> nameClassMap = ObjectMap.of(
			"better_js.Desktop", Desktop.class,
			"better_js.utils.MyReflect", MyReflect.class
	);*/

    public static Set<String> hashSet = new HashSet<>();

    static {
        hashSet.add("mindustry.core.Platform$1");
        hashSet.add("mindustry.mod.ModClassLoader");
    }

    /**
     * 创建一个自己的AppClassLoader
     **/
    public static void definePlatformClassLoader() throws Throwable {
        ClassLoader platformLoader = ClassLoaders.platformClassLoader();
        Class<?> superClass = platformLoader.getClass();

        MyClass<?> myFactoryClass = new MyClass<>(superClass.getName() + "AOA_PAPA", superClass);
        boolean[] inChild = {false};
        myFactoryClass.setFunc("loadClassOrNull", (self, args) -> {
            Class<?> c = (Class<?>) args.get(args.size() - 1);
            if (c != null) return c;
            if (inChild[0]) return null;
            // try {
            // 	Time.mark();
            for (var trace : Thread.currentThread().getStackTrace()) {
                if (hashSet.contains(trace.getClassName())) return null;
            }
            // } finally {
            // 	Log.info(Time.elapsed());
            // }
            String name = (String) args.get(0);
			/* if (name.startsWith("libgdx")) {
				name = "arc" + name.substring(6);
			} */
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

        long off = junsafe.objectFieldOffset(
                Class.forName("jdk.internal.loader.BuiltinClassLoader"), "parent"
        );
        Object ins = myFactoryClass.define().getDeclaredConstructors()[0].newInstance(unsafe.getObject(platformLoader, off));
        unsafe.putObject(ClassLoaders.platformClassLoader(), off, ins);
    }

    /**
     * 创建一个自己的JavaLangAccess
     **/
    public static void defineJavaLangAccess() throws Throwable {
        Class<?> secrets = Tools.orThrow(() -> Class.forName("jdk.internal.access.SharedSecrets"),
                () -> Class.forName("jdk.internal.misc.SharedSecrets"));
        assert secrets != null;
        Object superAccess = unsafe.getObject(secrets, junsafe
                .objectFieldOffset(secrets, "javaLangAccess"));
        Class<?> superClass = superAccess.getClass();
        // TestMagic.init();

        MyClass<?> myFactoryClass = new MyClass<>(superClass.getName() + "AOA_PAPA", superClass);
        myFactoryClass.setField(PUBLIC | STATIC | FINAL, ClassLoader.class, "loader", loader);
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
        unsafe.putObject(secrets, junsafe
                .objectFieldOffset(secrets, "javaLangAccess"), ins);
        unsafe.putObject(ClassLoaders.class, junsafe
                .objectFieldOffset(ClassLoaders.class, "JLA"), ins);
        unsafe.putObject(BootLoader.class, junsafe
                .objectFieldOffset(BootLoader.class, "JLA"), ins);
    }

    public static void defineBootLoader() throws Throwable {
        Field f = ClassLoaders.class.getDeclaredField("BOOT_LOADER");

        ClassLoader bootLoader = (ClassLoader) f.get(null);
        Class<?> superClass = bootLoader.getClass();

        MyClass<?> myFactoryClass = new MyClass<>(superClass.getName() + "_i_hope", superClass);
        boolean[] inChild = {false};
        myFactoryClass.setFunc("loadClassOrNull", (self, args) -> {
            String name = (String) args.get(0);
            try {
                inChild[0] = true;
                return bootLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            } finally {
                inChild[0] = false;
            }
        }, PUBLIC, true, Class.class, String.class, boolean.class);
        myFactoryClass.setFunc("<init>", null, PUBLIC, Void.TYPE,
                superClass.getDeclaredConstructors()[0].getParameterTypes());
        // myFactoryClass.writeTo(new Fi("F:/classes/"));

        long off = junsafe.objectFieldOffset(
                Class.forName("jdk.internal.loader.BuiltinClassLoader"), "ucp"
        );
        Object ins = myFactoryClass.define().getDeclaredConstructors()[0].newInstance(unsafe.getObject(bootLoader, off));

        unsafe.putObject(ClassLoaders.class, junsafe.objectFieldOffset(
                ClassLoaders.class, "BOOT_LOADER"), ins);
    }

	/* public static void generateRedefineClass() {
		long closedOff = junsafe.objectFieldOffset(URLClassPath.class, "closed");
		ScriptInstaller.redefineClass = (loader, host, names, clinit) -> {
			URLClassPath appUcp = Reflect.get(BuiltinClassLoader.class, loader, "ucp");
			URLClassPath modUcp = Reflect.get(URLClassLoader.class, host, "ucp");
			unsafe.putBoolean(appUcp, closedOff, true);
			Seq<byte[]> bytes = names.map(name -> {
				try {
					return host.getResourceAsStream(name.replace('.', '/') + ".class").readAllBytes();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			// unsafe.putBoolean(appUcp, closedOff, true);
			bytes.each(b -> MyReflect.defineClass(loader, b));
			unsafe.putBoolean(appUcp, closedOff, true);
			unsafe.putBoolean(modUcp, closedOff, true);
			names.each(name -> {
				try {
					host.loadClass(name);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			});
			// loader.loadClass(name);
			// mods.mainLoader().loadClass(name);
			// Class.forName(name, true, loader);
			unsafe.putBoolean(appUcp, closedOff, false);
			// host.loadClass(name);
			names.each(name -> {
				try {
					host.loadClass(name);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			});
			if (clinit != null) clinit.run();
			// Log.info(NativeJavaObject.class == NativeJavaClass.class.getSuperclass());
			// Log.info(Class.forName(name, true, loader) == NativeJavaObject.class);
			unsafe.putBoolean(modUcp, closedOff, false);
		};
	} */

    public static void clearReflectionFilter() throws Throwable {
        ((Map) lookup.findStaticGetter(reflect, "fieldFilterMap", Map.class)
                .invokeExact()).clear();
        ((Map) lookup.findStaticGetter(reflect, "methodFilterMap", Map.class)
                .invokeExact()).clear();
    }


	private interface FuncInvoke {
        Object invoke(Object obj, Object[] args) throws Throwable;
    }
}