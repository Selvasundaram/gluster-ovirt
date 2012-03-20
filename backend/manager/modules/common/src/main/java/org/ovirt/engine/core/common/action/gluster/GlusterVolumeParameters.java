package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeParameters extends VdcActionParametersBase {
    private Guid _volumeId;

    public GlusterVolumeParameters(Guid volumeId) {
        setVolumeId(volumeId);
    }

    public void setVolumeId(Guid volumeId) {
        this._volumeId = volumeId;
    }

    public Guid getVolumeId() {
        return _volumeId;
    }
}
