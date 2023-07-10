package better_js.mytest;

import better_js.*;
import better_js.reflect.JDKVars;
import better_js.utils.ByteCodeTools.MyClass;

public class TestFinal {
	public static void main(String[] args) throws Throwable {
		Desktop.init(getLoader());
		MyClass<?> f = new MyClass<>(Child.class.getName() + "1", Child.class);
		byte[] bytes = f.writer.toByteArray();
		JDKVars.junsafe.defineClass0(null, bytes, 0, bytes.length, getLoader(), null);
	}
	private static ClassLoader getLoader() {
		return TestFinal.class.getClassLoader();
	}

	public static class F2 extends F {
			public void a() {
				super.a();
			}
		}

	public static class F extends Child {
		public void a(){
			super.a();
		}
	}
	public static class Child {
		public void a(){
		}
		private Child(){
			a();
		}
	}
}
