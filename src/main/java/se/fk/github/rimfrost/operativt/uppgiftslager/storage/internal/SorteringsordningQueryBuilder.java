package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintBetween;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintContains;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintOffsetToNow;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;

/**
 * Builds parameterized native SQL queries for paginated, sorted uppgift retrieval.
 * <p>
 * Sorteringsordning entries define priority groups via a {@code CASE WHEN} expression.
 * The first entry whose constraints all match a row wins (AND semantics within an entry,
 * first-match-wins across entries). Rows that match no entry land in a catch-all group
 * at the end.
 * <p>
 * Field names from the sorteringsordning spec are mapped to {@code uppgift} column names
 * via a whitelist to prevent SQL injection.
 */
@ApplicationScoped
public class SorteringsordningQueryBuilder
{
   /** Maps SorteringsordningFieldEq string values to uppgift column names. */
   private static final Map<String, String> EQ_FIELD_TO_COLUMN = Map.of(
         "uppgift_id", "id",
         "status", "status",
         "regel", "regel",
         "roll", "roll",
         "verksamhetslogik", "verksamhetslogik",
         "beskrivning", "beskrivning");

   /** Maps SorteringsordningFieldString string values to uppgift column names. */
   private static final Map<String, String> STRING_FIELD_TO_COLUMN = Map.of(
         "status", "status",
         "regel", "regel",
         "roll", "roll",
         "verksamhetslogik", "verksamhetslogik",
         "beskrivning", "beskrivning");

   /** Maps SorteringsordningFieldDate string values to uppgift column names. */
   private static final Map<String, String> DATE_FIELD_TO_COLUMN = Map.of(
         "skapad", "skapad",
         "planerad_till", "planerad_till");

   /** Maps all SorteringsordningField string values (used by sort_by) to uppgift column names. */
   private static final Map<String, String> SORT_FIELD_TO_COLUMN;

   static
   {
      var m = new HashMap<String, String>();
      m.putAll(EQ_FIELD_TO_COLUMN);
      m.putAll(DATE_FIELD_TO_COLUMN);
      SORT_FIELD_TO_COLUMN = Map.copyOf(m);
   }

   /** All columns of the {@code uppgift} table, qualified with the subquery alias {@code u}. */
   private static final String UPPGIFT_COLUMNS = "u.id, u.handlaggning_id, u.handlaggar_id_typ_id, u.handlaggar_id_varde, "
         + "u.skapad, u.planerad_till, u.utford, u.status, u.regel, u.beskrivning, "
         + "u.verksamhetslogik, u.roll, u.url, u.sub_topic, u.reply_topic, u.erbjudande_id, u.erbjudande_namn, "
         + "u.reason, u.version, u.created_at, u.updated_at";

   @ConfigProperty(name = "quarkus.flyway.default-schema", defaultValue = "public")
   String schema;

   /**
    * Holds the generated page query, count query, and their shared named-parameter bindings.
    * {@code pageSql} must be executed with {@code setFirstResult}/{@code setMaxResults} for pagination;
    * it does not embed {@code LIMIT}/{@code OFFSET} literals.
    *
    * @param pageSql  native SQL that returns ordered rows (no LIMIT/OFFSET — use setMaxResults/setFirstResult)
    * @param countSql native SQL that returns the total row count as a single scalar
    * @param params   named parameter bindings to apply to {@code pageSql}
    */
   public record BuiltQuery(String pageSql, String countSql, Map<String, Object> params)
   {
      /** Defensive copy so callers cannot mutate the query's parameter bindings. */
      public BuiltQuery
      {
         params = Map.copyOf(params);
      }
   }

   /**
    * Builds a page query and a count query for the given sorteringsordning.
    * An empty or null entry list falls back to simple {@code created_at ASC} ordering.
    *
    * @param sorteringsordning the sort specification; entries define priority groups
    * @return a {@link BuiltQuery} ready to be executed
    */
   public BuiltQuery build(SorteringsordningEntity sorteringsordning)
   {
      var entries = sorteringsordning.entries();
      Map<String, Object> params = new HashMap<>();

      var table = schema + ".uppgift";
      // COUNT(*) is a full-table count: sorteringsordning entries only sort/group rows,
      // they never exclude rows. If filtering is ever added, countSql must gain a WHERE clause.
      var countSql = "SELECT COUNT(*) FROM " + table;

      if (entries == null || entries.isEmpty())
      {
         return new BuiltQuery(
               "SELECT * FROM " + table + " ORDER BY created_at ASC",
               countSql,
               params);
      }

      var sortGroupExpr = buildSortGroupExpr(entries, params);
      var orderByClause = buildOrderByClause(entries, "u");

      var pageSql = "SELECT " + UPPGIFT_COLUMNS
            + " FROM (SELECT *, " + sortGroupExpr + " AS sort_group FROM " + table + ") AS u"
            + " ORDER BY " + orderByClause;

      return new BuiltQuery(pageSql, countSql, params);
   }

