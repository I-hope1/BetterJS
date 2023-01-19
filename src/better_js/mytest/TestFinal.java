package better_js.mytest;

import better_js.*;
import better_js.reflect.JDKVars;
import better_js.utils.*;
import better_js.utils.ByteCodeTools.MyClass;

public class TestFinal {
	public static void main(String[] args) throws Throwable {
		Desktop.main(args);
		var f = new MyClass<>(FINAL.class.getName() + "1", FINAL.class);
		byte[] bytes = f.writer.toByteArray();
		JDKVars.unsafe.defineClass0(null, bytes, 0, bytes.length, MyReflect.IMPL_LOADER, null);
	}

	public static final class FINAL {}
}
