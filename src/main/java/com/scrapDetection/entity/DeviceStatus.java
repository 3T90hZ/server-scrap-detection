package com.scrapDetection.entity;

public enum DeviceStatus {
    ACTIVE,    // device can authenticate and send detection events
    REVOKED,   // key compromised / device retired — auth always rejected
    DISABLED   // temporarily paused by the yard owner — auth rejected until re-enabled
}
