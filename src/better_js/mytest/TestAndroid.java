package better_js.mytest;

import arc.files.Fi;
import arc.util.*;
import better_js.Main;
import better_js.utils.*;
import better_js.utils.ByteCodeTools.MyClass;
// import com.esotericsoftware.reflectasm.FieldAccess;
import dalvik.system.*;
import hope_android.FieldUtils;
import interfaces.InvokeFunc;
import mindustry.Vars;
import mindustry.android.AndroidRhinoContext.AndroidContextFactory;
import mindustry.world.blocks.ItemSelection;
import rhino.*;
import rhino.classfile.*;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

import static better_js.Main.unsafe;

public class TestAndroid {

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
		if (true) return;
		Log.info(ItemSelection.class);
		Tools.forceRun(() -> {
			Log.info("======hotfix=====");
			Fi target = Vars.mods.getMod(Main.class).root.child("AAKPA.jar");
			Fi toFile = Vars.tmpDirectory.child("padcjikzn.jar");
			target.copyTo(toFile);

			ClassLoader pathLoader = Vars.class.getClassLoader();
			/*Log.info(pathLoader);
			Log.info(pathLoader.getParent());*/
			var base = new BaseDexClassLoader(toFile.path(), null, null, pathLoader.getParent()) {
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
			// base.loadClass("mindustry.world.blocks.ItemSelection"+);
			// Log.info(ItemSelection.class.hashCode());
			try {
				VMRuntime.registerAppInfo(Vars.tmpDirectory.child("dexCache").path(), new String[]{toFile.path()});
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

	static final Method m;
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
		TestAndroid ins = new TestAndroid();
		Time.mark();
		Runnable r = TestAndroid::aMethod;
		// InvokeFunc acc = new MyMethodAccessor(m).generateMethod();
		// Object[] ARR = {};
		for (int i = 0; i < 1E5; i++) {
			// acc.invoke_2rp0dmwi2la(ins, ARR);
			r.run();
		}
		Log.info("interface:" + Time.elapsed());
		Time.mark();
		for (int i = 0; i < 1E5; i++) {
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
		Field f = TestAndroid.class.getDeclaredField("a");
		f.setAccessible(true);
		Object ins = new TestAndroid();
		Time.mark();
		for (int i = 0; i < 1E6; i++) {
			f.get(ins);
		}
		Log.info("field:" + Time.elapsed());
		// int index = access.getIndex(f);
		// long off = ;
		Time.mark();
		for (int i = 0; i < 1E6; i++) {
			// access.get(ins, index);
			unsafe.getObject(ins, FieldUtils.getFieldOffset(f));
		}
		Log.info("unsafe:" + Time.elapsed());
		/*Class<?> cls = Class.forName("rhino.JavaMembers");
		MyReflect.setPublic(cls);
		Log.info(Modifier.toString(cls.getModifiers()));*/
	}

	public int a = 1;

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
			Field field = ReflectUtils.findField(classLoader, "pathList");
			Object pathList = field.get(classLoader);


			//3.1、把补丁包patch.dex转化为Element[]  (patch)
			Method method = ReflectUtils.findMethod(pathList, "makeDexElements", List.class, File.class, List.class, ClassLoader.class);
			method.setAccessible(true);
			//构建第一个参数
			ArrayList<File> patchs = new ArrayList<>();
			patchs.add(patch);
			//构建第三个参数
			ArrayList<IOException> suppressedExceptions = new ArrayList<>();
			//执行
			Object[] patchElements = (Object[]) method.invoke(null, patchs, null, suppressedExceptions, classLoader);


			//3.2获得pathList的dexElements属性（old）
			Field dexElementsField = ReflectUtils.findField(pathList, "dexElements");
			Object[] dexElements = (Object[]) dexElementsField.get(pathList);

			//3.3、patch+old合并，并反射赋值给pathList的dexElements
			Object[] newElements = (Object[]) Array.newInstance(patchElements.getClass().getComponentType(), patchElements.length + dexElements.length);
			System.arraycopy(patchElements, 0, newElements, 0, patchElements.length);
			System.arraycopy(dexElements, 0, newElements, patchElements.length, dexElements.length);
			Log.info(Arrays.toString(newElements));
			dexElementsField.set(pathList, newElements);
		} catch (Exception e) {
			Log.err(e);
		}
	}
}
