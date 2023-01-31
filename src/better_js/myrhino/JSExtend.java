package better_js.myrhino;

import rhino.*;

public class JSExtend implements IdFunctionCall {
	private static final Object FTAG = "JSExtend";

	@Override
	public Object execIdCall(IdFunctionObject f, Context context, Scriptable scope, Scriptable thisObj, Object[] objects) {
		if (f.hasTag(FTAG) && f.methodId() == 1) {
			// return createClass();
		}
		throw f.unknown();
	}

	public static void init(Context cx, Scriptable scope, boolean sealed) {
		JSExtend obj = new JSExtend();
		IdFunctionObject ctor = new IdFunctionObject(obj, FTAG, 1, "JSExtend", 1, scope);
		ctor.markAsConstructor(null);
		if (sealed) {
			ctor.sealObject();
		}

		ctor.exportAsScopeProperty();
	}
}
