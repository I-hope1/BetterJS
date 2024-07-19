package better_js.android;

import arc.*;
import arc.backend.android.*;
import arc.util.*;
import better_js.utils.*;
import better_js.utils.A_ASM.MyClass;

import com.android.dex.Dex;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.*;

import dalvik.system.*;
import hope_android.FieldUtils;
import mindustry.*;
import mindustry.android.AndroidLauncher;
import rhino.*;
import rhino.classfile.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

import static better_js.Main.unsafe;
import static better_js.utils.Tools.*;

public interface AndroidDefiner {
	long     off_pathList        = MyReflect.offset(BaseDexClassLoader.class, "pathList");
	long     off_mCookie         = MyReflect.offset(DexFile.class, "mCookie");
	long     off_mInternalCookie = MyReflect.offset(DexFile.class, "mInternalCookie");
	long     off_mFileName       = MyReflect.offset(DexFile.class, "mFileName");
	Class<?> cl_PathList         = MyReflect.classOrNull("dalvik.system.DexPathList");

	Field f_dexElements = nl(() -> cl_PathList.getDeclaredField("dexElements"));

	long           off_dexElements  = MyReflect.getOffset(f_dexElements, false);
	Class<?>       cl_ElementArr    = f_dexElements.getType();
	Class<?>       cl_Element       = cl_ElementArr.getComponentType();
	Constructor<?> ctor_Element     = ctor(cl_Element, DexFile.class);
	Constructor<?> ctor_ElementAll  = ctor(cl_Element, DexFile.class, File.class);
	Constructor<?> ctor_ElementFile = ctor(cl_Element, File.class);

	// Method m_addDexPath = nl(() -> cl_PathList.getMethod("addDexPath", String.class, File.class, boolean.class));
	// Method m_invoke     = nl(() -> Method.class.getMethod("invoke", Object.class, Object[].class));
	Method m_openDexFile = nl(() -> DexFile.class.getDeclaredMethod("openDexFile", String.class, String.class, int.class, ClassLoader.class, cl_ElementArr));
	Method m_findClass   = nl(() -> cl_Element.getDeclaredMethod("findClass", String.class, ClassLoader.class, List.class));
	// Method m_init       = nl(() -> cl_Element.getDeclaredMethod("maybeInit"));
	// Method m_setTrusted = nl(() -> DexFile.class.getDeclaredMethod("setTrusted"));
	// Method m_loadDex    = nl(() -> DexFile.class.getDeclaredMethod("loadDex", String.class, String.class, int.class, ClassLoader.class, cl_ElementArr));

	Constructor<DexFile> cons_DexFileByte     = nl(() -> nl(() -> DexFile.class.getDeclaredConstructor(ByteBuffer[].class, ClassLoader.class, cl_ElementArr),
	 () -> DexFile.class.getDeclaredConstructor(ByteBuffer.class)));
	Constructor<DexFile> cons_DexFileWithFile = nl(() -> DexFile.class.getDeclaredConstructor(File.class, ClassLoader.class, cl_ElementArr));

	Class<?>       cl_myElement       = nl(() -> {
		MyClass<?> myClass = new MyClass<>(cl_Element.getName() + "$i0", cl_Element);
		String     desc    = "(Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/List;)Ljava/lang/Class;";
		myClass.setFunc("<init>", null, 1, true, Void.TYPE, DexFile.class, File.class);
		myClass.writer.startMethod("findClass", desc, ClassFileWriter.ACC_PUBLIC);
		myClass.writer.addLoadThis();
		myClass.writer.addALoad(1);
		myClass.writer.add(ByteCode.ACONST_NULL);
		myClass.writer.addALoad(3);
		myClass.writer.addInvoke(ByteCode.INVOKESPECIAL, "dalvik/system/DexPathList$Element", "findClass", desc);
		myClass.writer.add(ByteCode.ARETURN);
		myClass.writer.stopMethod((short) 4); // 1 + 3
		return myClass.defineNative();
	});
	Constructor<?> ctor_MyElementFile = ctor(cl_myElement, DexFile.class, File.class);
	static void defineClassWithBytecode(ByteBuffer wrap, ClassLoader definingContext,
																			ClassLoader defLoader)
	 throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Object dxFi = cons_DexFileByte.getParameterCount() == 1 ? cons_DexFileByte.newInstance(wrap) : cons_DexFileByte.newInstance(new ByteBuffer[]{wrap},
		 defLoader, null);
		ClassLoader dexLoader = or(() -> definingContext, Vars.class::getClassLoader);

