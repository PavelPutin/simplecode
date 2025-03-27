package ru.vsu.ppa.simplecode.service;

public enum JobeResponses {
    COMPILATION_ERROR(11),
    RUNTIME_ERROR(12),
    TIME_LIMIT_EXCEEDED(13),
    OK(15),
    MEMORY_LIMIT_EXCEEDED(17),
    ILLEGAL_SYSTEM_CALL(19),
    INTERNAL_ERROR(20),
    SERVER_OVERLOAD(21);

    private final int code;

    JobeResponses(int code) {
        this.code = code;
    }

    public static JobeResponses fromCode(int code) {
        for (JobeResponses jobeResponses : JobeResponses.values()) {
            if (jobeResponses.code == code) {
                return jobeResponses;
            }
        }
        throw new IllegalArgumentException("unknown code " + code);
    }
}
