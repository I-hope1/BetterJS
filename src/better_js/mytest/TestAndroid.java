package better_js.mytest;

import arc.files.Fi;
import arc.util.*;
import better_js.Main;
import better_js.utils.*;
import better_js.utils.Tools.NotTimeException;
import dalvik.system.*;
import hope_android.FieldUtils;
import mindustry.Vars;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class TestAndroid {

	private static final Class<?> CLS_PathList;

	static {
		try {
			CLS_PathList = Class.forName("dalvik.system.DexPathList");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Throwable {
		// var tmp = new MyClass<>("hope_android/FieldUtils", Object.class);
		// tmp.addInterface(FieldUtils.class);
		/*tmp.setFunc("fieldOffset", cfw -> {
			cfw.addALoad(0);
			cfw.addInvoke(ByteCode.INVOKEVIRTUAL, "java/lang/reflect/Field", "getOffset", "()I");
			return 1;
		}, Modifier.PUBLIC | Modifier.FINAL, int.class, Field.class);*/

		// test
		// testField();
		// testMethod();
		// TestPrivate.test();
		/* Time.runTask(1, () -> {
			try {
				Field f = ClassLoader.class.getDeclaredField("parent");
				f.setAccessible(true);
				f.set(Context.class.getClassLoader(), Vars.mods.mainLoader());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}); */
		if (true) return;
		// Log.info(ItemSelection.class);
		Tools.forceRun(() -> {
			if (Vars.mods.getMod(Main.class) == null) throw new NotTimeException();
			Log.info("======hotfix=====");
			Fi target = Vars.mods.getMod(Main.class).root.child("classes.dex");
			// Fi toFile = Vars.tmpDirectory.child("padcjikzn.jar");
			// target.copyTo(toFile);

			ClassLoader pathLoader = Vars.class.getClassLoader();
			/*Log.info(pathLoader);
			Log.info(pathLoader.getParent());*/
			BaseDexClassLoader base = new BaseDexClassLoader(target.path(), null, null, pathLoader.getParent()) {
				private boolean inChild;

				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					try {
						return super.findClass(name);
					} catch (ClassNotFoundException e) {
						if (inChild) throw e;
						return findClassInChild(name);
					}

				}

				public Class<?> findClassInChild(String name) throws ClassNotFoundException {
					inChild = true;
					try {
						return pathLoader.loadClass(name);
					} finally {
						inChild = false;
					}
				}
			};
			try {
				Field f = ClassLoader.class.getDeclaredField("parent");
				f.setAccessible(true);
				f.set(pathLoader, base);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			// base.loadClass("mindustry.world.blocks.ItemSelection");
			// Log.info(ItemSelection.class.hashCode());
			try {
				VMRuntime.registerAppInfo(Vars.tmpDirectory.child("dexCache").path(), new String[]{target.path()});
			} catch (Throwable e) {
				Log.err(e);
			}
			// ((BaseDexClassLoader)pathLoader).addDexPath(toFile.path(), true);

			/*testHotfix(pathLoader, toFile.file());
			try {
				pathLoader.loadClass("mindustry.world.blocks.ItemSelection");
			} catch (Throwable e){
				throw new RuntimeException(e);
			}*/
			// Log.info(ItemSelection.class);

			// Log.info(new JavaMembers(Vars.mods.getScripts().scope, Vars.class));
		});
	}

	static final Method       m;
	static final MethodHandle handle;

	static {
		try {
			m = TestAndroid.class.getDeclaredMethod("aMethod");
			m.setAccessible(true);
			handle = MethodHandles.lookup().unreflect(m);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void testMethod() throws Throwable {
		// TestAndroid ins = new TestAndroid();
		// InvokeFunc acc = new MyMethodAccessor(m).generateMethod();
		// float[]  count = {0, 0, 0};
		// Method   m     = TestAndroid.class.getDeclaredMethod("aMethod");
		// m.setAccessible(true);
		Object[] ARGS = {};

		int amount = (int) 1E5;
		Time.mark();
		Runnable r = TestAndroid::aMethod;
		for (int i = 0; i < amount; i++) {
			// acc.invoke_2rp0dmwi2la(ins, ARR);
			// m.invoke(null, ARGS);
			// handle.invokeExact();
			// aMethod();
			r.run();
		}
		Log.info("inter:" + Time.elapsed());
		Time.mark();
		for (int i = 0; i < amount; i++) {
			// m.invoke(ins);
			bMethod();
		}
		Log.info("direct:" + Time.elapsed());
		/*Time.mark();
		for (int i = 0; i < 1E6; i++) {
			handle.invoke();
		}
		Log.info("MethodHandle:" + Time.elapsed());*/

		// Log.info(handle.getClass());
	}

	public static void testField() throws Exception {
		Field f = TestAndroid.class.getDeclaredField("a"), fb = TestAndroid.class.getDeclaredField("b");
		f.setAccessible(true);
		Object  ins      = new TestAndroid();
		boolean isStatic = Modifier.isStatic(f.getModifiers());
		Class.forName("better_js.utils.MyReflect");
		Time.mark();
		MyReflect.setValue(isStatic ? f.getDeclaringClass() : ins, FieldUtils.getFieldOffset(fb), 0, f.getType());
		// Log.info("unsafe-1: @ms", Time.elapsed());
		// Time.mark();
		// MyReflect.setValue(isStatic ? f.getDeclaringClass() : ins, FieldUtils.getFieldOffset(fb), 0, f.getType());
		// Log.info("unsafe-2: @ms", Time.elapsed());
		// if (true) return;
		double times = 2E6;
		Time.mark();
		for (float i = 0; i < times; i++) {
			f.set(ins, i);
		}
		Log.info("field: @ms, res: @", Time.elapsed() / times, a);

		// StringBuilder sb = new StringBuilder();
		// float last = 0;
		Time.mark();
		for (float i = 0; i < times; i++) {
			MyReflect.setValue(isStatic ? f.getDeclaringClass() : ins, FieldUtils.getFieldOffset(f), i, f.getType());
		}
		// Log.info(sb.toString());
		Log.info("unsafe: @ms, res: @", Time.elapsed() / times, a);
		/*Class<?> cls = Class.forName("rhino.JavaMembers");
		MyReflect.setPublic(cls);
		Log.info(Modifier.toString(cls.getModifiers()));*/
	}

	static double a = 1;
	static double b = 1;

	public static void aMethod() {
	}

	public static void bMethod() {
	}


	public static void testHotfix(ClassLoader classLoader, File patch) {
		if (classLoader == null || !patch.exists()) {
			Log.info(patch);
			return;
		}
		try {
			//反射获取到DexPathList属性对象pathList;
			Field  field    = BaseDexClassLoader.class.getDeclaredField("pathList");
			field.setAccessible(true);
			Object pathList = field.get(classLoader);


			//3.1、把补丁包patch.dex转化为Element[]  (patch)
			Method method = CLS_PathList.getDeclaredMethod("makeDexElements", List.class, File.class, List.class, ClassLoader.class);
			method.setAccessible(true);
			//构建第一个参数
			ArrayList<File> patchs = new ArrayList<>();
			patchs.add(patch);
			//构建第三个参数
			ArrayList<IOException> suppressedExceptions = new ArrayList<>();
			//执行
			Object[] patchElements = (Object[]) method.invoke(null, patchs, null, suppressedExceptions, classLoader);


			//3.2获得pathList的dexElements属性（old）
			Field    dexElementsField = CLS_PathList.getDeclaredField("dexElements");
			dexElementsField.setAccessible(true);
			Object[] dexElements      = (Object[]) dexElementsField.get(pathList);

			//3.3、patch+old合并，并反射赋值给pathList的dexElements
			Object[] newElements = (Object[]) Array.newInstance(patchElements.getClass().getComponentType(), patchElements.length + dexElements.length);
			System.arraycopy(patchElements, 0, newElements, 0, patchElements.length);
			System.arraycopy(dexElements, 0, newElements, patchElements.length, dexElements.length);
			// Log.info(Arrays.toString(newElements));
			dexElementsField.set(pathList, newElements);
		} catch (Exception e) {
			Log.err(e);
		}
	}
}
