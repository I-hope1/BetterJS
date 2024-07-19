package rhino.regexp;

import rhino.Context;
import rhino.Scriptable;

public class NativeRegExpInstantiator {

    private NativeRegExpInstantiator() {}

    static NativeRegExp withLanguageVersion(int languageVersion) {
        if (languageVersion < Context.VERSION_ES6) {
            return new NativeRegExpCallable();
        } else {
            return new NativeRegExp();
        }
    }

    static NativeRegExp withLanguageVersionScopeCompiled(
            int languageVersion, Scriptable scope, RECompiled compiled) {
        if (languageVersion < Context.VERSION_ES6) {
            return new NativeRegExpCallable(scope, compiled);
        } else {
            return new NativeRegExp(scope, compiled);
        }
    }
}
