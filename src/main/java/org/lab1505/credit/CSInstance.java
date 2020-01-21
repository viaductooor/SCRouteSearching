package org.lab1505.credit;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 11:25
 *
 * <p>
 * A CSInstance is a valid assignment pair of a worker and a task.
 * </p>
 */
public class CSInstance {
    public CSTask task;
    public CSWorker worker;

    public CSInstance(CSWorker worker,CSTask task) {
        this.task = task;
        this.worker = worker;
    }
}
