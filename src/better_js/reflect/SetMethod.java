package better_js.reflect;

import arc.struct.ObjectMap;
import better_js.Test.MyMethodAccessor;
import better_js.utils.*;
import better_js.utils.ByteCodeTools.MyClass;
import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.*;
import jdk.internal.reflect.MethodAccessor;
import rhino.classfile.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;

import static better_js.Test.*;
import static better_js.utils.ByteCodeTools.*;

public class SetMethod {
	private static final ObjectMap<Method, MethodAccessor> classObjectMapObjectMap = new ObjectMap<>();
	public static final Unsafe unsafe = Unsafe.getUnsafe();
	public static final long off = unsafe.objectFieldOffset(Method.class, "methodAccessor");

	@Deprecated
	public static void init0(MyClass<?> myFactoryClass) {
		/*myFactoryClass.setFunc("newMethodAccessor", (self, _args) -> {
			Method method = (Method) _args.get(0);
			MethodAccessor base = (MethodAccessor) _args.get(1);
			return new MethodAccessor() {
				int times = 0;

				@Override
				public Object invoke(Object obj, Object[] args)
						throws IllegalArgumentException, InvocationTargetException {
					if (++times > 15) {
						unsafe.putReference(method, off, generateMethod(method));
					}
					return base.invoke(obj, args);
				}
			};
		}, Modifier.PUBLIC, true, MethodAccessor.class, Method.class);*/
	}

	private static Object generateMethod(Method m) {
		var base = m.getDeclaringClass();
		var newMethod = new MyClass<>(base.getName() + "$" + System.nanoTime(), Object.class);
		newMethod.addInterface(MethodAccessor.class);
		newMethod.setFunc("invoke", cfw -> {
			boolean isStatic = Modifier.isStatic(m.getModifiers());
			if (!isStatic) {
				cfw.addALoad(1);
				cfw.add(ByteCode.CHECKCAST, nativeName(base));
			}
			Class<?>[] args = m.getParameterTypes();
			for (int i = 0; i < args.length; i++) {
				cfw.addALoad(2);
				cfw.addPush(i);
				cfw.add(ByteCode.AALOAD);
				ByteCodeTools.addCast(cfw, args[0]);
			}
			cfw.addInvoke(Modifier.isInterface(m.getModifiers()) ? ByteCode.INVOKEINTERFACE
							: isStatic ? ByteCode.INVOKESTATIC : ByteCode.INVOKEVIRTUAL,
					nativeName(base), m.getName(), nativeMethod(m.getReturnType(), args));
			if (m.getReturnType() == Void.TYPE) cfw.add(ByteCode.ACONST_NULL);
			else if (m.getReturnType().isPrimitive()) addBox(cfw, m.getReturnType());
			cfw.add(ByteCode.ARETURN);
			return 3;
		}, Modifier.PUBLIC, Object.class, Object.class, Object[].class);
		try {
			return unsafe.allocateInstance(MyReflect.defineClass(base.getClassLoader(), newMethod.writer.toByteArray(), base.getProtectionDomain()));
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}


	@Deprecated
	public static void init_dis2(MyClass<?> myFactoryClass) {
		myFactoryClass.setFunc("newMethodAccessor", (self, _args) -> {
			Method m = (Method) _args.get(0);
			final int[] ID = {0};
			if (false) return classObjectMapObjectMap.get(m, () -> {
				var myClass2 = new MyClass<>("_HAKP$" + ID[0]++, Object.class);
				myClass2.writer.setFlags((short) (Modifier.PRIVATE | Modifier.FINAL));
				String fieldName = "__handle__";
				myClass2.addInterface(MethodAccessor.class);
				try {
					myClass2.setField(Modifier.STATIC | Modifier.FINAL, MethodHandle.class, fieldName, lookup.unreflect(m));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				boolean isStatic = Modifier.isStatic(m.getModifiers());
				Class<?>[] pars = m.getParameterTypes();
				ClassFileWriter cfw = myClass2.writer;
				cfw.startMethod("invoke", nativeMethod(Object.class, Object.class, Object[].class), (short) (Modifier.PUBLIC | Modifier.FINAL));
				cfw.add(ByteCode.GETSTATIC, myClass2.adapterName, "__handle__", typeToNative(MethodHandle.class));
				if (!isStatic) {
					cfw.add(ByteCode.ALOAD, 1);
					cfw.add(ByteCode.CHECKCAST, nativeName(m.getDeclaringClass()));
				}
				int i = 0;
				for (; i < pars.length; i++) {
					cfw.add(ByteCode.ALOAD, 2);
					cfw.addPush(i);
					cfw.add(ByteCode.AALOAD);
					addCast(cfw, pars[i]);
				}
				String ObjectName = typeToNative(Object.class);
				cfw.addInvoke(ByteCode.INVOKEVIRTUAL, nativeName(MethodHandle.class), "invoke", "(" + (isStatic ? "" : typeToNative(m.getDeclaringClass())) + ObjectName.repeat(pars.length) + ")" + ObjectName);
				cfw.add(ByteCode.ARETURN);
				cfw.stopMethod((short) 3);
				try {
					return (MethodAccessor) unsafe.allocateInstance(myClass2.define());
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				}
			});
			return new MyMethodAccessor(m);
		}, Modifier.PUBLIC, MethodAccessor.class, Method.class);
	}

	public static void init(MyClass<?> myFactoryClass) {
		ObjectMap<Class<?>, ObjectMap<Method, MethodAccessor>> map = new ObjectMap<>();
		myFactoryClass.buildSuperFunc("super_newMethodAccessor", "newMethodAccessor", MethodAccessor.class, Method.class);

		myFactoryClass.setFunc("newMethodAccessor", (self, _args) -> {
			Method m = (Method) _args.get(0);
			return map.get(m.getDeclaringClass(), ObjectMap::new)
					.get(m, () -> ((AAA) self).super_newMethodAccessor(m));
		}, Modifier.PUBLIC, MethodAccessor.class, Method.class);
	}

	public interface AAA {
		MethodAccessor super_newMethodAccessor(Method m);
	}
}
