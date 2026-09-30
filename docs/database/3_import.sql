INSERT INTO sportclub.role (id, name) VALUES (default, 'admin');
INSERT INTO sportclub.role (id, name) VALUES (default, 'trainer');
INSERT INTO sportclub.role (id, name) VALUES (default, 'customer');

INSERT INTO sportclub.sport (id, name) VALUES (default, 'Tennis');
INSERT INTO sportclub.sport (id, name) VALUES (default, 'Jalgpall');
INSERT INTO sportclub.sport (id, name) VALUES (default, 'Korvpall');
INSERT INTO sportclub.sport (id, name) VALUES (default, 'Golf');

INSERT INTO sportclub.area (id, name) VALUES (default, 'Harjumaa');
INSERT INTO sportclub.area (id, name) VALUES (default, 'Läänemaa');
INSERT INTO sportclub.area (id, name) VALUES (default, 'Saaremaa');
INSERT INTO sportclub.area (id, name) VALUES (default, 'Hiiumaa');
INSERT INTO sportclub.area (id, name) VALUES (default, 'Pärnumaa');

INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 1, 'admin@admin.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'trainer@trainer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 3, 'customer@customer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 3, 'inactive@inactive.ee', '123', 'D');

-- Treenerid (role_id 2), vt docs/balsamic/notes/TrainingsView-markmed.md
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'jaana.kask@trainer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'jaanus.tubli@trainer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'aivar.lahe@trainer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'mihkel.maru@trainer.ee', '123', 'A');
INSERT INTO sportclub.user (id, role_id, email, password, status) VALUES (default, 2, 'reena.sibul@trainer.ee', '123', 'A');

INSERT INTO sportclub.profile (id, user_id, first_name, last_name, phone_number, area_id) VALUES (default, 5, 'Jaana', 'Kask', 56712345, 1);
INSERT INTO sportclub.profile (id, user_id, first_name, last_name, phone_number, area_id) VALUES (default, 6, 'Jaanus', 'Tubli', 51234567, 5);
INSERT INTO sportclub.profile (id, user_id, first_name, last_name, phone_number, area_id) VALUES (default, 7, 'Aivar', 'Lahe', 53344556, 1);
INSERT INTO sportclub.profile (id, user_id, first_name, last_name, phone_number, area_id) VALUES (default, 8, 'Mihkel', 'Maru', 55667788, 1);
INSERT INTO sportclub.profile (id, user_id, first_name, last_name, phone_number, area_id) VALUES (default, 9, 'Reena', 'Sibul', 58899001, 1);

INSERT INTO sportclub.facility (id, area_id, name, address, description) VALUES (default, 1, 'Laagri Tennisekeskus', 'Veskitammi 2, Laagri', NULL);
INSERT INTO sportclub.facility (id, area_id, name, address, description) VALUES (default, 5, 'Pärnu Tennise- ja Padelikeskus', 'Suur-Jõe 63a, Pärnu', NULL);
INSERT INTO sportclub.facility (id, area_id, name, address, description) VALUES (default, 1, 'Tallink Tennisekeskus', 'Paldiski mnt 80, Tallinn', NULL);
INSERT INTO sportclub.facility (id, area_id, name, address, description) VALUES (default, 1, 'Hiiu Staadion', 'Kadaka tee 62, Tallinn', NULL);
INSERT INTO sportclub.facility (id, area_id, name, address, description) VALUES (default, 1, 'Niitvälja Golf', 'Vana-Vahi tee 1, Niitvälja küla', NULL);

INSERT INTO sportclub.sportclub (id, name) VALUES (default, 'Beeta Tenniseklubi');
INSERT INTO sportclub.sportclub (id, name) VALUES (default, 'Alta Tenniseklubi');
INSERT INTO sportclub.sportclub (id, name) VALUES (default, 'Laeva Tenniseklubi');
INSERT INTO sportclub.sportclub (id, name) VALUES (default, 'FC Jalg');
INSERT INTO sportclub.sportclub (id, name) VALUES (default, 'Tore Golfklubi');

INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 1, 5);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 1, 6);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 2, 6);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 3, 6);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 3, 7);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 4, 8);
INSERT INTO sportclub.sportclub_trainer (id, sportclub_id, user_id) VALUES (default, 5, 9);

INSERT INTO sportclub.skill_level (id, sport_id, name) VALUES (default, 1, 'Algtase');
INSERT INTO sportclub.skill_level (id, sport_id, name) VALUES (default, 1, 'Kesktase');
INSERT INTO sportclub.skill_level (id, sport_id, name) VALUES (default, 1, 'Edasijõudnud');
INSERT INTO sportclub.skill_level (id, sport_id, name) VALUES (default, 2, 'Algtase');
INSERT INTO sportclub.skill_level (id, sport_id, name) VALUES (default, 4, 'Edasijõudnud');

