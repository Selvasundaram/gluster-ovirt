package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Serializable;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class image_storage_domain_map_id implements Serializable {
    private static final long serialVersionUID = -5870880575903017188L;

    @Type(type = "guid")
    public Guid storageDomainId;

    @Type(type = "guid")
    public Guid imageId;

    public image_storage_domain_map_id() {
    }

    public image_storage_domain_map_id(Guid imageId, Guid storageDomainId) {
        this.imageId = imageId;
        this.storageDomainId = storageDomainId;
    }
}
