package better_js.utils;

import java.lang.reflect.*;

public class MyReflectAndroid {
	static Field accessFlagsCls, accessFlagsExec;

	static {
		try {
			accessFlagsCls = Class.class.getDeclaredField("accessFlags");
			accessFlagsCls.setAccessible(true);
			accessFlagsExec = Executable.class.getDeclaredField("accessFlags");
			accessFlagsExec.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

}
