package org.ovirt.engine.core.bll;

import java.util.concurrent.atomic.AtomicInteger;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.SetTaskGroupStatusVisitor;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * EntityAsyncTask: Base class for all tasks regarding a specific entity (VM,
 * VmTemplate). The 'OnAfterEntityTaskEnded' method will be executed only if all
 * other tasks regarding the relevant entity have already ended.
 */
public class EntityAsyncTask extends SPMAsyncTask {
    protected static final Object _lockObject = new Object();

    private static java.util.HashMap<Object, EntityMultiAsyncTasks> _multiTasksByEntities =
            new java.util.HashMap<Object, EntityMultiAsyncTasks>();

    private static AtomicInteger _endActionsInProgress = new AtomicInteger(0);

    public static int getEndActionsInProgress() {
        return _endActionsInProgress.get();
    }

    private static EntityMultiAsyncTasks GetEntityMultiAsyncTasksByContainerId(Object containerID) {
        EntityMultiAsyncTasks entityInfo = null;
        synchronized (_lockObject) {
            if (_multiTasksByEntities.containsKey(containerID) && _multiTasksByEntities.get(containerID) != null) {
                entityInfo = _multiTasksByEntities.get(containerID);
            }
        }

        return entityInfo;
    }

    private EntityMultiAsyncTasks GetEntityMultiAsyncTasks() {
        return GetEntityMultiAsyncTasksByContainerId(getContainerId());
    }

    public EntityAsyncTask(AsyncTaskParameters parameters) {
        super(parameters);
        synchronized (_lockObject) {
            if (!_multiTasksByEntities.containsKey(getContainerId())) {
                log.infoFormat("EntityAsyncTask::Adding EntityMultiAsyncTasks object for entity '{0}'",
                        getContainerId());
                _multiTasksByEntities.put(getContainerId(), new EntityMultiAsyncTasks(getContainerId()));
            }
        }

        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        entityInfo.AttachTask(this);
    }

