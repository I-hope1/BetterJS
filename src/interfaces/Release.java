package interfaces;

import java.lang.reflect.*;

public class Release {

	public static class SClass {
		public static boolean aBoolean() {
			return false;
		}
	}

	public static void main(String[] args) throws Throwable {
		Class.forName("ihope_lib.MyReflect", true, Release.class.getClassLoader());
		Field f = Class.class.getDeclaredField("module");
		f.setAccessible(true);
		f.set(Release.class, Object.class.getModule());

		Method m = Module.class.getDeclaredMethod("implAddExports", String.class);
		Method openM = Module.class.getDeclaredMethod("implAddOpens", String.class);
		m.setAccessible(true);
		openM.setAccessible(true);
		m.invoke(Object.class.getModule(), "jdk.internal.reflect");
		Class<?> InstrumentationImpl0 = Class.forName("sun.instrument.InstrumentationImpl");
		openM.invoke(InstrumentationImpl0.getModule(), "sun.instrument");
		// m.invoke(InstrumentationImpl0.getModule(), "sun.instrument");

		Constructor<?> c = InstrumentationImpl0.getDeclaredConstructor(long.class, boolean.class, boolean.class);
		c.setAccessible(true);

		// byte[] b = new Fi("F:/Reflection.class").readBytes();

		// InstrumentationImpl.loadAgent("Reflection.jar");
		// Instrumentation instrumentation = (Instrumentation) c.newInstance(-1, true, true);

		// System.out.println(Arrays.toString(instrumentation.getAllLoadedClasses()));

		/*ClassReader reader = new ClassReader(Reflection.class.getName());
		reader.accept(new ClassVisitor(Opcodes.ASM9) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				if (name.equals("verifyMemberAccess")) {
					System.out.println(descriptor);
					return null;
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		}, 0);*/
		// new Fi("F:/Reflection.class").writeBytes(ClassLoader.getSystemResourceAsStream(Reflection.class.getName().replace('.', '/') + ".class").readAllBytes());
		/*instrumentation.redefineClasses(new ClassDefinition(
				Reflection.class,
				new ClassWriter(reader, 0).toByteArray()
		));*/
	}
}
