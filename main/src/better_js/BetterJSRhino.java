package better_js;

import arc.func.Prov;
import arc.util.Log;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.MyReflect;
import rhino.*;

import static better_js.ForRhino.wrapFactory;
import static better_js.Main.unsafe;

public class BetterJSRhino {
	public static Status status = Status.normal;
	public static final
	Object[] EMPTY_ARGS = {},
	 ONE_ARGS           = {null};

	public static Object[] one_arg(Object o) {
		ONE_ARGS[0] = o;
		return ONE_ARGS;
	}

	public static Object evalFunc(Context cx, Scriptable scope, Object func, Object obj) {
		return evalFuncWithStatus(cx, scope, func, obj, Status.access);
	}

	public static Object evalFuncWithStatus(Context cx, Scriptable scope, Object func, Object obj, Status defStatus) {
		if (!(func instanceof Function f))
			throw new IllegalArgumentException(func + " isn't function");
		if (obj != null && !(obj instanceof NativeJavaObject)) {
			// Log.info("cl: @, super: @", obj.getClass(), NativeJavaObject.class);
			throw new IllegalArgumentException(obj + " isn't NativeJavaObject");
		}

		Function scopeF = f instanceof ArrowFunction ? (Function) unsafe.getObject(f, targetOff) : f;
		scopeF.setParentScope(new BaseFunction(scopeF.getParentScope(), null) {
			public Object get(String name, Scriptable start) {
				Object o = super.get(name, start);
				if (o instanceof NativeJavaObject) {
					return wrapAccess(cx, getParentScope(), o);
				}
				return o;
			}
		});

		Prov<?> prov = () -> f.call(cx, scopeF, null,
		 obj == null ? EMPTY_ARGS : one_arg(wrapAccess(cx, scope, obj))
		);
		Status last = status;
		if (status == Status.normal) {
			status = defStatus;
		}

		WrapFactory factory = cx.getWrapFactory();
		cx.setWrapFactory(wrapFactory);
		try {
			return prov.get();
		} finally {
			status = last;
			cx.setWrapFactory(factory);
		}
	}

	public static Object wrapAccess(Context cx, Scriptable scope, Object obj) {
		if (!(obj instanceof NativeJavaObject)) obj = Context.javaToJS(obj, scope);
		NativeJavaObject object = (NativeJavaObject) obj;
		Status           last   = status;
		if (status == Status.normal) {
			status = Status.access;
		} else {
			if (object instanceof NativeJavaArray) return object;
			if (object instanceof MyNativeJavaObject) {
				if (((MyNativeJavaObject) object).status == status) return object;
			}
		}
		try {
			if (object instanceof NativeJavaClass || object instanceof MyNativeJavaClass)
				return wrapFactory.wrapJavaClass(cx, scope, (Class<?>) object.unwrap());
			return wrapFactory.wrapNewObject(cx, scope, object.unwrap());
		} finally {
			status = last;
		}
	}

	public static final long targetOff = MyReflect.offset(ArrowFunction.class, "targetFunction");

	public static Object run(Prov prov, Context cx, Status last) {
		var factory = cx.getWrapFactory();
		//        cx.setWrapFactory(wrapFactory);
		try {
			return prov.get();
		} finally {
			//            cx.setWrapFactory(factory);
			status = last;
		}
	}

}