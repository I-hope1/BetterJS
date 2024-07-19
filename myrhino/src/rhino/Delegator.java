/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// API class

package rhino;

/**
 * This is a helper class for implementing wrappers around Scriptable objects. It implements the
 * Function interface and delegates all invocations to a delegee Scriptable object. The normal use
 * of this class involves creating a sub-class and overriding one or more of the methods.
 *
 * <p>A useful application is the implementation of interceptors, pre/post conditions, debugging.
 *
 * @see Function
 * @see Scriptable
 * @author Matthias Radestock
 */
public class Delegator implements Function, SymbolScriptable {

    protected Scriptable obj = null;

    /**
     * Create a Delegator prototype.
     *
     * <p>This constructor should only be used for creating prototype objects of Delegator.
     *
     * @see rhino.Delegator#construct
     */
    public Delegator() {}

    /**
     * Create a new Delegator that forwards requests to a delegee Scriptable object.
     *
     * @param obj the delegee
     * @see rhino.Scriptable
     */
    public Delegator(Scriptable obj) {
        this.obj = obj;
    }

    /**
     * Crete new Delegator instance. The default implementation calls this.getClass().newInstance().
     *
     * @see #construct(Context cx, Scriptable scope, Object[] args)
     */
    protected Delegator newInstance() {
        try {
            return this.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }

    /**
     * Retrieve the delegee.
     *
     * @return the delegee
     */
    public Scriptable getDelegee() {
        return obj;
    }

    /**
     * Set the delegee.
     *
     * @param obj the delegee
     * @see rhino.Scriptable
     */
    public void setDelegee(Scriptable obj) {
        this.obj = obj;
    }

    /** @see rhino.Scriptable#getClassName */
    @Override
    public String getClassName() {
        return getDelegee().getClassName();
    }

    /** @see rhino.Scriptable#get(String, Scriptable) */
    @Override
    public Object get(String name, Scriptable start) {
        return getDelegee().get(name, start);
    }

    @Override
    public Object get(Symbol key, Scriptable start) {
        final Scriptable delegee = getDelegee();
        if (delegee instanceof SymbolScriptable) {
            return ((SymbolScriptable) delegee).get(key, start);
        }
        return Scriptable.NOT_FOUND;
    }

    /** @see rhino.Scriptable#get(int, Scriptable) */
    @Override
    public Object get(int index, Scriptable start) {
        return getDelegee().get(index, start);
    }

    /** @see rhino.Scriptable#has(String, Scriptable) */
    @Override
    public boolean has(String name, Scriptable start) {
        return getDelegee().has(name, start);
    }

    @Override
    public boolean has(Symbol key, Scriptable start) {
        final Scriptable delegee = getDelegee();
        if (delegee instanceof SymbolScriptable) {
            return ((SymbolScriptable) delegee).has(key, start);
        }
        return false;
    }

    /** @see rhino.Scriptable#has(int, Scriptable) */
    @Override
    public boolean has(int index, Scriptable start) {
        return getDelegee().has(index, start);
    }

    /** @see rhino.Scriptable#put(String, Scriptable, Object) */
    @Override
    public void put(String name, Scriptable start, Object value) {
        getDelegee().put(name, start, value);
    }

    /** @see rhino.SymbolScriptable#put(Symbol, Scriptable, Object) */
    @Override
    public void put(Symbol symbol, Scriptable start, Object value) {
        final Scriptable delegee = getDelegee();
        if (delegee instanceof SymbolScriptable) {
            ((SymbolScriptable) delegee).put(symbol, start, value);
        }
    }

    /** @see rhino.Scriptable#put(int, Scriptable, Object) */
    @Override
    public void put(int index, Scriptable start, Object value) {
        getDelegee().put(index, start, value);
    }

    /** @see rhino.Scriptable#delete(String) */
    @Override
    public void delete(String name) {
        getDelegee().delete(name);
    }

    @Override
    public void delete(Symbol key) {
        final Scriptable delegee = getDelegee();
        if (delegee instanceof SymbolScriptable) {
            ((SymbolScriptable) delegee).delete(key);
        }
    }

    /** @see rhino.Scriptable#delete(int) */
    @Override
    public void delete(int index) {
        getDelegee().delete(index);
    }

    /** @see rhino.Scriptable#getPrototype */
    @Override
    public Scriptable getPrototype() {
        return getDelegee().getPrototype();
    }

    /** @see rhino.Scriptable#setPrototype */
    @Override
    public void setPrototype(Scriptable prototype) {
        getDelegee().setPrototype(prototype);
    }

    /** @see rhino.Scriptable#getParentScope */
    @Override
    public Scriptable getParentScope() {
        return getDelegee().getParentScope();
    }

    /** @see rhino.Scriptable#setParentScope */
    @Override
    public void setParentScope(Scriptable parent) {
        getDelegee().setParentScope(parent);
    }

    /** @see rhino.Scriptable#getIds */
    @Override
    public Object[] getIds() {
        return getDelegee().getIds();
    }

    /**
     * Note that this method does not get forwarded to the delegee if the <code>hint</code>
     * parameter is null, <code>ScriptRuntime.ScriptableClass</code> or <code>
     * ScriptRuntime.FunctionClass</code>. Instead the object itself is returned.
     *
     * @param hint the type hint
     * @return the default value
     * @see rhino.Scriptable#getDefaultValue
     */
    @Override
    public Object getDefaultValue(Class<?> hint) {
        return (hint == null
                        || hint == ScriptRuntime.ScriptableClass
                        || hint == ScriptRuntime.FunctionClass)
                ? this
                : getDelegee().getDefaultValue(hint);
    }

    /** @see rhino.Scriptable#hasInstance */
    @Override
    public boolean hasInstance(Scriptable instance) {
        return getDelegee().hasInstance(instance);
    }

    /** @see rhino.Function#call */
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        return ((Function) getDelegee()).call(cx, scope, thisObj, args);
    }

    /**
     * Note that if the <code>delegee</code> is <code>null</code>, this method creates a new
     * instance of the Delegator itself rather than forwarding the call to the <code>delegee</code>
     * . This permits the use of Delegator prototypes.
     *
     * @param cx the current Context for this thread
     * @param scope an enclosing scope of the caller except when the function is called from a
     *     closure.
     * @param args the array of arguments
     * @return the allocated object
     * @see Function#construct(Context, Scriptable, Object[])
     */
    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        Scriptable myDelegee = getDelegee();
        if (myDelegee == null) {
            // this little trick allows us to declare prototype objects for Delegators
            Delegator n = newInstance();
            Scriptable delegee;
            if (args.length == 0) {
                delegee = cx.newObject(scope);
            } else {
                delegee = ScriptRuntime.toObject(cx, scope, args[0]);
            }
            n.setDelegee(delegee);
            return n;
        }
        return ((Function) myDelegee).construct(cx, scope, args);
    }
}
