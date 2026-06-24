package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintBetween;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintContains;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintOffsetToNow;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldDate;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SorteringsordningQueryBuilder}.
 * Plain JUnit — no Quarkus container needed.
 */
public class SorteringsordningQueryBuilderTest
{
   SorteringsordningQueryBuilder builder;

   @BeforeEach
   void setUp()
   {
      builder = new SorteringsordningQueryBuilder();
      builder.schema = "public";
   }

   /**
    * With no entries there is nothing to sort by, so the builder falls back to
    * {@code created_at ASC} — the insertion-order stable tiebreaker that is always present.
    * Also verifies that {@code countSql} and {@code params} are in their empty/default state.
    */
   @Test
   @DisplayName("Tom entry-lista ger fallback ORDER BY created_at ASC")
   public void empty_entries_returns_fallback_query()
   {
      var entity = entity(List.of());
      var built = builder.build(entity);

      assertEquals("SELECT * FROM public.uppgift ORDER BY created_at ASC", built.pageSql());
      assertEquals("SELECT COUNT(*) FROM public.uppgift", built.countSql());
      assertTrue(built.params().isEmpty());
   }

   /**
    * {@code null} entries and an empty list both represent "no sort specification".
    * Both should produce the same {@code created_at ASC} fallback, not a NullPointerException.
    */
   @Test
   @DisplayName("Null entries ger fallback-query")
   public void null_entries_returns_fallback_query()
   {
      var entity = new SorteringsordningEntity(null, null, null, null);
      var built = builder.build(entity);

      assertTrue(built.pageSql().contains("ORDER BY created_at ASC"));
   }

   /**
    * A catch-all entry has no constraints. The builder detects this and emits
    * {@code WHEN TRUE THEN <groupIdx>} so every row matches that group — the SQL
    * equivalent of an unconditional else-branch.
    * No parameters are bound because there are no constraint values to parameterize.
    */
   @Test
   @DisplayName("Catch-all entry (inga constraints) ger WHEN TRUE THEN 0")
   public void catch_all_entry_generates_when_true()
   {
      var entity = entity(List.of(catchAll()));
      var built = builder.build(entity);

      assertTrue(built.pageSql().contains("WHEN TRUE THEN 0"));
      assertTrue(built.params().isEmpty());
   }

