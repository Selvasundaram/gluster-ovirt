package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

public class CreateGlusterVolumeParameters extends VdsGroupParametersBase {
    private GlusterVolumeEntity volume;

    public CreateGlusterVolumeParameters(GlusterVolumeEntity volume) {
        setVolume(volume);
    }

    public GlusterVolumeEntity getVolume() {
        return volume;
    }

    public void setVolume(GlusterVolumeEntity volume) {
        this.volume = volume;
    }
}