		Object pathList0 = unsafe.getObject(dexLoader, off_pathList);
		Object els       = unsafe.getObject(pathList0, off_dexElements);
		int    len       = Array.getLength(els);
		Object newCls    = Array.newInstance(cl_Element, len + 1);
		//noinspection SuspiciousSystemArraycopy
		System.arraycopy(els, 0, newCls, 0, len);
		Array.set(newCls, len, ctor_ElementAll.newInstance(dxFi, null));
		unsafe.putObject(pathList0, off_dexElements, newCls);
	}
	static void defineClassWithFile(File file, ClassLoader definingContext) {
		defineClassWithFile0(file, definingContext, false);
	}
	static void defineClassWithFile0(File file, ClassLoader definingContext, boolean beforeApp) {
		try {
			defineClassWithFile1(file, definingContext, beforeApp);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({"SuspiciousSystemArraycopy"})
	static void defineClassWithFile1(File file, ClassLoader definingContext, boolean beforeApp)
	 throws Exception {
		ClassLoader dexLoader = or(() -> definingContext, Vars.class::getClassLoader);
		Object      pathList0 = unsafe.getObject(dexLoader, off_pathList);
		Object      arr       = unsafe.getObject(pathList0, off_dexElements);
		int         len       = Array.getLength(arr);
		Object      newArr    = Array.newInstance(cl_Element, len + 1);

		DexFile dxFi = cons_DexFileWithFile.newInstance(file, definingContext, null);
		// m_invoke.invoke(m_setTrusted, dxFi, new Object[0]);

		Object newElement = ctor_MyElementFile.newInstance(dxFi, file);
		// m_init.invoke(newElement);
		if (beforeApp) {
			// 插入到数组的第一个
			System.arraycopy(arr, 0, newArr, 1, len);
			Array.set(newArr, 0, newElement);
		} else {
			// 插入到数组的最后一个
			System.arraycopy(arr, 0, newArr, 0, len);
			Array.set(newArr, len, newElement);
		}
		unsafe.putObject(pathList0, off_dexElements, newArr);
		// Log.info(pathList0);

		{
			Object appDexFileElement = Array.get(arr, 0);
			Field  f_dexFile         = cl_Element.getDeclaredField("dexFile");
			f_dexFile.setAccessible(true);
			DexFile appDexFile = (DexFile) f_dexFile.get(appDexFileElement);
			appDexFile.close();
			Object cookie = m_openDexFile.invoke(null, unsafe.getObject(appDexFile, off_mFileName), null, 0, null, newArr);
			unsafe.putObject(appDexFile, off_mCookie, cookie);
			unsafe.putObject(appDexFile, off_mInternalCookie, cookie);
			extracted();
		}

		// {
		// 	long l = FieldUtils.getFieldOffset(ClassLoader.class.getDeclaredField("parent"));
		// 	unsafe.putObject(definingContext, l, new ClassLoader(definingContext.getParent()) {
		// 		protected Class<?> findClass(String name) throws ClassNotFoundException {
		// 			Class c = dxFi.loadClass(name, null);
		// 			if (c == null) c = super.findClass(name);
		// 			return c;
		// 		}
		// 	});
		// }
		// m_addDexPath.invoke(pathList0,null, file, true);
		// {
		// 	Class<?> BootClassLoader = Class.forName("java.lang.BootClassLoader");
		// 	long     l               = FieldUtils.getFieldOffset(BootClassLoader.getDeclaredField("instance"));
		// 	unsafe.putObject(BootClassLoader, l, Vars.mods.mainLoader());
		// }
		l:
		{
			/* 已加载的类Table */
			Field field = ClassLoader.class.getDeclaredField("classTable");
			long  l     = FieldUtils.getFieldOffset(field);
			if (unsafe.getLong(definingContext.getParent(), l) == unsafe.getLong(definingContext, l))
				break l;
			unsafe.copyMemory(unsafe.getLong(definingContext.getParent(), l) + 32,
			 unsafe.getLong(definingContext, l) + 32, 8);
			unsafe.copyMemory(unsafe.getLong(definingContext.getParent(), l) + 32,
			 unsafe.getLong(Vars.mods.mainLoader(), l) + 32, 8);
			unsafe.copyMemory(unsafe.getLong(definingContext.getParent(), l) + 32,
			 unsafe.getLong(AndroidDefiner.class.getClassLoader(), l) + 32, 8);
			// unsafe.putLong(definingContext, l, unsafe.getLong(definingContext.getParent(), l));
			// unsafe.putLong(Vars.mods.mainLoader(), l, unsafe.getLong(definingContext.getParent(), l));
			// unsafe.putLong(AndroidDefiner.class.getClassLoader(), l, unsafe.getLong(definingContext.getParent(), l));
		}
		Class<?> present = definingContext.loadClass("rhino.Context");
		if (present == null) return;
		Class<?> origin = AndroidDefiner.class.getClassLoader().loadClass("rhino.Context");

		try {
			Log.info("origin(@) == present(@): @;\n{direct}: @", origin, present, origin == present, present == Context.class);
			Log.info(present.getDeclaredField("f"));
		} catch (Throwable e) {
			Log.err(e);
		}
		// System.exit(-1);
	}
	private static void extracted() {
		new Thread(() -> {
			try {
				ApplicationListener listener = new ApplicationListener() {
					public void init() {
						ApplicationListener.super.init();
					}
				};
				Class<?> Looper = Class.forName("android.os.Looper");
				Field    field  = Looper.getDeclaredField("sThreadLocal");
				field.setAccessible(true);
				ThreadLocal o          = (ThreadLocal) field.get(null);
				Field       mainLooper = Looper.getDeclaredField("sMainLooper");
				mainLooper.setAccessible(true);
				o.set(mainLooper.get(null));
				// Reflect.invoke(Looper, "prepare");

				AndroidLauncher launcher = new AndroidLauncher();
				// Method          initialize     = AndroidApplication.class.getDeclaredMethod("initialize", ApplicationListener.class);
				// initialize.invoke(launcher, listener);
				Tools.clone(Vars.platform, launcher, AndroidApplication.class.getSuperclass());

				Method          init     = AndroidApplication.class.getDeclaredMethod("init", ApplicationListener.class, AndroidApplicationConfiguration.class, boolean.class);
				init.setAccessible(true);
				init.invoke(launcher, listener, new AndroidApplicationConfiguration(), true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).start();
		unsafe.park(true, Long.MAX_VALUE);
	}

	static <T> T nl(CProv<T> prov) {
		return nl(prov, () -> null);
	}
	static <T> T nl(CProv<T> prov, CProv<T> def) {
		T res;
		try {
			res = prov.get();
		} catch (Throwable e) {
			try {
				res = def.get();
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
		if (res instanceof AccessibleObject obj) obj.setAccessible(true);
		return res;
	}
	static Constructor<?> ctor(Class<?> cl, Class<?>... args) {
		return nl(() -> cl.getDeclaredConstructor(args));
	}
	static Class<?> defineClassNative(String name, byte[] bytes)
	 throws Exception {
		DexOptions                      dexOptions = new DexOptions();
		com.android.dx.dex.file.DexFile dexFile    = new com.android.dx.dex.file.DexFile(dexOptions);
		DirectClassFile                 classFile  = new DirectClassFile(bytes, name.replace('.', '/') + ".class");
		classFile.setAttributeFactory();
		classFile.getMagic();
		DxContext context = new DxContext();
		dexFile.add(CfTranslator.translate(context, classFile, new CfOptions(), dexOptions, dexFile));
		bytes = dexFile.toDex();
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		Object     dxFi = cons_DexFileByte.getParameterCount() == 1 ? cons_DexFileByte.newInstance(wrap) : cons_DexFileByte.newInstance(new ByteBuffer[]{wrap}, null, null);
		Object     elem = ctor_ElementAll.newInstance(dxFi, null);
		return (Class<?>) m_findClass.invoke(elem, name, null, new ArrayList<>());
	}
	interface CProv<R> {
		R get() throws Throwable;
	}
}