   /**
    * Verifies the basic {@code ConstraintEq} path: the field resolves to its column name
    * and the value is bound as a named parameter rather than inlined, preventing SQL injection.
    * <p>
    * The parameter key {@code p_0_0_val} follows the naming scheme
    * {@code p_{entryIdx}_{constraintIdx}_{suffix}} — entry 0, constraint 0.
    */
   @Test
   @DisplayName("ConstraintEq genererar column = :param-predikat")
   public void eq_constraint_generates_equals_predicate()
   {
      var entry = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.REGEL, "test"));
      var built = builder.build(entity(List.of(entry)));

      assertTrue(built.pageSql().contains("regel = :p_0_0_val"));
      assertEquals("test", built.params().get("p_0_0_val"));
   }

   /**
    * {@code UPPGIFT_ID} is the one {@link SorteringsordningFieldEq} value whose database column
    * name differs from its field name — the column is {@code id}, not {@code uppgift_id}.
    * This test pins that mapping so a map refactor cannot silently break it.
    */
   @Test
   @DisplayName("ConstraintEq med UPPGIFT_ID mappar till kolumnen 'id'")
   public void eq_uppgift_id_maps_to_id_column()
   {
      var entry = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.UPPGIFT_ID, "some-uuid"));
      var built = builder.build(entity(List.of(entry)));

      assertTrue(built.pageSql().contains("id = :p_0_0_val"));
   }

   /**
    * Verifies that every {@link SorteringsordningFieldString} value is present in
    * {@code STRING_FIELD_TO_COLUMN} and maps to the correct column name.
    * <p>
    * Runs once per enum constant. The parameter name {@code p_0_0_val} is predictable:
    * {@code p_{entryIdx}_{constraintIdx}_{suffix}} — here a single entry (0) with a
    * single constraint (0).
    */
   @ParameterizedTest(name = "ConstraintContains({0}) genererar LIKE-predikat mot rätt kolumn")
   @EnumSource(SorteringsordningFieldString.class)
   public void contains_constraint_generates_like_predicate_for_all_string_fields(SorteringsordningFieldString field)
   {
      var constraint = new ConstraintContains();
      constraint.setField(field);
      constraint.setValue("test");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertTrue(built.pageSql().contains(field.toString() + " LIKE :p_0_0_val"));
      assertEquals("%test%", built.params().get("p_0_0_val"));
   }

   /**
    * Verifies that {@code ConstraintBetween} produces a {@code BETWEEN} predicate and that
    * both bound values are the exact {@link LocalDate} objects passed in — not strings or
    * other converted types — so JPA can bind them directly to date columns.
    */
   @Test
   @DisplayName("ConstraintBetween genererar BETWEEN-predikat med from/to-parametrar")
   public void between_constraint_generates_between_predicate()
   {
      var from = LocalDate.of(2026, 1, 1);
      var to = LocalDate.of(2026, 12, 31);

      var constraint = new ConstraintBetween();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setFrom(from);
      constraint.setTo(to);

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertTrue(built.pageSql().contains("skapad BETWEEN :p_0_0_from AND :p_0_0_to"));
      assertEquals(from, built.params().get("p_0_0_from"));
      assertEquals(to, built.params().get("p_0_0_to"));
   }

   /**
    * {@code ConstraintOffsetToNow} computes a sliding window relative to today:
    * {@code from = today + offset}, {@code to = today}. A negative offset means the window
    * reaches into the past. This test uses {@code -7d} and verifies that {@code from} is
    * bound to {@code today - 7 days} and {@code to} to today, computed at build time.
    */
   @Test
   @DisplayName("ConstraintOffsetToNow -7d binder from=nu-7d och to=nu")
   public void offset_to_now_negative_days_binds_correct_dates()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.PLANERAD_TILL);
      constraint.setOffset("-7d");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertTrue(built.pageSql().contains("planerad_till BETWEEN :p_0_0_from AND :p_0_0_to"));
      assertEquals(LocalDate.now().minusDays(7), built.params().get("p_0_0_from"));
      assertEquals(LocalDate.now(), built.params().get("p_0_0_to"));
   }

   /**
    * The {@code sort_by} for one entry must not affect ordering of rows in other groups.
    * The builder achieves this by emitting {@code CASE WHEN u.sort_group = N THEN u.col END},
    * which evaluates to {@code NULL} for rows belonging to any other group — and {@code NULL}
    * values sort last and do not disturb the ordering of those other groups.
    */
   @Test
   @DisplayName("sort_by ASC genererar CASE WHEN sort_group = 0 THEN col END ASC")
   public void sort_by_asc_generates_conditional_order_by()
   {
      var entry = new SorteringsordningEntry();
      entry.setSortBy(sortBy(SorteringsordningField.ROLL, SortBy.DirectionEnum.ASC));

      var built = builder.build(entity(List.of(entry)));

      assertTrue(built.pageSql().contains("CASE WHEN u.sort_group = 0 THEN u.roll END ASC"));
   }

   /**
    * Same as {@link #sort_by_asc_generates_conditional_order_by} but for {@code DESC}.
    * Verifies that the direction token is placed after {@code END}, not inside the {@code CASE}.
    */
   @Test
   @DisplayName("sort_by DESC genererar CASE WHEN sort_group = 0 THEN col END DESC")
   public void sort_by_desc_generates_desc_order_by()
   {
      var entry = new SorteringsordningEntry();
      entry.setSortBy(sortBy(SorteringsordningField.SKAPAD, SortBy.DirectionEnum.DESC));

      var built = builder.build(entity(List.of(entry)));

      assertTrue(built.pageSql().contains("CASE WHEN u.sort_group = 0 THEN u.skapad END DESC"));
   }

   /**
    * {@code created_at} is appended as the final ORDER BY term for every query, regardless
    * of how many entries or sort_by fields are configured. It acts as a stable tiebreaker
    * so that rows with identical sort keys are returned in a consistent, reproducible order
    * across paginated requests.
    */
   @Test
   @DisplayName("ORDER BY avslutas alltid med u.created_at ASC som tiebreaker")
   public void order_by_always_ends_with_created_at_tiebreaker()
   {
      var built = builder.build(entity(List.of(catchAll())));

      assertTrue(built.pageSql().endsWith("u.created_at ASC"));
   }

   /**
    * Named parameters in the shared SQL must be unique across all entries and constraints.
    * The builder scopes each name by entry index and constraint index, so two entries with
    * the same constraint type produce distinct parameter keys ({@code p_0_0_val} and
    * {@code p_1_0_val}) that can coexist in the same query without collision.
    */
   @Test
   @DisplayName("Flera entries ger unika parameternamn (p_0_ respektive p_1_)")
   public void multiple_entries_generate_unique_parameter_names()
   {
      var entry1 = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.REGEL, "a"));
      var entry2 = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.ROLL, "b"));

      var built = builder.build(entity(List.of(entry1, entry2)));

      assertEquals("a", built.params().get("p_0_0_val"));
      assertEquals("b", built.params().get("p_1_0_val"));
   }

   /**
    * Multiple constraints within one entry must all be satisfied for a row to match that group
    * (AND semantics). The builder joins them with {@code AND} inside a single parenthesised
    * expression so the group predicate reads {@code (c1 AND c2 AND ...)}.
    */
   @Test
   @DisplayName("Flera constraints i samma entry ger AND-predikat")
   public void multiple_constraints_in_entry_generates_and_predicate()
   {
      var entry = entryWithConstraints(
            eqConstraint(SorteringsordningFieldEq.REGEL, "a"),
            eqConstraint(SorteringsordningFieldEq.ROLL, "b"));

      var built = builder.build(entity(List.of(entry)));

      assertTrue(built.pageSql().contains("regel = :p_0_0_val"));
      assertTrue(built.pageSql().contains("roll = :p_0_1_val"));
      assertTrue(built.pageSql().contains(" AND "));
   }

   /**
    * The schema prefix must appear in both {@code pageSql} and {@code countSql} so that
    * both queries target the correct database schema. Missing the prefix in either query
    * would cause it to resolve against the session's default schema, which may differ in
    * production.
    */
   @Test
   @DisplayName("Schema-prefix används i både pageSql och countSql")
   public void schema_is_qualified_in_both_queries()
   {
      builder.schema = "myschema";
      var built = builder.build(entity(List.of(catchAll())));

      assertTrue(built.pageSql().contains("FROM myschema.uppgift"));
      assertTrue(built.countSql().contains("FROM myschema.uppgift"));
   }

   /**
    * {@code -2w} should resolve to exactly two weeks before today. The bound {@code from}
    * parameter is the only assertion — {@code to} is always today and covered by
    * {@link #offset_to_now_negative_days_binds_correct_dates}.
    */
   @Test
   @DisplayName("parseOffset: -2w ger 2 veckor bakåt")
   public void parse_offset_negative_weeks()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("-2w");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertEquals(LocalDate.now().minusWeeks(2), built.params().get("p_0_0_from"));
   }

   /**
    * {@code -1m} should resolve to one calendar month before today, handled by
    * {@link java.time.Period#ofMonths} which respects varying month lengths.
    */
   @Test
   @DisplayName("parseOffset: -1m ger 1 månad bakåt")
   public void parse_offset_negative_months()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("-1m");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertEquals(LocalDate.now().minusMonths(1), built.params().get("p_0_0_from"));
   }

   /**
    * {@code -1y} should resolve to one calendar year before today, handled by
    * {@link java.time.Period#ofYears} which accounts for leap years.
    */
   @Test
   @DisplayName("parseOffset: -1y ger 1 år bakåt")
   public void parse_offset_negative_years()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("-1y");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertEquals(LocalDate.now().minusYears(1), built.params().get("p_0_0_from"));
   }

   /**
    * A positive offset means the window starts in the future ({@code from = today + 7d}).
    * This is an unusual but valid configuration, e.g. to match tasks planned for the coming week.
    */
   @Test
   @DisplayName("parseOffset: positiv offset (7d framåt) hanteras")
   public void parse_offset_positive_days()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("7d");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertEquals(LocalDate.now().plusDays(7), built.params().get("p_0_0_from"));
   }

   /**
    * A {@code null} offset is treated as {@link java.time.Period#ZERO}, making {@code from}
    * equal to today. This avoids a NullPointerException on the caller's behalf and produces
    * a degenerate window [{@code today, today}] that only matches tasks dated exactly today.
    */
   @Test
   @DisplayName("parseOffset: null offset ger Period.ZERO — ingen exception")
   public void parse_offset_null_returns_zero_period()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset(null);

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      // Period.ZERO → LocalDate.now().plus(ZERO) == LocalDate.now()
      assertEquals(LocalDate.now(), built.params().get("p_0_0_from"));
   }

   /**
    * A blank (whitespace-only) offset is treated the same as {@code null} — {@link java.time.Period#ZERO}.
    * Both cases guard against malformed input arriving from a persisted sorteringsordning spec.
    */
   @Test
   @DisplayName("parseOffset: blank offset ger Period.ZERO — ingen exception")
   public void parse_offset_blank_returns_zero_period()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("   ");

      var built = builder.build(entity(List.of(entryWithConstraints(constraint))));

      assertEquals(LocalDate.now(), built.params().get("p_0_0_from"));
   }

   /**
    * An unrecognised unit character (here {@code x}) must fail fast with an
    * {@link IllegalArgumentException} rather than silently producing an incorrect period.
    */
   @Test
   @DisplayName("parseOffset: okänd tidsenhet kastar IllegalArgumentException")
   public void parse_offset_unknown_unit_throws_illegal_argument()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOffset("7x");

      assertThrows(IllegalArgumentException.class,
            () -> builder.build(entity(List.of(entryWithConstraints(constraint)))));
   }

   /**
    * The switch in {@code buildPredicate} has a {@code default} branch that throws
    * {@link IllegalArgumentException} for any constraint type not handled by the known cases.
    * A plain {@link String} is passed here to trigger that branch without needing a new
    * constraint subclass.
    */
   @Test
   @DisplayName("buildPredicate: okänd constraint-typ kastar IllegalArgumentException")
   public void build_predicate_unknown_constraint_type_throws_illegal_argument()
   {
      // Pass a plain String as a constraint to trigger the default branch in the switch
      var entry = entryWithConstraints("not-a-constraint");

      assertThrows(IllegalArgumentException.class,
            () -> builder.build(entity(List.of(entry))));
   }

   /**
    * {@code buildHandlaggareListQuery} must filter by handläggare identity so only that
    * handläggare's tasks are included in the result.
    */
   @Test
   @DisplayName("buildHandlaggareListQuery: WHERE-klausul filtrerar på handlaggar_id_typ_id och handlaggar_id_varde")
   public void handlaggare_list_query_contains_handlaggare_filter()
   {
      var built = builder.buildHandlaggareListQuery(entity(List.of()), "typ-1", "varde-1");

      assertTrue(built.pageSql().contains("handlaggar_id_typ_id = :hl_typ_id"));
      assertTrue(built.pageSql().contains("handlaggar_id_varde = :hl_varde"));
      assertEquals("typ-1", built.params().get("hl_typ_id"));
      assertEquals("varde-1", built.params().get("hl_varde"));
   }

   /**
    * Without sorteringsordning entries the fallback ORDER BY is {@code created_at ASC}.
    */
   @Test
   @DisplayName("buildHandlaggareListQuery: tom entry-lista ger fallback ORDER BY created_at ASC")
   public void handlaggare_list_query_empty_entries_falls_back_to_created_at()
   {
      var built = builder.buildHandlaggareListQuery(entity(List.of()), "t", "v");

      assertTrue(built.pageSql().contains("ORDER BY created_at ASC"));
   }

   /**
    * With a sorteringsordning the query wraps the table in a subquery and orders by sort_group,
    * matching the same structure as the page query.
    */
   @Test
   @DisplayName("buildHandlaggareListQuery: entry med constraints ger ORDER BY sort_group via subquery")
   public void handlaggare_list_query_with_entries_orders_by_sort_group()
   {
      var entry = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.ROLL, "PRIO"));
      var built = builder.buildHandlaggareListQuery(entity(List.of(entry)), "t", "v");

      assertTrue(built.pageSql().contains("sort_group"));
      assertTrue(built.pageSql().contains("ORDER BY u.sort_group ASC"));
      assertTrue(built.pageSql().contains("u.created_at ASC"));
   }

   /**
    * {@code buildHandlaggareListQuery} must not include a countSql — it is a plain list query.
    */
   @Test
   @DisplayName("buildHandlaggareListQuery: countSql är null")
   public void handlaggare_list_query_has_no_count_sql()
   {
      var built = builder.buildHandlaggareListQuery(entity(List.of()), "t", "v");

      assertNull(built.countSql());
   }

   /**
    * {@code buildAssignQuery} must filter for unassigned rows so it never selects an already-claimed task.
    */
   @Test
   @DisplayName("buildAssignQuery: filtrerar på handlaggar_id IS NULL")
   public void assign_query_filters_unassigned_rows()
   {
      var built = builder.buildAssignQuery(entity(List.of()));

      assertTrue(built.pageSql().contains("handlaggar_id_typ_id IS NULL"));
      assertTrue(built.pageSql().contains("handlaggar_id_varde IS NULL"));
   }

   /**
    * The fallback assign query (no entries) must include {@code FOR UPDATE SKIP LOCKED} directly
    * after the ORDER BY — no CTE needed when there is no sort group expression to compute.
    */
   @Test
   @DisplayName("buildAssignQuery: tom entry-lista ger fallback LIMIT 1 FOR UPDATE SKIP LOCKED")
   public void assign_query_empty_entries_falls_back_to_created_at()
   {
      var built = builder.buildAssignQuery(entity(List.of()));

      assertTrue(built.pageSql().contains("ORDER BY created_at ASC"));
      assertTrue(built.pageSql().contains("LIMIT 1"));
      assertTrue(built.pageSql().contains("FOR UPDATE SKIP LOCKED"));
   }

   /**
    * With a sorteringsordning the assign query must use a CTE ({@code WITH candidate AS}) so that
    * locking and priority ordering are atomic. {@code FOR UPDATE SKIP LOCKED} must appear inside the
    * CTE body, not as a trailing clause on the outer SELECT, so that PostgreSQL skips already-locked
    * rows and continues to the next eligible candidate rather than returning empty.
    */
   @Test
   @DisplayName("buildAssignQuery: entry med constraints ger CTE med FOR UPDATE SKIP LOCKED inuti")
   public void assign_query_with_entries_uses_cte_with_locking_inside()
   {
      var entry = entryWithConstraints(eqConstraint(SorteringsordningFieldEq.ROLL, "PRIO"));
      var built = builder.buildAssignQuery(entity(List.of(entry)));

      assertTrue(built.pageSql().startsWith("WITH candidate AS ("));
      assertTrue(built.pageSql().contains("ranked.sort_group"));
      assertTrue(built.pageSql().contains("LIMIT 1 FOR UPDATE SKIP LOCKED)"));
   }

   /**
    * {@code buildAssignQuery} must not include a countSql — only one row is ever selected.
    */
   @Test
   @DisplayName("buildAssignQuery: countSql är null")
   public void assign_query_has_no_count_sql()
   {
      var built = builder.buildAssignQuery(entity(List.of()));

      assertNull(built.countSql());
   }

   // --- builders ---

   private static SorteringsordningEntity entity(List<SorteringsordningEntry> entries)
   {
      return new SorteringsordningEntity(UUID.randomUUID(), OffsetDateTime.now(), UUID.randomUUID().toString(), entries);
   }

   private static SorteringsordningEntry catchAll()
   {
      return new SorteringsordningEntry();
   }

   @SuppressWarnings("unchecked")
   private static SorteringsordningEntry entryWithConstraints(Object... constraints)
   {
      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraints));
      return entry;
   }

   private static ConstraintEq eqConstraint(SorteringsordningFieldEq field, String value)
   {
      var c = new ConstraintEq();
      c.setField(field);
      c.setOperator(ConstraintEq.OperatorEnum.EQ);
      c.setValue(value);
      return c;
   }

   private static SortBy sortBy(SorteringsordningField field, SortBy.DirectionEnum direction)
   {
      var s = new SortBy();
      s.setField(field);
      s.setDirection(direction);
      return s;
   }
}
