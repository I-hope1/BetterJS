package better_js.reflect;

import better_js.utils.A_ASM.MyClass;
import rhino.classfile.ByteCode;
import rhino.classfile.ClassFileWriter;

import java.lang.reflect.Modifier;

public class SetField {
	// public static final Unsafe unsafe = Unsafe.getUnsafe();
	public static void init(MyClass<?> myFactoryClass) throws ClassNotFoundException {
		Class<?> cls = Class.forName("jdk.internal.reflect.UnsafeFieldAccessorFactory");
		ClassFileWriter cfw = myFactoryClass.writer;
		String type = "(Ljava/lang/reflect/Field;Z)Ljdk/internal/reflect/FieldAccessor;";
		cfw.startMethod("newFieldAccessor", type, (short) Modifier.PUBLIC);

		cfw.addALoad(1);
		cfw.addPush(JDKVars.VERSION <= 11);
		cfw.addInvoke(ByteCode.INVOKESTATIC, cls.getName(), "newFieldAccessor", type);
		// cfw.add(ByteCode.ACONST_NULL);
		cfw.add(ByteCode.ARETURN);

		cfw.stopMethod((short) 3); // this + args + var * 1
	}
	/*public static void init_disabled(MyClass<?> myFactoryClass) {
		myFactoryClass.setFunc("newFieldAccessor", (self, _args) -> {
			Field f = (Field) _args.get(0);
			final boolean isStatic = Modifier.isStatic(f.getModifiers());
			if (isStatic) return new FieldAccessor() {
				public final long off = unsafe.staticFieldOffset(f);
				public final Class<?> base = f.getDeclaringClass();
				public final Class<?> type = ByteCodeTools.box(f.getType());

				public Object get(Object obj) throws IllegalArgumentException {
					return unsafe.getObject(base, off);
				}

				public boolean getBoolean(Object obj) throws IllegalArgumentException {
					return unsafe.getBoolean(base, off);
				}

				public byte getByte(Object obj) throws IllegalArgumentException {
					return unsafe.getByte(base, off);
				}

				public char getChar(Object obj) throws IllegalArgumentException {
					return unsafe.getChar(base, off);
				}

				public short getShort(Object obj) throws IllegalArgumentException {
					return unsafe.getShort(base, off);
				}

				public int getInt(Object obj) throws IllegalArgumentException {
					return unsafe.getInt(base, off);
				}

				public long getLong(Object obj) throws IllegalArgumentException {
					return unsafe.getLong(base, off);
				}

				public float getFloat(Object obj) throws IllegalArgumentException {
					return unsafe.getFloat(base, off);
				}

				public double getDouble(Object obj) throws IllegalArgumentException {
					return unsafe.getDouble(base, off);
				}

				public void set(Object obj, Object val) throws IllegalArgumentException {
					if (!type.isInstance(val)) throw new IllegalArgumentException("" + val);
					unsafe.putObject(base, off, val);
				}

				public void setBoolean(Object obj, boolean val) throws IllegalArgumentException {
					unsafe.putBoolean(base, off, val);
				}

				public void setByte(Object obj, byte val) throws IllegalArgumentException {
					unsafe.putByte(base, off, val);
				}

				public void setChar(Object obj, char val) throws IllegalArgumentException {
					unsafe.putChar(base, off, val);
				}

				public void setShort(Object obj, short val) throws IllegalArgumentException {
					unsafe.putShort(base, off, val);
				}

				public void setInt(Object obj, int val) throws IllegalArgumentException {
					unsafe.putInt(base, off, val);
				}

				public void setLong(Object obj, long val) throws IllegalArgumentException {
					unsafe.putLong(base, off, val);
				}

				public void setFloat(Object obj, float val) throws IllegalArgumentException {
					unsafe.putFloat(base, off, val);
				}

				public void setDouble(Object obj, double val) throws IllegalArgumentException {
					unsafe.putDouble(base, off, val);
				}
			};
			return new FieldAccessor() {
				public final long off = unsafe.objectFieldOffset(f);
				public final Class<?> base = f.getDeclaringClass();
				public final Class<?> type = ByteCodeTools.box(f.getType());

				public Object get(Object obj) throws IllegalArgumentException {
					return unsafe.getObject(obj, off);
				}

				public boolean getBoolean(Object obj) throws IllegalArgumentException {
					return unsafe.getBoolean(obj, off);
				}

				public byte getByte(Object obj) throws IllegalArgumentException {
					return unsafe.getByte(obj, off);
				}

				public char getChar(Object obj) throws IllegalArgumentException {
					return unsafe.getChar(obj, off);
				}

				public short getShort(Object obj) throws IllegalArgumentException {
					return unsafe.getShort(obj, off);
				}

				public int getInt(Object obj) throws IllegalArgumentException {
					return unsafe.getInt(obj, off);
				}

				public long getLong(Object obj) throws IllegalArgumentException {
					return unsafe.getLong(obj, off);
				}

				public float getFloat(Object obj) throws IllegalArgumentException {
					return unsafe.getFloat(obj, off);
				}

				public double getDouble(Object obj) throws IllegalArgumentException {
					return unsafe.getDouble(obj, off);
				}

				public void set(Object obj, Object val) throws IllegalArgumentException {
					unsafe.putObject(obj, off, val);
				}

				public void setBoolean(Object obj, boolean val) throws IllegalArgumentException {
					unsafe.putBoolean(obj, off, val);
				}

				public void setByte(Object obj, byte val) throws IllegalArgumentException {
					unsafe.putByte(obj, off, val);
				}

				public void setChar(Object obj, char val) throws IllegalArgumentException {
					unsafe.putChar(obj, off, val);
				}

				public void setShort(Object obj, short val) throws IllegalArgumentException {
					unsafe.putShort(obj, off, val);
				}

				public void setInt(Object obj, int val) throws IllegalArgumentException {
					unsafe.putInt(obj, off, val);
				}

				public void setLong(Object obj, long val) throws IllegalArgumentException {
					unsafe.putLong(obj, off, val);
				}

				public void setFloat(Object obj, float val) throws IllegalArgumentException {
					unsafe.putFloat(obj, off, val);
				}

				public void setDouble(Object obj, double val) throws IllegalArgumentException {
					unsafe.putDouble(obj, off, val);
				}
			};
		}, Modifier.PUBLIC, FieldAccessor.class, Field.class, boolean.class);

		// print(unsafe.arrayBaseOffset(ints.getClass()));
	}*/
}
