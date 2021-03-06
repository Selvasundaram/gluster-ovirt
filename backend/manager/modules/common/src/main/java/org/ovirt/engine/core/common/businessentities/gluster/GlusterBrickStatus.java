package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum for status of a Gluster Volume Brick. Represents the status of the brick process that runs on the server to
 * which the brick belongs.
 *
 * @see GlusterBrickEntity
 * @see GlusterVolumeEntity
 */
public enum GlusterBrickStatus {
    /**
     * Brick process up and running
     */
    ONLINE,
    /**
     * Brick process down
     */
    OFFLINE;

    public int getValue() {
        return ordinal();
    }

    public static GlusterBrickStatus forValue(int ordinal) {
        return values()[ordinal];
    }
}
