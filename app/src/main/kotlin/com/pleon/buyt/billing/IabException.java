package com.pleon.buyt.billing;

/**
 * Exception thrown when something went wrong with in-app billing.
 * An IabException has an associated IabResult (an error).
 * To get the IAB result that caused this exception to be thrown,
 * call {@link #getResult()}.
 */
public class IabException extends Exception {

    private IabResult result;

    IabException(int response, String message) {
        this(new IabResult(response, message));
    }

    IabException(int response, String message, Exception cause) {
        this(new IabResult(response, message), cause);
    }

    private IabException(IabResult r) {
        this(r, null);
    }

    private IabException(IabResult r, Exception cause) {
        super(r.getMessage(), cause);
        result = r;
    }

    /**
     * Returns the IAB result (error) that this exception signals.
     */
    public IabResult getResult() {
        return result;
    }
}
