package org.ovirt.engine.core.common.glusteractions;

import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterParametersBase extends VdsGroupParametersBase {

    public GlusterParametersBase(Guid vdsGroupId) {
        super(vdsGroupId);
    }
}
