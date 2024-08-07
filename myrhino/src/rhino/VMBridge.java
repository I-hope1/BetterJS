/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// API class

package rhino;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/** 为了适配，我创建了一些静态方法 */
public abstract class VMBridge {

	static final VMBridge instance = makeInstance();

	private static VMBridge makeInstance() {
		String[] classNames = {
		 "rhino.VMBridge_custom", "rhino.jdk18.VMBridge_jdk18",
		 };
		for (int i = 0; i != classNames.length; ++i) {
			String   className = classNames[i];
			Class<?> cl        = Kit.classOrNull(className);
			if (cl != null) {
				VMBridge bridge = (VMBridge) Kit.newInstanceOrNull(cl);
				if (bridge != null) {
					return bridge;
				}
			}
		}
		throw new IllegalStateException("Failed to create VMBridge instance");
	}

	/**
	 * Return a helper object to optimize {@link Context} access.
	 *
	 * <p>The runtime will pass the resulting helper object to the subsequent calls to {@link
	 * #getContext0(Object contextHelper)} and {@link #setContext0(Object contextHelper, Context cx)}
	 * methods. In this way the implementation can use the helper to cache information about current
	 * thread to make {@link Context} access faster.
	 */
	protected abstract Object getThreadContextHelper0();
	public static Object getThreadContextHelper() {
		return instance.getThreadContextHelper0();
	}

	/**
	 * Get {@link Context} instance associated with the current thread or null if none.
	 *
	 * @param contextHelper The result of {@link #getThreadContextHelper0()} called from the current
	 *                      thread.
	 */
	protected abstract Context getContext0(Object contextHelper);
	public static Context getContext(Object contextHelper) {
		return instance.getContext0(contextHelper);
	}

	/**
	 * Associate {@link Context} instance with the current thread or remove the current association
	 * if <code>cx</code> is null.
	 *
	 * @param contextHelper The result of {@link #getThreadContextHelper0()} called from the current
	 *                      thread.
	 */
	protected abstract void setContext0(Object contextHelper, Context cx);

	public static void setContext(Object contextHelper, Context cx) {
		instance.setContext0(contextHelper, cx);
	}


	/**
	 * In many JVMSs, public methods in private classes are not accessible by default (Sun Bug
	 * #4071593). VMBridge instance should try to workaround that via, for example, calling
	 * method.setAccessible(true) when it is available. The implementation is responsible to catch
	 * all possible exceptions like SecurityException if the workaround is not available.
	 *
	 * @return true if it was possible to make method accessible or false otherwise.
	 */
	protected abstract boolean tryToMakeAccessible(AccessibleObject accessible);

	/**
	 * Create helper object to create later proxies implementing the specified interfaces later.
	 * Under JDK 1.3 the implementation can look like:
	 *
	 * <pre>
	 * return java.lang.reflect.Proxy.getProxyClass(..., interfaces).
	 *     getConstructor(new Class[] {
	 *         java.lang.reflect.InvocationHandler.class });
	 * </pre>
	 *
	 * @param interfaces Array with one or more interface class objects.
	 */
	protected abstract Object getInterfaceProxyHelper0(ContextFactory cf, Class<?>[] interfaces);
	public static Object getInterfaceProxyHelper(ContextFactory cf, Class<?>[] interfaces) {
		return instance.getInterfaceProxyHelper0(cf, interfaces);
	}

	/**
	 * Create proxy object for {@link InterfaceAdapter}. The proxy should call {@link
	 * InterfaceAdapter#invoke(ContextFactory, Object, Scriptable, Object, Method, Object[])} as
	 * implementation of interface methods associated with <code>proxyHelper</code>. {@link Method}
	 *
	 * @param proxyHelper The result of the previous call to {@link
	 *                    #getInterfaceProxyHelper0(ContextFactory, Class[])}.
	 */
	protected abstract Object newInterfaceProxy(
	 Object proxyHelper,
	 ContextFactory cf,
	 InterfaceAdapter adapter,
	 Object target,
	 Scriptable topScope);
}
