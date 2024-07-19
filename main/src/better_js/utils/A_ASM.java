package better_js.utils;

import arc.files.Fi;
import arc.func.Prov;
import arc.struct.*;
import arc.util.OS;
import better_js.utils.A_ASM.MethodDeclared.MethodAccess.MOpcode;
import hope_android.FieldUtils;
import jdk.internal.misc.Unsafe;
import rhino.classfile.ClassFileWriter;

import java.io.*;
import java.lang.annotation.*;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import static better_js.utils.MyReflect.*;
import static rhino.classfile.ByteCode.*;
import static rhino.classfile.ClassFileWriter.*;
// import static org.objectweb.asm.Opcodes.*;

public class A_ASM {
	public static final HashMap<MethodSignature, String> METHOD_CACHES = new HashMap<>();

	public static final String
	 KEY_FUNCTION     = "_KF",
	 NAME_INIT        = "<init>",
	 TMP_CLASS        = "_HOPE_",
	 CONSUMER_TYPE    = "Ljava/util/function/Consumer;",
	 CONSUMER_M       = "java/util/function/Consumer",
	 ARRAY_LIST_TYPE  = "Ljava/util/ArrayList;",
	 ARRAY_LIST_M     = "java/util/ArrayList",
	 BI_FUNCTION_TYPE = "Ljava/util/function/BiFunction;",
	 BI_FUNCTION_M    = "java/util/function/BiFunction",
	 VOID_METHOD      = "()V";
	private static int lastID = 0;

	/*static  {
		Field f = ClassWriter.class.getDeclaredField("symbolTable");
		f.getType().getDeclaredField("")
	}*/
	public static class MyClass<T> {
		public final ClassFileWriter writer;
		public final String          adapterName, superName;
		public final  Class<?>            superClass;
		private final ArrayList<Queue<?>> queues = new ArrayList<>();