INSERT INTO sportclub.training_group (id, sportclub_id, sport_id, user_id, name, description, skill_level_id) VALUES (default, 1, 1, 5, 'Tennis - Algtase', NULL, 1);
INSERT INTO sportclub.training_group (id, sportclub_id, sport_id, user_id, name, description, skill_level_id) VALUES (default, 2, 1, 6, 'Tennis - Kesktase', NULL, 2);
INSERT INTO sportclub.training_group (id, sportclub_id, sport_id, user_id, name, description, skill_level_id) VALUES (default, 3, 1, 7, 'Tennis - Edasijõudnud', NULL, 3);
INSERT INTO sportclub.training_group (id, sportclub_id, sport_id, user_id, name, description, skill_level_id) VALUES (default, 4, 2, 8, 'Jalgpall - Algtase', NULL, 4);
INSERT INTO sportclub.training_group (id, sportclub_id, sport_id, user_id, name, description, skill_level_id) VALUES (default, 5, 4, 9, 'Golf - Edasijõudnud', 'Treeninggrupp on mõeldud edasijõudnud oskustasemega mängijatele', 5);

INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 1, 1, 'Tennis - Algtase (Laagri)', 4, 'Kõvakattega siseväljak', NULL, NULL, '19:30:00', '21:00:00', 90, 'P');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 2, 2, 'Tennis - Kesktase (Pärnu)', 4, NULL, NULL, NULL, '18:30:00', '20:00:00', 90, 'L');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 3, 3, 'Tennis - Edasijõudnud (Tallink)', 4, NULL, NULL, NULL, '18:00:00', '19:30:00', 90, 'P');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 4, 4, 'Jalgpall - Algtase (Hiiu)', 22, NULL, NULL, NULL, '19:00:00', '20:30:00', 90, 'P');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 5, 5, 'Golf - Edasijõudnud (Niitvälja)', 12, NULL, NULL, NULL, '14:00:00', '15:30:00', 90, 'P');
-- Lisatreeningud (training id 6-10), et nimekirjas oleks paginationi testimiseks rohkem ridu
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 1, 1, 'Tennis - Algtase hommik (Laagri)', 4, 'Kõvakattega siseväljak', NULL, NULL, '09:00:00', '10:30:00', 90, 'L');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 2, 2, 'Tennis - Kesktase nädalavahetus (Pärnu)', 4, NULL, NULL, NULL, '11:00:00', '12:30:00', 90, 'P');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 3, 3, 'Tennis - Edasijõudnud õhtu (Tallink)', 4, NULL, NULL, NULL, '20:00:00', '21:30:00', 90, 'L');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 4, 4, 'Jalgpall - Algtase nädalavahetus (Hiiu)', 22, NULL, NULL, NULL, '12:00:00', '13:30:00', 90, 'L');
INSERT INTO sportclub.training (id, training_group_id, default_facility_id, name, maxsize, description, default_start_date, default_end_date, default_start_time, default_end_time, duration, weekdays) VALUES (default, 5, 5, 'Golf - Edasijõudnud hommik (Niitvälja)', 12, NULL, NULL, NULL, '10:00:00', '11:30:00', 90, 'L');

INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 1, 1, '2026-10-20', '19:30:00', 90, 'A', 2, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 1, 1, '2026-10-21', '19:30:00', 90, 'A', 3, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 1, 1, '2026-10-22', '19:30:00', 90, 'A', 3, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 2, 2, '2026-10-19', '18:30:00', 90, 'A', 4, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 3, 3, '2026-10-21', '18:00:00', 90, 'A', 2, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 4, 4, '2026-10-22', '19:00:00', 90, 'A', 16, 22, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 5, 5, '2026-09-21', '14:00:00', 90, 'A', 0, 12, '2026-09-01');
-- Lisatreeningute toimumisajad (training_date id 8-12)
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 6, 1, '2026-10-24', '09:00:00', 90, 'A', 1, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 7, 2, '2026-10-25', '11:00:00', 90, 'A', 3, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 8, 3, '2026-10-24', '20:00:00', 90, 'A', 4, 4, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 9, 4, '2026-10-24', '12:00:00', 90, 'A', 10, 22, '2026-09-01');
INSERT INTO sportclub.training_date (id, training_id, facility_id, start_date, start_time, duration, status, user_count, max_size, date_added) VALUES (default, 10, 5, '2026-10-24', '10:00:00', 90, 'A', 5, 12, '2026-09-01');

-- customer@customer.ee (user id 3) on Tennis/Jalgpall gruppide liige, kuid mitte Golfi grupi liige
INSERT INTO sportclub.user_training_group (id, user_id, training_group_id) VALUES (default, 3, 1);
INSERT INTO sportclub.user_training_group (id, user_id, training_group_id) VALUES (default, 3, 2);
INSERT INTO sportclub.user_training_group (id, user_id, training_group_id) VALUES (default, 1, 3);
INSERT INTO sportclub.user_training_group (id, user_id, training_group_id) VALUES (default, 2, 4);

-- customer@customer.ee registreeritud 20.09.2026 Tennis Algtase treeningule (training_date id 1)
INSERT INTO sportclub.user_training (id, user_id, training_date_id) VALUES (default, 3, 1);
INSERT INTO sportclub.user_training (id, user_id, training_date_id) VALUES (default, 2, 2);
INSERT INTO sportclub.user_training (id, user_id, training_date_id) VALUES (default, 1, 3);