   /**
    * Builds a query that fetches all uppgifter assigned to the given handläggare,
    * ordered according to the sorteringsordning. Falls back to {@code created_at ASC}
    * if the sorteringsordning has no entries.
    *
    * @param sorteringsordning the sort specification
    * @param handlaggarTypId   the handläggare identity type id
    * @param handlaggarVarde   the handläggare identity value
    * @return a {@link BuiltQuery} ready to be executed (no {@code countSql})
    */
   public BuiltQuery buildHandlaggareListQuery(SorteringsordningEntity sorteringsordning, String handlaggarTypId,
         String handlaggarVarde)
   {
      var entries = sorteringsordning.entries();
      Map<String, Object> params = new HashMap<>();
      params.put("hl_typ_id", handlaggarTypId);
      params.put("hl_varde", handlaggarVarde);

      var table = schema + ".uppgift";
      var whereClause = "handlaggar_id_typ_id = :hl_typ_id AND handlaggar_id_varde = :hl_varde";

      if (entries == null || entries.isEmpty())
      {
         return new BuiltQuery(
               "SELECT * FROM " + table + " WHERE " + whereClause + " ORDER BY created_at ASC",
               null,
               params);
      }

      var sortGroupExpr = buildSortGroupExpr(entries, params);
      var orderByClause = buildOrderByClause(entries, "u");

      var pageSql = "SELECT " + UPPGIFT_COLUMNS
            + " FROM (SELECT *, " + sortGroupExpr + " AS sort_group FROM " + table
            + " WHERE " + whereClause + ") AS u"
            + " ORDER BY " + orderByClause;

      return new BuiltQuery(pageSql, null, params);
   }

   /**
    * Builds a query that fetches all uppgifter assigned to any of the given team members,
    * ordered according to the sorteringsordning. Falls back to {@code created_at ASC}
    * if the sorteringsordning has no entries.
    * <p>
    * The team member filter is expressed as a disjunction of {@code (typ_id, varde)} equality
    * predicates using named parameters — one pair per team member — to prevent SQL injection.
    *
    * @param sorteringsordning the sort specification
    * @param teamMembers       the team member identities to filter on; must not be empty
    * @return a {@link BuiltQuery} ready to be executed (no {@code countSql})
    */
   public BuiltQuery buildTeamListQuery(SorteringsordningEntity sorteringsordning, List<Idtyp> teamMembers)
   {
      var entries = sorteringsordning.entries();
      Map<String, Object> params = new HashMap<>();

      var whereClause = IntStream.range(0, teamMembers.size())
            .mapToObj(i -> {
               var typKey = "tm_typ_" + i;
               var vardeKey = "tm_varde_" + i;
               params.put(typKey, teamMembers.get(i).typId());
               params.put(vardeKey, teamMembers.get(i).varde());
               return "(handlaggar_id_typ_id = :" + typKey + " AND handlaggar_id_varde = :" + vardeKey + ")";
            })
            .collect(Collectors.joining(" OR "));

      var table = schema + ".uppgift";

      if (entries == null || entries.isEmpty())
      {
         return new BuiltQuery(
               "SELECT * FROM " + table + " WHERE " + whereClause + " ORDER BY created_at ASC",
               null,
               params);
      }

      var sortGroupExpr = buildSortGroupExpr(entries, params);
      var orderByClause = buildOrderByClause(entries, "u");

      var pageSql = "SELECT " + UPPGIFT_COLUMNS
            + " FROM (SELECT *, " + sortGroupExpr + " AS sort_group FROM " + table
            + " WHERE " + whereClause + ") AS u"
            + " ORDER BY " + orderByClause;

      return new BuiltQuery(pageSql, null, params);
   }