		public MyClass(String name, Class<T> superClass, String... interfaces) {
			this.superClass = superClass;
			adapterName = name.replace('.', '/');
			// Log.info(adapterName);
			superName = dot2slash(superClass);
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

		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, boolean buildSuper,
														Class<V> returnType, Class<?>... args) {
			setFunc(name, func2, Modifier.PUBLIC, buildSuper, returnType, args);
		}


		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, int flags, boolean buildSuper,
														Class<V> returnType, Class<?>... args) {
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
			String fieldName = KEY_FUNCTION + "_l" + lastID++;
			short  max       = (short) (args.length + 1);
			int    v1        = max++, v2 = max++;
			queues.add(new Queue<>(fieldName, () -> func2));
			writer.addField(fieldName, BI_FUNCTION_TYPE, (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			// var writer = writer;
			writer.startMethod(name, nativeMethod(returnType, args), (short) flags);

			if (buildSuper) {
				buildSuper(writer, name, returnType, args);
				// 储存为v1
				if (returnType != void.class) writer.add(addStore(returnType), v1);
			}

			// new ArrayList(args.length)
			writer.add(NEW, ARRAY_LIST_M);
			writer.add(DUP);
			writer.add(BIPUSH, args.length + (buildSuper ? 1 : 0));
			writer.addInvoke(INVOKESPECIAL, ARRAY_LIST_M, NAME_INIT, "(I)V");
			writer.add(ASTORE, v2);

			// 将参数储存 arraylist
			for (int i = 0; i < args.length; i++) {
				writer.add(ALOAD, v2); // arraylist
				writer.add(addLoad(args[i]), i + 1);
				addBox(writer, args[i]);
				writer.addInvoke(INVOKEVIRTUAL, ARRAY_LIST_M, "add", "(Ljava/lang/Object;)Z"/*+ ArrayList_NATIVE +";"*/);
			}
			// 将super的返回值存入arraylist
			if (buildSuper && returnType != void.class) {
				writer.add(ALOAD, v2); // arraylist
				writer.add(addLoad(returnType), v1); // super return
				addBox(writer, returnType);
				// add
				writer.addInvoke(INVOKEVIRTUAL, ARRAY_LIST_M, "add", "(Ljava/lang/Object;)Z"/*+ ArrayList_NATIVE +";"*/);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, BI_FUNCTION_TYPE);

			writer.addLoadThis(); // this
			writer.add(ALOAD, v2); // arraylist

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, BI_FUNCTION_M, "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			addCast(writer, returnType);
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(buildReturn(returnType));
			// writer.visitMaxs(max, max); // this + args + var * 1
			writer.stopMethod(max);

			// writer.visitEnd();
		}

		public <V> void setFunc(String name, BiFunction<T, ArrayList<Object>, Object> func2, int flags, Class<V> returnType,
														Class<?>... args) {
			setFunc(name, func2, flags, false, returnType, args);
		}

		public void setFuncVoid(String name, Consumer<T> afCons, boolean buildSuper, Class<?>... args) {
			String fieldName = KEY_FUNCTION + "_l" + lastID++;
			queues.add(new Queue<>(fieldName, () -> afCons));
			writer.addField(fieldName, CONSUMER_TYPE, (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			// var writer = writer;
			writer.startMethod(name, nativeMethod(void.class, args), ACC_PUBLIC);

			if (buildSuper) {
				buildSuper(writer, name, void.class, args);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, CONSUMER_TYPE);

			writer.addLoadThis(); // this

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, CONSUMER_M, "accept", "(Ljava/lang/Object;)V");
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(RETURN);

			writer.stopMethod((short) (args.length + 2)/*, args.length + 1*/); // this
			// writer.visitEnd();
		}

		public <P1> void setFuncVoid(String name, BiConsumer<T, P1> afCons, Class<P1> arg1, boolean buildSuper) {
			String fieldName = KEY_FUNCTION + "_l" + lastID++;
			queues.add(new Queue<>(fieldName, () -> afCons));
			writer.addField(fieldName, LtypeName(BiConsumer.class), (short) (ACC_PRIVATE | ACC_STATIC | ACC_FINAL));
			String methodType = "(" + LtypeName(arg1) + ")V";
			// var writer = writer;
			writer.startMethod(name, methodType, ACC_PUBLIC);

			if (buildSuper) {
				writer.addLoadThis(); // this
				writer.add(addLoad(arg1), 1); // args[1]
				// super
				writer.addInvoke(INVOKESPECIAL, superName, name, methodType);
			}

			// 获取functionKey字段
			writer.add(GETSTATIC, adapterName, fieldName, LtypeName(BiConsumer.class));

			writer.addLoadThis(); // this
			writer.add(addLoad(arg1), 1); // args[1]

			// V get(args)
			writer.addInvoke(INVOKEINTERFACE, dot2slash(BiConsumer.class), "apply", "(Ljava/lang/Object;Ljava/lang/Object;)V");
			// writer.add(CHECKCAST, nativeName(returnType));
			writer.add(RETURN);

			// writer.visitMaxs(2, 2); // this + arg
			// writer.visitEnd();
		}


		public void setFuncVoid(String name, BiConsumer<T, ArrayList<Object>> cons2, boolean buildSuper, Class<?>... args) {
			setFuncVoid(name, cons2, Modifier.PUBLIC, buildSuper, args);
		}


		public void setFuncVoid(String name, BiConsumer<T, ArrayList<Object>> cons2, int flags, boolean buildSuper,
														Class<?>... args) {
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
			writer.startMethod(methodName, nativeMethod(fieldType), ACC_PUBLIC);
			writer.addLoadThis(); // this
			writer.add(GETFIELD, adapterName, fieldName, LtypeName(fieldType));
			writer.add(buildReturn(fieldType));
			// writer.visitMaxs(1, 1); // this
			writer.stopMethod((short) 1);

			// writer.visitEnd();
		}

		public void buildPutFieldFunc(String fieldName, String methodName, Class<?> fieldType) {
			// var writer = writer;
			writer.startMethod(methodName, nativeMethod(void.class, fieldType), ACC_PUBLIC);
			writer.addLoadThis(); // this
			writer.add(addLoad(fieldType), 1); // arg1
			writer.add(PUTFIELD, adapterName, fieldName, LtypeName(fieldType));
			writer.add(RETURN);
			writer.stopMethod((short) 2/*, 2*/); // this + arg1

			// writer.visitEnd();
		}

		public void
		setField(int flags, Class<?> type, String name) {
			// return asm.addField(flags, name, typeToNative(type), null, null);
			writer.addField(name, LtypeName(type), (short) flags);
		}

		public <T2> void setField(int flags, Class<?> type, String name, T2 value) {
			if (!Modifier.isStatic(flags)) throw new IllegalArgumentException("field is not static.");
			queues.add(new Queue<>(name, () -> value));
			// asm.addField(flags, name, typeToNative(type), null, null);
			writer.addField(name, LtypeName(type), (short) flags);
		}

		public Class<? extends T> defineNative() {
			return (Class<? extends T>) MyReflect.defineClassNative(adapterName, superClass, writer.toByteArray());
		}
		public Class<? extends T> define() {
			return define(superClass.getClassLoader());
		}

		public Class<? extends T> define(ClassLoader loader) {
			Class<?> base = MyReflect.defineClass(adapterName, loader, writer.toByteArray());
			setFunctionKey(queues, base);
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
			Method     m;
			Class<?>[] types, realTypes;
			Class<?>   returnType;
			String     descriptor;
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
				writer.addInvoke(INVOKESTATIC, dot2slash(cls),
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

		public void writeTo(File fi) {
			try {
				if (!fi.exists()) fi.createNewFile();
				var stream = new FileOutputStream(fi);
				stream.write(writer.toByteArray());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void writeTo(Fi fi) {
			if (fi.isDirectory()) fi = fi.child(adapterName.replace('/', '.') + ".class");
			fi.writeBytes(writer.toByteArray());
		}

		public void addInterface(Class<?> cls) {
			writer.addInterface(cls.getName().replace('.', '/'));
		}

		public <V> void setFuncSelf(String name, ToIntFunction<ClassFileWriter> run, int flags, Class<V> returnType,
																Class<?>... args) {
			writer.startMethod(name, nativeMethod(returnType, args), (short) flags);
			writer.stopMethod((short) run.applyAsInt(writer)); // this + args + var * 1
		}
	}
	private static void setFunctionKey(ArrayList<Queue<?>> queues, Class<?> base) {
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
	}

	public static class Queue<T> {
		public String   name;
		public Prov<T>  func;
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

	public static String dot2slash(Class<?> cls) {
		return cls.getName().replace('.', '/');
	}

	public static String nativeMethod(MethodType methodType) {
		return nativeMethod(methodType.returnType(),
		 methodType.parameterArray());
	}
	public static String nativeMethod(Class<?> returnType, Class<?>... args) {
		return METHOD_CACHES.computeIfAbsent(new MethodSignature(returnType, args), __ -> {
			StringBuilder builder = new StringBuilder("(");
			for (Class<?> arg : args) {
				builder.append(LtypeName(arg));
			}
			builder.append(")").append(LtypeName(returnType));
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
		String tmp = dot2slash(box(type));
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
			writer.addInvoke(INVOKESTATIC, dot2slash(boxCls), "valueOf", nativeMethod(boxCls, type));
		}
	}


	/** 含有前缀L的名称 */
	public static String LtypeName(Class<?> cls) {
		if (cls == ArrayList.class) return ARRAY_LIST_TYPE;
		if (cls == BiFunction.class) return BI_FUNCTION_TYPE;
		if (cls == Consumer.class) return CONSUMER_TYPE;
		if (cls == int.class) return "I";
		if (cls == long.class) return "J";
		if (cls == float.class) return "F";
		if (cls == double.class) return "D";
		if (cls == char.class) return "C";
		if (cls == short.class) return "S";
		if (cls == byte.class) return "B";
		if (cls == boolean.class) return "Z";
		if (cls == void.class) return "V";
		if (cls.isArray()) return "[" + LtypeName(cls.getComponentType());
		return "L" + dot2slash(cls) + ";";
	}
	/* 我不知道array是什么样的 */
	public static String LtypeName(String cls) {
		if (cls.length() == 1) {
			return switch (cls.charAt(0)) {
				case 'I', 'J', 'F', 'D', 'C', 'S', 'B', 'Z', 'V' -> cls;
				default -> "L" + cls + ";";
			};
		}
		return "L" + cls + ";";
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
		Include           include;
		for (Method m : methods) {
			include = m.getAnnotation(Include.class);
			if (include == null) continue;
			int mod = m.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
				Class<?>[] realTypes = new Class[m.getParameterCount() - 1];
				Class<?>[] args      = m.getParameterTypes();
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
		public Method     method;
		public Include    include;
		public Class<?>[] realTypes0;
		public String     descriptor;

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

	/**
	 * Provides a key with which to distinguish previously generated
	 * method stored in a hash table.
	 */
	static class MethodSignature {
		Class<?>   returnType;
		Class<?>[] args;

		MethodSignature(Class<?> returnType, Class<?>[] args) {
			this.returnType = returnType;
			this.args = args;
		}
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodSignature sig))
				return false;
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
		public int hashCode() {
			return returnType.hashCode() + Arrays.hashCode(args);
		}
	}

	public static class MethodDeclared {
		private static final String ldcName = "<ldc>";

		private final MyClass<?> f;
		private final int        flags;
		private final String     name;
		private final MethodType fmethodType;

		private final ArrayList<Variable> locals = new ArrayList<>();
		private final String              descriptor;
		private       Variable            caller;
		public MethodDeclared(MyClass<?> myClass, int flags, String name, MethodType fmethodType) {
			this.f = myClass;
			this.flags = flags;
			this.fmethodType = fmethodType;
			this.name = name;
			descriptor = nativeMethod(fmethodType);
			f.writer.startMethod(name, descriptor, (short) flags);
		}
		Stack<Variable> variables = new Stack<>();

		/* public Variable ldc(Class<?> clazz) {
			f.writer.addLoadConstant(clazz);
			return new Variable(ldcName, "java/lang/Class");
		} */
		public Variable ldc(boolean k) {
			f.writer.addPush(k);
			return new Variable(ldcName, "Z");
		}
		public Variable ldc(long i) {
			f.writer.addPush(i);
			return new Variable(ldcName, "J");
		}
		public Variable ldc(double i) {
			f.writer.addPush(i);
			return new Variable(ldcName, "D");
		}
		public Variable ldc(float i) {
			f.writer.addLoadConstant(i);
			return new Variable(ldcName, "F");
		}
		public Variable ldc(int i) {
			f.writer.addPush(i);
			return new Variable(ldcName, "I");
		}
		public Variable ldc(String str) {
			f.writer.addLoadConstant(str);
			return new Variable(ldcName, "java/lang/String");
		}
		private String getName(Variable caller) {
			if (caller == Variable.THIS) return f.adapterName;
			if (caller == Variable.SUPER) return f.superName;
			if (caller == Variable.NULL) return null;
			//noinspection StringEquality
			return caller.name == ldcName ? caller.type : caller.name;
		}
		public MethodAccess methodAccess(MOpcode op, Variable caller) {
			return new MethodAccess(op).caller(caller);
		}
		public MethodAccess methodAccess(MOpcode op) {
			MethodAccess access = new MethodAccess(op);
			if (caller != null) {
				access.caller(caller);
				caller = null;
			}
			return access;
		}
		public FieldAccess fieldAccess(Variable caller) {
			return new FieldAccess().caller(caller);
		}
		public FieldAccess fieldAccess() {
			FieldAccess access = new FieldAccess();
			if (caller != null) {
				access.caller(caller);
				caller = null;
			}
			return access;
		}
		public FieldAccess fieldAccessStatic() {
			return new FieldAccess().static_();
		}

		public Variable _null() {
			f.writer.add(ACONST_NULL);
			return Variable.NULL;
		}
		public boolean hasReturn = false;
		public MethodDeclared _return(Class<?> type) {
			f.writer.add(buildReturn(type));
			hasReturn = true;
			return this;
		}

		public void build() {
			if (!hasReturn) {
				if (fmethodType.returnType() == void.class) f.writer.add(RETURN);
				else throw new IllegalStateException("You don't build return.");
			}
			f.writer.stopMethod((short) (
			 Arrays.stream(fmethodType.parameterArray())
				.mapToInt(A_ASM::getStackSize)
				.reduce(0, Integer::sum) // 每个参数都是一个变量
			 + (Modifier.isStatic(flags) ? 0 : 1) // this也是一个变量（static没有this）
			 + locals.size() // 新建的变量
			));
		}
		public Variable _this() {
			f.writer.add(ALOAD_0);
			return Variable.THIS;
		}
		public Variable _super() {
			f.writer.add(ALOAD_0);
			return Variable.SUPER;
		}
		public static class Variable {
			String name, type;
			public Variable(String name, String type) {
				this.name = name;
				this.type = type;
			}
			public String name() {
				return name;
			}
			public String type() {
				return type;
			}
			static Variable SUPER = new Variable("<super>", null);
			static Variable THIS  = new Variable("<this>", null);
			static Variable NULL  = new Variable("null", null);
		}
		public MethodDeclared buildSuper() {
			f.writer.addLoadThis();
			int len = fmethodType.parameterCount();
			for (short i = 1; i <= len; i++) {
				// cb.emitShort((short) addLoad(args[i - 1]), i);
				f.writer.add(addLoad(fmethodType.parameterType(i - 1)), i);
			}
			f.writer.addInvoke(INVOKESPECIAL, f.superName, name, descriptor);
			return this;
		}

		public class MethodAccess {
			public enum MOpcode {
				// 0xB6
				virtual, special, static_, interface_, dynamic
			}
			MOpcode op;
			public MethodAccess(MOpcode op) {
				this.op = op;
			}
			public String clazz, name, methodType;
			public MethodAccess caller(String clazz) {
				this.clazz = clazz;
				return this;
			}
			public MethodAccess caller(Class<?> clazz) {
				return caller(dot2slash(clazz));
			}
			public MethodAccess name(String name) {
				this.name = name;
				return this;
			}
			public MethodAccess type(String type) {
				this.methodType = type;
				return this;
			}
			public MethodAccess type(MethodType type) {
				return type(nativeMethod(type));
			}
			public MethodAccess caller(Variable caller) {
				clazz = getName(caller);
				return this;
			}
			public MethodAccess addToArgs(Variable variable) {
				return this;
			}
			public MethodAccess addToArgs(Variable... variables) {
				return this;
			}

			public Variable build() {
				f.writer.addInvoke(op.ordinal() + 0xB6, clazz, name, methodType);
				if (methodType.length() - methodType.lastIndexOf(')') == 2) return null;
				String varName = methodType.substring(methodType.lastIndexOf(')') + 2, methodType.length() - 1);
				return new Variable(varName, null);
			}
			public MethodDeclared buildAsCaller() {
				f.writer.addInvoke(op.ordinal() + 0xB6, clazz, name, methodType);
				String varName = methodType.substring(methodType.lastIndexOf(')') + 2, methodType.length() - 1);
				caller = new Variable(varName, null);
				return MethodDeclared.this;
			}
			public String toString() {
				return "MethodAccess{" +
							 "op=" + op +
							 ", clazz='" + clazz + '\'' +
							 ", name='" + name + '\'' +
							 ", methodType='" + methodType + '\'' +
							 '}';
			}
		}

		public class FieldAccess {
			boolean isStatic;
			public FieldAccess static_() {
				isStatic = true;
				return this;
			}
			String className, fieldName, fieldType;
			public FieldAccess clazz(String className) {
				this.className = className;
				return this;
			}
			public FieldAccess clazz(Class<?> clazz) {
				return clazz(dot2slash(clazz));
			}
			public FieldAccess name(String fieldName) {
				this.fieldName = fieldName;
				return this;
			}
			public FieldAccess type(String fieldType) {
				this.fieldType = fieldType;
				return this;
			}
			public FieldAccess type(Class<?> clazz) {
				return type(dot2slash(clazz));
			}
			public FieldAccess caller(Variable caller) {
				className = getName(caller);
				return this;
			}

			public Variable buildGet() {
				if (fieldType == null) throw new IllegalStateException("fieldType is null");
				if (className == null) throw new IllegalStateException("className is null");
				if (fieldName == null) throw new IllegalStateException("fieldName is null");
				f.writer.add(isStatic ? GETSTATIC : GETFIELD, className, fieldName,
				 "L" + fieldType + ";");
				return new Variable(fieldType, null);
			}
			public MethodDeclared getAsCaller() {
				caller = buildGet();
				return MethodDeclared.this;
			}

			public Variable buildSet(Variable value) {
				if (fieldType == null) throw new IllegalStateException("fieldType is null");
				if (className == null) throw new IllegalStateException("className is null");
				if (fieldName == null) throw new IllegalStateException("fieldName is null");
				f.writer.add(isStatic ? PUTSTATIC : PUTFIELD, className, fieldName,
				 "L" + fieldType + ";");
				return value;
			}
		}
	}
	private static int getStackSize(Class<?> t) {
		return t == double.class || t == long.class ? 2 : 1;
	}
}
