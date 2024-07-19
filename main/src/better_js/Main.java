package better_js;

import arc.util.*;
import better_js.myrhino.*;
import better_js.mytest.TestAndroid;
import better_js.utils.MyReflect;
import dalvik.system.VMRuntime;
import mindustry.content.Blocks;
import mindustry.mod.Mod;
import mindustry.world.blocks.power.PowerNode;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;

import static mindustry.Vars.mods;

public class Main extends Mod {
    // public static Scripts scripts;
    public static final Unsafe unsafe = HopeVars.unsafe;

    static void internalModule() {
        try {
            Field moduleF = Class.class.getDeclaredField("module");
            /** 设置模块，使 JavaMembers 可以 setAccessible。
             @see java.lang.reflect.AccessibleObject#checkCanSetAccessible(Class, Class, boolean) */
            long off = unsafe.objectFieldOffset(moduleF);
            Module java_base = Object.class.getModule();
            unsafe.putObject(ForRhino.class, off, java_base);
            unsafe.putObject(MyMemberBox.class, off, java_base);
            unsafe.putObject(MyJavaMembers.class, off, java_base);
            unsafe.putObject(MyInterfaceAdapter.class, off, java_base);
            unsafe.putObject(MyReflect.class, off, java_base);
            unsafe.putObject(ScriptInstaller.class, off, java_base);
            // unsafe.putObject(Class.forName("rhino.JavaMembers"), off, java_base);
            // unsafe.putObject(Class.forName("rhino.VMBridge"), off, java_base);
            // 使json更快
            // unsafe.putObject(Json.class, off, java_base);
            unsafe.putObject(Reflect.class, off, java_base);
        } catch (Exception ignored) {
        }
    }

    public static final boolean disabledAll = false;
    public static Main main;

    public Main() {
        try {
            if (Class.forName(Main.class.getName(), false, mods.mainLoader()) != Main.class)
                return;
        } catch (ClassNotFoundException ignored) {
        }
        if (disabledAll) return;
        Log.info("load BetterJS constructor");
        main = this;

        if (!OS.isAndroid) internalModule();
        try {
            ScriptInstaller.initScripts();
        } catch (Throwable e) {
            Log.err(e);
        }
        // TestKt.main();
        // if (OS.isAndroid) TestAndroid.main(null);
        // Time.run(0, () -> {
        //     Blocks.battery.addBar("power", PowerNode.makePowerBalance());
        //     Blocks.battery.addBar("batteries", PowerNode.makeBatteryBalance());
        // });
    }

    static void clearReflectionFilter() throws Throwable {
        if (!OS.isAndroid) {
            Desktop.clearReflectionFilter();
            return;
        }
//		VMRuntime.getRuntime().setHiddenApiExemptions(new String[]{"L"});
        Method methodM = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
        methodM.setAccessible(true);
        LOOKUP.load();
//        Method m2 = (Method) LOOKUP.trust.unreflect(methodM).invoke(VMRuntime.class,
//                "setHiddenApiExemptions", new Class[]{String[].class});
		Method m2 = (Method) methodM.invoke(VMRuntime.class,
				"setHiddenApiExemptions", new Class[]{String[].class});
        m2.setAccessible(true);
        m2.invoke(VMRuntime.getRuntime(), (Object) new String[]{"L"});
    }

    static class LOOKUP {
        static MethodHandles.Lookup trust;
        static final Unsafe unsafe = getUnsafe();

        static Unsafe getUnsafe() {
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                return (Unsafe) theUnsafe.get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static void load() throws Exception {
			trust = MethodHandles.lookup();
        }
    }
}
