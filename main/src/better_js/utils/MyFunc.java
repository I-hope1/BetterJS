package better_js.utils;

import rhino.*;

public class MyFunc implements Function {
	Receiver receiver;
	public MyFunc(Receiver receiver) {
		this.receiver = receiver;
	}
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length == 0) throw new IllegalArgumentException("wrong args length");
		return receiver.get(cx, scope, args);
	}
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new RuntimeException();
	}
	public String getClassName() {
		return "aaa";
	}
	public Object get(String name, Scriptable start) {
		return null;
	}
	public Object get(int index, Scriptable start) {
		return null;
	}
	public boolean has(String name, Scriptable start) {
		return false;
	}
	public boolean has(int index, Scriptable start) {
		return false;
	}
	public void put(String name, Scriptable start, Object value) {}
	public void put(int index, Scriptable start, Object value) {}
	public void delete(String name) {}
	public void delete(int index) {}
	public Scriptable getPrototype() {
		return null;
	}
	public void setPrototype(Scriptable prototype) {}
	public Scriptable getParentScope() {
		return null;
	}
	public void setParentScope(Scriptable parent) {}
	public Object[] getIds() {
		return new Object[0];
	}
	public Object getDefaultValue(Class<?> hint) {
		return null;
	}
	public boolean hasInstance(Scriptable instance) {
		return false;
	}
	public interface Receiver {
		Object get(Context ctx, Scriptable scope, Object[] args);
	}
}
