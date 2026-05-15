CREATE SEQUENCE tasks_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE tasks (
    id BIGINT PRIMARY KEY DEFAULT nextval('tasks_seq'),
    job_id BIGINT NOT NULL,
    manifest TEXT,
    task_index INTEGER NOT NULL,
    task_key VARCHAR(255),
    attempt INTEGER NOT NULL,
    status VARCHAR(50),
    failure_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    kubernetes_job_name VARCHAR(255),
    locked_by VARCHAR(255),
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_tasks_job
        FOREIGN KEY (job_id)
        REFERENCES jobs(id)
);

CREATE UNIQUE INDEX uq_tasks_job_task_index_attempt
ON tasks (job_id, task_index, attempt);
