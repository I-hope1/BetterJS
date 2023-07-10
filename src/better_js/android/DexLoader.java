package better_js.android;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION;
import arc.Core;
import arc.util.*;
import com.android.dex.Dex;
import com.android.dx.cf.direct.*;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.*;
import com.android.dx.dex.file.DexFile;
import dalvik.system.*;
import mindustry.Vars;
import rhino.GeneratedClassLoader;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static better_js.Main.unsafe;
import static better_js.utils.Tools.or;

public class DexLoader {
	// public static ClassLoader defLoader = null;
	public static void init() {}

	/**
	 * Compiles java bytecode to dex bytecode and loads it
	 *
	 * @author F43nd1r
	 * @since 11.01.2016
	 */
	public abstract static class BaseAndroidClassLoader extends ClassLoader implements GeneratedClassLoader {
		public ClassLoader definingContext;
		public BaseAndroidClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> defineClass(String name, byte[] data) {
			try {
				DexOptions      dexOptions = new DexOptions();
				DexFile         dexFile    = new DexFile(dexOptions);
				DirectClassFile classFile  = new DirectClassFile(data, name.replace('.', '/') + ".class", true);
				classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
				classFile.getMagic();
				DxContext context = new DxContext();
				Time.mark();
				dexFile.add(CfTranslator.translate(context, classFile, null, new CfOptions(), dexOptions, dexFile));
				Dex dex    = new Dex(dexFile.toDex(null, false));
				/* Dex oldDex = getLastDex();
				if (oldDex != null) {
					dex = new DexMerger(new Dex[]{dex, oldDex}, CollisionPolicy.KEEP_FIRST, context).merge();
				} */
				return loadClass(dex, name);
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException("Failed to define class", e);
			} finally {
				// Log.info("@ms", Time.elapsed());
			}
		}

		protected abstract Class<?> loadClass(Dex dex, String name) throws ClassNotFoundException;

		protected abstract Dex getLastDex();

		protected abstract void reset();

		@Override
		public void linkClass(Class<?> aClass) {}

		@Override
		public Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			Class<?> loadedClass = findLoadedClass(name);
			if (loadedClass == null) {
				Dex dex = getLastDex();
				if (dex != null) {
					loadedClass = loadClass(dex, name);
				}
				if (loadedClass == null) {
					loadedClass = definingContext.loadClass(name);
				}
			}
			return loadedClass;
		}
	}

	public static class FileAndroidClassLoader extends BaseAndroidClassLoader {
		private static int  instanceCounter = 0;
		private final  File dexFile;

		public FileAndroidClassLoader(ClassLoader parent, File cacheDir) {
			super(parent);
			int id = instanceCounter++;
			dexFile = new File(cacheDir, id + ".dex");
			cacheDir.mkdirs();
			reset();
		}

		@Override
		protected Class<?> loadClass(Dex dex, String name) throws ClassNotFoundException {
			try {
				dex.writeTo(dexFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			android.content.Context context = (android.content.Context) Core.app;
			return new DexClassLoader(dexFile.getPath(), VERSION.SDK_INT >= 21 ? context.getCodeCacheDir().getPath() : context.getCacheDir().getAbsolutePath(), null, getParent()).loadClass(name);
		}

		@Override
		protected Dex getLastDex() {
			if (dexFile.exists()) {
				try {
					return new Dex(dexFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void reset() {
			dexFile.delete();
		}
	}

	@TargetApi(Build.VERSION_CODES.O)
	public static class InMemoryAndroidClassLoader extends BaseAndroidClassLoader {
		protected Dex last;

		public InMemoryAndroidClassLoader(ClassLoader parent) {
			super(parent);
		}

		public dalvik.system.DexFile dexFile;
		public void setDexFile(dalvik.system.DexFile dexFile) {
			this.dexFile = dexFile;
		}
		protected Class<?> loadClass(Dex dex, String name) throws ClassNotFoundException {
			last = dex;
			try {
				ByteBuffer wrap = ByteBuffer.wrap(dex.getBytes());
				Object dxFi = DexCons.getParameterCount() == 1 ? DexCons.newInstance(wrap) : DexCons.newInstance(new ByteBuffer[]{wrap},
						getParent(), null);
				ClassLoader dexLoader = or(() -> this.definingContext, Vars.class::getClassLoader);

				Object pathList = unsafe.getObject(dexLoader, DexLoader.pathList);
				Object els      = unsafe.getObject(pathList, dexElements);
				int    len      = Array.getLength(els);
				Object newCls   = Array.newInstance(ElementCl, len + 1);
				System.arraycopy(els, 0, newCls, 0, len);
				Array.set(newCls, len, ElementCons.newInstance(dxFi));
				Log.info(Arrays.toString((Object[]) newCls));
				unsafe.putObject(pathList, dexElements, newCls);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return definingContext.loadClass(name);
		}
		/* protected Class<?> loadClass(Dex dex, String name) throws ClassNotFoundException {
			last = dex;
			var loader = new InMemoryDexClassLoader(ByteBuffer.wrap(dex.getBytes()), getParent());

			Object obj = unsafe.getObject(loader, pathList);
			unsafe.putObject(obj, definingContext, MyJavaAdapter.sampleClass != null ? MyJavaAdapter.sampleClass.getClassLoader() : this.loader);

			return loader.loadClass(name);
		} */

		@Override
		protected Dex getLastDex() {
			return last;
		}

		@Override
		protected void reset() {
			last = null;
		}
	}


	public static final long pathList, dexElements;
	public static Constructor<?> DexCons;
	public static final Constructor<?> ElementCons;

	private static final Class<?> PathList, ElementCl;

	static {
		try {
			PathList = Class.forName("dalvik.system.DexPathList");
			Field f = BaseDexClassLoader.class.getDeclaredField("pathList");
			pathList = unsafe.objectFieldOffset(f);
			f = PathList.getDeclaredField("dexElements");
			dexElements = unsafe.objectFieldOffset(f);
			Class<?> ElementArrCl = f.getType();
			ElementCl = ElementArrCl.getComponentType();

			try {
				DexCons = dalvik.system.DexFile.class.getDeclaredConstructor(ByteBuffer[].class, ClassLoader.class, ElementArrCl);
			} catch (NoSuchMethodException e) {
				DexCons = dalvik.system.DexFile.class.getDeclaredConstructor(ByteBuffer.class);
			}
			DexCons.setAccessible(true);

			ElementCons = ElementCl.getDeclaredConstructor(dalvik.system.DexFile.class);
			ElementCons.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
