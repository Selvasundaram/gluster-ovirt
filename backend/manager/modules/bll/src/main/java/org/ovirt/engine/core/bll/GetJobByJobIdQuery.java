package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.queries.GetJobByJobIdQueryParameters;

/**
 * Returns a Job by its job-ID
 */
public class GetJobByJobIdQuery<P extends GetJobByJobIdQueryParameters> extends QueriesCommandBase<P> {

    public GetJobByJobIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(JobRepositoryFactory.getJobRepository()
                .getJobWithSteps(getParameters().getJobId()));
    }
}
