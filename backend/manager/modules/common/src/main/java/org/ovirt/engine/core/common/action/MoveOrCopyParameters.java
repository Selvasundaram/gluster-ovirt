package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;

public class MoveOrCopyParameters extends StorageDomainParametersBase implements Serializable {
    private static final long serialVersionUID = 1051590893103934441L;

    private Map<Guid, Guid> imageToDestinationDomainMap;

    public MoveOrCopyParameters(Guid containerId, Guid storageDomainId) {
        super(storageDomainId);
        setContainerId(containerId);
        setTemplateMustExists(false);
        setForceOverride(false);
    }

    private Guid privateContainerId = Guid.Empty;

    public Guid getContainerId() {
        return privateContainerId;
    }

    public void setContainerId(Guid value) {
        privateContainerId = value;
    }

    private boolean privateCopyCollapse;

    public boolean getCopyCollapse() {
        return privateCopyCollapse;
    }

    public void setCopyCollapse(boolean value) {
        privateCopyCollapse = value;
    }

    private HashMap<String, DiskImageBase> privateDiskInfoList;

    public HashMap<String, DiskImageBase> getDiskInfoList() {
        return privateDiskInfoList;
    }

    public void setDiskInfoList(HashMap<String, DiskImageBase> value) {
        privateDiskInfoList = value;
    }

    public ValueObjectMap getDiskInfoValueObjectMap() {
        return new ValueObjectMap(privateDiskInfoList, false);
    }

    public void setDiskInfoValueObjectMap(ValueObjectMap value) {
        privateDiskInfoList = (value != null) ? new HashMap<String, DiskImageBase>(value.asMap()) : null;
    }

    private boolean privateTemplateMustExists;

    public boolean getTemplateMustExists() {
        return privateTemplateMustExists;
    }

    public void setTemplateMustExists(boolean value) {
        privateTemplateMustExists = value;
    }

    private boolean privateForceOverride;

    public boolean getForceOverride() {
        return privateForceOverride;
    }

    public void setForceOverride(boolean value) {
        privateForceOverride = value;
    }

    public MoveOrCopyParameters() {
    }

    public void setImageToDestinationDomainMap(Map<Guid, Guid> map) {
        imageToDestinationDomainMap = map;
    }

    public Map<Guid, Guid> getImageToDestinationDomainMap() {
        return imageToDestinationDomainMap;
    }
}