   /**
    * Builds a query that atomically selects the single highest-priority unassigned uppgift
    * and locks it with {@code FOR UPDATE SKIP LOCKED}.
    * <p>
    * Uses a CTE ({@code WITH candidate AS (...)}) so that locking and priority ordering happen
    * in a single statement. If the top-ranked row is already held by another transaction,
    * {@code SKIP LOCKED} skips it and the next eligible row is returned instead — avoiding
    * the TOCTOU gap of a two-phase approach where a row can be claimed between the rank step
    * and the lock step. Falls back to {@code created_at ASC} ordering if the sorteringsordning
    * has no entries.
    *
    * @param sorteringsordning the sort specification that determines task priority
    * @return a {@link BuiltQuery} with {@code pageSql} ready to be executed (no {@code countSql})
    */
   public BuiltQuery buildAssignQuery(SorteringsordningEntity sorteringsordning)
   {
      var entries = sorteringsordning.entries();
      Map<String, Object> params = new HashMap<>();

      var table = schema + ".uppgift";
      var unassignedFilter = "handlaggar_id_typ_id IS NULL AND handlaggar_id_varde IS NULL";

      if (entries == null || entries.isEmpty())
      {
         var sql = "SELECT * FROM " + table + " WHERE " + unassignedFilter
               + " ORDER BY created_at ASC LIMIT 1 FOR UPDATE SKIP LOCKED";
         return new BuiltQuery(sql, null, params);
      }

      var sortGroupExpr = buildSortGroupExpr(entries, params);
      var orderByClause = buildOrderByClause(entries, "ranked");

      var sql = "WITH candidate AS ("
            + "SELECT id FROM (SELECT *, " + sortGroupExpr + " AS sort_group FROM " + table
            + " WHERE " + unassignedFilter + ") AS ranked"
            + " ORDER BY " + orderByClause
            + " LIMIT 1 FOR UPDATE SKIP LOCKED"
            + ") SELECT * FROM " + table + " WHERE id = (SELECT id FROM candidate)";

      return new BuiltQuery(sql, null, params);
   }

   /**
    * Builds the {@code CASE WHEN} expression that assigns each row to a sort group.
    * The first matching entry wins; unmatched rows get group index {@code entries.size()}.
    */
   private String buildSortGroupExpr(List<SorteringsordningEntry> entries, Map<String, Object> params)
   {
      var sb = new StringBuilder("CASE");
      for (int i = 0; i < entries.size(); i++)
      {
         var constraints = entries.get(i).getConstraints();
         if (constraints == null || constraints.isEmpty())
         {
            sb.append(" WHEN TRUE THEN ").append(i);
            break;
         }
         sb.append(" WHEN ")
               .append(buildConstraintPredicates(constraints, i, params))
               .append(" THEN ").append(i);
      }
      sb.append(" ELSE ").append(entries.size()).append(" END");
      return sb.toString();
   }

   /**
    * Combines all constraints for one entry into a single AND-predicate enclosed in parentheses.
    * Parameter names are scoped by entry index {@code i} and constraint index {@code j}
    * to guarantee uniqueness across the full query.
    */
   private String buildConstraintPredicates(List<?> constraints, int entryIdx, Map<String, Object> params)
   {
      var predicates = new ArrayList<String>();
      for (int j = 0; j < constraints.size(); j++)
      {
         predicates.add(buildPredicate(constraints.get(j), "p_" + entryIdx + "_" + j + "_", params));
      }
      return "(" + String.join(" AND ", predicates) + ")";
   }

