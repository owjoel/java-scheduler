CREATE SEQUENCE jobs_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE jobs (
    id BIGINT PRIMARY KEY DEFAULT nextval('jobs_seq'),
    job_definition_id BIGINT NOT NULL,
    name VARCHAR(255),
    operator VARCHAR(255),
    job_type VARCHAR(50) NOT NULL,
    option_values JSONB,
    template TEXT NOT NULL,
    max_retries INTEGER NOT NULL,
    with_count INTEGER,
    with_keys JSONB,
    completion_strategy VARCHAR(50),
    status VARCHAR(50),
    failure_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    locked_by VARCHAR(255),
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_jobs_job_definition
        FOREIGN KEY (job_definition_id)
        REFERENCES job_definitions(id)
);

CREATE INDEX idx_jobs_reconcile_claim
ON jobs (status, locked_until, created_at)
WHERE status IN ('QUEUED', 'RUNNING', 'COMPLETING', 'FAILING');
