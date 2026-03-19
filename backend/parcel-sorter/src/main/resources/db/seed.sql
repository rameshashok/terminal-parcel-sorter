INSERT INTO sorting_rules (rule_code, description, postal_code_pattern, assigned_belt, priority, active)
VALUES
  ('RULE-001', 'North region parcels under 5kg', '1%', 'Belt-A', 10, true),
  ('RULE-002', 'South region parcels', '2%', 'Belt-B', 10, true),
  ('RULE-003', 'East region parcels', '3%', 'Belt-C', 10, true),
  ('RULE-004', 'West region parcels', '4%', 'Belt-D', 10, true),
  ('RULE-005', 'Fragile/oversized parcels over 20kg', '5%', 'Belt-E', 20, true),
  ('RULE-006', 'Express delivery parcels', '6%', 'Belt-F', 30, true)
ON CONFLICT DO NOTHING;
