# -- Table definitions

# --- !Ups
CREATE TABLE enquete (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    gender varchar(6) NOT NULL,
    message varchar(255) NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP() NOT NULL
);

CREATE TABLE task (
    id int PRIMARY KEY AUTO_INCREMENT,
    title varchar(32) NOT NULL,
    description varchar(255),
    is_done boolean default FALSE,
    created_at timestamp default CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE userinfo (
    name varchar(32) PRIMARY KEY NOT NULL,
    password varchar(64) NOT NULL
);

CREATE TABLE task_userinfo_map (
    task_id int NOT NULL,
    user_name varchar(32) NOT NULL
);

# --- !Downs
DROP TABLE enquete;

DROP TABLE task;

DROP TABLE userinfo;

DROP TABLE task_userinfo_map;