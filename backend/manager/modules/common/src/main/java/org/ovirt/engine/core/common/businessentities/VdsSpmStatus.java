package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsSpmStatus")
public enum VdsSpmStatus {
    None(0),
    Contending(1),
    SPM(2);

    private int intValue;
    private static java.util.HashMap<Integer, VdsSpmStatus> mappings = new HashMap<Integer, VdsSpmStatus>();

    static {
        for (VdsSpmStatus vdsSpmStatus : values()) {
            mappings.put(vdsSpmStatus.getValue(), vdsSpmStatus);
        }
    }

    private VdsSpmStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VdsSpmStatus forValue(int value) {
        return mappings.get(value);
    }
}
