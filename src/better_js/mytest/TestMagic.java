package better_js.mytest;

import better_js.reflect.JDKVars;
import better_js.utils.MyReflect;
import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.vm.annotation.ForceInline;
import rhino.classfile.*;

import java.lang.reflect.*;

import static better_js.reflect.JDKVars.MagicAccessorImpl;
import static better_js.utils.ByteCodeTools.*;

public class TestMagic {
	public static final Unsafe unsafe = JDKVars.unsafe;
	public static void init() throws Exception {
		MyClass<?> magic = new MyClass<>(MagicAccessorImpl.getName() + "_1", MagicAccessorImpl);
		magic.addInterface(TestInterface.class);
		magic.setFunc("test", cfw -> {
			// cfw.addALoad(0);
			/*cfw.addPush(true);
			cfw.add(ByteCode.PUTFIELD, nativeName(AccessibleObject.class), "override", typeToNative(boolean.class));*/
			// cfw.add(ByteCode.GETSTATIC, nativeName(System.class), "out", typeToNative(PrintStream.class));
			// cfw.addInvoke(ByteCode.INVOKEVIRTUAL, nativeName(PrintStream.class), "println", nativeMethod(void.class, int.class));
			/*cfw.addALoad(1);
			buildInvoke(cfw, (short) ByteCode.INVOKEINTERFACE, FuncInvoke.class, "run", void.class);*/

			cfw.add(ByteCode.RETURN);
			return 2;
		}, Modifier.PUBLIC, void.class, FuncInvoke.class);
		byte[] bytes = magic.writer.toByteArray();
		Class<?> cls = unsafe.defineClass(null, bytes, 0, bytes.length, MyReflect.IMPL_LOADER, null);
		TestInterface o = (TestInterface) unsafe.allocateInstance(cls);
		// Method m = Object.class.getDeclaredMethod("hashCode");
		// print(m.isAccessible());
		// print(m.isAccessible());
	}

	public static class TOP {
		private static int AAA = 0;
	}

	public static void buildGetField(ClassFileWriter cfw, Class<?> cls, String fieldName, Class<?> type, boolean isStatic) {
		cfw.add(isStatic ? ByteCode.GETSTATIC : ByteCode.GETFIELD, nativeName(cls), fieldName, typeToNative(type));
	}
	public static void buildPutField(ClassFileWriter cfw, Class<?> cls, String fieldName, Class<?> type, boolean isStatic) {
		cfw.add(isStatic ? ByteCode.PUTSTATIC : ByteCode.PUTFIELD, nativeName(cls), fieldName, typeToNative(type));
	}
	public static void buildGetField(ClassFileWriter cfw, String className, String fieldName, String fieldType, boolean isStatic) {
		cfw.add(isStatic ? ByteCode.GETSTATIC : ByteCode.GETFIELD, className, fieldName, fieldType);
	}
	public static void buildPutField(ClassFileWriter cfw, String className, String fieldName, String fieldType, boolean isStatic) {
		cfw.add(isStatic ? ByteCode.PUTSTATIC : ByteCode.PUTFIELD, className, fieldName, fieldType);
	}
	public static void buildInvoke(ClassFileWriter cfw, short invokeType, Class<?> cls, String methodName, Class<?> returnType, Class<?>... args) {
		cfw.addInvoke(invokeType, nativeName(cls), methodName, nativeMethod(returnType, args));
	}

	public interface FuncInvoke {
		@ForceInline
		@CallerSensitive
		void run();
	}
	public interface TestInterface {
		void test(FuncInvoke i);
	}
}
