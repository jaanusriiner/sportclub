-- Kustutab minu_projekt schema (mis põhimõtteliselt kustutab kõik tabelid)
DROP SCHEMA IF EXISTS sportclub CASCADE;
-- Loob uue minu_projekt schema vajalikud õigused
CREATE SCHEMA sportclub
-- taastab vajalikud andmebaasi õigused
    GRANT ALL ON SCHEMA sportclub TO postgres;
GRANT ALL ON SCHEMA sportclub TO PUBLIC;