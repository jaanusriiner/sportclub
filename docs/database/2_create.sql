-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2026-09-21 06:04:49.277

-- tables
-- Table: area
CREATE TABLE area (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT area_pk PRIMARY KEY (id)
);

-- Table: facility
CREATE TABLE facility (
                          id serial  NOT NULL,
                          area_id int  NOT NULL,
                          name varchar(255)  NOT NULL,
                          address varchar(255)  NOT NULL,
                          description varchar(255)  NULL,
                          CONSTRAINT facility_pk PRIMARY KEY (id)
);
-- Table: facility_image
CREATE TABLE facility_image (
                          id serial  NOT NULL,
                          facility_id int  NOT NULL,
                          image_bytes bytea NOT NULL,
                          CONSTRAINT facilityimage_pk PRIMARY KEY (id)
);

-- Table: join_application
CREATE TABLE join_application (
                                  id serial  NOT NULL,
                                  user_id int  NOT NULL,
                                  training_group_id int  NOT NULL,
                                  status varchar(3)  NOT NULL,
                                  CONSTRAINT joinapplication_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile (
                         id serial  NOT NULL,
                         user_id int  NOT NULL,
                         first_name varchar(255)  NOT NULL,
                         last_name varchar(255)  NOT NULL,
                         phone_number int  NOT NULL,
                         area_id int  NOT NULL,
                         CONSTRAINT profile_pk PRIMARY KEY (id)
);

-- Table: review
CREATE TABLE review (
                        id serial  NOT NULL,
                        description varchar(500)  NOT NULL,
                        training_date_id int  NOT NULL,
                        CONSTRAINT review_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: skill_level
CREATE TABLE skill_level (
                             id serial  NOT NULL,
                             sport_id int  NOT NULL,
                             name varchar(30)  NOT NULL,
                             CONSTRAINT skilllevel_pk PRIMARY KEY (id)
);

-- Table: sport
CREATE TABLE sport (
                       id serial  NOT NULL,
                       name varchar(255)  NOT NULL,
                       CONSTRAINT sport_pk PRIMARY KEY (id)
);

-- Table: sport_facility
CREATE TABLE sport_facility (
                                id serial  NOT NULL,
                                sport_Id int  NOT NULL,
                                facility_id int  NOT NULL,
                                CONSTRAINT sport_facility_pk PRIMARY KEY (id)
);

-- Table: sportclub
CREATE TABLE sportclub (
                           id serial  NOT NULL,
                           name varchar(100)  NOT NULL,
                           CONSTRAINT sportclub_pk PRIMARY KEY (id)
);

-- Table: sportclub_trainer
CREATE TABLE sportclub_trainer (
                                   id serial  NOT NULL,
                                   sportclub_id int  NOT NULL,
                                   user_id int  NOT NULL,
                                   CONSTRAINT sportclub_trainer_pk PRIMARY KEY (id)
);

-- Table: trainer_application
CREATE TABLE trainer_application (
                                     id serial  NOT NULL,
                                     user_id int  NOT NULL,
                                     sportclub_id int  NOT NULL,
                                     status varchar(3)  NOT NULL,
                                     CONSTRAINT trainerapplication_pk PRIMARY KEY (id)
);

-- Table: training
CREATE TABLE training (
                          id serial  NOT NULL,
                          training_group_id int  NOT NULL,
                          default_facility_id int  NOT NULL,
                          name varchar(255)  NOT NULL,
                          maxsize int  NOT NULL,
                          description varchar(255)  NULL,
                          default_start_date date  NULL,
                          default_end_date date  NULL,
                          default_start_time time  NOT NULL,
                          default_end_time time  NOT NULL,
                          duration int  NOT NULL,
                          weekdays varchar(255)  NOT NULL,
                          CONSTRAINT training_pk PRIMARY KEY (id)
);

-- Table: training_date
CREATE TABLE training_date (
                               id serial  NOT NULL,
                               training_id int  NOT NULL,
                               facility_id int  NOT NULL,
                               start_date date  NOT NULL,
                               start_time time  NOT NULL,
                               duration int  NOT NULL,
                               status varchar(3)  NOT NULL,
                               user_count int  NOT NULL,
                               max_size int  NOT NULL,
                               date_added date  NOT NULL,
                               CONSTRAINT training_date_pk PRIMARY KEY (id)
);

-- Table: training_group
CREATE TABLE training_group (
                                id serial  NOT NULL,
                                sportclub_id int  NOT NULL,
                                sport_id int  NOT NULL,
                                user_id int  NOT NULL,
                                name varchar(100)  NOT NULL,
                                description varchar(255)  NULL,
                                skill_level_id int  NOT NULL,
                                CONSTRAINT traininggroup_pk PRIMARY KEY (id)
);

-- Table: user
CREATE TABLE "user" (
                        id serial  NOT NULL,
                        role_id int  NOT NULL,
                        email varchar(255)  NOT NULL,
                        password varchar(255)  NOT NULL,
                        status varchar(3)  NOT NULL,
                        CONSTRAINT CHECK_0 CHECK (( email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$' )) NOT DEFERRABLE INITIALLY IMMEDIATE,
                        CONSTRAINT user_pk PRIMARY KEY (id)
);

-- Table: user_sport
CREATE TABLE user_sport (
                            id serial  NOT NULL,
                            sport_id int  NOT NULL,
                            user_id int  NOT NULL,
                            CONSTRAINT user_sport_pk PRIMARY KEY (id)
);

-- Table: user_training
CREATE TABLE user_training (
                               id serial  NOT NULL,
                               user_id int  NOT NULL,
                               training_date_id int  NOT NULL,
                               CONSTRAINT user_training_pk PRIMARY KEY (id)
);

-- Table: user_training_group
CREATE TABLE user_training_group (
                                     id serial  NOT NULL,
                                     user_id int  NOT NULL,
                                     training_group_id int  NOT NULL,
                                     CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);

-- foreign keys
-- Reference: facility_area (table: facility)
ALTER TABLE facility ADD CONSTRAINT facility_area
    FOREIGN KEY (area_id)
        REFERENCES area (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: facility_image_facility (table: facility_image)
ALTER TABLE facility_image ADD CONSTRAINT facility_image_facility
    FOREIGN KEY (facility_id)
        REFERENCES facility (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: join_application_training_group (table: join_application)
ALTER TABLE join_application ADD CONSTRAINT join_application_training_group
    FOREIGN KEY (training_group_id)
        REFERENCES training_group (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: joinapplication_user (table: join_application)
ALTER TABLE join_application ADD CONSTRAINT joinapplication_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: profile_area (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_area
    FOREIGN KEY (area_id)
        REFERENCES area (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: profile_user (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: review_training_date (table: review)
ALTER TABLE review ADD CONSTRAINT review_training_date
    FOREIGN KEY (training_date_id)
        REFERENCES training_date (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: skilllevel_sport (table: skill_level)
ALTER TABLE skill_level ADD CONSTRAINT skilllevel_sport
    FOREIGN KEY (sport_id)
        REFERENCES sport (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: sport_facility_facility (table: sport_facility)
ALTER TABLE sport_facility ADD CONSTRAINT sport_facility_facility
    FOREIGN KEY (facility_id)
        REFERENCES facility (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: sport_facility_sport (table: sport_facility)
ALTER TABLE sport_facility ADD CONSTRAINT sport_facility_sport
    FOREIGN KEY (sport_Id)
        REFERENCES sport (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: sportclub_trainer_sportclub (table: sportclub_trainer)
ALTER TABLE sportclub_trainer ADD CONSTRAINT sportclub_trainer_sportclub
    FOREIGN KEY (sportclub_id)
        REFERENCES sportclub (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: sportclub_trainer_user (table: sportclub_trainer)
ALTER TABLE sportclub_trainer ADD CONSTRAINT sportclub_trainer_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: trainerapplication_sportclub (table: trainer_application)
ALTER TABLE trainer_application ADD CONSTRAINT trainerapplication_sportclub
    FOREIGN KEY (sportclub_id)
        REFERENCES sportclub (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: trainerapplication_user (table: trainer_application)
ALTER TABLE trainer_application ADD CONSTRAINT trainerapplication_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_date_facility (table: training_date)
ALTER TABLE training_date ADD CONSTRAINT training_date_facility
    FOREIGN KEY (facility_id)
        REFERENCES facility (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_date_training (table: training_date)
ALTER TABLE training_date ADD CONSTRAINT training_date_training
    FOREIGN KEY (training_id)
        REFERENCES training (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_facility (table: training)
ALTER TABLE training ADD CONSTRAINT training_facility
    FOREIGN KEY (default_facility_id)
        REFERENCES facility (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_group_skill_level (table: training_group)
ALTER TABLE training_group ADD CONSTRAINT training_group_skill_level
    FOREIGN KEY (skill_level_id)
        REFERENCES skill_level (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_training_group (table: training)
ALTER TABLE training ADD CONSTRAINT training_training_group
    FOREIGN KEY (training_group_id)
        REFERENCES training_group (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: traininggroup_sport (table: training_group)
ALTER TABLE training_group ADD CONSTRAINT traininggroup_sport
    FOREIGN KEY (sport_id)
        REFERENCES sport (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: traininggroup_sportclub (table: training_group)
ALTER TABLE training_group ADD CONSTRAINT traininggroup_sportclub
    FOREIGN KEY (sportclub_id)
        REFERENCES sportclub (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: traininggroup_user (table: training_group)
ALTER TABLE training_group ADD CONSTRAINT traininggroup_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_role (table: user)
ALTER TABLE "user" ADD CONSTRAINT user_role
    FOREIGN KEY (role_id)
        REFERENCES role (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_sport_sport (table: user_sport)
ALTER TABLE user_sport ADD CONSTRAINT user_sport_sport
    FOREIGN KEY (sport_id)
        REFERENCES sport (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_sport_user (table: user_sport)
ALTER TABLE user_sport ADD CONSTRAINT user_sport_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_training_group_training_group (table: user_training_group)
ALTER TABLE user_training_group ADD CONSTRAINT user_training_group_training_group
    FOREIGN KEY (training_group_id)
        REFERENCES training_group (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_training_training_date (table: user_training)
ALTER TABLE user_training ADD CONSTRAINT user_training_training_date
    FOREIGN KEY (training_date_id)
        REFERENCES training_date (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_training_user (table: user_training)
ALTER TABLE user_training ADD CONSTRAINT user_training_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_traininggroup_user (table: user_training_group)
ALTER TABLE user_training_group ADD CONSTRAINT user_traininggroup_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- views
-- View: v_training_date_overview
-- Üks rida iga training_date kirje kohta (grupeerimist ei toimu).
-- Kasutajapõhised väljad (userIsRegistered, userIsTrainingGroupMember) arvutatakse
-- service kihis requestUserId järgi, sest view ei saa parameetreid vastu võtta.
CREATE VIEW v_training_date_overview AS
SELECT td.id                                 AS training_date_id,
       tg.id                                 AS training_group_id,
       s.id                                  AS sport_id,
       s.name                                AS sport_name,
       f.id                                  AS facility_id,
       f.name                                AS facility_name,
       f.area_id                             AS area_id,
       tg.user_id                            AS trainer_id,
       p.first_name || ' ' || p.last_name    AS trainer_name,
       sc.id                                 AS sportclub_id,
       sc.name                               AS sportclub_name,
       sl.id                                 AS skill_level_id,
       sl.name                               AS skill_level_name,
       td.start_date                         AS training_date,
       td.start_time                         AS training_time,
       td.status                             AS status,
       td.user_count                         AS user_count,
       td.max_size                           AS max_size
FROM training_date td
         JOIN training t ON t.id = td.training_id
         JOIN training_group tg ON tg.id = t.training_group_id
         JOIN sport s ON s.id = tg.sport_id
         JOIN facility f ON f.id = td.facility_id
         JOIN sportclub sc ON sc.id = tg.sportclub_id
         JOIN skill_level sl ON sl.id = tg.skill_level_id
         LEFT JOIN profile p ON p.user_id = tg.user_id;

-- View: v_training_date_extended
-- Laiendatud versioon v_training_date_overview'st: üks rida iga AKTIIVSE (status = 'A')
-- training_date kirje kohta, lisaks grupi/piirkonna/asukoha/treeneri üksikasjad.
-- review_description on kõigi selle treeningu arvustuste kirjeldused kokku (eraldatud " | "),
-- et ridade arv ei kahekordistuks, kui ühel training_date'l on mitu arvustust.
CREATE VIEW v_training_date_extended AS
SELECT td.id                                 AS training_date_id,
       tg.id                                 AS training_group_id,
       tg.name                               AS training_group_name,
       tg.description                        AS training_group_description,
       s.id                                  AS sport_id,
       s.name                                AS sport_name,
       f.id                                  AS facility_id,
       f.name                                AS facility_name,
       f.address                             AS facility_address,
       f.description                         AS facility_description,
       f.area_id                             AS area_id,
       a.name                                AS area_name,
       tg.user_id                            AS trainer_id,
       p.first_name || ' ' || p.last_name    AS trainer_name,
       u.email                               AS trainer_email,
       sc.id                                 AS sportclub_id,
       sc.name                               AS sportclub_name,
       sl.id                                 AS skill_level_id,
       sl.name                               AS skill_level_name,
       td.start_date                         AS training_date,
       td.start_time                         AS training_time,
       td.duration                           AS training_date_duration,
       td.status                             AS status,
       td.user_count                         AS user_count,
       td.max_size                           AS training_date_max_size,
       t.weekdays                            AS training_weekdays,
       (SELECT string_agg(r.description, ' | ' ORDER BY r.id)
        FROM review r
        WHERE r.training_date_id = td.id)    AS review_description
FROM training_date td
         JOIN training t ON t.id = td.training_id
         JOIN training_group tg ON tg.id = t.training_group_id
         JOIN sport s ON s.id = tg.sport_id
         JOIN facility f ON f.id = td.facility_id
         JOIN area a ON a.id = f.area_id
         JOIN sportclub sc ON sc.id = tg.sportclub_id
         JOIN skill_level sl ON sl.id = tg.skill_level_id
         JOIN "user" u ON u.id = tg.user_id
         LEFT JOIN profile p ON p.user_id = tg.user_id
WHERE td.status = 'A';

-- End of file.

