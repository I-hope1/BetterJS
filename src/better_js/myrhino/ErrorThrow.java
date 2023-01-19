package better_js.myrhino;

import rhino.*;

public class ErrorThrow {

	public static RuntimeException reportRuntimeError0(String messageId) {
		return Context.reportRuntimeError(ScriptRuntime.getMessage0(messageId));
	}
	public static RuntimeException reportRuntimeError1(String messageId, Object arg1) {
		return Context.reportRuntimeError(ScriptRuntime.getMessage1(messageId, arg1));
	}

	public static RuntimeException reportRuntimeError2(String messageId, Object arg1, Object arg2) {
		return Context.reportRuntimeError(ScriptRuntime.getMessage2(messageId, arg1, arg2));
	}

	public static RuntimeException reportRuntimeError3(String messageId, Object arg1, Object arg2, Object arg3) {
		return Context.reportRuntimeError(ScriptRuntime.getMessage3(messageId, arg1, arg2, arg3));
	}

	public static RuntimeException reportRuntimeError4(String messageId, Object arg1, Object arg2, Object arg3, Object arg4) {
		return Context.reportRuntimeError(ScriptRuntime.getMessage4(messageId, arg1, arg2, arg3, arg4));
	}

	protected static Scriptable ensureScriptable(Object arg){
        if(!(arg instanceof Scriptable))
            throw ScriptRuntime.typeError1("msg.arg.not.object", ScriptRuntime.typeof(arg));
        return (Scriptable)arg;
    }
}
