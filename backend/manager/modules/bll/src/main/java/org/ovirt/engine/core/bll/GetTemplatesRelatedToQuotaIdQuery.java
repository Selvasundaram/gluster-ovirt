package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTemplatesRelatedToQuotaIdQuery<P extends GetEntitiesRelatedToQuotaIdParameters>
        extends QueriesCommandBase<P> {
    public GetTemplatesRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> vmTemplates = DbFacade.getInstance().getVmTemplateDAO().getAllTemplatesRelatedToQuotaId(
                getParameters().getQuotaId());
        for (VmTemplate vmTemplate : vmTemplates) {
            VmTemplateHandler.UpdateDisksFromDb(vmTemplate);
        }
        getQueryReturnValue().setReturnValue(vmTemplates);
    }
}
