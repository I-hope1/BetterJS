package better_js.myrhino;

import arc.struct.ObjectMap;
import arc.util.Log;
import better_js.myrhino.MyNativeJavaObject.Status;
import hope_android.FieldUtils;
import mindustry.Vars;
import mindustry.mod.Mods;
import mindustry.type.Category;
import rhino.*;

import java.lang.reflect.*;
import java.util.*;

import static better_js.ForRhino.*;
import static better_js.Main.unsafe;
import static java.lang.reflect.Modifier.*;
import static better_js.BetterJSRhino.*;

/**
 * @author Mike Shaver
 * @author Norris Boyd
 * @see MyNativeJavaObject
 * @see MyNativeJavaClass
 */
public class MyJavaMembers {
	/*static long classShutterOff;
	static {
		classShutterOff = Context.class.getDeclaredField("classShutter");
	}*/

	public MyJavaMembers(Scriptable scope, Class<?> cl) {
		this(scope, cl, false);
	}

	public MyJavaMembers(Scriptable scope, Class<?> cl, boolean includeProtected) {

		this.members = new ObjectMap<>();
		this.staticMembers = new ObjectMap<>();
		this.cl = cl;
		reflect(scope, includeProtected, status != Status.normal);
	}

	boolean has(String name, boolean isStatic) {
		if ((isStatic ? staticMembers : members).containsKey(name)) {
			return true;
		}
		return findExplicitFunction(name, isStatic) != null;
	}

	/*long time = 0;
	float total = 0;

	long time2 = 0;
	float total2 = 0;*/

	Object get(Scriptable scope, String name, Object javaObject,
	           boolean isStatic) {
		// long last = System.nanoTime();
		// try {
		// Time.mark();

		Object member = (isStatic ? staticMembers : members).get(name);

		if (!isStatic && member == null) {
			// Try to get static member from instance (LC3)
			member = staticMembers.get(name);
		}
		if (member == null) {
			member = getExplicitFunction(scope, name,
					javaObject, isStatic);
			if (member == null) {
				return Scriptable.NOT_FOUND;
			}
		}
		if (member instanceof Scriptable) {
			return member;
		}

		Context cx = Context.getContext();
		Object rval;
		Class<?> type;
		try {
			if (member instanceof MyBeanProperty) {
				MyBeanProperty bp = (MyBeanProperty) member;
				// long last = System.nanoTime();
				if (bp.getter == null) {
					return Scriptable.NOT_FOUND;
				}
				rval = bp.getter.invoke(javaObject, Context.emptyArgs);
				type = bp.getter.method().getReturnType();
				/*total2 += System.nanoTime() - last;
				if (++time2 >= 1E5) {
					Log.info("method:" + total2);
					time2 = 0;
					total2 = 0;
				}*/
			} else {
				Field field = (Field) member;
				// rval = field.get(javaObject);
				// rval = isStatic ? field.get(null) : unsafe.getObject(javaObject, unsafe.objectFieldOffset(field));
				rval = Vars.mobile ? unsafe.getObject(
						isStatic ? field.getDeclaringClass() : javaObject,
						FieldUtils.getFieldOffset(field))
						: field.get(javaObject);
				type = field.getType();
			}
		} catch (Exception ex) {
			throw Context.throwAsScriptRuntimeEx(ex);
		}

		// Need to wrap the object before we return it.
		// long last = System.nanoTime();
		// try {
		return cx.getWrapFactory().wrap(cx,
				scope,
				rval, type);
		/*} finally {
			if (isStatic) {
				total += System.nanoTime() - last;
				if (++time >= 1E5) {
					Log.info("field:" + total);
					time = 0;
					total = 0;
				}
			}
		}*/

		/*} finally {
			average += System.nanoTime() - last;
			if (time != 0) average /= 2;
			if (++time >= 1E5) {
				Log.info("mem:" + average);
				// Log.err(new Throwable());
				time = 0;
				average = 0;
			}
		}*/
	}

