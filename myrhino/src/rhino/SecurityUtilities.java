/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package rhino;

import java.security.ProtectionDomain;

import static rhino.ScriptRuntimeES6.doPrivileged;

/** @author Attila Szegedi */
public class SecurityUtilities {
	/**
	 * Retrieves a system property within a privileged block. Use it only when the property is used
	 * from within Rhino code and is not passed out of it.
	 *
	 * @param name the name of the system property
	 *
	 * @return the value of the system property
	 */
	public static String getSystemProperty(final String name) {
		return doPrivileged(() -> System.getProperty(name));
	}

	public static ProtectionDomain getProtectionDomain(final Class<?> clazz) {
		return doPrivileged(() -> clazz.getProtectionDomain());
	}

	/**
	 * Look up the top-most element in the current stack representing a script and return its
	 * protection domain. This relies on the system-wide SecurityManager being an instance of {@link
	 * RhinoSecurityManager}, otherwise it returns <code>null</code>.
	 *
	 * @return The protection of the top-most script in the current stack, or null
	 */
	public static ProtectionDomain getScriptProtectionDomain() {
		return null;
	}
}
