package better_js.utils;

import arc.files.Fi;
import arc.func.Prov;
import arc.struct.*;
import arc.util.*;
import hope_android.FieldUtils;
import jdk.internal.misc.Unsafe;
import rhino.classfile.ClassFileWriter;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import static better_js.utils.MyReflect.unsafe;
import static rhino.classfile.ByteCode.*;
import static rhino.classfile.ClassFileWriter.*;
// import static org.objectweb.asm.Opcodes.*;

public class ByteCodeTools {
	public static final ObjectMap<MethodSignature, String> METHOD_CACHES = new ObjectMap<>();
	public static final String
			functionsKey = "_KSINIA_Functions",
			INIT = "<init>",
			TMP_CLASS = "_tmp_ADAPTER",
			Consumer_TYPE = typeToNative(Consumer.class),
			Consumer_NATIVE = nativeName(Consumer.class),
			ArrayList_TYPE = typeToNative(ArrayList.class),
			ArrayList_NATIVE = nativeName(ArrayList.class),
			BiFunction_TYPE = typeToNative(BiFunction.class),
			BiFunction_NATIVE = nativeName(BiFunction.class),

	VOID_METHOD = nativeMethod(void.class);
	private static int lastID = 0;

	/*static  {
		Field f = ClassWriter.class.getDeclaredField("symbolTable");
		f.getType().getDeclaredField("")
	}*/
	public static class MyClass<T> {
		public final ClassFileWriter writer;
		public final String adapterName, superName;
		public final Class<?> superClass;
		private final Seq<Queue<?>> queues = new Seq<>();

