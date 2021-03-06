package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * A {@link DetailModelProvider} implementation that uses {@link UserPortalModelResolver} to retrieve UiCommon
 * {@link EntityModel}.
 *
 * @param <M>
 *            Parent model type.
 * @param <D>
 *            Detail model type.
 */
public class UserPortalDetailModelProvider<M extends ListWithDetailsModel, D extends EntityModel> extends TabModelProvider<D> implements DetailModelProvider<M, D> {

    private final ModelProvider<M> parentModelProvider;
    private final Class<D> detailModelClass;
    private final UserPortalModelResolver modelResolver;

    public UserPortalDetailModelProvider(BaseClientGinjector ginjector,
            ModelProvider<M> parentModelProvider, Class<D> detailModelClass,
            UserPortalModelResolver modelResolver) {
        super(ginjector);
        this.parentModelProvider = parentModelProvider;
        this.detailModelClass = detailModelClass;
        this.modelResolver = modelResolver;
    }

    @Override
    public D getModel() {
        return modelResolver.<D, M> getDetailModel(detailModelClass, parentModelProvider);
    }

    protected M getParentModel() {
        return parentModelProvider.getModel();
    }

    @Override
    public void onSubTabSelected() {
        getParentModel().setActiveDetailModel(getModel());
    }

}
