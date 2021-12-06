package com.upgrade.techchallenge.campsitereserve.utils;

import java.util.UUID;

public class TrackIdGenerator {

    public static String generateTrackId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
