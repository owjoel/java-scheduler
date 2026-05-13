CREATE SEQUENCE job_definitions_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE jobs_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE job_definitions (
    id BIGINT PRIMARY KEY DEFAULT nextval('job_definitions_seq'),
    name VARCHAR(255),
    description TEXT,
    author VARCHAR(255),
    template TEXT,
    options JSONB,
    cron_expression VARCHAR(255),
    max_retries INTEGER,
    with_count INTEGER,
    with_keys JSONB,
    schedule_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_scheduled_at TIMESTAMP WITH TIME ZONE,
    next_scheduled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE jobs (
    id BIGINT PRIMARY KEY DEFAULT nextval('jobs_seq'),
    job_definition_id BIGINT NOT NULL,
    name VARCHAR(255),
    operator VARCHAR(255),
    job_type VARCHAR(50) NOT NULL,
    option_values JSONB,
    template TEXT NOT NULL,
    with_count INTEGER,
    with_keys JSONB,
    status VARCHAR(50),
    failure_message TEXT,
    start TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_jobs_job_definition
        FOREIGN KEY (job_definition_id)
        REFERENCES job_definitions(id)
);
