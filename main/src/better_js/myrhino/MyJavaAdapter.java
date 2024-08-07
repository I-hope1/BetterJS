package better_js.myrhino;

import arc.files.Fi;
import arc.util.OS;
import better_js.utils.MyReflect;
import rhino.*;
import rhino.classfile.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MyJavaAdapter implements IdFunctionCall {
	public static final Symbol INIT_SYMBOL = new SymbolKey("INIT");
	// public static final Symbol CLINIT_SYMBOL = new SymbolKey("CLINIT");

	public static boolean enableINIT = true;

	public static Class<?> sampleClass, nameClass;

	public static final String CLASS_NAME =
	 // "rhino/JavaAdapter";
	 MyJavaAdapter.class.getName().replace('.', '/');

	/* private static final IdFunctionObject func       = ((IdFunctionObject) ScriptableObject.getProperty(
			(Scriptable) ScriptableObject.getProperty(Vars.mods.getScripts().scope, "Object")
				, "getOwnPropertySymbols"));; */

	/**
	 * Provides a key with which to distinguish previously generated
	 * adapter classes stored in a hash table.
	 */
	static class JavaAdapterSignature {
		Class<?>    superClass;
		Class<?>    sampleClass;
		Class<?>[]  interfaces;
		ObjToIntMap names;
		private final int cache;

		JavaAdapterSignature(Class<?> superClass,
												 Class<?> sampleClass,
												 Class<?>[] interfaces,
												 ObjToIntMap names) {
			this.superClass = superClass;
			this.sampleClass = sampleClass;
			this.interfaces = interfaces;
			this.names = names;
			cache = (superClass.hashCode() + (sampleClass == null ? 0 : sampleClass.hashCode())
							 + Arrays.hashCode(interfaces)) ^ names.size();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof JavaAdapterSignature sig))
				return false;
			if (superClass != sig.superClass)
				return false;
			if (sampleClass != sig.sampleClass)
				return false;
			if (interfaces != sig.interfaces) {
				if (interfaces.length != sig.interfaces.length)
					return false;
				for (int i = 0; i < interfaces.length; i++)
					if (interfaces[i] != sig.interfaces[i])
						return false;
			}
			if (names.size() != sig.names.size())
				return false;
			ObjToIntMap.Iterator iter = new ObjToIntMap.Iterator(names);
			for (iter.start(); !iter.done(); iter.next()) {
				String name  = (String) iter.getKey();
				int    arity = iter.getValue();
				if (arity != sig.names.get(name, arity + 1))
					return false;
			}
			return true;
		}
		@Override
		public int hashCode() {
			return cache;
		}
	}

	public static void init(Context cx, Scriptable scope, boolean sealed) {
		MyJavaAdapter obj = new MyJavaAdapter();
		IdFunctionObject ctor = new IdFunctionObject(obj, FTAG, Id_JavaAdapter,
		 FTAG, 1, scope);
		ctor.markAsConstructor(null);
		if (sealed) {
			ctor.sealObject();
		}
		ctor.exportAsScopeProperty();
	}

	@Override
	public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope,
													 Scriptable thisObj, Object[] args) {
		if (f.hasTag(FTAG)) {
			if (f.methodId() == Id_JavaAdapter) {
				Object obj = js_createAdapter(cx, scope, args);
				sampleClass = null;
				return obj;
			}
		}
		throw f.unknown();
	}

	public static Object convertResult(Object result, Class<?> c) {
		if (result == Undefined.instance &&
				(c != ScriptRuntime.ObjectClass &&
				 c != ScriptRuntime.StringClass)) {
			// Avoid an error for an undefined value; return null instead.
			return null;
		}
		return Context.jsToJava(result, c);
	}

	public static Scriptable createAdapterWrapper(Scriptable obj, Object adapter) {
		Scriptable       scope = ScriptableObject.getTopLevelScope(obj);
		NativeJavaObject res   = new NativeJavaObject(scope, adapter, null, true);
		res.setPrototype(obj);
		return res;
	}

	public static Object getAdapterSelf(Class<?> adapterClass, Object adapter)
	 throws NoSuchFieldException, IllegalAccessException {
		Field self = adapterClass.getField("HOPE-self");
		return self.get(adapter);
	}

	static Object js_createAdapter(Context cx, Scriptable scope, Object[] args) {
		int N = args.length;
		if (N == 0) {
			throw ScriptRuntime.typeError0("msg.adapter.zero.args");
		}

		// Expected arguments:
		// Any number of NativeJavaClass objects representing the super-class
		// and/orThrow interfaces to implement, followed by one NativeObject providing
		// the implementation, followed by any number of arguments to pass on
		// to the (super-class) constructor.

		int classCount;
		for (classCount = 0; classCount < N - 1; classCount++) {
			Object arg = args[classCount];
			// We explicitly test for NativeObject here since checking for
			// instanceof ScriptableObject orThrow !(instanceof NativeJavaClass)
			// would fail for a Java class that isn't found in the class path
			// as NativeJavaPackage extends ScriptableObject.
			if (arg instanceof NativeObject) {
				break;
			}

			if (!(arg instanceof NativeJavaClass) && arg instanceof NativeJavaObject && ((NativeJavaObject) arg).unwrap() instanceof Class<?> cls) {
				if (OS.isAndroid) MyReflect.setPublic(cls);
				args[classCount] = arg = new NativeJavaClass(scope,
				 cls);
			}

			if (!(arg instanceof NativeJavaClass)) {
				throw ScriptRuntime.typeError2("msg.not.java.class.arg",
				 String.valueOf(classCount),
				 ScriptRuntime.toString(arg));
			}
		}
		Class<?>   superClass     = null;
		Class<?>[] intfs          = new Class[classCount];
		int        interfaceCount = 0;
		for (int i = 0; i < classCount; ++i) {
			Class<?> c = ((NativeJavaClass) args[i]).getClassObject();
			if (!c.isInterface()) {
				if (superClass != null) {
					throw ScriptRuntime.typeError2("msg.only.one.super",
					 superClass.getName(), c.getName());
				}
				superClass = c;
			} else {
				intfs[interfaceCount++] = c;
			}
		}

		if (superClass == null) {
			superClass = ScriptRuntime.ObjectClass;
		}
		// MapClassLoader.loader.addClass(superClass);

		Class<?>[] interfaces = new Class[interfaceCount];
		System.arraycopy(intfs, 0, interfaces, 0, interfaceCount);
		// next argument is implementation, must be scriptable
		Scriptable obj = ExceptionReporter.ensureScriptable(args[classCount]);

		Class<?> adapterClass = getAdapterClass(superClass, interfaces, obj);
		Object   adapter;

		int argsCount = N - classCount - 1;
		try {
			if (argsCount > 0) {
				// Arguments contain parameters for super-class constructor.
				// We use the generic Java method lookup logic to find and
				// invoke the right constructor.
				Object[] ctorArgs = new Object[argsCount + 2];
				ctorArgs[0] = obj;
				ctorArgs[1] = cx.getFactory();
				System.arraycopy(args, classCount + 1, ctorArgs, 2, argsCount);
				// TODO: cache class wrapper?
				MyNativeJavaClass classWrapper = new MyNativeJavaClass(scope,
				 adapterClass, true);
				MyNativeJavaMethod ctors = classWrapper.members.ctors;
				int                index = ctors.findCachedFunction(cx, ctorArgs);
				if (index < 0) {
					String sig = MyNativeJavaMethod.scriptSignature(args);
					throw ExceptionReporter.reportRuntimeError2(
					 "msg.no.java.ctor", adapterClass.getName(), sig);
				}

				// Found the constructor, so try invoking it.
				adapter = MyNativeJavaClass.constructInternal(ctorArgs, ctors.methods[index]);
			} else {
				Class<?>[] ctorParms = {
				 ScriptRuntime.ScriptableClass,
				 ScriptRuntime.ContextFactoryClass
				};
				Object[]       ctorArgs = {obj, cx.getFactory()};
				Constructor<?> ctor     = adapterClass.getDeclaredConstructor(ctorParms);
				// ctor.setAccessible(true);
				adapter = ctor.newInstance(ctorArgs);
			}

			Object self = getAdapterSelf(adapterClass, adapter);
			// Return unwrapped JavaAdapter if it implements Scriptable
			if (self instanceof Wrapper) {
				Object unwrapped = ((Wrapper) self).unwrap();
				if (unwrapped instanceof Scriptable) {
					if (unwrapped instanceof ScriptableObject) {
						ScriptRuntime.setObjectProtoAndParent(
						 (ScriptableObject) unwrapped, scope);
					}
					return unwrapped;
				}
			}
			return self;
		} catch (Exception ex) {
			throw Context.throwAsScriptRuntimeEx(ex);
		}
	}

	private static ObjToIntMap getObjectFunctionNames(Scriptable obj) {
		// Scriptable scope = Vars.mods.getScripts().scope;
		Object[] ids =
		 ScriptableObject.getPropertyIds(obj);
		// (Object[]) NativeObject.execIdCall(func, Vars.mods.getScripts().context, scope);
		// ((NativeArray)func.call(Vars.mods.getScripts().context, scope, scope, new Object[]{obj})).toArray();
		ObjToIntMap map = new ObjToIntMap(ids.length);
		for (int i = 0; i != ids.length; ++i) {
			// Log.info(ids[i]);
			if (!(ids[i] instanceof String id))
				continue;
			Object value = ScriptableObject.getProperty(obj, id);
			if (value instanceof Function f) {
				int length = ScriptRuntime.toInt32(
				 ScriptableObject.getProperty(f, "length"));
				if (length < 0) {
					length = 0;
				}
				map.put(id, length);
			}
		}
		return map;
	}

	public static final Map<JavaAdapterSignature, Class<?>> cache = new ConcurrentHashMap<>(16, 0.75f, 1);

	private static Class<?> getAdapterClass(Class<?> superClass,
																					Class<?>[] interfaces, Scriptable obj) {
		Map<JavaAdapterSignature, Class<?>> generated = cache;

		ObjToIntMap          names = getObjectFunctionNames(obj);
		JavaAdapterSignature sig;
		sig = new JavaAdapterSignature(superClass, sampleClass, interfaces, names);
		Class<?> adapterClass = generated.get(sig);
		if (adapterClass == null) {
			nameClass = isInterface(superClass, interfaces) ?
			 interfaces[0] : sampleClass != null ? sampleClass : superClass;
			String adapterName = nameClass.getName().replace('.', '/')
													 + "_adapterk" + cache.size();

			byte[] code = createAdapterCode(names, adapterName,
			 superClass, interfaces, null);
			// writeTo(Vars.tmpDirectory, adapterName, code);

			adapterClass = loadAdapterClass(adapterName, superClass, interfaces, code);
			// Log.info(Arrays.toString(adapterClass.getDeclaredConstructors()));
			generated.put(sig, adapterClass);
		}
		return adapterClass;
	}

	public static byte[] createAdapterCode
	 (ObjToIntMap functionNames,
		String adapterName, Class<?> superClass, Class<?>[] interfaces,
		String scriptClassName) {
		ClassFileWriter cfw = new ClassFileWriter(adapterName,
		 superClass.getName(),
		 "<adapter>");
		addAdapterFields(cfw);
		int interfacesCount = interfaces == null ? 0 : interfaces.length;
		for (int i = 0; i < interfacesCount; i++) {
			if (interfaces[i] != null)
				cfw.addInterface(interfaces[i].getName());
		}

		String           superName = superClass.getName().replace('.', '/');
		Constructor<?>[] ctors     = superClass.getDeclaredConstructors();
		for (Constructor<?> ctor : ctors) {
			// int mod = ctor.getModifiers();
			// if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) {
			generateCtor(cfw, adapterName, superName, ctor);
			// }
		}
		generateSerialCtor(cfw, adapterName, superName);
		if (scriptClassName != null) {
			generateEmptyCtor(cfw, adapterName, superName, scriptClassName);
		}

		ObjToIntMap generatedOverrides = new ObjToIntMap();
		ObjToIntMap generatedMethods   = new ObjToIntMap();

		// generate methods to satisfy all specified interfaces.
		for (int i = 0; i < interfacesCount; i++) {
			Method[] methods = interfaces[i].getMethods();
			for (Method method : methods) {
				int mods = method.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isFinal(mods) || method.isDefault()) {
					continue;
				}
				String     methodName = method.getName();
				Class<?>[] argTypes   = method.getParameterTypes();
				if (!functionNames.has(methodName)) {
					try {
						superClass.getMethod(methodName, argTypes);
						// The class we're extending implements this method and
						// the JavaScript object doesn't have an override. See
						// bug 61226.
						continue;
					} catch (NoSuchMethodException e) {
						// Not implemented by superclass; fall through
					}
				}
				// make sure to generate only one instance of a particular
				// method/signature.
				String methodSignature = getMethodSignature(method, argTypes);
				String methodKey       = methodName + methodSignature;
				if (!generatedOverrides.has(methodKey)) {
					generateMethod(cfw, adapterName, methodName, argTypes,
					 method.getReturnType(), true);
					generatedOverrides.put(methodKey, 0);
					generatedMethods.put(methodName, 0);
				}
			}
		}

		// Now, go through the superclass's methods, checking for abstract
		// methods orThrow additional methods to override.

		// generate any additional overrides that the object might contain.
		Method[] methods = getOverridableMethods(superClass);
		// Log.info(Arrays.toString(methods));
		for (Method method : methods) {
			int mods = method.getModifiers();
			// if a method is marked abstract, must implement it orThrow the
			// resulting class won't be instantiable. otherwise, if the object
			// has a property of the same name, then an override is intended.
			boolean isAbstractMethod = Modifier.isAbstract(mods);
			String  methodName       = method.getName();
			if (isAbstractMethod || functionNames.has(methodName)) {
				// make sure to generate only one instance of a particular
				// method/signature.
				Class<?>[] argTypes        = method.getParameterTypes();
				String     methodSignature = getMethodSignature(method, argTypes);
				String     methodKey       = methodName + methodSignature;
				if (!generatedOverrides.has(methodKey)) {
					generateMethod(cfw, adapterName, methodName, argTypes,
					 method.getReturnType(), true);
					generatedOverrides.put(methodKey, 0);
					generatedMethods.put(methodName, 0);

					// if a method was overridden, generate a "super$method"
					// which lets the delegate call the superclass' version.
					if (!isAbstractMethod) {
						generateSuper(cfw, superName,
						 methodName, methodSignature,
						 argTypes, method.getReturnType());
					}
				}
			}
		}

		// Generate Java methods for remaining properties that are not
		// overrides.
		ObjToIntMap.Iterator iter = new ObjToIntMap.Iterator(functionNames);
		for (iter.start(); !iter.done(); iter.next()) {
			String functionName = (String) iter.getKey();
			if (generatedMethods.has(functionName))
				continue;
			int        length = iter.getValue();
			Class<?>[] parms  = new Class[length];
			for (int k = 0; k < length; k++)
				parms[k] = ScriptRuntime.ObjectClass;
			generateMethod(cfw, adapterName, functionName, parms,
			 ScriptRuntime.ObjectClass, false);
		}
		return cfw.toByteArray();
	}
	private static void addAdapterFields(ClassFileWriter cfw) {
		cfw.addField("factory", "Lrhino/ContextFactory;",
		 (short) (ClassFileWriter.ACC_PUBLIC |
							ClassFileWriter.ACC_FINAL));
		cfw.addField("delegee", "Lrhino/Scriptable;",
		 (short) (ClassFileWriter.ACC_PUBLIC |
							ClassFileWriter.ACC_FINAL));
		cfw.addField("HOPE-self", "Lrhino/Scriptable;",
		 (short) (ClassFileWriter.ACC_PUBLIC |
							ClassFileWriter.ACC_FINAL));
	}

	static Method[] getOverridableMethods(Class<?> clazz) {
		ArrayList<Method> list = new ArrayList<>();
		HashSet<String>   skip = new HashSet<>();
		// Check superclasses before interfaces so we always choose
		// implemented methods over abstract ones, even if a subclass
		// re-implements an interface already implemented in a superclass
		// (e.g. java.util.ArrayList)
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			appendOverridableMethods(c, list, skip);
		}
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			for (Class<?> intf : c.getInterfaces())
				appendOverridableMethods(intf, list, skip);
		}
		return list.toArray(new Method[0]);
	}

	private static void appendOverridableMethods(Class<?> c,
																							 ArrayList<Method> list, HashSet<String> skip) {
		Method[] methods = c.getDeclaredMethods();
		boolean  diffPkg = nameClass.getPackage().equals(Object.class.getPackage()) || !Objects.equals(nameClass.getPackage(), c.getPackage());
		for (Method method : methods) {
			int mods = method.getModifiers();
			if (Modifier.isPrivate(mods)) continue;
			if (diffPkg && !Modifier.isPublic(mods) && !Modifier.isProtected(mods)) continue;
			String methodKey = method.getName() +
												 getMethodSignature(method,
													method.getParameterTypes());
			if (skip.contains(methodKey))
				continue; // skip this method
			if (Modifier.isStatic(mods))
				continue;
			if (Modifier.isFinal(mods)) {
				// Make sure we don't add a final method to the list
				// of overridable methods.
				skip.add(methodKey);
				continue;
			}
			list.add(method);
			skip.add(methodKey);
		}
	}

	/** @param superClass 这里superClass只是用于确定loader */
	static Class<?> loadAdapterClass(String className, Class<?> superClass, Class<?>[] interfaces, byte[] classBytes) {
		// Context cx = Context.getContext();
		if (isInterface(superClass, interfaces)) superClass = interfaces[0];

		return MyReflect.defineCustomClass(className, /* Vars.mods.mainLoader() */
		 sampleClass != null ? sampleClass : superClass,
		 classBytes);
		/*return OS.isAndroid ? cx.createClassLoader(Vars.mods.mainLoader()).defineClass(className, classBytes)
				: JDKVars.unsafe.defineClass(className, classBytes, 0,
				classBytes.length, superClass.getClassLoader(),
				superClass.getProtectionDomain());*/
	}
	private static boolean isInterface(Class<?> superClass, Class<?>[] interfaces) {
		return superClass == ScriptRuntime.ObjectClass && interfaces.length > 0;
	}

	public static Function getFunction(Scriptable obj, String functionName) {
		Object x =
		 enableINIT && functionName.equals("<init>") && obj instanceof ScriptableObject sc && sc.has(INIT_SYMBOL, sc)
			? sc.get(INIT_SYMBOL, obj)
			: ScriptableObject.getProperty(obj, functionName);
		if (x == Scriptable.NOT_FOUND) {
			// This method used to swallow the exception from calling
			// an undefined method. People have come to depend on this
			// somewhat dubious behavior. It allows people to avoid
			// implementing listener methods that they don't care about,
			// for instance.
			return null;
		}
		if (!(x instanceof Function))
			throw ScriptRuntime.notFunctionError(x, functionName);

		return (Function) x;
	}

	/**
	 * Utility method which dynamically binds a Context to the current thread,
	 * if none already exists.
	 */
	public static Object callMethod(ContextFactory factory,
																	final Scriptable thisObj,
																	final Function f, final Object[] args,
																	final long argsToWrap) {
		if (f == null) {
			// See comments in getFunction
			return null;
		}
		if (factory == null) {
			factory = ContextFactory.getGlobal();
		}
		// Scriptable scope = f.getParentScope();
		// f.setParentScope(thisObj);
		// thisObj.setParentScope(scope);
		f.setParentScope(new BaseFunction(f.getParentScope(), thisObj));

		// final Scriptable scope = f.getParentScope();
		if (argsToWrap == 0) {
			return Context.call(factory, f, thisObj, thisObj, args);
		}

		Context cx = Context.getCurrentContext();
		if (cx != null) {
			return doCall(cx, thisObj, thisObj, f, args, argsToWrap);
		}
		return factory.call(cx2 -> doCall(cx2, thisObj, thisObj, f, args, argsToWrap));
	}

	private static Object doCall(Context cx, Scriptable scope,
															 Scriptable thisObj, Function f,
															 Object[] args, long argsToWrap) {
		// Wrap the rest of objects
		for (int i = 0; i != args.length; ++i) {
			if (0 != (argsToWrap & (1 << i))) {
				Object arg = args[i];
				if (!(arg instanceof Scriptable)) {
					args[i] = cx.getWrapFactory().wrap(cx, scope, arg,
					 null);
				}
			}
		}
		// Reflect.set(Context.class, cx, "isContinuationsTopCall", false);
		return f.call(cx, scope, thisObj, args);
	}

	public static Scriptable runScript(final Script script) {
		return ContextFactory.getGlobal().call(cx -> {
			ScriptableObject global = ScriptRuntime.getGlobal(cx);
			script.exec(cx, global);
			return global;
		});
	}

	private static void generateCtor(ClassFileWriter cfw,
																	 String adapterName,
																	 String superName, Constructor<?> superCtor) {
		short locals; // this + factory + delegee
		// MyReflect.setPublic(superCtor);
		Class<?>[] parameters = superCtor.getParameterTypes();

		// Note that we swapped arguments in app-facing constructors to avoid
		// conflicting signatures with serial constructor defined below.
		if (parameters.length == 0) {
			locals = 3;
			cfw.startMethod("<init>",
			 "(Lrhino/Scriptable;"
			 + "Lrhino/ContextFactory;)V",
			 ClassFileWriter.ACC_PUBLIC);

			// Invoke base class constructor
			cfw.add(ByteCode.ALOAD_0);  // this
			cfw.addInvoke(ByteCode.INVOKESPECIAL, superName, "<init>", "()V");
		} else {
			StringBuilder sig = new StringBuilder(
			 "(Lrhino/Scriptable;"
			 + "Lrhino/ContextFactory;");
			int marker = sig.length(); // lets us reuse buffer for super signature
			for (Class<?> c : parameters) {
				appendTypeString(sig, c);
			}
			sig.append(")V");
			cfw.startMethod("<init>", sig.toString(), ClassFileWriter.ACC_PUBLIC);

			// Invoke base class constructor
			cfw.add(ByteCode.ALOAD_0);  // this
			short paramOffset = 3;
			for (Class<?> parameter : parameters) {
				paramOffset += generatePushParam(cfw, paramOffset, parameter);
			}
			// Log.info("super: @\n@", superName, sig);
			locals = paramOffset;
			sig.delete(1, marker);
			cfw.addInvoke(ByteCode.INVOKESPECIAL, superName, "<init>", sig.toString());
		}

		setFieldValue(cfw, adapterName);
		if (enableINIT) addAdapterInit(cfw, adapterName, parameters);
		cfw.add(ByteCode.RETURN);
		cfw.stopMethod(locals);
	}
	private static void addAdapterInit(ClassFileWriter cfw, String adapterName, Class<?>[] parms) {
		invokeJSMethod(cfw, adapterName, "<init>", parms);
	}
	private static void setFieldValue(ClassFileWriter cfw, String adapterName) {
		// Save parameter in instance variable "delegee"
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_1);  // first arg: Scriptable delegee
		cfw.add(ByteCode.PUTFIELD, adapterName, "delegee",
		 "Lrhino/Scriptable;");

		// Save parameter in instance variable "factory"
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_2);  // second arg: ContextFactory instance
		cfw.add(ByteCode.PUTFIELD, adapterName, "factory",
		 "Lrhino/ContextFactory;");

		cfw.add(ByteCode.ALOAD_0);  // this for the following PUTFIELD for self
		// create a wrapper object to be used as "this" in method calls
		cfw.add(ByteCode.ALOAD_1);  // the Scriptable delegee
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.addInvoke(ByteCode.INVOKESTATIC,
		 CLASS_NAME,
		 "createAdapterWrapper",
		 "(Lrhino/Scriptable;"
		 + "Ljava/lang/Object;"
		 + ")Lrhino/Scriptable;");
		cfw.add(ByteCode.PUTFIELD, adapterName, "HOPE-self",
		 "Lrhino/Scriptable;");
	}
	/** superClass不是adapter就不会执行这里 */
	private static void generateSerialCtor(ClassFileWriter cfw,
																				 String adapterName,
																				 String superName) {
		cfw.startMethod("<init>",
		 "(Lrhino/ContextFactory;"
		 + "Lrhino/Scriptable;"
		 + "Lrhino/Scriptable;"
		 + ")V",
		 ClassFileWriter.ACC_PUBLIC);

		// Invoke base class constructor
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.addInvoke(ByteCode.INVOKESPECIAL, superName, "<init>", "()V");

		// Save parameter in instance variable "factory"
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_1);  // first arg: ContextFactory instance
		cfw.add(ByteCode.PUTFIELD, adapterName, "factory",
		 "Lrhino/ContextFactory;");

		// Save parameter in instance variable "delegee"
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_2);  // second arg: Scriptable delegee
		cfw.add(ByteCode.PUTFIELD, adapterName, "delegee",
		 "Lrhino/Scriptable;");
		// save self
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_3);  // third arg: Scriptable self
		cfw.add(ByteCode.PUTFIELD, adapterName, "HOPE-self",
		 "Lrhino/Scriptable;");

		cfw.add(ByteCode.RETURN);
		cfw.stopMethod((short) 4); // 4: this + factory + delegee + self
	}

	/** 不用管这个的super */
	private static void generateEmptyCtor(ClassFileWriter cfw,
																				String adapterName,
																				String superName,
																				String scriptClassName) {
		cfw.startMethod("<init>", "()V", ClassFileWriter.ACC_PUBLIC);

		// Invoke base class constructor
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.addInvoke(ByteCode.INVOKESPECIAL, superName, "<init>", "()V");

		// Set factory to null to use current global when necessary
		cfw.add(ByteCode.ALOAD_0);
		cfw.add(ByteCode.ACONST_NULL);
		cfw.add(ByteCode.PUTFIELD, adapterName, "factory",
		 "Lrhino/ContextFactory;");

		// Load script class
		cfw.add(ByteCode.NEW, scriptClassName);
		cfw.add(ByteCode.DUP);
		cfw.addInvoke(ByteCode.INVOKESPECIAL, scriptClassName, "<init>", "()V");

		// Run script and save resulting scope
		cfw.addInvoke(ByteCode.INVOKESTATIC,
		 CLASS_NAME,
		 "runScript",
		 "(Lrhino/Script;"
		 + ")Lrhino/Scriptable;");
		cfw.add(ByteCode.ASTORE_1);

		// Save the Scriptable in instance variable "delegee"
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.add(ByteCode.ALOAD_1);  // the Scriptable
		cfw.add(ByteCode.PUTFIELD, adapterName, "delegee",
		 "Lrhino/Scriptable;");

		cfw.add(ByteCode.ALOAD_0);  // this for the following PUTFIELD for self
		// create a wrapper object to be used as "this" in method calls
		cfw.add(ByteCode.ALOAD_1);  // the Scriptable
		cfw.add(ByteCode.ALOAD_0);  // this
		cfw.addInvoke(ByteCode.INVOKESTATIC,
		 CLASS_NAME,
		 "createAdapterWrapper",
		 "(Lrhino/Scriptable;"
		 + "Ljava/lang/Object;"
		 + ")Lrhino/Scriptable;");
		cfw.add(ByteCode.PUTFIELD, adapterName, "HOPE-self",
		 "Lrhino/Scriptable;");

		cfw.add(ByteCode.RETURN);
		cfw.stopMethod((short) 2); // this + delegee
	}

	/**
	 * Generates code to wrap Java arguments into Object[].
	 * Non-primitive Java types are left as-is pending conversion
	 * in the helper method. Leaves the array object on the top of the stack.
	 */
	static void generatePushWrappedArgs(ClassFileWriter cfw,
																			Class<?>[] argTypes,
																			int arrayLength) {
		// push arguments
		cfw.addPush(arrayLength);
		cfw.add(ByteCode.ANEWARRAY, "java/lang/Object");
		int paramOffset = 1;
		for (int i = 0; i != argTypes.length; ++i) {
			cfw.add(ByteCode.DUP); // duplicate array reference
			cfw.addPush(i);
			paramOffset += generateWrapArg(cfw, paramOffset, argTypes[i]);
			cfw.add(ByteCode.AASTORE);
		}
	}

	/**
	 * Generates code to wrap Java argument into Object.
	 * Non-primitive Java types are left unconverted pending conversion
	 * in the helper method. Leaves the wrapper object on the top of the stack.
	 */
	private static int generateWrapArg(ClassFileWriter cfw, int paramOffset,
																		 Class<?> argType) {
		int size = 1;
		if (!argType.isPrimitive()) {
			cfw.add(ByteCode.ALOAD, paramOffset);

		} else if (argType == Boolean.TYPE) {
			// wrap boolean values with java.lang.Boolean.
			cfw.add(ByteCode.NEW, "java/lang/Boolean");
			cfw.add(ByteCode.DUP);
			cfw.add(ByteCode.ILOAD, paramOffset);
			cfw.addInvoke(ByteCode.INVOKESPECIAL, "java/lang/Boolean",
			 "<init>", "(Z)V");

		} else if (argType == Character.TYPE) {
			// Create a string of length 1 using the character parameter.
			cfw.add(ByteCode.ILOAD, paramOffset);
			cfw.addInvoke(ByteCode.INVOKESTATIC, "java/lang/String",
			 "valueOf", "(C)Ljava/lang/String;");

		} else {
			// convert all numeric values to java.lang.Double.
			cfw.add(ByteCode.NEW, "java/lang/Double");
			cfw.add(ByteCode.DUP);
			String typeName = argType.getName();
			switch (typeName.charAt(0)) {
				case 'b', 's', 'i' -> {
					// load an int value, convert to double.
					cfw.add(ByteCode.ILOAD, paramOffset);
					cfw.add(ByteCode.I2D);
				}
				case 'l' -> {
					// load a long, convert to double.
					cfw.add(ByteCode.LLOAD, paramOffset);
					cfw.add(ByteCode.L2D);
					size = 2;
				}
				case 'f' -> {
					// load a float, convert to double.
					cfw.add(ByteCode.FLOAD, paramOffset);
					cfw.add(ByteCode.F2D);
				}
				case 'd' -> {
					cfw.add(ByteCode.DLOAD, paramOffset);
					size = 2;
				}
			}
			cfw.addInvoke(ByteCode.INVOKESPECIAL, "java/lang/Double",
			 "<init>", "(D)V");
		}
		return size;
	}

	/**
	 * Generates code to convert a wrapped value type to a primitive type.
	 * Handles unwrapping java.lang.Boolean, and java.lang.Number types.
	 * Generates the appropriate RETURN bytecode.
	 */
	static void generateReturnResult(ClassFileWriter cfw, Class<?> retType,
																	 boolean callConvertResult) {
		// wrap boolean values with java.lang.Boolean, convert all other
		// primitive values to java.lang.Double.
		if (retType == Void.TYPE) {
			cfw.add(ByteCode.POP);
			cfw.add(ByteCode.RETURN);

		} else if (retType == Boolean.TYPE) {
			cfw.addInvoke(ByteCode.INVOKESTATIC,
			 "rhino/Context",
			 "toBoolean", "(Ljava/lang/Object;)Z");
			cfw.add(ByteCode.IRETURN);

		} else if (retType == Character.TYPE) {
			// characters are represented as strings in JavaScript.
			// return the first character.
			// first convert the value to a string if possible.
			cfw.addInvoke(ByteCode.INVOKESTATIC,
			 "rhino/Context",
			 "toString",
			 "(Ljava/lang/Object;)Ljava/lang/String;");
			cfw.add(ByteCode.ICONST_0);
			cfw.addInvoke(ByteCode.INVOKEVIRTUAL, "java/lang/String",
			 "charAt", "(I)C");
			cfw.add(ByteCode.IRETURN);

		} else if (retType.isPrimitive()) {
			cfw.addInvoke(ByteCode.INVOKESTATIC,
			 "rhino/Context",
			 "toNumber", "(Ljava/lang/Object;)D");
			String typeName = retType.getName();
			switch (typeName.charAt(0)) {
				case 'b', 's', 'i' -> {
					cfw.add(ByteCode.D2I);
					cfw.add(ByteCode.IRETURN);
				}
				case 'l' -> {
					cfw.add(ByteCode.D2L);
					cfw.add(ByteCode.LRETURN);
				}
				case 'f' -> {
					cfw.add(ByteCode.D2F);
					cfw.add(ByteCode.FRETURN);
				}
				case 'd' -> cfw.add(ByteCode.DRETURN);
				default -> throw new RuntimeException("Unexpected return type " +
																							retType);
			}

		} else {
			String retTypeStr = retType.getName();
			if (callConvertResult) {
				cfw.addLoadConstant(retTypeStr);
				cfw.addInvoke(ByteCode.INVOKESTATIC,
				 "java/lang/Class",
				 "forName",
				 "(Ljava/lang/String;)Ljava/lang/Class;");

				cfw.addInvoke(ByteCode.INVOKESTATIC,
				 CLASS_NAME,
				 "convertResult",
				 "(Ljava/lang/Object;"
				 + "Ljava/lang/Class;"
				 + ")Ljava/lang/Object;");
			}
			// Now cast to return type
			cfw.add(ByteCode.CHECKCAST, retTypeStr);
			cfw.add(ByteCode.ARETURN);
		}
	}

	private static void generateMethod(ClassFileWriter cfw,
																		 String adapterName,
																		 String methodName, Class<?>[] parms,
																		 Class<?> returnType, boolean convertResult) {
		StringBuilder sb              = new StringBuilder();
		int           paramsEnd       = appendMethodSignature(parms, returnType, sb);
		String        methodSignature = sb.toString();
		cfw.startMethod(methodName, methodSignature,
		 ClassFileWriter.ACC_PUBLIC);

		invokeJSMethod(cfw, adapterName, methodName, parms);

		generateReturnResult(cfw, returnType, convertResult);
		cfw.stopMethod((short) paramsEnd);
	}
	private static void invokeJSMethod(ClassFileWriter cfw, String adapterName, String methodName, Class<?>[] parms) {
		// Prepare stack to call method
		// push factory
		cfw.add(ByteCode.ALOAD_0);
		cfw.add(ByteCode.GETFIELD, adapterName, "factory",
		 "Lrhino/ContextFactory;");

		// push self
		cfw.add(ByteCode.ALOAD_0);
		cfw.add(ByteCode.GETFIELD, adapterName, "HOPE-self",
		 "Lrhino/Scriptable;");

		// push function
		cfw.add(ByteCode.ALOAD_0);
		cfw.add(ByteCode.GETFIELD, adapterName, "delegee",
		 "Lrhino/Scriptable;");
		cfw.addPush(methodName);
		cfw.addInvoke(ByteCode.INVOKESTATIC,
		 CLASS_NAME,
		 "getFunction",
		 "(Lrhino/Scriptable;"
		 + "Ljava/lang/String;"
		 + ")Lrhino/Function;");

		// push arguments
		generatePushWrappedArgs(cfw, parms, parms.length);

		// push bits to indicate which parameters should be wrapped
		if (parms.length > 64) {
			// If it will be an issue, then passing a static boolean array
			// can be an option, but for now using simple bitmask
			throw ExceptionReporter.reportRuntimeError0(
			 "JavaAdapter can not subclass methods with more then 64 arguments.");
		}
		long convertionMask = 0;
		for (int i = 0; i != parms.length; ++i) {
			if (!parms[i].isPrimitive()) {
				convertionMask |= (1 << i);
			}
		}
		cfw.addPush(convertionMask);

		// go through utility method, which creates a Context to run the
		// method in.
		cfw.addInvoke(ByteCode.INVOKESTATIC,
		 // MyJavaAdapter.class.getName().replace('.', '/'),
		 CLASS_NAME,
		 "callMethod",
		 "(Lrhino/ContextFactory;"
		 + "Lrhino/Scriptable;"
		 + "Lrhino/Function;"
		 + "[Ljava/lang/Object;"
		 + "J"
		 + ")Ljava/lang/Object;");
	}

	/**
	 * Generates code to push typed parameters onto the operand stack
	 * prior to a direct Java method call.
	 */
	private static int generatePushParam(ClassFileWriter cfw, int paramOffset,
																			 Class<?> paramType) {
		if (!paramType.isPrimitive()) {
			cfw.addALoad(paramOffset);
			return 1;
		}
		String typeName = paramType.getName();
		switch (typeName.charAt(0)) {
			case 'z', 'b', 'c', 's', 'i' -> {
				// load an int value, convert to double.
				cfw.addILoad(paramOffset);
				return 1;
			}
			case 'l' -> {
				// load a long, convert to double.
				cfw.addLLoad(paramOffset);
				return 2;
			}
			case 'f' -> {
				// load a float, convert to double.
				cfw.addFLoad(paramOffset);
				return 1;
			}
			case 'd' -> {
				cfw.addDLoad(paramOffset);
				return 2;
			}
		}
		throw Kit.codeBug();
	}

	/**
	 * Generates code to return a Java type, after calling a Java method
	 * that returns the same type.
	 * Generates the appropriate RETURN bytecode.
	 */
	private static void generatePopResult(ClassFileWriter cfw,
																				Class<?> retType) {
		if (retType.isPrimitive()) {
			String typeName = retType.getName();
			switch (typeName.charAt(0)) {
				case 'b', 'c', 's', 'i', 'z' -> cfw.add(ByteCode.IRETURN);
				case 'l' -> cfw.add(ByteCode.LRETURN);
				case 'f' -> cfw.add(ByteCode.FRETURN);
				case 'd' -> cfw.add(ByteCode.DRETURN);
			}
		} else {
			cfw.add(ByteCode.ARETURN);
		}
	}

	/**
	 * Generates a method called "super$methodName()" which can be called
	 * from JavaScript that is equivalent to calling "super.methodName()"
	 * from Java. Eventually, this may be supported directly in JavaScript.
	 */
	private static void generateSuper(ClassFileWriter cfw,
																		String superName, String methodName,
																		String methodSignature,
																		Class<?>[] parms, Class<?> returnType) {
		cfw.startMethod("super$" + methodName, methodSignature,
		 ClassFileWriter.ACC_PUBLIC);

		// push "this"
		cfw.add(ByteCode.ALOAD, 0);

		// push the rest of the parameters.
		int paramOffset = 1;
		for (Class<?> parm : parms) {
			paramOffset += generatePushParam(cfw, paramOffset, parm);
		}

		// call the superclass implementation of the method.
		cfw.addInvoke(ByteCode.INVOKESPECIAL,
		 superName, methodName,
		 methodSignature);

		// now, handle the return type appropriately.
		if (!returnType.equals(Void.TYPE)) {
			generatePopResult(cfw, returnType);
		} else {
			cfw.add(ByteCode.RETURN);
		}
		cfw.stopMethod((short) (paramOffset + 1));
	}

	/**
	 * Returns a fully qualified method name concatenated with its signature.
	 */
	private static String getMethodSignature(Method method, Class<?>[] argTypes) {
		StringBuilder sb = new StringBuilder();
		appendMethodSignature(argTypes, method.getReturnType(), sb);
		return sb.toString();
	}

	static int appendMethodSignature(Class<?>[] argTypes,
																	 Class<?> returnType,
																	 StringBuilder sb) {
		sb.append('(');
		int firstLocal = 1 + argTypes.length; // includes this.
		for (Class<?> type : argTypes) {
			appendTypeString(sb, type);
			if (type == Long.TYPE || type == Double.TYPE) {
				// adjust for double slot
				++firstLocal;
			}
		}
		sb.append(')');
		appendTypeString(sb, returnType);
		return firstLocal;
	}

	private static StringBuilder appendTypeString(StringBuilder sb, Class<?> type) {
		while (type.isArray()) {
			sb.append('[');
			type = type.getComponentType();
		}
		if (type.isPrimitive()) {
			char typeLetter;
			if (type == Boolean.TYPE) {
				typeLetter = 'Z';
			} else if (type == Long.TYPE) {
				typeLetter = 'J';
			} else {
				String typeName = type.getName();
				typeLetter = Character.toUpperCase(typeName.charAt(0));
			}
			sb.append(typeLetter);
		} else {
			sb.append('L');
			sb.append(type.getName().replace('.', '/'));
			sb.append(';');
		}
		return sb;
	}

	static int[] getArgsToConvert(Class<?>[] argTypes) {
		int count = 0;
		for (int i = 0; i != argTypes.length; ++i) {
			if (!argTypes[i].isPrimitive())
				++count;
		}
		if (count == 0)
			return null;
		int[] array = new int[count];
		count = 0;
		for (int i = 0; i != argTypes.length; ++i) {
			if (!argTypes[i].isPrimitive())
				array[count++] = i;
		}
		return array;
	}
	public static void writeTo(Fi fi, String adapterName, byte[] bytes) {
		if (fi.isDirectory()) fi = fi.child(adapterName.replace('/', '.') + ".class");
		fi.writeBytes(bytes);
	}

	public static final String FTAG           = "MyJavaAdapter";
	public static final int    Id_JavaAdapter = 1;
}