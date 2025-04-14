CREATE TABLE IF NOT EXISTS global_settings
(
    settings_key   VARCHAR(255) PRIMARY KEY,
    settings_value VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS clients
(
    id       UUID PRIMARY KEY,
    login    VARCHAR(255) NOT NULL,
    age      INT          NOT NULL,
    location VARCHAR(255) NOT NULL,
    gender   VARCHAR(6)   NOT NULL
);

CREATE TABLE IF NOT EXISTS advertisers
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS advertisements
(
    id                  UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    advertiser_id       UUID           NOT NULL,
    title               VARCHAR(255)   NOT NULL,
    text                TEXT           NOT NULL,
    impressions_limit   INTEGER        NOT NULL,
    clicks_limit        INTEGER        NOT NULL,
    current_impressions INTEGER        NOT NULL DEFAULT 0,
    current_clicks      INTEGER        NOT NULL DEFAULT 0,
    cost_per_impression DECIMAL(10, 2) NOT NULL,
    cost_per_click      DECIMAL(10, 2) NOT NULL,
    targeting_gender    VARCHAR(6),
    targeting_age_from  INTEGER,
    targeting_age_to    INTEGER,
    targeting_location  VARCHAR(255),
    start_date          INTEGER        NOT NULL,
    end_date            INTEGER        NOT NULL,
    image_url           TEXT                    DEFAULT NULL,
    moderated_and_valid BOOLEAN                 DEFAULT TRUE
);

-- ALTER TABLE advertisements
--     ADD CONSTRAINT IF NOT EXISTS fk_advertisements_advertiser
--     FOREIGN KEY (advertiser_id)
--     REFERENCES advertisers(id);

CREATE TABLE IF NOT EXISTS ml_scores
(
    id            VARCHAR PRIMARY KEY,
    client_id     UUID    NOT NULL,
    advertiser_id UUID    NOT NULL,
    score         INTEGER NOT NULL
);

-- ALTER TABLE ml_scores
--     ADD CONSTRAINT IF NOT EXISTS fk_ml_scores_client
--     FOREIGN KEY (client_id)
--     REFERENCES clients(id);
--
-- ALTER TABLE ml_scores
--     ADD CONSTRAINT IF NOT EXISTS fk_ml_scores_advertiser
--     FOREIGN KEY (advertiser_id)
--     REFERENCES advertisers(id);

CREATE TABLE IF NOT EXISTS client_actions
(
    id               VARCHAR PRIMARY KEY,
    client_id        UUID           NOT NULL,
    advertisement_id UUID           NOT NULL,
    advertiser_id    UUID           NOT NULL,
    action_type      VARCHAR(255)   NOT NULL,
    cost             DECIMAL(10, 2) NOT NULL,
    created_at       INTEGER        NOT NULL
);

-- ALTER TABLE client_actions
--     ADD CONSTRAINT fk_client_actions_client
--     FOREIGN KEY (client_id)
--     REFERENCES clients(id);
--
-- ALTER TABLE client_actions
--     ADD CONSTRAINT fk_client_actions_advertisement
--     FOREIGN KEY (advertisement_id)
--     REFERENCES advertisements(id);
--
-- ALTER TABLE client_actions
--     ADD CONSTRAINT fk_client_actions_advertiser
--     FOREIGN KEY (advertiser_id)
--     REFERENCES advertisers(id);


-- Insert initial settings
INSERT INTO global_settings (settings_key, settings_value)
VALUES ('application_date', '0')
ON CONFLICT DO NOTHING;

INSERT INTO global_settings (settings_key, settings_value)
VALUES ('moderation_enabled', 'false')
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_advertisements_advertiser ON advertisements (advertiser_id);
CREATE INDEX IF NOT EXISTS idx_ml_scores_client ON ml_scores (client_id);
CREATE INDEX IF NOT EXISTS idx_ml_scores_advertiser ON ml_scores (advertiser_id);
CREATE INDEX IF NOT EXISTS idx_client_actions_client ON client_actions (client_id);
CREATE INDEX IF NOT EXISTS idx_client_actions_advertisement ON client_actions (advertisement_id);
CREATE INDEX IF NOT EXISTS idx_client_actions_advertiser ON client_actions (advertiser_id);
CREATE INDEX IF NOT EXISTS idx_client_actions_created_at ON client_actions (created_at);
