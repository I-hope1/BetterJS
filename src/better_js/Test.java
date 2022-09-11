package better_js;

import apzmagic.MAGICIMPL;
import arc.files.Fi;
import arc.util.Time;
import better_js.reflect.*;
import better_js.utils.*;
import hope_android.FieldUtils;
import interfaces.*;
import jdk.internal.reflect.*;
import jdk.internal.reflect.ConstantPool.Tag;
import jdk.internal.reflect.MethodAccessor;
import jdk.internal.reflect1.Func2;
import jdk.internal.vm.annotation.ForceInline;
import mindustry.mod.Mod;
import rhino.classfile.*;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.*;

import static better_js.utils.ByteCodeTools.*;

public class Test {
	public static final Unsafe unsafe;
	public static final Lookup lookup;
	public static final MethodHandle mh;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);
			/*Constructor<?> cons = Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
			Lookup _lookup = (Lookup) ReflectionFactory.getReflectionFactory().newConstructorForSerialization(
					Lookup.class, cons
			).newInstance(Lookup.class, null, -1);
			_lookup = (Lookup) _lookup.findStaticGetter(Lookup.class, "IMPL_LOOKUP", Lookup.class).invoke();
			lookup = _lookup;*/
			lookup = (Lookup) unsafe.getObject(Lookup.class, unsafe.staticFieldOffset(Lookup.class.getDeclaredField("IMPL_LOOKUP")));
			mh = lookup.unreflectSetter(Test2.class.getDeclaredField("IAAA"));
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

	public static void main(String[] args) throws Throwable {
		((Map) lookup.findStaticGetter(reflect, "fieldFilterMap", Map.class)
				.invokeExact()).clear();
		((Map) lookup.findStaticGetter(reflect, "methodFilterMap", Map.class)
				.invokeExact()).clear();
		Field moduleSetter = Class.class.getDeclaredField("module");
		unsafe.putObject(Test.class, unsafe.objectFieldOffset(moduleSetter), Object.class.getModule());
		Method m = Module.class.getDeclaredMethod("implAddOpens", String.class);
		m.setAccessible(true);
		Module java_base = Object.class.getModule();
		m.invoke(java_base, "jdk.internal.reflect");
		m.invoke(java_base, "jdk.internal.misc");
		m.invoke(java_base, "jdk.internal.platform");
		/*m = Module.class.getDeclaredMethod("implAddExports", String.class);
		m.setAccessible(true);
		m.invoke(java_base, "jdk.internal.reflect");
		m.invoke(java_base, "jdk.internal.misc");
		m.invoke(java_base, "jdk.internal.access");*/
		Method addReads = Module.class.getDeclaredMethod("addReads0", Module.class, Module.class);
		addReads.setAccessible(true);
		addReads.invoke(null, reflect.getModule(), null);

		// Method method2 = Test2.class.getDeclaredMethod("aaa");

		// MyReflect.defineClass(null, new FileInputStream("libs/ByteCodeTools.class").readAllBytes());
		// print(Arrays.toString(new FileInputStream("OOAOAtmp/MyJavaMembers.class").readAllBytes()));
		// prov: R get()
		byte[] bytes = {-54, -2, -70, -66, 0, 0, 0, 55, 0, 12, 7, 0, 2, 1, 0, 26, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 49, 47, 80, 114, 111, 118, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 3, 103, 101, 116, 1, 0, 20, 40, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 9, 83, 105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 5, 40, 41, 84, 84, 59, 1, 0, 40, 60, 84, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 62, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 9, 80, 114, 111, 118, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 4, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 2, 0, 8, 0, 2, 0, 7, 0, 0, 0, 2, 0, 9, 0, 10, 0, 0, 0, 2, 0, 11};
		MyReflect.defineClass(null, bytes);
		// cons2: void get(P1, P2)
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 12, 7, 0, 2, 1, 0, 27, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 49, 47, 67, 111, 110, 115, 50, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 3, 103, 101, 116, 1, 0, 39, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 41, 86, 1, 0, 9, 83, 105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 9, 40, 84, 84, 59, 84, 78, 59, 41, 86, 1, 0, 60, 60, 84, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 78, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 62, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 10, 67, 111, 110, 115, 50, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 4, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 2, 0, 8, 0, 2, 0, 7, 0, 0, 0, 2, 0, 9, 0, 10, 0, 0, 0, 2, 0, 11};
		MyReflect.defineClass(null, bytes);
		// func2: R get(P1, P2)
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 12, 7, 0, 2, 1, 0, 27, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 49, 47, 70, 117, 110, 99, 50, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 3, 103, 101, 116, 1, 0, 56, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 9, 83, 105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 13, 40, 84, 80, 49, 59, 84, 80, 50, 59, 41, 84, 82, 59, 1, 0, 82, 60, 80, 49, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 80, 50, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 82, 58, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 62, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 10, 70, 117, 110, 99, 50, 46, 106, 97, 118, 97, 6, 1, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 4, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 2, 0, 8, 0, 2, 0, 7, 0, 0, 0, 2, 0, 9, 0, 10, 0, 0, 0, 2, 0, 11};
		MyReflect.defineClass(null, bytes);
		// MagicPublic
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 1, 0, 45, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 95, 80, 85, 66, 76, 73, 67, 7, 0, 1, 1, 0, 38, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 7, 0, 3, 1, 0, 13, 95, 95, 66, 89, 84, 69, 95, 67, 108, 97, 115, 115, 48, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 12, 0, 6, 0, 7, 10, 0, 4, 0, 8, 1, 0, 4, 67, 111, 100, 101, 1, 0, 13, 83, 116, 97, 99, 107, 77, 97, 112, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 0, 1, 0, 2, 0, 4, 0, 0, 0, 0, 0, 1, 0, 1, 0, 6, 0, 7, 0, 1, 0, 10, 0, 0, 0, 25, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 1, 0, 11, 0, 0, 0, 2, 0, 0, 0, 1, 0, 12, 0, 0, 0, 2, 0, 5};
		MyReflect.defineClass(null, bytes);
		// MAGICIMPL
		bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 1, 0, 18, 97, 112, 122, 109, 97, 103, 105, 99, 47, 77, 65, 71, 73, 67, 73, 77, 80, 76, 7, 0, 1, 1, 0, 45, 106, 100, 107, 47, 105, 110, 116, 101, 114, 110, 97, 108, 47, 114, 101, 102, 108, 101, 99, 116, 47, 77, 97, 103, 105, 99, 65, 99, 99, 101, 115, 115, 111, 114, 73, 109, 112, 108, 95, 80, 85, 66, 76, 73, 67, 7, 0, 3, 1, 0, 13, 95, 95, 66, 89, 84, 69, 95, 67, 108, 97, 115, 115, 48, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 12, 0, 6, 0, 7, 10, 0, 4, 0, 8, 1, 0, 4, 67, 111, 100, 101, 1, 0, 13, 83, 116, 97, 99, 107, 77, 97, 112, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 0, 1, 0, 2, 0, 4, 0, 0, 0, 0, 0, 1, 0, 1, 0, 6, 0, 7, 0, 1, 0, 10, 0, 0, 0, 25, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 1, 0, 11, 0, 0, 0, 2, 0, 0, 0, 1, 0, 12, 0, 0, 0, 2, 0, 5};
		MyReflect.defineClass(null, bytes);


		/*var tmp = new MyClass<>("hope_android/FieldUtils", Object.class);
		tmp.setFunc("getFieldOffset", cfw -> {
			cfw.addALoad(0);
			cfw.addInvoke(ByteCode.INVOKEVIRTUAL, nativeName(Field.class), "getOffset", "()I");
			cfw.add(ByteCode.IRETURN);
			return 2;
		}, Modifier.PUBLIC | Modifier.STATIC, int.class, Field.class);
		FileOutputStream outputStream = new FileOutputStream("OOAOAtmp/FieldUtils.class");
		outputStream.write(tmp.writer.toByteArray());
		outputStream.close();*/
		// print(unsafe.allocateInstance(tmp.define()));

		// setOverride
		/*bytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 55, 0, 9, 7, 0, 7, 7, 0, 8, 1, 0, 11, 115, 101, 116, 79, 118, 101, 114, 114, 105, 100, 101, 1, 0, 39, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 114, 101, 102, 108, 101, 99, 116, 47, 65, 99, 99, 101, 115, 115, 105, 98, 108, 101, 79, 98, 106, 101, 99, 116, 59, 41, 86, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 16, 83, 101, 116, 79, 118, 101, 114, 114, 105, 100, 101, 46, 106, 97, 118, 97, 1, 0, 22, 105, 110, 116, 101, 114, 102, 97, 99, 101, 115, 47, 83, 101, 116, 79, 118, 101, 114, 114, 105, 100, 101, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 6, 1, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 4, 1, 0, 3, 0, 4, 0, 0, 0, 1, 0, 5, 0, 0, 0, 2, 0, 6};
		MyReflect.defineClass(null, bytes);*/

		var interfaceObj = defineInterfaceClass();
		defineReflectionFactory(interfaceObj);

		// TestMagic.init();
		// print(TestMagic.MagicAccessorImpl.getProtectionDomain());
		/*Method method = Test2.class.getDeclaredMethod("aaa");
		MethodAccessor acc = ReflectionFactory.getReflectionFactory().newMethodAccessor(method);
		Object[] arr = new Object[]{};
		Test2 test2 = new Test2();
		Time.mark();
		for (int i = 0; i < 1E7; i++) {
			method.invoke(test2, arr);
			// acc.invoke(test2, arr);
			// test2.aaa();
		}
		print(Time.elapsed());*/

		/*lookup.findSetter(Module.class, "name", String.class)
				.invoke(Object.class.getModule(), null);*/

		// TestMagic.init();

		/*Method method = Test2.class.getDeclaredMethod("aaa");
		MethodAccessor mac = ReflectionFactory.getReflectionFactory().newMethodAccessor(method);
		Test2 test2 = new Test2();
		int pre_times = 1_0000;
		final Object[] EMPTY = {};
		for (int i = 0; i < pre_times; i++) {
			mh.invoke(test2);
			// test2.aaa();
			// mac.invoke(test2, EMPTY);
		}
		int times = 1_0000_0000;
		long lastTime = System.nanoTime();
		for (int i = 0; i < times; i++) {
			// mac.invoke(test2, EMPTY);
			// test2.aaa();
			mh.invoke(test2);
		}
		print((System.nanoTime() - lastTime) / (float) times);*/
		/*lastTime = System.nanoTime();
		for (int i = 0; i < times; i++) {
			mac.invoke(test2, EMPTY);
			// test2.aaa();
			// mh.invoke(test2);
		}
		print((System.nanoTime() - lastTime) / (float) times);*/
		/*final Field field = Test2.class.getDeclaredField("iaaa");
		field.setAccessible(true);
		Test2 test2 = new Test2();
		int pre_times = 10_0000;
		for (int i = 0; i < pre_times; i++) {
			field.setInt(test2, i);
		}
		int times = 100_0000;
		long lastTime = System.nanoTime();
		for (int i = 0; i < times; i++) {
			field.setInt(test2, i);
		}
		print((System.nanoTime() - lastTime) / (float) times);*/

	}

	/**
	 * 定义一个支持接口类
	 * 可以随意设置override
	 * 可以获取方法的MethodAccessor
	 **/
	public static AO_MyInterface defineInterfaceClass() throws Throwable {
		/*MyClass<?> emptyC = new MyClass<>(TestMagic.MagicAccessorImpl.getName() + "_PUBLIC", TestMagic.MagicAccessorImpl);
		emptyC.writer.setFlags((short) Modifier.PUBLIC);
		emptyC.setFunc("<init>", (Func2) null, Modifier.PUBLIC, void.class);
		print(Arrays.toString(emptyC.writer.toByteArray()));
		try {
			// FileOutputStream outputStream = new FileOutputStream("OOAOAtmp/apzmagic.MAGICIMPL.class");
			FileOutputStream outputStream = new FileOutputStream("OOAOAtmp/MagicAccessorImpl_PUBLIC.class");
			outputStream.write(emptyC.writer.toByteArray());
			outputStream.close();
		} catch (Exception e) {
			print(e);
		}*/
		/*MyClass<?> emptyC = new MyClass<>("apzmagic.MAGICIMPL", Class.forName("jdk.internal.reflect.MagicAccessorImpl_PUBLIC"));
		emptyC.writer.setFlags((short) Modifier.PUBLIC);
		emptyC.setFunc("<init>", (Func2) null, Modifier.PUBLIC, void.class);
		print(Arrays.toString(emptyC.writer.toByteArray()));
		try {
			// FileOutputStream outputStream = new FileOutputStream("OOAOAtmp/apzmagic.MAGICIMPL.class");
			FileOutputStream outputStream = new FileOutputStream("OOAOAtmp/MAGICIMPL.class");
			outputStream.write(emptyC.writer.toByteArray());
			outputStream.close();
		} catch (Exception e) {
			print(e);
		}*/

		MyClass<?> javaLangClass = new MyClass<>("I_HOPE_SetOverride", MAGICIMPL.class);
		javaLangClass.addInterface(AO_MyInterface.class);
		javaLangClass.setFunc("setOverride", cfw -> {
			cfw.addALoad(1);
			cfw.addPush(true);
			cfw.add(ByteCode.PUTFIELD, nativeName(AccessibleObject.class), "override", typeToNative(boolean.class));
			cfw.add(ByteCode.RETURN);
			return 2;
		}, Modifier.PUBLIC, void.class, AccessibleObject.class);

		// return (SetOverride) unsafe.allocateInstance(javaLangClass.define());
		byte[] bytes = javaLangClass.writer.toByteArray();
		return (AO_MyInterface) unsafe.allocateInstance(JDKVars.unsafe.defineClass0(null, bytes, 0, bytes.length, MyReflect.IMPL_LOADER, null));
	}

	/**
	 * 创建一个自己的反射工厂
	 **/
	public static void defineReflectionFactory(AO_MyInterface override) throws Throwable {
		Class<?> factory = Class.forName("jdk.internal.reflect.ReflectionFactory");
		// TestMagic.init();

		MyClass<?> myFactoryClass = new MyClass<>(factory.getName() + "AOA_PAPA", factory);
		myFactoryClass.setFunc("copyMethod", (self, _args) -> {
			Method method = (Method) _args.get(0);
			override.setOverride(method);
			return method;
		}, Modifier.PUBLIC, Method.class, Method.class);
		myFactoryClass.setFunc("copyField", (self, _args) -> {
			Field field = (Field) _args.get(0);
			// field.setAccessible(true);
			override.setOverride(field);
			return field;
		}, Modifier.PUBLIC, Field.class, Field.class);
		myFactoryClass.setFunc("copyConstructor", (self, _args) -> {
			Constructor<?> cons = (Constructor<?>) _args.get(0);
			// field.setAccessible(true);
			override.setOverride(cons);
			return cons;
		}, Modifier.PUBLIC, Constructor.class, Constructor.class);
		myFactoryClass.setFunc("<init>", (Func2) null, Modifier.PUBLIC, void.class);
		// ObjectMap<Class<?>, ObjectMap<Method, MethodAccessor>> classObjectMapObjectMap = new ObjectMap<>();
		// SetMethod.init(myFactoryClass);
		SetField.init(myFactoryClass);

		/*lookup.findSetter(ClassLoader.class, "parent", ClassLoader.class)
				.invoke(factory.getClassLoader(), Test.class.getClassLoader());*/
		// Class<?> cls = ;
		Object ins = unsafe.allocateInstance(myFactoryClass.define());


		// print(cls);
		/*byte[] bytes = myFactoryClass.writer.toByteArray();
		Object ins = unsafe.allocateInstance(SetField.unsafe.defineClass0(null, bytes, 0, bytes.length, MyReflect.IMPL_LOADER, null));*/

		/*Constructor<?> cons = cls.getDeclaredConstructor();
		cons.setAccessible(true);
		Object ins = cons.newInstance();*/
		Tools.clone(ReflectionFactory.getReflectionFactory(),
				ins, factory);
		Field f = AccessibleObject.class.getDeclaredField("reflectionFactory");
		unsafe.putObject(AccessibleObject.class, unsafe.staticFieldOffset(f), ins);
		f = factory.getDeclaredField("soleInstance");
		unsafe.putObject(factory, unsafe.staticFieldOffset(f), ins);
		f = Class.class.getDeclaredField("reflectionFactory");
		unsafe.putObject(Class.class, unsafe.staticFieldOffset(f), ReflectionFactory.getReflectionFactory());

		// print(MyReflect.class.getDeclaredFields());

		/*ConstantPool pool = SharedSecrets.getJavaLangAccess().getConstantPool(Func2.class);
		StringBuilder builder = new StringBuilder();
		Object o;
		for (int i = 0, size = pool.getSize(); i < size; i++) {
			o = getTag(pool, i, pool.getTagAt(i));
			builder.append(o.getClass().isArray() ? Arrays.toString((Object[]) o) : o).append('\n');
		}
		print(builder);*/
	}

	public static Object getTag(ConstantPool pool, int i, Tag tag) {
		switch (tag) {
			case UTF8:
				return pool.getUTF8At(i);
			case INTEGER:
				return pool.getIntAt(i);
			case FLOAT:
				return pool.getFloatAt(i);
			case LONG:
				return pool.getLongAt(i);
			case DOUBLE:
				return pool.getDoubleAt(i);
			case CLASS:
				return pool.getClassAt(i);
			case STRING:
				return pool.getStringAt(i);
			case FIELDREF:
				return pool.getFieldAt(i);
			case METHODREF:
				return pool.getNameAndTypeRefInfoAt(pool.getNameAndTypeRefIndexAt(i));
			case INTERFACEMETHODREF:
				return pool.getMemberRefInfoAt(i);
			case NAMEANDTYPE:
				return pool.getNameAndTypeRefInfoAt(i);
			case METHODHANDLE:
			case METHODTYPE:
				// TODO
				return pool.getTagAt(i);

			// return pool.getClassAt(i);
			case INVOKEDYNAMIC:
				return pool.getNameAndTypeRefInfoAt(pool.getNameAndTypeRefIndexAt(i));
			// return Arrays.toString(pool.getNameAndTypeRefInfoAt(i));
			case INVALID:
				return "";
			default:
				return "";
		}
	}

	private static void getMember() {
		/*Class<?> member = Class.forName("java.lang.invoke.MemberName");
		Class<?> factory = Class.forName("java.lang.invoke.MemberName$Factory");
		Object INSTANCE = lookup.findStatic(member, "getFactory",
						MethodType.methodType(factory)).invoke();
		MethodHandle getCons = lookup.findVirtual(factory, "getMethods",
				MethodType.methodType(List.class, Class.class, boolean.class, Class.class));
		List list = (List) getCons.invoke(INSTANCE, Test.class, false, Object.class);
		Class<?> DirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
		MethodHandle make = lookup.findStatic(DirectMethodHandle, "make",
				MethodType.methodType(DirectMethodHandle, member));
		for (var item : list) {
			print(make.invoke(item));
		}*/


		// mc.invoke(Object.class,m);

		// System.out.println(f.canAccess(null));


		// .unreflect(m);
		// handle.invoke(f, true);
		/*new AccessibleObject() {
			{
				setAccessible(true);
				f.setAccessible(true);
			}
		};*/
		// f.setAccessible(true);
		// lookup.findConstructor(Lookup.class, MethodType.methodType(void.class, Class.class, Class.class, int.class));
	}

	public static void print(Object o) {
		if (o instanceof Object[]) o = Arrays.toString((Object[]) o);
		System.out.println(o);
	}


	/*public static class HandleAccessor implements MethodAccessor {
		final boolean isStatic;
		final int len;
		final MethodHandle handle;

		public HandleAccessor(Method m) {
			isStatic = Modifier.isStatic(m.getModifiers());
			len = m.getParameterCount();
			try {
				this.handle = lookup.unreflect(m);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public Object invoke(Object obj, Object[] args) {
			try {
				if (len == 0) {
					if (isStatic)
						return handle.invoke();
					return handle.invoke(obj);
				}
				if (len == 1) {
					if (isStatic)
						return handle.invoke(args[1]);
					return handle.invoke(obj, args[1]);
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}*/

	/*private static final MethodHandle handle;

	static {
		try {
			handle = lookup.findStatic(Test.class, "test", MethodType.methodType(void.class, Integer.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}*/

	public static final class MyMethodAccessor implements MethodAccessor {
		// public static final Lookup lookup = Test.lookup;
		private final MethodHandle handle;
		private final FuncInvoke func;

		public MyMethodAccessor(Method method) {
			boolean isStatic = Modifier.isStatic(method.getModifiers());
			int len = method.getParameterCount();
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
		@ForceInline
		public Object invoke(Object obj, Object[] args) {
			try {
				return func.invoke(obj, args);
				// return (Integer) Test.handle.invokeExact((Integer) args[0]);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class Test2 {
		private static int IAAA = 10082820;

		public void aaa() {
		}
	}

	private interface FuncInvoke {
		Object invoke(Object obj, Object[] args) throws Throwable;
	}


}

