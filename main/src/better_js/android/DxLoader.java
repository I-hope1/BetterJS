package better_js.android;

import com.android.dex.Dex;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.*;
import com.android.dx.dex.file.DexFile;
import rhino.GeneratedClassLoader;

import java.io.*;
import java.nio.ByteBuffer;


/** 参考反编译的AndroidRhinoContext
 * @see mindustry.android.AndroidRhinoContext */
public class DxLoader {

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
				DirectClassFile classFile  = new DirectClassFile(data, name.replace('.', '/') + ".class");
				classFile.setAttributeFactory();
				classFile.getMagic();
				DxContext context = new DxContext();
				dexFile.add(CfTranslator.translate(context, classFile, new CfOptions(),  dexOptions, dexFile));
				Dex dex = new Dex(dexFile.toDex());
				/* Dex oldDex = getLastDex();
				if (oldDex != null) {
					dex = new DexMerger(new Dex[]{dex, oldDex}, CollisionPolicy.KEEP_FIRST, context).merge();
				} */
				return loadClass(dex, name);
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException("Failed to define class", e);
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

	public static class InMemoryAndroidClassLoader extends BaseAndroidClassLoader {
		protected Dex last;

		public InMemoryAndroidClassLoader(ClassLoader parent) {
			super(parent);
		}
		protected Class<?> loadClass(Dex dex, String name) throws ClassNotFoundException {
			last = dex;
			try {
				AndroidDefiner.defineClassWithBytecode(ByteBuffer.wrap(dex.getBytes()),
				 definingContext, getParent());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return definingContext.loadClass(name);
		}

		@Override
		protected Dex getLastDex() {
			return last;
		}

		@Override
		protected void reset() {
			last = null;
		}
	}

}
