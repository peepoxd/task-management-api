-- Seed data for Task Management API
-- Password: password123 (BCrypt encoded)
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@demo.com', 'ADMIN'),
('john', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'john@demo.com', 'USER'),
('jane', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'jane@demo.com', 'USER');

INSERT INTO projects (name, description, owner_id, created_at, updated_at) VALUES
('Website Redesign', 'Redesign the company website with modern UI', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mobile App', 'Build cross-platform mobile application', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_at, updated_at) VALUES
('Design mockups', 'Create UI/UX mockups for homepage', 'TODO', 'HIGH', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Setup CI/CD', 'Configure GitHub Actions pipeline', 'IN_PROGRESS', 'MEDIUM', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Write API docs', 'Document REST API endpoints', 'TODO', 'LOW', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Login screen', 'Implement login screen with social auth', 'TODO', 'HIGH', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Push notifications', 'Add push notification support', 'TODO', 'MEDIUM', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