	void put(Scriptable scope, String name, Object javaObject,
	         Object value, boolean isStatic) {
		ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
		Object member = ht.get(name);
		if (!isStatic && member == null) {
			// Try to get static member from instance (LC3)
			member = staticMembers.get(name);
		}
		if (member == null)
			throw reportMemberNotFound(name);
		if (member instanceof MyFieldAndMethods) {
			MyFieldAndMethods fam = (MyFieldAndMethods) ht.get(name);
			member = fam.field;
		}

		// Is this a bean property "set"?
		if (member instanceof MyBeanProperty) {
			MyBeanProperty bp = (MyBeanProperty) member;
			if (bp.setter == null) {
				throw reportMemberNotFound(name);
			}
			// If there's only one setter orThrow if the value is null, use the
			// main setter. Otherwise, let the NativeJavaMethod decide which
			// setter to use:
			if (bp.setters == null || value == null) {
				Class<?> setType = bp.setter.argTypes[0];
				Object[] args = {Context.jsToJava(value, setType)};
				try {
					bp.setter.invoke(javaObject, args);
				} catch (Exception ex) {
					throw Context.throwAsScriptRuntimeEx(ex);
				}
			} else {
				Object[] args = {value};
				bp.setters.call(Context.getContext(),
						ScriptableObject.getTopLevelScope(scope),
						scope, args);
			}
		} else {
			if (!(member instanceof Field)) {
				String str = (member == null) ? "msg.java.internal.private"
						: "msg.java.method.assign";
				throw ErrorThrow.reportRuntimeError1(str, name);
			}
			Field field = (Field) member;
			Object javaValue = Context.jsToJava(value, field.getType());
			try {
				/*if (isStatic) {
					field.set(null, javaValue);
				} else {
					unsafe.putObject(javaObject, unsafe.objectFieldOffset(field), javaObject);
				}*/
				if (Vars.mobile)
					unsafe.putObject(isStatic ?
									field.getDeclaringClass() : javaObject,
							FieldUtils.getFieldOffset(field), javaValue);
				else {
					try {
						field.set(javaObject, javaValue);
					} catch (IllegalAccessException e) {
						unsafe.putObject(isStatic ? field.getDeclaringClass() : javaObject,
								isStatic ? unsafe.staticFieldOffset(field) : unsafe.objectFieldOffset(field),
								javaValue);
					}
				}

			} catch (IllegalArgumentException argEx) {
				throw ErrorThrow.reportRuntimeError3(
						"msg.java.internal.field.type",
						value.getClass().getName(), field,
						javaObject.getClass().getName());
			}
		}
	}

	Object[] getIds(boolean isStatic) {
		return (isStatic ? staticMembers : members).keys().toSeq().toArray();
	}

	static String javaSignature(Class<?> type) {
		if (!type.isArray()) {
			return type.getName();
		}
		int arrayDimension = 0;
		do {
			++arrayDimension;
			type = type.getComponentType();
		} while (type.isArray());
		String name = type.getName();
		String suffix = "[]";
		if (arrayDimension == 1) {
			return name.concat(suffix);
		}
		int length = name.length() + arrayDimension * suffix.length();
		StringBuilder sb = new StringBuilder(length);
		sb.append(name);
		while (arrayDimension != 0) {
			--arrayDimension;
			sb.append(suffix);
		}
		return sb.toString();
	}

