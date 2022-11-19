package better_js.myrhino;

import arc.util.*;
import better_js.utils.MyMethodAccessor;
import interfaces.InvokeFunc;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.reflect.*;
import mindustry.Vars;
import rhino.*;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;

import static better_js.utils.MyMethodAccessor.*;

/**
 * Wrapper class for Method and Constructor instances to cache
 * getParameterTypes() results, recover from IllegalAccessException
 * in some cases and provide serialization support.
 *
 * @author Igor Bukanov
 */

public final class MyMemberBox {
	private transient Member memberObject;
	transient Class<?>[] argTypes;
	transient Object delegateTo;
	transient boolean vararg;
	private InvokeFunc invokeFunc;

	ConstructorAccessor cac;
	MethodAccessor mac;

	public InvokeFunc initFunc(Method method) {
		if (mac != null) return mac::invoke;
		return method::invoke;
		// final int[] times = {0};
		/*if (++times[0] > inflationThreshold && !lastGenerated) {
				invokeFunc = MyMethodAccessor.generateMethod(method);
				// Log.info("ok");
				lastGenerated = true;
			} else if (lastGenerated) {
				--times[0];
				lastGenerated = false;
			}
			return method.invoke(obj, args);*/
	}

	/*static ObjectMap<Class<?>, ConstructorAccess<?>> constructorCaches = new ObjectMap<>();

	public InvokeFunc initFunc(Constructor<?> ctor) {
		final int[] times = {0};
		return (obj, args) -> {
			if (++times[0] > inflationThreshold && !lastGenerated) {
				var ctorAccess = constructorCaches.get(ctor.getDeclaringClass(), () -> ConstructorAccess.get(ctor.getDeclaringClass()));
				ctorAccess.newInstance()
				lastGenerated = true;
			} else if (lastGenerated) {
				--times[0];
				lastGenerated = false;
			}
			return ctor.newInstance(obj, args);
		};
	}*/

	static final ReflectionFactory factory;

	static {
		factory = Vars.mobile ? null :
				ReflectionFactory.getReflectionFactory();
	}

	public MyMemberBox(Method method) {
		init(method);
	}

	public MyMemberBox(Constructor<?> constructor) {
		init(constructor);
	}

	private void init(Method method) {
		this.memberObject = method;
		this.argTypes = method.getParameterTypes();
		this.vararg = method.isVarArgs();
		if (!Vars.mobile) {
			mac = factory.getMethodAccessor(method);
			if (mac == null) {
				mac = factory.newMethodAccessor(method);
			}
		}
	}

	private void init(Constructor<?> constructor) {
		this.memberObject = constructor;
		this.argTypes = constructor.getParameterTypes();
		this.vararg = constructor.isVarArgs();
		if (!Vars.mobile) {
			cac = factory.getConstructorAccessor(constructor);
			if (cac == null) {
				cac = factory.newConstructorAccessor(constructor);
			}
		}
	}

	Method method() {
		return (Method) memberObject;
	}

	Constructor<?> ctor() {
		return (Constructor<?>) memberObject;
	}

	Member member() {
		return memberObject;
	}

	boolean isMethod() {
		return memberObject instanceof Method;
	}

	boolean isCtor() {
		return memberObject instanceof Constructor;
	}

	boolean isStatic() {
		return Modifier.isStatic(memberObject.getModifiers());
	}

	boolean isPublic() {
		return Modifier.isPublic(memberObject.getModifiers());
	}

	String getName() {
		return memberObject.getName();
	}

	Class<?> getDeclaringClass() {
		return memberObject.getDeclaringClass();
	}

	String toJavaDeclaration() {
		StringBuilder sb = new StringBuilder();
		if (isMethod()) {
			Method method = method();
			sb.append(method.getReturnType());
			sb.append(' ');
			sb.append(method.getName());
		} else {
			Constructor<?> ctor = ctor();
			String name = ctor.getDeclaringClass().getName();
			int lastDot = name.lastIndexOf('.');
			if (lastDot >= 0) {
				name = name.substring(lastDot + 1);
			}
			sb.append(name);
		}
		sb.append(MyJavaMembers.liveConnectSignature(argTypes));
		return sb.toString();
	}

	@Override
	public String toString() {
		return memberObject.toString();
	}

	public Object invoke(Object target, Object[] args) {
		if (invokeFunc == null) invokeFunc = initFunc(method());
		try {
			return invokeFunc.invoke_2rp0dmwi2la(target, args);
		} catch (InvocationTargetException ite) {
			// Must allow ContinuationPending exceptions to propagate unhindered
			Throwable e = ite;
			do {
				e = ((InvocationTargetException) e).getTargetException();
			} while ((e instanceof InvocationTargetException));
			if (e instanceof ContinuationPending)
				throw (ContinuationPending) e;
			throw Context.throwAsScriptRuntimeEx(e);
		} catch (Throwable ex) {
			throw Context.throwAsScriptRuntimeEx(ex);
		}
	}

	Object newInstance(Object[] args) {
		Constructor<?> ctor = ctor();
		try {
			if (cac != null) return cac.newInstance(args);
			return ctor.newInstance(args);
		} catch (Throwable ex) {
			throw Context.throwAsScriptRuntimeEx(ex);
		}
	}
}
