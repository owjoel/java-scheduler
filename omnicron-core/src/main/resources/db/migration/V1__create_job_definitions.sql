CREATE SEQUENCE job_definitions_seq START WITH 1 INCREMENT BY 50;

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
    completion_strategy VARCHAR(50),
    schedule_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_scheduled_at TIMESTAMP WITH TIME ZONE,
    next_scheduled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_job_definitions_schedule_due
ON job_definitions (schedule_enabled, next_scheduled_at)
WHERE schedule_enabled = true;
