package better_js.myrhino;

import rhino.*;

/**
 * This class reflects a single Java constructor into the JavaScript
 * environment.  It satisfies a request for an overloaded constructor,
 * as introduced in LiveConnect 3.
 * All NativeJavaConstructors behave as JSRef `bound' methods, in that they
 * always construct the same NativeJavaClass regardless of any reparenting
 * that may occur.
 * @author Frank Mitchell
 * @see NativeJavaMethod
 * @see NativeJavaPackage
 * @see MyNativeJavaClass
 */

public class MyNativeJavaConstructor extends BaseFunction{
    MyMemberBox ctor;

    public MyNativeJavaConstructor(MyMemberBox ctor){
        this.ctor = ctor;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                       Object[] args){
        return MyNativeJavaClass.constructSpecific(cx, scope, args, ctor);
    }

    @Override
    public String getFunctionName(){
        String sig = MyJavaMembers.liveConnectSignature(ctor.argTypes);
        return "<init>".concat(sig);
    }

    @Override
    public String toString(){
        return "[JavaConstructor " + ctor.getName() + "]";
    }
}

