# SQL queries by test — SorteringsordningQueryBuilderTest

`<cols>` = `u.id, u.handlaggning_id, u.handlaggar_id_typ_id, u.handlaggar_id_varde, u.skapad, u.planerad_till, u.utford, u.status, u.regel, u.beskrivning, u.verksamhetslogik, u.roll, u.url, u.sub_topic, u.reply_topic, u.erbjudande_id, u.erbjudande_namn, u.reason, u.version, u.created_at, u.updated_at`

| Test | pageSql |
|---|---|
| `empty_entries_returns_fallback_query` | `SELECT * FROM public.uppgift ORDER BY created_at ASC` |
| `null_entries_returns_fallback_query` | `SELECT * FROM public.uppgift ORDER BY created_at ASC` |
| `catch_all_entry_generates_when_true` | `SELECT <cols> FROM (SELECT *, CASE WHEN TRUE THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `eq_constraint_generates_equals_predicate` | `SELECT <cols> FROM (SELECT *, CASE WHEN (regel = :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `eq_uppgift_id_maps_to_id_column` | `SELECT <cols> FROM (SELECT *, CASE WHEN (id = :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `contains_constraint [status]` | `SELECT <cols> FROM (SELECT *, CASE WHEN (status LIKE :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `contains_constraint [regel]` | `SELECT <cols> FROM (SELECT *, CASE WHEN (regel LIKE :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `contains_constraint [roll]` | `SELECT <cols> FROM (SELECT *, CASE WHEN (roll LIKE :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `contains_constraint [verksamhetslogik]` | `SELECT <cols> FROM (SELECT *, CASE WHEN (verksamhetslogik LIKE :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `contains_constraint [beskrivning]` | `SELECT <cols> FROM (SELECT *, CASE WHEN (beskrivning LIKE :p_0_0_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `between_constraint_generates_between_predicate` | `SELECT <cols> FROM (SELECT *, CASE WHEN (skapad BETWEEN :p_0_0_from AND :p_0_0_to) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `offset_to_now_negative_days_binds_correct_dates` | `SELECT <cols> FROM (SELECT *, CASE WHEN (planerad_till BETWEEN :p_0_0_from AND :p_0_0_to) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `sort_by_asc_generates_conditional_order_by` | `SELECT <cols> FROM (SELECT *, CASE WHEN TRUE THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, CASE WHEN u.sort_group = 0 THEN u.roll END ASC, u.created_at ASC` |
| `sort_by_desc_generates_desc_order_by` | `SELECT <cols> FROM (SELECT *, CASE WHEN TRUE THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, CASE WHEN u.sort_group = 0 THEN u.skapad END DESC, u.created_at ASC` |
| `order_by_always_ends_with_created_at_tiebreaker` | `SELECT <cols> FROM (SELECT *, CASE WHEN TRUE THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `multiple_entries_generate_unique_parameter_names` | `SELECT <cols> FROM (SELECT *, CASE WHEN (regel = :p_0_0_val) THEN 0 WHEN (roll = :p_1_0_val) THEN 1 ELSE 2 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `multiple_constraints_in_entry_generates_and_predicate` | `SELECT <cols> FROM (SELECT *, CASE WHEN (regel = :p_0_0_val AND roll = :p_0_1_val) THEN 0 ELSE 1 END AS sort_group FROM public.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `schema_is_qualified_in_both_queries [pageSql]` | `SELECT <cols> FROM (SELECT *, CASE WHEN TRUE THEN 0 ELSE 1 END AS sort_group FROM myschema.uppgift) AS u ORDER BY u.sort_group ASC, u.created_at ASC` |
| `schema_is_qualified_in_both_queries [countSql]` | `SELECT COUNT(*) FROM myschema.uppgift` |
| `parseOffset` tests | No SQL difference — only the bound value of `:p_0_0_from` changes per offset unit |
| Error tests (`unknown_unit`, `unknown_constraint_type`) | Throw before producing SQL |
