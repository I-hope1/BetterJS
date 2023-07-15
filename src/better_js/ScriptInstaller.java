package better_js;

import arc.Core;
import arc.files.*;
import arc.struct.Seq;
import arc.util.*;
import better_js.Main.*;
import better_js.android.DexLoader;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.MyReflect;
import mindustry.Vars;
import rhino.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;

public class ScriptInstaller {
	public static ContextFactory factory;
	public static Function       wa;
	public static Function       ef;
	public static Function       efm;
	public static Function       interfaceAdapter;
	public static void initScripts() {
		wa = new MyFunc((cx, scope, args) ->
		 BetterJSRhino.wrapAccess(cx, scope, args[0])
		);
		ef = new MyFunc((cx, scope, args) ->
		 BetterJSRhino.evalFunc(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0])
		);
		efm = new MyFunc((cx, scope, args) ->
		 BetterJSRhino.evalFuncWithStatus(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0], Status.accessMethod)
		);
		interfaceAdapter = new MyFunc((cx, scope, args) ->
		 MyInterfaceAdapter.create(cx, (Class<?>) (
			args[0] instanceof NativeJavaObject ? ((NativeJavaObject) args[0]).unwrap() : args[0]
		 ), (ScriptableObject) args[1])
		);

		if (!OS.isAndroid) {
			try {
				Desktop.main(null);
				// init MyJavaAdapter
				MyReflect.defineClass(JavaAdapter.class.getClassLoader(),
				 Objects.requireNonNull(Main.class.getClassLoader()
					 .getResourceAsStream("better_js/myrhino/MyJavaAdapter.class"))
					.readAllBytes());
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} else Time.runTask(0, () -> {
			Fi fi   = Vars.mods.getMod(Main.class).root.child("AndroidLib.jar");
			Fi toFi = Vars.tmpDirectory.child(fi.name());
			fi.copyTo(toFi);
			/* fi = new ZipFi(toFi).child("classes.dex");
			toFi = Vars.tmpDirectory.child(fi.name());
			fi.copyTo(toFi); */
			try {
				DexLoader.defineClassWithFile(toFi.file(), JavaAdapter.class.getClassLoader());
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			Main.clearReflectionFilter();
		} catch (Throwable ex) {
			Throwable cause = ex;
			while (cause.getCause() != null) cause = cause.getCause();
			Log.err("can't clear reflection filter.", cause);
		}
		Core.app.post(ScriptInstaller::installScripts);
	}
	public static void installScripts() {
		Vars.mods.getScripts();
		try {
			factory = ForRhino.createFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		/*if (Context.getCurrentContext() != null) {
			try {
				MyReflect.setValue(Context.getCurrentContext(),
						Context.class.getDeclaredField("factory"),
						factory, true);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}*/
		MyFunc cl_setter = new MyFunc((cx, scope, args) -> {
			MyJavaAdapter.sampleClass = (Class<?>) ((NativeJavaObject) args[0]).unwrap();
			return Undefined.instance;
		});
		MyFunc symbol_setter = new MyFunc((cx, scope, args) -> {
			var obj = ((ScriptableObject) args[0]);
			ScriptableObject.putProperty(obj, (Symbol) args[1], args[2]);
			return Undefined.instance;
		});
		/* 因为handle不能反射调用 */
		MyFunc handle_invoker = new MyFunc((cx, scope, args) -> {
			try {
				Object[] _args = (Object[]) Array.newInstance(Object.class, args.length - 1);

				MethodHandle handle = (MethodHandle) ((NativeJavaObject) args[0]).unwrap();
				for (int i = 0; i < _args.length; i++) {
					_args[i] = Context.jsToJava(args[i + 1], handle.type().parameterType(i));
				}
				return handle.invokeWithArguments(_args);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});

		MyFunc del = new MyFunc((cx, scope, args) -> {
			if (!(args[0] instanceof NativeJavaObject obj)) return null;
			try {
				Field f = NativeJavaObject.class.getDeclaredField("members");
				f.setAccessible(true);
				Object members = f.get(obj);
				f = Class.forName("rhino.JavaMembers").getDeclaredField("ctors");
				f.setAccessible(true);
				NativeJavaMethod ctors = (NativeJavaMethod) f.get(members);
				f = NativeJavaMethod.class.getDeclaredField("methods");
				f.setAccessible(true);
				Object[] arr       = (Object[]) f.get(ctors);
				Class<?> memberBox = Class.forName("rhino.MemberBox");
				Field    f2        = memberBox.getDeclaredField("memberObject");
				f2.setAccessible(true);
				Seq    seq    = new Seq<>();
				Object unwrap = ((NativeJavaObject) args[1]).unwrap();
				for (Object o : arr) {
					if (!unwrap.equals(f2.get(o))) seq.add(o);
				}
				// JSFunc.showInfo(f2.get(arr[2]));
				// JSFunc.showInfo(unwrap);
				f.set(ctors, seq.toArray(memberBox));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return null;
		});

		Scriptable scope = Vars.mods.getScripts().scope;
		// Context    cx    = mods.getScripts().context;
		// cx.setWrapFactory(ForRhino.wrapFactory);
		// 		cx.setOptimizationLevel(-1);
		Object obj = new ScriptableObject() {
			final IdFunctionObject adapter = new IdFunctionObject(new MyJavaAdapter(), MyJavaAdapter.FTAG, MyJavaAdapter.Id_JavaAdapter,
			 "MyJavaAdapter", 1, scope);

			{
				adapter.markAsConstructor(null);
			}

			public String getClassName() {
				return "$AX";
			}
			public Object get(String key, Scriptable obj) {
				return switch (key) {
					case "wa" -> wa;
					case "ef" -> ef;
					case "efm" -> efm;
					case "in" -> interfaceAdapter;
					case "extend" -> adapter;
					case "cl" -> cl_setter;
					case "sb" -> symbol_setter;
					case "invoke" -> handle_invoker;
					case "pr" -> Context.javaToJS(PRIVATE.class, scope);
					case "INIT" -> MyJavaAdapter.INIT_SYMBOL;
					case "del" -> del;
					/* case "loader":
						return cloader; */
					default -> NOT_FOUND;
				};
			}
		};
		ScriptableObject.putConstProperty(scope, "$AX", obj);
	}
}