    @Override
    protected void ConcreteStartPollingTask() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        entityInfo.StartPollingTask(getTaskID());
    }

    @Override
    protected void OnTaskEndSuccess() {
        LogEndTaskSuccess();
        OnCurrentTaskEndSuccess();
        EndActionIfNecessary();
    }

    private void EndActionIfNecessary() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        if (entityInfo == null) {
            log.warnFormat(
                    "EntityAsyncTask::EndActionIfNecessary: No info is available for entity '{0}', current task ('{1}') was probably created while other tasks were in progress, clearing task.",
                    getContainerId(),
                    getTaskID());

            ClearAsyncTask();
        }

        else if (entityInfo.ShouldEndAction()) {
            log.infoFormat(
                    "EntityAsyncTask::EndActionIfNecessary: All tasks of entity '{0}' has ended -> executing 'EndAction'",
                    getContainerId());

            log.infoFormat(
                    "EntityAsyncTask::EndAction: Ending action for {0} tasks (entity ID: '{1}'): calling EndAction for action type '{2}'.",
                    entityInfo.getTasksCountCurrentActionType(),
                    entityInfo.getContainerId(),
                    entityInfo.getActionType());

            entityInfo.MarkAllWithAttemptingEndAction();
            _endActionsInProgress.incrementAndGet();
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    EndCommandAction();
                }
            });
        }
    }

    private void EndCommandAction() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        VdcReturnValueBase vdcReturnValue = null;
        ExecutionContext context = null;

        boolean success = true;
        for (EndedTaskInfo taskInfo : entityInfo.getEndedTasksInfo().getTasksInfo()) {
            success = taskInfo.getTaskStatus().getTaskEndedSuccessfully();
            if (!success) {
                break;
            }
        }

        // set all task to success/fail
        async_tasks dbAsyncTask = getParameters().getDbAsyncTask();
        for (EndedTaskInfo taskInfo : entityInfo.getEndedTasksInfo().getTasksInfo()) {
            dbAsyncTask.getaction_parameters()
                    .Accept(taskInfo, new SetTaskGroupStatusVisitor(success));
        }

        try {
            log.infoFormat("EntityAsyncTask::EndCommandAction [within thread]context: Attempting to EndAction '{0}'",
                    entityInfo.getActionType());

            try {
                /**
                 * Creates context for the job which monitors the action
                 */
                NGuid stepId = dbAsyncTask.getStepId();
                if (stepId != null) {
                    context = ExecutionHandler.createFinlalizingContext(stepId.getValue());
                }

                vdcReturnValue =
                        Backend.getInstance().endAction(entityInfo.getActionType(),
                                dbAsyncTask.getaction_parameters(),
                                new CommandContext(context));
            } catch (RuntimeException Ex) {
                String errorMessage =
                        String
                                .format("EntityAsyncTask::EndCommandAction [within thread]: EndAction for action type %1$s threw an exception",
                                        entityInfo.getActionType());

                log.error(errorMessage, Ex);
            }
        }

        catch (RuntimeException Ex2) {
            log.error(
                    "EntityAsyncTask::EndCommandAction [within thread]: An exception has been thrown (not related to 'EndAction' itself)",
                    Ex2);
        }

        finally {
            handleEndActionResult(entityInfo, vdcReturnValue, context);
            _endActionsInProgress.decrementAndGet();
        }
    }

    private static void handleEndActionResult(EntityMultiAsyncTasks entityInfo,
            VdcReturnValueBase vdcReturnValue,
            ExecutionContext context) {
        try {
            if (entityInfo != null) {
                log.infoFormat(
                        "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type '{0}' completed, handling the result.",
                        entityInfo.getActionType());

                if (vdcReturnValue == null || (!vdcReturnValue.getSucceeded() && vdcReturnValue.getEndActionTryAgain())) {
                    log.infoFormat(
                            "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type {0} hasn't succeeded, not clearing tasks, will attempt again next polling.",
                            entityInfo.getActionType());

                    entityInfo.Repoll();
                }

                else {
                    log.infoFormat(
                            "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type {0} {1}succeeded, clearing tasks.",
                            entityInfo.getActionType(),
                            (vdcReturnValue.getSucceeded() ? "" : "hasn't "));

                    /**
                     * Terminate the job by the return value of EndAction.
                     * The operation will end also the FINALIZING step.
                     */
                    if (context != null) {
                        ExecutionHandler.endTaskJob(context, vdcReturnValue.getSucceeded());
                    }

                    entityInfo.ClearTasks();

                    synchronized (_lockObject) {
                        if (entityInfo.getAllCleared()) {
                            log.infoFormat(
                                    "EntityAsyncTask::HandleEndActionResult [within thread]: Removing EntityMultiAsyncTasks object for entity '{0}'",
                                    entityInfo.getContainerId());
                            _multiTasksByEntities.remove(entityInfo.getContainerId());
                        } else {
                            entityInfo.resetActionTypeIfNecessary();
                            entityInfo.StartPollingNextTask();
                        }
                    }
                }
            }
        }

        catch (RuntimeException ex) {
            log.error("EntityAsyncTask::HandleEndActionResult [within thread]: an exception has been thrown", ex);
        }
    }

    @Override
    protected void OnTaskEndFailure() {
        LogEndTaskFailure();
        OnCurrentTaskEndFailure();
        EndActionIfNecessary();
    }

    @Override
    protected void OnTaskDoesNotExist() {
        LogTaskDoesntExist();
        OnCurrentTaskDoesNotExist();
        EndActionIfNecessary();
    }

    /**
     * Executed when the task ends successfully, regardless of whether there are
     * other running tasks on the same entity or not.
     */
    protected void OnCurrentTaskEndSuccess() {
    }

    /**
     * Executed when the task ended with a failure, regardless of whether there
     * are other running tasks on the same entity or not.
     */
    protected void OnCurrentTaskEndFailure() {
    }

    /**
     * Executed when we find out that the task doesn't exist, regardless of
     * whether there are other running tasks on the same entity or not.
     */
    protected void OnCurrentTaskDoesNotExist() {
    }

    private static Log log = LogFactory.getLog(EntityAsyncTask.class);
}
