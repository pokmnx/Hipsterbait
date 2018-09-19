package com.hipsterbait.android.Resources.Exceptions;

public class RequiredValueMissing extends DBInconsistencyException {
    public RequiredValueMissing(String message) {
        super(message);
    }
}
