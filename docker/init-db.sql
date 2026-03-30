CREATE EXTENSION IF NOT EXISTS vector;

-- Seed sorting rules (runs only on first container creation)
INSERT INTO sorting_rules (id, rulecode, description, postalcodepattern, assignedbelt, priority, active)
SELECT
  nextval('sorting_rules_seq'), rulecode, description, postalcodepattern, assignedbelt, priority, active
FROM (VALUES
  ('RULE-001', 'North region parcels under 5kg',       '1%', 'Belt-A', 10, true),
  ('RULE-002', 'South region parcels',                  '2%', 'Belt-B', 10, true),
  ('RULE-003', 'East region parcels',                   '3%', 'Belt-C', 10, true),
  ('RULE-004', 'West region parcels',                   '4%', 'Belt-D', 10, true),
  ('RULE-005', 'Fragile/oversized parcels over 20kg',  '5%', 'Belt-E', 20, true),
  ('RULE-006', 'Express delivery parcels',              '6%', 'Belt-F', 30, true)
) AS v(rulecode, description, postalcodepattern, assignedbelt, priority, active)
WHERE NOT EXISTS (SELECT 1 FROM sorting_rules LIMIT 1);
