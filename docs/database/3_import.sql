INSERT INTO sportclub.role (id, name) VALUES (default, 'admin');
INSERT INTO sportclub.role (id, name) VALUES (default, 'trainer');
INSERT INTO sportclub.role (id, name) VALUES (default, 'customer');

INSERT INTO sportclub.sport (id, name) VALUES (default, 'Tennis');
INSERT INTO sportclub.sport (id, name) VALUES (default, 'Football');
INSERT INTO sportclub.sport (id, name) VALUES (default, 'Basketball');
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

