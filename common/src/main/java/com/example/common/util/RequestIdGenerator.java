package com.example.common.util;

import java.util.UUID;

public class RequestIdGenerator {
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
