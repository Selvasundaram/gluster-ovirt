package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.gin.BaseSystemModule;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemMessages;
import org.ovirt.engine.ui.userportal.system.ApplicationInit;

import com.google.inject.Singleton;

/**
 * GIN module containing UserPortal infrastructure and configuration bindings.
 */
public class SystemModule extends BaseSystemModule {

    @Override
    protected void configure() {
        bindInfrastructure();
        bindConfiguration();
    }

    void bindInfrastructure() {
        bindCommonInfrastructure();
        bind(ApplicationInit.class).asEagerSingleton();
    }

    void bindConfiguration() {
        bindPlaceConfiguration(ApplicationPlaces.loginPlace, ApplicationPlaces.basicMainTabPlace);
        bindResourceConfiguration(ApplicationConstants.class, ApplicationMessages.class,
                ApplicationResources.class, ApplicationTemplates.class);
        bind(ApplicationResourcesWithLookup.class).in(Singleton.class);
        bind(MainTabBasicListItemMessages.class).in(Singleton.class);
    }

}
