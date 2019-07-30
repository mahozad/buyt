package com.pleon.buyt.billing;

/**
 * Exception thrown when encountering an invalid Base64 input character.
 *
 * @author nelson
 */
class Base64DecoderException extends Exception {

    private static final long serialVersionUID = 1L;

    Base64DecoderException(String s) {
        super(s);
    }
}
