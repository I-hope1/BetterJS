package better_js;

import static better_js.Desktop.unsafe;
import static better_js.ForRhino.unwrap;
import static better_js.reflect.JDKVars.junsafe;
import static mindustry.Vars.mods;

import java.lang.reflect.Field;
import java.net.*;
import java.util.*;

import arc.Core;
import arc.files.*;
import arc.struct.Seq;
import arc.util.*;
import better_js.android.AndroidDefiner;
import better_js.myrhino.*;
import better_js.myrhino.MyNativeJavaObject.Status;
import better_js.utils.*;
import jdk.internal.loader.*;
import mindustry.Vars;
import rhino.*;

public class ScriptInstaller {
	private static final String KEY_NAME = "$AX";

	// public static        ContextFactory factory;
	public static Object wa;
	public static Object ef;
	public static Object efm;
	public static Object interfaceAdapter;
	private static void __(Object... b) {}
	public static void initScripts() {
		try {
			Main.clearReflectionFilter();
		} catch (Throwable ex) {
			Log.err("Could not clear filter", ex);
		}
		if (OS.isAndroid) {
			resolveAndroid();
		} else {
			resolveDesktop();
		}

		wa = new MyFunc((cx, scope, args) -> BetterJSRhino.wrapAccess(cx, scope, args[0]));
		ef = new MyFunc((cx, scope, args) -> BetterJSRhino.evalFunc(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0]));
		efm = new MyFunc((cx, scope, args) -> BetterJSRhino.evalFuncWithStatus(cx, scope, args.length == 1 ? args[0] : args[1], args.length == 1 ? null : args[0], Status.accessMethod));
		interfaceAdapter = new MyFunc((cx, scope, args) ->
		 MyInterfaceAdapter.create(cx, unwrap(args[0]), unwrap(args[1]))
		);
		Core.app.post(ScriptInstaller::installScripts);
	}
	private static void resolveAndroid() {
		URL    url = Main.class.getClassLoader().getResource("mod.hjson");
		String path;
		try {
			path = Objects.requireNonNull(url).toURI().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		path = path.substring(path.indexOf("file:") + 5, path.lastIndexOf('!'));

		Fi root = new ZipFi(new Fi(path));
		Fi fi;
		Fi toFi;

		fi = root.child("rhino.jar");
		if (fi.exists()) {
			toFi = Vars.tmpDirectory.child(fi.name());
			fi.copyTo(toFi);
			AndroidDefiner.defineClassWithFile0(toFi.file(), Core.class.getClassLoader(), true);
			// Log.info(String.join("\n",Arrays.stream(Context.class.getDeclaredFields()).map(String::valueOf).toArray(String[]::new)));
		}

		fi = root.child("AndroidJavaAdapterLib.jar");
		toFi = Vars.tmpDirectory.child(fi.name());
		fi.copyTo(toFi);
		AndroidDefiner.defineClassWithFile(toFi.file(), JavaAdapter.class.getClassLoader());

	}
	private static void resolveDesktop() {
		try {
			desktopInit();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static void desktopInit() throws Throwable {
		Desktop.main();
		addUcp();
		/* redefineClass(
				 Seq.with(
					"rhino.SymbolScriptable",
					"rhino.Token",
					"rhino.TokenStream",
					"rhino.Node",
					"rhino.Node$NodeIterator",
					"rhino.Node$PropListItem",
					"rhino.ast.AstNode",
					"rhino.ast.Jump",
					"rhino.ast.Scope",
					"rhino.ast.ScriptNode",
					"rhino.ast.Symbol",
					"rhino.ast.AstRoot",
					"rhino.Parser",
					"rhino.Parser$PerFunctionVariables",
					"rhino.Parser$ConditionData",
					"rhino.IRFactory",
					"rhino.Decompiler",
					"rhino.ast.VariableDeclaration",
					"rhino.ast.Block",
					"rhino.ast.BigIntLiteral",
					"rhino.ast.CatchClause",
					"rhino.ast.TaggedTemplateLiteral",
					"rhino.ast.TemplateCharacters",
					"rhino.ast.UpdateExpression",
					"rhino.ast.TemplateLiteral"
				 ), () -> {
					 __(NativeJavaObject.class == NativeJavaClass.class.getSuperclass()
						, Token.class.getSuperclass() == Object.class
						, IRFactory.class, Context.class, Parser.class
						, Name.class.getSuperclass() == AstNode.class
						, NewExpression.class.getSuperclass() == FunctionCall.class
						, FunctionNode.class.getSuperclass() == ScriptNode.class
						, FunctionCall.class, StringLiteral.class);
					 Log.info(FunctionNode.class.getSuperclass() == ScriptNode.class);
				 }); */
		Desktop.init2();

		// init MyJavaAdapter
		try (var res = Main.class.getClassLoader()
		 .getResourceAsStream("better_js/myrhino/MyJavaAdapter.class")) {
			MyReflect.defineClass(Vars.class.getClassLoader(),
			 res.readAllBytes());
		}
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void addUcp() {
		long         ucpOff = junsafe.objectFieldOffset(BuiltinClassLoader.class, "ucp");
		URLClassPath ucp    = (URLClassPath) unsafe.getObject(Vars.class.getClassLoader(), ucpOff);
		ArrayDeque   list   = Reflect.get(URLClassPath.class, ucp, "unopenedUrls");
		ArrayList    path   = Reflect.get(URLClassPath.class, ucp, "path");

		Fi root = new ZipFi(Fi.get(((URLClassLoader) Main.class.getClassLoader()).getURLs()[0].getFile()));
		Fi fi   = root.child("rhino.jar");
		if (!fi.exists() || fi.isDirectory()) return;
		Fi toFi = Vars.tmpDirectory.child(fi.name());
		fi.copyTo(toFi);
		// Log.info("real: @ (expect: @)", Token.TEMPLATE_LITERAL, 170);
		try {
			URL url = toFi.file().toURI().toURL();
			list.addFirst(url);
			path.add(0, url);
			Vars.class.getClassLoader()
			 /* 一个不存在的文件 */
			 .getResource("$-");
			ArrayList loaders = Reflect.get(URLClassPath.class, ucp, "loaders");
			Reflect.set(URLClassPath.class, ucp, "loaders", Seq.with(loaders).reverse().list());

			// Log.info(Seq.with(Drill.class.getFields()).map(t -> t + "\n"));
			// Log.info("real: @ (expect: @)", Token.TEMPLATE_LITERAL, 170);
			// Log.info(loaders);
		} catch (MalformedURLException ignored) {}
	}

	public static void installScripts() {
		mods.getScripts();
		if (mods.getScripts().hasErrored())
			throw new IllegalStateException("Your platform cannot load scripts.");

		MyFunc cl_setter = new MyFunc((cx, scope, args) -> {
			MyJavaAdapter.sampleClass = unwrap(args[0]);
			return Undefined.instance;
		});
		MyFunc symbol_setter = new MyFunc((cx, scope, args) -> {
			ScriptableObject.putProperty(unwrap(args[0]), (Symbol) args[1], args[2]);
			return Undefined.instance;
		});
		/* 因为handle不能反射调用 */

		MyFunc del = new MyFunc((cx, scope, args) -> {
			if (!(args[0] instanceof NativeJavaObject obj)) return null;
			try {
				Object           members = Reflect.get(NativeJavaObject.class, obj, "members");
				NativeJavaMethod ctors   = Reflect.get(Class.forName("rhino.JavaMembers"), members, "ctors");
				Field            f       = NativeJavaMethod.class.getDeclaredField("methods");
				f.setAccessible(true);
				Object[] arr       = (Object[]) f.get(ctors);
				Class<?> memberBox = Class.forName("rhino.MemberBox");
				Field    f2        = memberBox.getDeclaredField("memberObject");
				f2.setAccessible(true);
				Seq    seq    = new Seq<>();
				Object unwrap = unwrap(args[1]);
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

		Scriptable scope = mods.getScripts().scope;

		// cx.setWrapFactory(ForRhino.wrapFactory);
		//		 		cx.setOptimizationLevel(-1);
		Object obj = new ScriptableObject() {
			final IdFunctionObject adapter = new IdFunctionObject(new MyJavaAdapter(), MyJavaAdapter.FTAG, MyJavaAdapter.Id_JavaAdapter,
			 "MyJavaAdapter", 1, scope);

			{
				adapter.markAsConstructor(null);
			}

			public String getClassName() {
				return KEY_NAME;
			}
			public Object get(String key, Scriptable obj) {
				return switch (key) {
					case "wa" -> wa;
					case "ef" -> ef;
					case "efm" -> efm;
					case "in" -> interfaceAdapter;
					case "extend" -> adapter;
					case "cl_set" -> cl_setter;
					case "sb" -> symbol_setter;
					case "INIT" -> MyJavaAdapter.INIT_SYMBOL;
					case "del" -> del;
					/* case "loader":
						return cloader; */
					default -> NOT_FOUND;
				};
			}
		};
		ScriptableObject.putConstProperty(scope, KEY_NAME, obj);
	}
	public interface RedefineClass {
		void get(ClassLoader loader, ClassLoader host, Seq<String> name, Runnable clinit)
		 throws Exception;
	}
}