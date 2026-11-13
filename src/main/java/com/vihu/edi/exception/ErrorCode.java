package com.vihu.edi.exception;

public enum ErrorCode {
    FILE_NOT_FOUND(2),
    FILE_READ_ERROR(3),
    NOT_X12(4),
    INVALID_ISA(5);

    private final int exitCode;

    ErrorCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