   /**
    * Translates a single constraint to a SQL predicate fragment and registers its parameters.
    * All column names are resolved through the whitelist maps — no user-controlled strings reach the SQL.
    */
   private String buildPredicate(Object constraint, String prefix, Map<String, Object> params)
   {
      return switch (constraint)
      {
         case ConstraintEq eq ->
         {
            var col = EQ_FIELD_TO_COLUMN.get(eq.getField().toString());
            // Fires if the OpenAPI spec adds a new SorteringsordningFieldEq value but this map is not updated.
            if (col == null)
            {
               throw new IllegalArgumentException("Unmapped eq field: " + eq.getField());
            }
            var p = prefix + "val";
            params.put(p, eq.getValue());
            yield col + " = :" + p;
         }
         case ConstraintContains contains ->
         {
            var col = STRING_FIELD_TO_COLUMN.get(contains.getField().toString());
            // Fires if the OpenAPI spec adds a new SorteringsordningFieldString value but this map is not updated.
            if (col == null)
            {
               throw new IllegalArgumentException("Unmapped contains field: " + contains.getField());
            }
            var p = prefix + "val";
            params.put(p, "%" + contains.getValue() + "%");
            yield col + " LIKE :" + p;
         }
         case ConstraintBetween between ->
         {
            var col = DATE_FIELD_TO_COLUMN.get(between.getField().toString());
            // Fires if the OpenAPI spec adds a new SorteringsordningFieldDate value but this map is not updated.
            if (col == null)
            {
               throw new IllegalArgumentException("Unmapped between field: " + between.getField());
            }
            var pFrom = prefix + "from";
            var pTo = prefix + "to";
            params.put(pFrom, between.getFrom());
            params.put(pTo, between.getTo());
            yield col + " BETWEEN :" + pFrom + " AND :" + pTo;
         }
         case ConstraintOffsetToNow offsetToNow ->
         {
            var col = DATE_FIELD_TO_COLUMN.get(offsetToNow.getField().toString());
            // Fires if the OpenAPI spec adds a new SorteringsordningFieldDate value but this map is not updated.
            if (col == null)
            {
               throw new IllegalArgumentException("Unmapped offset_to_now field: " + offsetToNow.getField());
            }
            var pFrom = prefix + "from";
            var pTo = prefix + "to";
            params.put(pFrom, LocalDate.now().plus(parseOffset(offsetToNow.getOffset())));
            params.put(pTo, LocalDate.now());
            yield col + " BETWEEN :" + pFrom + " AND :" + pTo;
         }
         default -> throw new IllegalArgumentException("Unknown constraint type: " + constraint.getClass());
      };
   }

   /**
    * Builds the ORDER BY clause: sort_group first, then per-entry sort_by (as CASE WHEN expressions
    * that are non-null only for the target group), then created_at as a stable tiebreaker.
    *
    * @param entries the sorteringsordning entries
    * @param alias   the table alias used in the enclosing query (e.g. {@code "u"} or {@code "ranked"})
    */
   private String buildOrderByClause(List<SorteringsordningEntry> entries, String alias)
   {
      var sb = new StringBuilder(alias + ".sort_group ASC");
      for (int i = 0; i < entries.size(); i++)
      {
         var sortBy = entries.get(i).getSortBy();
         if (sortBy != null)
         {
            var col = SORT_FIELD_TO_COLUMN.get(sortBy.getField().toString());
            var dir = sortBy.getDirection() == SortBy.DirectionEnum.DESC ? "DESC" : "ASC";
            sb.append(", CASE WHEN ").append(alias).append(".sort_group = ").append(i)
                  .append(" THEN ").append(alias).append(".").append(col).append(" END ").append(dir);
         }
      }
      sb.append(", ").append(alias).append(".created_at ASC");
      return sb.toString();
   }

   /**
    * Parses an offset string such as {@code -7d}, {@code 2w}, {@code -1m} into a {@link Period}.
    * Supported units: {@code d} (days), {@code w} (weeks), {@code m} (months), {@code y} (years).
    *
    * @param offset the offset string to parse
    * @return the corresponding {@link Period}, negated for negative offsets
    */
   private Period parseOffset(String offset)
   {
      if (offset == null || offset.isBlank())
      {
         return Period.ZERO;
      }
      var s = offset.strip();
      var negative = s.startsWith("-");
      if (negative)
      {
         s = s.substring(1);
      }
      var unit = s.charAt(s.length() - 1);
      var amount = Integer.parseInt(s.substring(0, s.length() - 1));
      var period = unitToPeriod(unit, amount);
      return negative ? period.negated() : period;
   }

   /**
    * Converts a single unit character and an amount into a {@link Period}.
    *
    * @param unit   {@code d}, {@code w}, {@code m}, or {@code y}
    * @param amount the number of units
    * @return the corresponding {@link Period}
    * @throws IllegalArgumentException if {@code unit} is not one of the supported characters
    */
   private Period unitToPeriod(char unit, int amount)
   {
      // @formatter:off
      return switch (unit)
      {
         case 'd' -> Period.ofDays(amount);
         case 'w' -> Period.ofWeeks(amount);
         case 'm' -> Period.ofMonths(amount);
         case 'y' -> Period.ofYears(amount);
         default  -> throw new IllegalArgumentException("Unknown offset unit: " + unit);
      };
      // @formatter:on
   }
}
