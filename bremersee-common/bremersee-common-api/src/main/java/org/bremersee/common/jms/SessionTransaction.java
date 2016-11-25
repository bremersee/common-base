/**
 * 
 */
package org.bremersee.common.jms;

/**
 * @author Christian Bremer
 */
public enum SessionTransaction {
    NONE, LOCAL, XA, AUTO;

    public boolean isSessionTransacted() {
        return SessionTransaction.LOCAL.equals(this)
                || SessionTransaction.XA.equals(this);
    }

    public boolean isXA() {
        return SessionTransaction.XA.equals(this);
    }
}
