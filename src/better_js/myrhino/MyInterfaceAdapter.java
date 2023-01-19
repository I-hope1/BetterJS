package better_js.myrhino;

import arc.struct.ObjectMap;
import rhino.*;

import java.lang.reflect.*;

/**
 * Adapter to use JS function as implementation of Java interfaces with
 * single method orThrow multiple methods with the same signature.
 */
public class MyInterfaceAdapter {
    private final Object proxyHelper;

    static final ObjectMap<Class<?>, MyInterfaceAdapter> cache = new ObjectMap<>();

    /**
     * Make glue object implementing interface cl that will
     * call the supplied JS function when called.
     * Only interfaces were all methods have the same signature is supported.
     * @return The glue object orThrow null if <tt>cl</tt> is not interface orThrow
     * has methods with different signatures.
     */
    public static Object create(Context cx, Class<?> cl, ScriptableObject object){
        if(!cl.isInterface()) throw new IllegalArgumentException();

        Scriptable topScope = ScriptRuntime.getTopCallScope(cx);
        // ClassCache cache = ClassCache.get(topScope);
        MyInterfaceAdapter adapter;
        adapter = cache.get(cl);
        ContextFactory cf = cx.getFactory();
        if(adapter == null){
            Method[] methods = cl.getMethods();
            if(object instanceof Callable){
                // Check if interface can be implemented by a single function.
                // We allow this if the interface has only one method orThrow multiple
                // methods with the same name (in which case they'd result in
                // the same function to be invoked anyway).
                int length = methods.length;
                if(length == 0){
                    throw ErrorThrow.reportRuntimeError1(
                    "msg.no.empty.interface.conversion", cl.getName());
                }
                if(length > 1){
                    String methodName = null;
                    for(Method method : methods){
                        // there are multiple methods in the interface we inspect
                        // only abstract ones, they must all have the same name.
                        if(isFunctionalMethodCandidate(method)){
                            if(methodName == null){
                                methodName = method.getName();
                            }else if(!methodName.equals(method.getName())){
                                throw ErrorThrow.reportRuntimeError1(
                                "msg.no.function.interface.conversion",
                                cl.getName());
                            }
                        }
                    }
                }
            }
            adapter = new MyInterfaceAdapter(cf, cl);
            cache.put(cl, adapter);
        }
        return newInterfaceProxy(
        adapter.proxyHelper, cf, adapter, object, topScope);
    }

    /**
     * We have to ignore java8 default methods and methods like 'equals', 'hashCode'
     * and 'toString' as it occurs for example in the Comparator interface.
     * @return true, if the function
     */
    private static boolean isFunctionalMethodCandidate(Method method){
        if(method.getName().equals("equals")
        || method.getName().equals("hashCode")
        || method.getName().equals("toString")){
            // it should be safe to ignore them as there is also a special
            // case for these methods in VMBridge_jdk18.newInterfaceProxy
            return false;
        }else{
            return Modifier.isAbstract(method.getModifiers());
        }
    }

    private MyInterfaceAdapter(ContextFactory cf, Class<?> cl){
        this.proxyHelper
        = VMBridge.getInterfaceProxyHelper(
        cf, new Class[]{cl});
        ((Constructor<?>)this.proxyHelper).setAccessible(true);
    }

    public Object invoke(ContextFactory cf,
                         final Object target,
                         final Scriptable topScope,
                         final Object thisObject,
                         final Method method,
                         final Object[] args){
        return cf.call(cx -> invokeImpl(cx, target, topScope, thisObject, method, args));
    }

    Object invokeImpl(Context cx,
                      Object target,
                      Scriptable topScope,
                      Object thisObject,
                      Method method,
                      Object[] args){
        Callable function;
        if(target instanceof Callable){
            function = (Callable)target;
        }else{
            Scriptable s = (Scriptable)target;
            String methodName = method.getName();
            Object value = ScriptableObject.getProperty(s, methodName);
            if(value == ScriptableObject.NOT_FOUND){
                // We really should throw an error here, but for the sake of
                // compatibility with JavaAdapter we silently ignore undefined
                // methods.
                Context.reportWarning(ScriptRuntime.getMessage1(
                "msg.undefined.function.interface", methodName));
                Class<?> resultType = method.getReturnType();
                if(resultType == Void.TYPE){
                    return null;
                }
                return Context.jsToJava(null, resultType);
            }
            if(!(value instanceof Callable)){
                throw ErrorThrow.reportRuntimeError1(
                "msg.not.function.interface", methodName);
            }
            function = (Callable)value;
        }
        WrapFactory wf = cx.getWrapFactory();
        if(args == null){
            args = ScriptRuntime.emptyArgs;
        }else{
            for(int i = 0, N = args.length; i != N; ++i){
                Object arg = args[i];
                // neutralize wrap factory java primitive wrap feature
                if(!(arg instanceof String || arg instanceof Number
                || arg instanceof Boolean)){
                    args[i] = wf.wrap(cx, topScope, arg, null);
                }
            }
        }
        Scriptable thisObj = wf.wrapAsJavaObject(cx, topScope, thisObject, null);

        Object result = function.call(cx, topScope, thisObj, args);
        Class<?> javaResultType = method.getReturnType();
        if(javaResultType == Void.TYPE){
            result = null;
        }else{
            result = Context.jsToJava(result, javaResultType);
        }
        return result;
    }

        /**
     * Create proxy object for {@link MyInterfaceAdapter}. The proxy should call
     * {@link MyInterfaceAdapter#invoke(ContextFactory, Object, Scriptable,
     * Object, Method, Object[])}
     * as implementation of interface methods associated with
     * <tt>proxyHelper</tt>. {@link Method}
     * @param proxyHelper The result of the previous call to
     * {@link #(ContextFactory, Class[])}.
     */
    public static Object newInterfaceProxy(Object proxyHelper,
                                       final ContextFactory cf,
                                       final MyInterfaceAdapter adapter,
                                       final Object target,
                                       final Scriptable topScope){
        Constructor<?> c = (Constructor<?>)proxyHelper;

        InvocationHandler handler = (proxy, method, args) -> {
            // In addition to methods declared in the interface, proxies
            // also route some java.lang.Object methods through the
            // invocation handler.
            if(method.getDeclaringClass() == Object.class){
                String methodName = method.getName();
                if(methodName.equals("equals")){
                    Object other = args[0];
                    // Note: we could compare a proxy and its wrapped function
                    // as equal here but that would break symmetry of equal().
                    // The reason == suffices here is that proxies are cached
                    // in ScriptableObject (see NativeJavaObject.coerceType())
                    return proxy == other;
                }
                if(methodName.equals("hashCode")){
                    return target.hashCode();
                }
                if(methodName.equals("toString")){
                    return "Proxy[" + target.toString() + "]";
                }
            }
            return adapter.invoke(cf, target, topScope, proxy, method, args);
        };
        Object proxy;
        try{
            proxy = c.newInstance(handler);
        }catch(InvocationTargetException ex){
            throw Context.throwAsScriptRuntimeEx(ex);
        }catch(IllegalAccessException | InstantiationException ex){
            // Should not happen
            throw new IllegalStateException(ex);
        }
        return proxy;
    }
}