		public MyClass(String name, Class<T> superClass, String... interfaces) {
			this.superClass = superClass;
			adapterName = name.replace('.', '/');
			// Log.info(adapterName);
			superName = nativeName(superClass);
			// ByteVector vec = ByteVectorFactory.create();
			// asm = new ClassFileAssembler(vec);
			// ClassReader reader = fi == null ? null : new ClassReader(fi.readBytes());
			// asm = new ClassWriter(null, 0);
			writer = new ClassFileWriter(adapterName, superName,
					TMP_CLASS);
			writer.setFlags(ClassFileWriter.ACC_PUBLIC);
			for (String anInterface : interfaces) {
				writer.addInterface(anInterface);
			}

			// asm.emitMagicAndVersion();

			// asm.emitShort(add(numCPEntries, S1));
			// asm.emitConstantPoolUTF8(name);
			// asm.emitConstantPoolClass(asm.cpi());
			// thisClassId = asm.cpi();
			// asm.emitConstantPoolUTF8(nativeName(superClass));
			// asm.emitConstantPoolClass(asm.cpi());
			// superClassId = asm.cpi();
			/*asm.visit(52 // java 8
					, ACC_PUBLIC, adapterName,
					null, superName, interfaces);*/

			// writer.addField(allSuperKey, typeToNative(ObjectMap.class), (short) (Modifier.PUBLIC | Modifier.STATIC));
			/*setField(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, ObjectMap.class, superMethod);
			runs.add(() -> {
				writer.add(NEW, nativeName(ObjectMap.class));
				writer.add(DUP);
				writer.addInvoke(INVOKESPECIAL, nativeName(ObjectMap.class), "<init>", nativeMethod(void.class, int.class));
				writer.add(PUTSTATIC, adapterName, superMethod, typeToNative(ObjectMap.class));
			});*/
		}

		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, boolean buildSuper, Class<V> returnType, Class<?>... args) {
			setFunc(name, func2, ACC_PUBLIC, buildSuper, returnType, args);
		}


		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, int flags, boolean buildSuper, Class<V> returnType, Class<?>... args) {
			if (func2 == null) {
				// ClassFileAssembler cb = new ClassFileAssembler();
				// incoming arguments
				// cb.setMaxLocals(args.length + 1);
				// cb.opc_aload_1();
				writer.startMethod(name, nativeMethod(returnType, args), (short) flags);
				writer.addLoadThis();
				for (short i = 1; i <= args.length; i++) {
					// cb.emitShort((short) addLoad(args[i - 1]), i);
					writer.add(addLoad(args[i - 1]), i);
				}
				// asm.emitConstantPoolUTF8(name);
				// short nameIdx = asm.cpi();
				// asm.emitConstantPoolUTF8(nativeMethod(returnType, args));
				// short type = asm.cpi();
				// asm.emitConstantPoolNameAndType(nameIdx, type);
				// cb.opc_invokespecial(asm.cpi(), args.length, 0);
				writer.addInvoke(INVOKESPECIAL, superName, name, nativeMethod(returnType, args));
				// cb.opc_checkcast();
				addCast(writer, returnType);
				// asm.emitShort(buildReturn(returnType));
				// writer.add(CHECKCAST, nativeName(returnType));
				writer.add(buildReturn(returnType));
				// writer.visitMaxs(args.length + 1, args.length + 1);
				writer.stopMethod((short) (args.length + 1));
				// writer.visitEnd();
				return;
			}
			String fieldName = functionsKey + "_l" + lastID++;
			short max = (short) (args.length + 1);
			int v1 = max++, v2 = max++;
			queues.add(new Queue<>(fieldName, () -> func2));
			writer.addField(fieldName, BiFunction_TYPE, (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			// var writer = writer;
			writer.startMethod(name, nativeMethod(returnType, args), (short) flags);

			if (buildSuper) {
				buildSuper(writer, name, returnType, args);
				// 储存为v1
				if (returnType != void.class) writer.add(addStore(returnType), v1);
			}

			// new ArrayList(args.length)
			writer.add(NEW, ArrayList_NATIVE);
			writer.add(DUP);
			writer.add(BIPUSH, args.length + (buildSuper ? 1 : 0));
			writer.addInvoke(INVOKESPECIAL, ArrayList_NATIVE, INIT, "(I)V");
			writer.add(ASTORE, v2);

			// 将参数储存 arraylist
			for (int i = 0; i < args.length; i++) {
				writer.add(ALOAD, v2); // arraylist
				writer.add(addLoad(args[i]), i + 1);
				addBox(writer, args[i]);
				writer.addInvoke(INVOKEVIRTUAL, ArrayList_NATIVE, "add", "(Ljava/lang/Object;)Z"/*+ ArrayList_NATIVE +";"*/);
			}
			// 将super的返回值存入arraylist
			if (buildSuper && returnType != void.class) {
				writer.add(ALOAD, v2); // arraylist
				writer.add(addLoad(returnType), v1); // super return
				addBox(writer, returnType);
				// add
				writer.addInvoke(INVOKEVIRTUAL, ArrayList_NATIVE, "add", "(Ljava/lang/Object;)Z"/*+ ArrayList_NATIVE +";"*/);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, BiFunction_TYPE);

			writer.addLoadThis(); // this
			writer.add(ALOAD, v2); // arraylist

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, BiFunction_NATIVE, "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			addCast(writer, returnType);
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(buildReturn(returnType));
			// writer.visitMaxs(max, max); // this + args + var * 1
			writer.stopMethod(max);

			// writer.visitEnd();
		}

		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, int flags, Class<V> returnType, Class<?>... args) {
			setFunc(name, func2, flags, false, returnType, args);
		}

		public void setFuncVoid(String name, Consumer<T> afCons, boolean buildSuper, Class<?>... args) {
			String fieldName = functionsKey + "_l" + lastID++;
			queues.add(new Queue<>(fieldName, () -> afCons));
			writer.addField(fieldName, Consumer_TYPE, (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			// var writer = writer;
			writer.startMethod(name, nativeMethod(void.class, args), ACC_PUBLIC);

			if (buildSuper) {
				buildSuper(writer, name, void.class, args);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, Consumer_TYPE);

			writer.addLoadThis(); // this

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, Consumer_NATIVE, "accept", "(Ljava/lang/Object;)V");
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(RETURN);

			writer.stopMethod((short) (args.length + 2)/*, args.length + 1*/); // this
			// writer.visitEnd();
		}

		public <P1> void setFuncVoid(String name, BiConsumer<T, P1> afCons, Class<P1> arg1, boolean buildSuper) {
			String fieldName = functionsKey + "_l" + lastID++;
			queues.add(new Queue<>(fieldName, () -> afCons));
			writer.addField(fieldName, typeToNative(BiConsumer.class), (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			String methodType = "(" + typeToNative(arg1) + ")V";
			// var writer = writer;
			writer.startMethod(name, methodType, (short) ACC_PUBLIC);

			if (buildSuper) {
				writer.addLoadThis(); // this
				writer.add(addLoad(arg1), 1); // args[1]
				// super
				writer.addInvoke(INVOKESPECIAL, superName, name, methodType);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, typeToNative(BiConsumer.class));

			writer.addLoadThis(); // this
			writer.add(addLoad(arg1), 1); // args[1]

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, nativeName(BiConsumer.class), "apply", "(Ljava/lang/Object;Ljava/lang/Object;)V");
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(RETURN);

			// writer.visitMaxs(2, 2); // this + arg
			// writer.visitEnd();
		}


		public void setFuncVoid(String name, BiConsumer<T, ArrayList<Object>> cons2, boolean buildSuper, Class<?>... args) {
			setFuncVoid(name, cons2, Modifier.PUBLIC, buildSuper, args);
		}


		public void setFuncVoid(String name, BiConsumer<T, ArrayList<Object>> cons2, int flags, boolean buildSuper, Class<?>... args) {
			setFunc(name, (self, a) -> {
				cons2.accept(self, a);
				return null;
			}, flags, buildSuper, void.class, args);
		}

		public void buildSuperFunc(String name, String newName, Class<?> returnType, Class<?>... args) {
			// var writer = writer;
			writer.startMethod(newName, nativeMethod(returnType, args), ClassFileWriter.ACC_PUBLIC);
			writer.addLoadThis(); // this
			for (int i = 0; i < args.length; i++) {
				writer.add(addLoad(args[i]), i); // args[i]
				addCast(writer, args[i]);
			}
			writer.addInvoke(INVOKESPECIAL, superName, name, nativeMethod(returnType, args));
			// addCast(returnType);
			writer.add(buildReturn(returnType));
			writer.stopMethod(/*1 + args.length, */(short) (1 + args.length)); // this + args

			// writer.visitEnd();
		}

		public <K> void buildGetFieldFunc(String fieldName, String methodName, Class<K> fieldType) {
			// var writer = writer;
			writer.startMethod(methodName, nativeMethod(fieldType), (short) ACC_PUBLIC);
			writer.addLoadThis(); // this
			writer.add(GETFIELD, adapterName, fieldName, typeToNative(fieldType));
			writer.add(buildReturn(fieldType));
			// writer.visitMaxs(1, 1); // this
			writer.stopMethod((short) 1);

			// writer.visitEnd();
		}

		public void buildPutFieldFunc(String fieldName, String methodName, Class<?> fieldType) {
			// var writer = writer;
			writer.startMethod(methodName, nativeMethod(void.class, fieldType), (short) ACC_PUBLIC);
			writer.addLoadThis(); // this
			writer.add(addLoad(fieldType), 1); // arg1
			writer.add(PUTFIELD, adapterName, fieldName, typeToNative(fieldType));
			writer.add(RETURN);
			writer.stopMethod((short) 2/*, 2*/); // this + arg1

			// writer.visitEnd();
		}

		public void
		setField(int flags, Class<?> type, String name) {
			// return asm.addField(flags, name, typeToNative(type), null, null);
			writer.addField(name, typeToNative(type), (short) flags);
		}

		public <T2> void setField(int flags, Class<?> type, String name, T2 value) {
			if (!Modifier.isStatic(flags)) throw new IllegalArgumentException("field is not static.");
			queues.add(new Queue<>(name, () -> value));
			// asm.addField(flags, name, typeToNative(type), null, null);
			writer.addField(name, typeToNative(type), (short) flags);
		}


		public static float total;

		public Class<? extends T> define() {
			// writeTo(Vars.tmpDirectory.child("classes").child(adapterName.substring(adapterName.lastIndexOf('/')) + ".class"));

			// Time.mark();
			Class<?> base = MyReflect.defineClass(adapterName, superClass.getClassLoader(), writer.toByteArray());

			if (!queues.isEmpty()) {
				ObjectMap<String, Field> map = OS.isAndroid ? Seq.with(base.getDeclaredFields())
						.asMap(Field::getName) : null;
				for (var q : queues) {
					unsafe.putObject(base, OS.isAndroid ? FieldUtils.getFieldOffset(map.get(q.name))
									: Unsafe.getUnsafe().objectFieldOffset(base, q.name) /** 说是object，其实是any */
							// unsafe.staticFieldOffset(map.get(q.name))
							, q.get());
				}
			}
			// Log.info("@: @", ++anInt,Time.elapsed());
			return (Class<? extends T>) base;
		}

		public Class<? extends T> define(ClassLoader loader) {
			Class<?> base = MyReflect.defineClass(adapterName, loader, writer.toByteArray());

			ObjectMap<String, Field> map = OS.isAndroid ? Seq.with(base.getDeclaredFields())
					.asMap(Field::getName) : null;
			for (var q : queues) {
				unsafe.putObject(base, OS.isAndroid ? FieldUtils.getFieldOffset(map.get(q.name))
								: Unsafe.getUnsafe().objectFieldOffset(base, q.name) /** 说是object，其实是any */
						// unsafe.staticFieldOffset(map.get(q.name))
						, q.get());
			}
			return (Class<? extends T>) base;
		}

		private void buildSuper(ClassFileWriter writer, String name, Class<?> returnType, Class<?>... args) {
			// int label = writer.acquireLabel();
			writer.addLoadThis(); // this
			// args
			for (int i = 0; i < args.length; i++) {
				writer.add(addLoad(args[i]), i + 1);
				// addCast(writer, args[i]);
			}
			// super
			writer.addInvoke(INVOKESPECIAL, superName, name, nativeMethod(returnType, args));
			// writer.markLabel(label);
			// int catchLabel = writer.acquireLabel();
			// writer.markLabel(catchLabel);
			// writer.addExceptionHandler(label, label, catchLabel, "java/lang/Throwable");
		}

		public void visit(Class<?> cls) {
			visit(cls, cls.getDeclaredMethods());
		}

		public void visit(Class<?> cls, Method[] methods) {
			visitWithoutCheck(cls, filterMethods(methods));
		}

		public void visitWithoutCheck(Class<?> cls, FilterResult[] filterResults) {
			Method m;
			Class<?>[] types, realTypes;
			Class<?> returnType;
			String descriptor;
			for (FilterResult filterResult : filterResults) {
				if (filterResult == null) break;
				m = filterResult.method;
				boolean buildSuper = filterResult.include.buildSuper();
				types = m.getParameterTypes();
				returnType = m.getReturnType();
				realTypes = filterResult.realTypes0;
				descriptor = nativeMethod(returnType, realTypes);

				if (buildSuper) {
					writer.startMethod("super$_" + m.getName(),
							descriptor, ACC_PUBLIC);
					writer.addLoadThis(); // this
					for (int i = 1; i <= realTypes.length; i++) {
						writer.add(addLoad(types[i]), i);
						// addCast(writer, types[i]);
					}
					writer.addInvoke(INVOKESPECIAL, superName, m.getName(), descriptor);
					writer.add(buildReturn(returnType));
					writer.stopMethod((short) types.length);
				}

				writer.startMethod(m.getName(),
						descriptor, ACC_PUBLIC);
				writer.addLoadThis(); // this
				for (int i = 1; i <= realTypes.length; i++) {
					writer.add(addLoad(types[i]), i);
				}
				writer.addInvoke(INVOKESTATIC, nativeName(cls),
						m.getName(), nativeMethod(returnType, types));
				writer.add(buildReturn(returnType));
				writer.stopMethod((short) types.length);
				// writer.visitEnd();

			}
			/*// build super
				writer.startMethod("super$_" + m.getName(), methodType, (short) Modifier.PUBLIC);
				writer.addLoadThis(); // this
				for (int i = 1; i < types.length; i++) {
					writer.add(addLoad(types[i]), i);
					// addCast(writer, types[i]);
				}
				writer.addInvoke(INVOKESPECIAL, superName, m.getName(), methodType);
				// addCast(writer, returnType);
				writer.add(buildReturn(returnType));
				writer.stopMethod((short) types.length); // this + args*/
		}

		public void writeTo(Fi fi) {
			if (fi.isDirectory()) fi = fi.child(adapterName.replace('/', '.') + ".class");
			fi.writeBytes(writer.toByteArray());
		}

		public void addInterface(Class<?> cls) {
			writer.addInterface(cls.getName().replace('.', '/'));
		}

		public <V> void setFuncSelf(String name, ToIntFunction<ClassFileWriter> run, int flags, Class<V> returnType, Class<?>... args) {
			writer.startMethod(name, nativeMethod(returnType, args), (short) flags);
			writer.stopMethod((short) run.applyAsInt(writer)); // this + args + var * 1
		}
	}

	public static class Queue<T> {
		public String name;
		public Prov<T> func;
		// prov 应该返回的类
		public Class<?> cls;

		public Queue(String name, Prov<T> func) {
			this.name = name;
			this.func = func;

			while ((cls = func.get().getClass()).isAnonymousClass()) ;
		}

		public T get() {
			return func.get();
		}
	}

	public static String nativeName(Class<?> cls) {
		return cls.getName().replace('.', '/');
	}

	public static String nativeMethod(Class<?> returnType, Class<?>... args) {
		return METHOD_CACHES.get(new MethodSignature(returnType, args), () -> {
			StringBuilder builder = new StringBuilder("(");
			for (Class<?> arg : args) {
				builder.append(typeToNative(arg));
			}
			builder.append(")").append(typeToNative(returnType));
			return builder.toString();
		});
	}

	/*static final ObjectMap<Class<?>, Class<?>> TO_BOX_MAP = ObjectMap.of(
			boolean.class, Boolean.class,
			byte.class, Byte.class,
			char.class, Character.class,
			short.class, Short.class,
			int.class, Integer.class,
			float.class, Float.class,
			long.class, Long.class,
			double.class, Double.class
	);*/

	public static Class<?> box(Class<?> type) {
		if (type == boolean.class) return Boolean.class;
		if (type == byte.class) return Byte.class;
		if (type == char.class) return Character.class;
		if (type == short.class) return Short.class;
		if (type == int.class) return Integer.class;
		if (type == float.class) return Float.class;
		if (type == long.class) return Long.class;
		if (type == double.class) return Double.class;
		return type;
		// return TO_BOX_MAP.get(type, type);
	}

	public static void addCast(/*MethodVisitor*/ClassFileWriter asm, Class<?> type) {
		if (type == Void.TYPE) return;
		String tmp = nativeName(box(type));
		if (type.isPrimitive()) {
			// asm.emitConstantPoolUTF8(tmp);
			// asm.emitConstantPoolClass(asm.cpi());
			// asm.opc_checkcast(asm.cpi());
			// asm.emitConstantPoolUTF8(type.getSimpleName() + "Value");
			// short nameIdx = asm.cpi();
			// asm.emitConstantPoolUTF8("()" + tmp);
			// short typeIdx = asm.cpi();
			// asm.emitConstantPoolNameAndType(nameIdx, typeIdx);
			// asm.opc_invokespecial(asm.cpi(), 0, 0);\
			asm.add(CHECKCAST, tmp);
			asm.addInvoke(INVOKEVIRTUAL, tmp,
					type.getSimpleName() + "Value", nativeMethod(type));
		} else {
			asm.add(CHECKCAST, tmp);
			// asm.emitConstantPoolUTF8(tmp);
			// asm.emitConstantPoolClass(asm.cpi());
			// asm.opc_checkcast(asm.cpi());
			// asm.opc_checkcast(asm.cpi());
		}
	}

	// int -> Integer (装箱）
	public static void addBox(ClassFileWriter/*MethodVisitor*/ writer, Class<?> type) {
		if (type.isPrimitive()) {
			Class<?> boxCls = box(type);
			// Log.debug(type);
			writer.addInvoke(INVOKESTATIC, nativeName(boxCls), "valueOf", nativeMethod(boxCls, type));
		}
	}


	public static String typeToNative(Class<?> cls) {
		if (ArrayList_TYPE != null && cls == ArrayList.class) return ArrayList_TYPE;
		if (cls == int.class) return "I";
		if (cls == long.class) return "J";
		if (cls == float.class) return "F";
		if (cls == double.class) return "D";
		if (cls == char.class) return "C";
		if (cls == short.class) return "S";
		if (cls == byte.class) return "B";
		if (cls == boolean.class) return "Z";
		if (cls == void.class) return "V";
		if (cls.isArray()) return "[" + typeToNative(cls.getComponentType());
		return "L" + nativeName(cls) + ";";
	}

	public static short buildReturn(Class<?> returnType) {
		if (returnType == boolean.class || returnType == int.class
				|| returnType == byte.class || returnType == short.class
				|| returnType == char.class)
			return IRETURN;
		if (returnType == long.class) return LRETURN;
		if (returnType == float.class) return FRETURN;
		if (returnType == double.class) return DRETURN;
		if (returnType == void.class) return RETURN;
		// if (returnType == byte.class) return BRETURN;
		return ARETURN;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Include {
		boolean buildSuper() default false;
	}

	public static FilterResult[] filterMethods(Method[] methods) {
		Seq<FilterResult> filterResults = new Seq<>(true, methods.length, FilterResult.class);
		Include include;
		for (Method m : methods) {
			include = m.getAnnotation(Include.class);
			if (include == null) continue;
			int mod = m.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
				Class<?>[] realTypes = new Class[m.getParameterCount() - 1];
				Class<?>[] args = m.getParameterTypes();
				System.arraycopy(args, 1, realTypes, 0, realTypes.length);
				filterResults.add(new FilterResult(
						m, include,
						realTypes, null));
			}
		}
		return filterResults.items;
		// return new FilterResult(methodsSeq.items, includeSeq.items, realTypes.items, );
	}

	public static class FilterResult {
		public Method method;
		public Include include;
		public Class<?>[] realTypes0;
		public String descriptor;

		public FilterResult(Method method, Include include, Class<?>[] realTypes0, String descriptor) {
			this.method = method;
			this.include = include;
			this.realTypes0 = realTypes0;
			this.descriptor = descriptor;
		}
	}

	// public static final ObjectIntMap<Class<?>> TYPE2LOAD = ObjectMap.of()
	public static int addLoad(Class<?> type) {
		if (type == boolean.class || type == char.class
				|| type == short.class || type == byte.class
				|| type == int.class) return ILOAD;
		if (type == float.class) return FLOAD;
		if (type == long.class) return LLOAD;
		if (type == double.class) return DLOAD;
		return ALOAD;
	}

	public static int addStore(Class<?> type) {
		if (type == boolean.class || type == char.class
				|| type == short.class || type == byte.class
				|| type == int.class) return ISTORE;
		if (type == float.class) return FSTORE;
		if (type == long.class) return LSTORE;
		if (type == double.class) return DSTORE;
		return ASTORE;
	}

	// static Field valueF /* String */,
	// 		symbolF /* ClassWriter */,
	// 		classNameF, constantPoolF /* SymbolTable */;

	/*static void reflect() throws Throwable {
		valueF = String.class.getDeclaredField("value");
		valueF.setAccessible(true);
		symbolF = ClassWriter.class.getDeclaredField("symbolTable");
		symbolF.setAccessible(true);
		classNameF = symbolF.getType().getDeclaredField("className");
		classNameF.setAccessible(true);
		constantPoolF = symbolF.getType().getDeclaredField("constantPool");
		constantPoolF.setAccessible(true);
	}*/


	static {
		try {
			// reflect();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Provides a key with which to distinguish previously generated
	 * method stored in a hash table.
	 */
	static class MethodSignature {
		Class<?> returnType;
		Class<?>[] args;

		MethodSignature(Class<?> returnType, Class<?>[] args) {
			this.returnType = returnType;
			this.args = args;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodSignature))
				return false;
			MethodSignature sig = (MethodSignature) obj;
			if (returnType != sig.returnType)
				return false;
			if (args != sig.args) {
				if (args.length != sig.args.length)
					return false;
				for (int i = 0; i < args.length; i++)
					if (args[i] != sig.args[i])
						return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return returnType.hashCode() + Arrays.hashCode(args);
		}
	}
}