	static String liveConnectSignature(Class<?>[] argTypes) {
		int N = argTypes.length;
		if (N == 0) {
			return "()";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i = 0; i != N; ++i) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(javaSignature(argTypes[i]));
		}
		sb.append(')');
		return sb.toString();
	}

	private MyMemberBox findExplicitFunction(String name, boolean isStatic) {
		int sigStart = name.indexOf('(');
		if (sigStart < 0) {
			return null;
		}

		ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
		MyMemberBox[] methodsOrCtors = null;
		boolean isCtor = (isStatic && sigStart == 0);

		if (isCtor) {
			// Explicit request for an overloaded constructor
			methodsOrCtors = ctors.methods;
		} else {
			// Explicit request for an overloaded method
			String trueName = name.substring(0, sigStart);
			Object obj = ht.get(trueName);
			if (!isStatic && obj == null) {
				// Try to get static member from instance (LC3)
				obj = staticMembers.get(trueName);
			}
			if (obj instanceof MyNativeJavaMethod) {
				MyNativeJavaMethod njm = (MyNativeJavaMethod) obj;
				methodsOrCtors = njm.methods;
			}
		}

		if (methodsOrCtors != null) {
			for (MyMemberBox methodsOrCtor : methodsOrCtors) {
				Class<?>[] type = methodsOrCtor.argTypes;
				String sig = liveConnectSignature(type);
				if (sigStart + sig.length() == name.length()
						&& name.regionMatches(sigStart, sig, 0, sig.length())) {
					return methodsOrCtor;
				}
			}
		}

		return null;
	}

	private Object getExplicitFunction(Scriptable scope, String name,
	                                   Object javaObject, boolean isStatic) {
		ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
		Object member = null;
		MyMemberBox methodOrCtor = findExplicitFunction(name, isStatic);

		if (methodOrCtor != null) {
			Scriptable prototype =
					ScriptableObject.getFunctionPrototype(scope);

			if (methodOrCtor.isCtor()) {
				MyNativeJavaConstructor fun =
						new MyNativeJavaConstructor(methodOrCtor);
				fun.setPrototype(prototype);
				member = fun;
				ht.put(name, fun);
			} else {
				String trueName = methodOrCtor.getName();
				member = ht.get(trueName);

				if (member instanceof MyNativeJavaMethod &&
						((MyNativeJavaMethod) member).methods.length > 1) {
					MyNativeJavaMethod fun =
							new MyNativeJavaMethod(methodOrCtor, name);
					fun.setPrototype(prototype);
					ht.put(name, fun);
					member = fun;
				}
			}
		}

		return member;
	}

	/**
	 * Retrieves mapping of methods to accessible methods for a class.
	 * In case the class is not public, retrieves methods with same
	 * signature as its public methods from public superclasses and
	 * interfaces (if they exist). Basically upcasts every method to the
	 * nearest accessible method.
	 */
	private static Method[] discoverAccessibleMethods(Class<?> clazz,
	                                                  boolean includeProtected,
	                                                  boolean includePrivate) {
		Map<MethodSignature, Method> map = new HashMap<>();
		discoverAccessibleMethods(clazz, map, includeProtected, includePrivate);
		return map.values().toArray(new Method[0]);
	}

	private static void discoverAccessibleMethods(Class<?> clazz,
	                                              Map<MethodSignature, Method> map, boolean includeProtected,
	                                              boolean includePrivate) {
		if (isPublic(clazz.getModifiers()) || includePrivate) {
			try {
				if (includeProtected || includePrivate) {
					while (clazz != null) {
						Method[] methods = clazz.getDeclaredMethods();
						for (Method method : methods) {
							int mods = method.getModifiers();

							if (isPublic(mods)
									|| isProtected(mods)
									|| includePrivate) {
								MethodSignature sig = new MethodSignature(method);
								if (!map.containsKey(sig)) {
									if (!method.isAccessible())
										method.setAccessible(true);
									map.put(sig, method);
								}
							}
						}
						Class<?>[] interfaces = clazz.getInterfaces();
						for (Class<?> intface : interfaces) {
							discoverAccessibleMethods(intface, map, includeProtected,
									includePrivate);
						}
						clazz = clazz.getSuperclass();
					}
				} else {
					Method[] methods = clazz.getMethods();
					for (Method method : methods) {
						try {
							MethodSignature sig = new MethodSignature(method);
							// Array may contain methods with same signature but different return value!
							if (!map.containsKey(sig)) {
								method.setAccessible(true);
								map.put(sig, method);
							}
						} catch (Throwable ignored) {
							//some methods may contain invalid signatures
						}
					}
				}
				return;
			} catch (Throwable e) {
				Context.reportWarning(
						"Could not discover accessible methods of class " +
								clazz.getName() + " due to lack of privileges, " +
								"attemping superclasses/interfaces.");
				// Fall through and attempt to discover superclass/interface
				// methods
			}
		}

		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> intface : interfaces) {
			discoverAccessibleMethods(intface, map, includeProtected,
					includePrivate);
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			discoverAccessibleMethods(superclass, map, includeProtected,
					includePrivate);
		}
	}

	private static final class MethodSignature {
		private final String name;
		private final Class<?>[] args;

		private MethodSignature(String name, Class<?>[] args) {
			this.name = name;
			this.args = args;
		}

		MethodSignature(Method method) {
			this(method.getName(), method.getParameterTypes());
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MethodSignature) {
				MethodSignature ms = (MethodSignature) o;
				return ms.name.equals(name) && Arrays.equals(args, ms.args);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ args.length;
		}
	}

	public void reflect(Scriptable scope,
	                    boolean includeProtected,
	                    boolean includePrivate) {
		// We reflect methods first, because we want overloaded field/method
		// names to be allocated to the NativeJavaMethod before the field
		// gets in the way.

		Method[] methods = discoverAccessibleMethods(cl, includeProtected,
				includePrivate);
		for (Method method : methods) {
			int mods = method.getModifiers();
			boolean isStatic = Modifier.isStatic(mods);
			ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
			String name = method.getName();
			Object value = ht.get(name);
			if (value == null) {
				ht.put(name, method);
			} else {
				ObjArray overloadedMethods;
				if (value instanceof ObjArray) {
					overloadedMethods = (ObjArray) value;
				} else {
					if (!(value instanceof Method)) Kit.codeBug();
					// value should be instance of Method as at this stage
					// staticMembers and members can only contain methods
					overloadedMethods = new ObjArray();
					overloadedMethods.add(value);
					ht.put(name, overloadedMethods);
				}
				overloadedMethods.add(method);
			}
		}

		// replace Method instances by wrapped NativeJavaMethod objects
		// first in staticMembers and then in members
		for (int tableCursor = 0; tableCursor != 2; ++tableCursor) {
			boolean isStatic = (tableCursor == 0);
			ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
			for (ObjectMap.Entry<String, Object> entry : ht.entries()) {
				MyMemberBox[] methodBoxes;
				Object value = entry.value;
				if (value instanceof Method) {
					methodBoxes = new MyMemberBox[1];
					methodBoxes[0] = new MyMemberBox((Method) value);
				} else {
					ObjArray overloadedMethods = (ObjArray) value;
					int N = overloadedMethods.size();
					if (N < 2) Kit.codeBug();
					methodBoxes = new MyMemberBox[N];
					for (int i = 0; i != N; ++i) {
						Method method = (Method) overloadedMethods.get(i);
						if (!method.isAccessible()) method.setAccessible(true);
						methodBoxes[i] = new MyMemberBox(method);
					}
				}
				MyNativeJavaMethod fun = new MyNativeJavaMethod(methodBoxes);
				if (scope != null) {
					ScriptRuntime.setFunctionProtoAndParent(fun, scope);
				}
				ht.put(entry.key, fun);
			}
		}

		// Reflect fields.
		Field[] fields = getAccessibleFields(includeProtected, includePrivate);
		for (Field field : fields) {
			String name = field.getName();
			int mods = field.getModifiers();
			boolean isStatic = Modifier.isStatic(mods);
			ObjectMap<String, Object> ht = isStatic ? staticMembers : members;
			Object member = ht.get(name);
			field.setAccessible(true);
			if (member == null || (status != Status.accessMethod && member instanceof MyNativeJavaMethod && !Modifier.isPrivate(mods))) { //change: fields will always mask methods
				ht.put(name, field);
			} else if (status != Status.accessMethod && member instanceof MyNativeJavaMethod) {
				MyNativeJavaMethod method = (MyNativeJavaMethod) member;
				MyFieldAndMethods fam = new MyFieldAndMethods(scope, method.methods, field);
				Map<String, MyFieldAndMethods> fmht = isStatic ? staticFieldAndMethods : fieldAndMethods;
				if (fmht == null) {
					fmht = new HashMap<>();
					if (isStatic) {
						staticFieldAndMethods = fmht;
					} else {
						fieldAndMethods = fmht;
					}
				}
				fmht.put(name, fam);
				ht.put(name, fam);
			} else if (member instanceof Field) {
				Field oldField = (Field) member;
				// If this newly reflected field shadows an inherited field,
				// then replace it. Otherwise, since access to the field
				// would be ambiguous from Java, no field should be
				// reflected.
				// For now, the first field found wins, unless another field
				// explicitly shadows it.
				if (oldField.getDeclaringClass().isAssignableFrom(field.getDeclaringClass())) {
					ht.put(name, field);
				}
			} else if (status != Status.accessMethod) {
				// "unknown member type"
				Kit.codeBug();
			}
		}

		// Create bean properties from corresponding get/set methods first for
		// static members and then for instance members
		if (status != Status.accessMethod) for (int tableCursor = 0; tableCursor != 2; ++tableCursor) {
			boolean isStatic = (tableCursor == 0);
			ObjectMap<String, Object> ht = isStatic ? staticMembers : members;

			ObjectMap<String, MyBeanProperty> toAdd = new ObjectMap<>();

			// Now, For each member, make "bean" properties.
			for (String name : ht.keys()) {
				// Is this a getter?
				boolean memberIsGetMethod = name.startsWith("get");
				boolean memberIsSetMethod = name.startsWith("set");
				boolean memberIsIsMethod = name.startsWith("is");
				if (memberIsGetMethod || memberIsIsMethod
						|| memberIsSetMethod) {
					// Double check name component.
					String nameComponent
							= name.substring(memberIsIsMethod ? 2 : 3);
					if (nameComponent.isEmpty())
						continue;

					// Make the bean property name.
					String beanPropertyName = nameComponent;
					char ch0 = nameComponent.charAt(0);
					if (Character.isUpperCase(ch0)) {
						if (nameComponent.length() == 1) {
							beanPropertyName = nameComponent.toLowerCase(Locale.ROOT);
						} else {
							char ch1 = nameComponent.charAt(1);
							if (!Character.isUpperCase(ch1)) {
								beanPropertyName = Character.toLowerCase(ch0)
										+ nameComponent.substring(1);
							}
						}
					}

					// If we already have a member by this name, don't do this
					// property.
					if (toAdd.containsKey(beanPropertyName))
						continue;
					Object v = ht.get(beanPropertyName);
					if (v != null) {
						// A private field shouldn't mask a public getter/setter
						if (!includePrivate || !(v instanceof Member) ||
								!Modifier.isPrivate(((Member) v).getModifiers())) {
							continue;
						}
					}

					// Find the getter method, orThrow if there is none, the is-
					// method.
					MyMemberBox getter;
					getter = findGetter(isStatic, ht, "get", nameComponent);
					// if (nameComponent.equals("scripts")) Log.info(cl);
					// If there was no valid getter, check for an is- method.
					if (getter == null) {
						getter = findGetter(isStatic, ht, "is", nameComponent);
					}

					// setter
					MyMemberBox setter = null;
					MyNativeJavaMethod setters = null;
					String setterName = "set".concat(nameComponent);

					if (ht.containsKey(setterName)) {
						// Is this value a method?
						Object member = ht.get(setterName);
						if (member instanceof MyNativeJavaMethod) {
							MyNativeJavaMethod njmSet = (MyNativeJavaMethod) member;
							if (getter != null) {
								// We have a getter. Now, do we have a matching
								// setter?
								Class<?> type = getter.method().getReturnType();
								setter = extractSetMethod(type, njmSet.methods,
										isStatic);
							} else {
								// No getter, find any set method
								setter = extractSetMethod(njmSet.methods,
										isStatic);
							}
							if (njmSet.methods.length > 1) {
								setters = njmSet;
							}
						}
					}
					// Make the property.
					toAdd.put(beanPropertyName, new MyBeanProperty(getter, setter, setters));
				}
			}

			// Add the new bean properties.
			ht.putAll(toAdd);
		}

		// Reflect constructors
		Constructor<?>[] constructors = getAccessibleConstructors(includePrivate);
		MyMemberBox[] ctorMembers = new MyMemberBox[constructors.length];
		for (int i = 0; i != constructors.length; ++i) {
			ctorMembers[i] = new MyMemberBox(constructors[i]);
		}
		ctors = new MyNativeJavaMethod(ctorMembers, cl.getSimpleName());
	}

	private Constructor<?>[] getAccessibleConstructors(boolean includePrivate) {
		// The JVM currently doesn't allow changing access on java.lang.Class
		// constructors, so don't try
		Constructor<?>[] cons;
		if (includePrivate && cl != ScriptRuntime.ClassClass) {
			cons = cl.getDeclaredConstructors();
		} else {
			cons = cl.getConstructors();
		}
		AccessibleObject.setAccessible(cons, true);
		return cons;
	}

	private Field[] getAccessibleFields(boolean includeProtected,
	                                    boolean includePrivate) {
		if (includePrivate || includeProtected) {
			List<Field> fieldsList = new ArrayList<>();
			Class<?> currentClass = cl;

			while (currentClass != null) {
				// get all declared fields in this class, make them
				// accessible, and save
				Field[] declared = currentClass.getDeclaredFields();
				for (Field field : declared) {
					int mod = field.getModifiers();
					if (includePrivate || isPublic(mod) || isProtected(mod)) {
						if (!field.isAccessible())
							field.setAccessible(true);
						fieldsList.add(field);
					}
				}
				// walk up superclass chain.  no need to deal specially with
				// interfaces, since they can't have fields
				currentClass = currentClass.getSuperclass();
			}

			return fieldsList.toArray(new Field[0]);
		}
		return cl.getFields();
	}

	private MyMemberBox findGetter(boolean isStatic, ObjectMap<String, Object> ht, String prefix,
	                               String propertyName) {
		String getterName = prefix.concat(propertyName);
		if (ht.containsKey(getterName)) {
			// Check that the getter is a method.
			Object member = ht.get(getterName);
			if (member instanceof MyNativeJavaMethod) {
				MyNativeJavaMethod njmGet = (MyNativeJavaMethod) member;
				return extractGetMethod(njmGet.methods, isStatic);
			}
		}
		return null;
	}

	private static MyMemberBox extractGetMethod(MyMemberBox[] methods,
	                                            boolean isStatic) {
		// Inspect the list of all MyMemberBox for the only one having no
		// parameters
		for (MyMemberBox method : methods) {
			// Does getter method have an empty parameter list with a return
			// value (eg. a getSomething() orThrow isSomething())?
			if (method.argTypes.length == 0 && (!isStatic || method.isStatic())) {
				Class<?> type = method.method().getReturnType();
				if (type != Void.TYPE) {
					return method;
				}
				break;
			}
		}
		return null;
	}

	private static MyMemberBox extractSetMethod(Class<?> type, MyMemberBox[] methods,
	                                            boolean isStatic) {
		//
		// Note: it may be preferable to allow NativeJavaMethod.findFunction()
		//       to find the appropriate setter; unfortunately, it requires an
		//       instance of the target arg to determine that.
		//

		// Make two passes: one to find a method with direct type assignment,
		// and one to find a widening conversion.
		for (int pass = 1; pass <= 2; ++pass) {
			for (MyMemberBox method : methods) {
				if (!isStatic || method.isStatic()) {
					Class<?>[] params = method.argTypes;
					if (params.length == 1) {
						if (pass == 1) {
							if (params[0] == type) {
								return method;
							}
						} else {
							if (params[0].isAssignableFrom(type)) {
								return method;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static MyMemberBox extractSetMethod(MyMemberBox[] methods,
	                                            boolean isStatic) {

		for (MyMemberBox method : methods) {
			if (!isStatic || method.isStatic()) {
				if (method.method().getReturnType() == Void.TYPE) {
					if (method.argTypes.length == 1) {
						return method;
					}
				}
			}
		}
		return null;
	}

	Map<String, MyFieldAndMethods> getFieldAndMethodsObjects(Scriptable scope,
	                                                         Object javaObject, boolean isStatic) {
		Map<String, MyFieldAndMethods> ht = isStatic ? staticFieldAndMethods : fieldAndMethods;
		if (ht == null) {
			return null;
		}
		int len = ht.size();
		Map<String, MyFieldAndMethods> result = new HashMap<>(len);
		for (MyFieldAndMethods fam : ht.values()) {
			MyFieldAndMethods famNew = new MyFieldAndMethods(scope, fam.methods,
					fam.field);
			famNew.javaObject = javaObject;
			result.put(fam.field.getName(), famNew);
		}
		return result;
	}

	static final long associatedScopeOff;

	static {
		try {
			associatedScopeOff = unsafe.objectFieldOffset(ClassCache.class.getDeclaredField("associatedScope"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static ObjectMap<Class<?>, MyJavaMembers> myJavaMembersCaches = new ObjectMap<>();

	static MyJavaMembers lookupClass(Scriptable scope, Class<?> dynamicType,
	                                 Class<?> staticType, boolean includeProtected) {
		/*Map<Class<?>, MyJavaMembers> ct = (Map<Class<?>, MyJavaMembers>) unsafe.getObject(cache, classCacheLong);
		if (ct == null) {
			unsafe.putObject(cache, classCacheLong, ct = new ConcurrentHashMap(16, 0.75F, 1));
		}*/
		return myJavaMembersCaches.get(dynamicType, () -> {
			ClassCache cache = ClassCache.get(scope);
			return new MyJavaMembers((Scriptable) unsafe.getObject(cache, associatedScopeOff),
					dynamicType, includeProtected);
		});
	}

	RuntimeException reportMemberNotFound(String memberName) {
		String msg = ScriptRuntime.getMessage2("msg.java.member.not.found",
				cl.getName(), memberName);
		return Context.reportRuntimeError(msg);
	}

	private final Class<?> cl;
	private final ObjectMap<String, Object> members;
	private Map<String, MyFieldAndMethods> fieldAndMethods;
	private final ObjectMap<String, Object> staticMembers;
	private Map<String, MyFieldAndMethods> staticFieldAndMethods;
	MyNativeJavaMethod ctors; // we use NativeJavaMethod for ctor overload resolution
}

class MyBeanProperty {
	MyBeanProperty(MyMemberBox getter, MyMemberBox setter, MyNativeJavaMethod setters) {
		this.getter = getter;
		this.setter = setter;
		this.setters = setters;
	}

	MyMemberBox getter;
	MyMemberBox setter;
	MyNativeJavaMethod setters;
}

class MyFieldAndMethods extends MyNativeJavaMethod {

	MyFieldAndMethods(Scriptable scope, MyMemberBox[] methods, Field field) {
		super(methods);
		this.field = field;
		setParentScope(scope);
		setPrototype(ScriptableObject.getFunctionPrototype(scope));
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		if (hint == ScriptRuntime.FunctionClass) return this;
		Object rval;
		Class<?> type;
		try {
			rval = Vars.mobile ?
					unsafe.getObject(Modifier.isStatic(field.getModifiers()) ?
									field.getDeclaringClass() : javaObject,
							FieldUtils.getFieldOffset(field))
					: field.get(javaObject);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		type = field.getType();
		Context cx = Context.getContext();
		rval = cx.getWrapFactory().wrap(cx, this, rval, type);
		if (rval instanceof Scriptable) {
			rval = ((Scriptable) rval).getDefaultValue(hint);
		}
		return rval;
	}

	Field field;
	Object javaObject;
}
