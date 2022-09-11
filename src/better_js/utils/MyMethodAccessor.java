package better_js.utils;

import arc.struct.ObjectMap;
import interfaces.InvokeFunc;
import mindustry.Vars;
import mindustry.android.AndroidRhinoContext.AndroidContextFactory;
import rhino.*;
import rhino.classfile.*;

import java.lang.reflect.*;

import static better_js.utils.ByteCodeTools.*;
import static better_js.utils.MyReflect.unsafe;
import static rhino.classfile.ByteCode.*;

public class MyMethodAccessor {
	public static boolean lastGenerated;
	private Method method;

	public MyMethodAccessor(Method method) {
		this.method = method;
	}

	public final InvokeFunc generateMethod() throws InstantiationException {
		return generateMethod(method);
	}

	public static InvokeFunc generateMethod(Method method) throws InstantiationException {
		/*if (false) {
			method.setAccessible(true);
			final MethodAccess acc = MethodAccess.get(method.getDeclaringClass());
			final int id = acc.getIndex(method);
			return (obj, args) -> acc.invoke(obj, id, args);
		}*/
		int mod = method.getModifiers();
		boolean isPublic = Modifier.isPublic(mod);
		boolean isProtected = Modifier.isProtected(mod);
		if (!isPublic && !isProtected) return method::invoke;
		boolean isStatic = Modifier.isStatic(mod);
		var baseClass = method.getDeclaringClass();
		MyReflect.setPublic(baseClass);
		String baseName = baseClass.getName().replace('.', '/');
		String adapterName = baseName + "$A471";
		var cfw = new ClassFileWriter(adapterName, isProtected ? baseName : "java/lang/Object", "<GENERATE_METHOD>");
		cfw.addInterface("interfaces/InvokeFunc");
		Class<?>[] args = method.getParameterTypes();
		Class<?> returnType = method.getReturnType();
		int count = args.length;
		cfw.startMethod("invoke_2rp0dmwi2la", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", (short) (Modifier.PUBLIC | Modifier.FINAL));
		if (!isStatic) {
			cfw.add(ALOAD, 1);
			addCast(cfw, baseClass);
		}
		// int max = count + 1;
		for (int i = 0; i < count; i++) {
				/*if (args[i] == long.class) max++;
				if (args[i] == double.class) max++;*/
			cfw.add(ALOAD, 2);
			cfw.addPush(i);
			cfw.add(AALOAD);
			addCast(cfw, args[i]);
		}
		cfw.addInvoke(isStatic ? INVOKESTATIC : baseClass.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
				baseName, method.getName(), nativeMethod(returnType, args));
		if (returnType == Void.TYPE) {
			cfw.add(ACONST_NULL);
		} else {
			addBox(cfw, returnType);
		}
		cfw.add(ARETURN);
		cfw.stopMethod((short) 3);
		// ByteCodeTools.writeTo(cfw, Vars.tmpDirectory.child(adapterName));
		return (InvokeFunc) unsafe.allocateInstance(((GeneratedClassLoader) ((AndroidContextFactory) ContextFactory.getGlobal())
				.createClassLoader(Vars.mods.mainLoader()))
				.defineClass(adapterName, cfw.toByteArray()));
	}

	public int times = 0;
	public static final int inflationThreshold = 15;
	private InvokeFunc func = (obj, args) -> {
		if (++times > inflationThreshold && !lastGenerated) {
			func = generateMethod();
			lastGenerated = true;
		} else if (lastGenerated) {
			--times;
			lastGenerated = false;
		}
		return method.invoke(obj, args);
	};

	public Object invoke(Object obj, Object... args) throws Throwable {
		return func.invoke_2rp0dmwi2la(obj, args);
	}

	static class AAA {
		Object a(Object[] arr) {
			return arr[0];
		}
	}
}
