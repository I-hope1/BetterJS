package better_js;

import arc.func.Prov;
import arc.util.Log;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import mindustry.Vars;
import rhino.*;

import static better_js.ForRhino.*;
import static better_js.myrhino.MyNativeJavaObject.Status.accessMethod;

public class BetterJSRhino {
	public static Status status = Status.normal;
	public static final
	Object[] EMPTY_ARGS = {},
			ONE_ARGS    = {null};

	public static Object[] one_arg(Object o) {
		ONE_ARGS[0] = o;
		return ONE_ARGS;
	}


	public static Object evalFunc(Context cx, Scriptable scope, Object func, Object obj) {
		return evalFuncWithStatus(cx, scope, func, obj, Status.access);
	}

	public static Object evalFuncWithStatus(Context cx, Scriptable scope, Object func, Object obj, Status defStatus) {
		if (!(func instanceof Callable))
			throw new IllegalArgumentException(func + " isn't function");
		if (obj != null && !(obj instanceof NativeJavaObject))
			throw new IllegalArgumentException(obj + " isn't NativeJavaObject");

		Prov prov = () -> ((Callable) func)
				.call(cx, scope, null,
				      obj == null ? EMPTY_ARGS : one_arg(wrapAccess(cx, scope, obj)));
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
			if (object instanceof MyNativeJavaObject) {
				if (((MyNativeJavaObject) object).status == status) return object;
			}
			if (object instanceof NativeJavaClass || object instanceof MyNativeJavaClass)
				return wrapFactory.wrapJavaClass(cx, scope, (Class<?>) object.unwrap());
			return wrapFactory.wrapNewObject(cx, scope, object.unwrap());
			// return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		}
		try {
			if (object instanceof NativeJavaClass || object instanceof MyNativeJavaClass)
				return new MyNativeJavaClass(scope, (Class<?>) object.unwrap(), true);
			return new MyNativeJavaObject(scope, object.unwrap(), object.getClass(), false);
		} finally {
			status = last;
		}
	}

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

	public static class DelegatingScope implements Scriptable {
		public Scriptable delegate;

		public DelegatingScope(Scriptable delegate) {
			this.delegate = delegate;
		}

		public String getClassName() {
			return delegate.getClassName();
		}

		public Object get(String name, Scriptable start) {
			Object tmp = delegate.get(name, start);
			// Log.info("ok");
			if (tmp instanceof MyNativeJavaObject) {
				MyNativeJavaObject obj = (MyNativeJavaObject) tmp;
				if (obj.status != status) {
					obj.initMembers();
				}
			}
			return tmp;
		}

		public Object get(int index, Scriptable start) {
			return delegate.get(index, start);
		}

		public boolean has(String name, Scriptable start) {
			return delegate.has(name, start);
		}

		public boolean has(int index, Scriptable start) {
			return delegate.has(index, start);
		}

		public void put(String name, Scriptable start, Object value) {
			if (this == start) start = Vars.mods.getScripts().scope;
			delegate.put(name, start, value);
		}

		public void put(int index, Scriptable start, Object value) {
			if (this == start) start = Vars.mods.getScripts().scope;
			delegate.put(index, start, value);
		}

		public void delete(String name) {
			delegate.delete(name);
		}

		public void delete(int index) {
			delegate.delete(index);
		}

		public Scriptable getPrototype() {
			Scriptable proto = delegate.getPrototype();
			if (proto == this) return null;
			return proto;
		}

		public void setPrototype(Scriptable scriptable) {
			delegate.setPrototype(scriptable);
		}

		public Scriptable getParentScope() {
			Scriptable scope = delegate.getParentScope();
			return scope instanceof DelegatingScope || scope == null ? scope : new DelegatingScope(scope);
		}

		public void setParentScope(Scriptable scope) {
			delegate.setParentScope(scope instanceof DelegatingScope || scope == null ? scope : new DelegatingScope(scope));
		}

		public Object[] getIds() {
			return delegate.getIds();
		}

		public Object getDefaultValue(Class<?> hint) {
			return delegate.getDefaultValue(hint);
		}

		public boolean hasInstance(Scriptable scriptable) {
			return delegate.hasInstance(scriptable);
		}
	}
}
