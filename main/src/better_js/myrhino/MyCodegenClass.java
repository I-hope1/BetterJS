package better_js.myrhino;

import mindustry.Vars;
import rhino.CompilerEnvirons;
import rhino.ast.ScriptNode;
import rhino.optimizer.Codegen;

public class MyCodegenClass extends Codegen {
	public Object compile(CompilerEnvirons compilerEnv, ScriptNode tree, String encodedSource, boolean returnFunction) {
		return super.compile(compilerEnv, tree, encodedSource, returnFunction);
	}
	// public static WatchWindow watch;
	public byte[] compileToClassFile(CompilerEnvirons compilerEnv, String mainClassName, ScriptNode scriptOrFn,
																	 String encodedSource, boolean returnFunction) {
		var bytes = super.compileToClassFile(compilerEnv, mainClassName, scriptOrFn, encodedSource, returnFunction);
		/* scriptOrFn.visit(node -> {
			Log.info(node.debugPrint());
			Log.info(node);
			return true;
		});
		Events.on(ClientLoadEvent.class, e -> {
			watch = new WatchWindow();
			watch.watchConst("scriptOrFn", scriptOrFn);
			watch.showIfOk();
		}); */
		Vars.tmpDirectory.child("Main.class").writeBytes(bytes);
		return bytes;
	}
}
