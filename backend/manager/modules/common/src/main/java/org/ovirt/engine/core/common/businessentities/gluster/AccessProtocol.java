package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum for the access protocols supported by Gluster Volumes.
 * @see GlusterVolumeEntity
 */
public enum AccessProtocol {
    GLUSTER,
    NFS;
    //CIFS;

    public int getValue() {
        return ordinal();
    }

    public static AccessProtocol forValue(int ordinal) {
        return values()[ordinal];
    }
}
