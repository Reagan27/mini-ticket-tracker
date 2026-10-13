CREATE TABLE tickets (
    id          UUID         PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL,
    priority    VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_tickets_status   CHECK (status   IN ('OPEN', 'IN_PROGRESS', 'CLOSED')),
    CONSTRAINT ck_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_tickets_status_created_at ON tickets (status, created_at DESC);
