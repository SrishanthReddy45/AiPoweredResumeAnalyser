CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    extracted_text TEXT,
    parsed_resume_data TEXT,
    storage_key VARCHAR(255) NOT NULL,
    processing_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resume_analyses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_description TEXT,
    overall_score INTEGER NOT NULL,
    ats_score INTEGER NOT NULL,
    matched_keywords TEXT,
    missing_keywords TEXT,
    matched_skills TEXT,
    missing_skills TEXT,
    strengths TEXT,
    weaknesses TEXT,
    recommendations TEXT,
    section_feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_analysis_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS generated_resumes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_resume_id BIGINT NOT NULL,
    analysis_id BIGINT,
    filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_generated_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_generated_resume FOREIGN KEY (source_resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    CONSTRAINT fk_generated_analysis FOREIGN KEY (analysis_id) REFERENCES resume_analyses(id) ON DELETE SET NULL
);
