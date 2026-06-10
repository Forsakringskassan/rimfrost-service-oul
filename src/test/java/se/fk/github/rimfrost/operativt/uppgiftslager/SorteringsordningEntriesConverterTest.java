package se.fk.github.rimfrost.operativt.uppgiftslager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.converter.SorteringsordningEntriesConverter;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SorteringsordningEntriesConverterTest
{
   private final SorteringsordningEntriesConverter converter = new SorteringsordningEntriesConverter();

   @Test
   @DisplayName("OUL-FR-09.4, OUL-FR-09.5: ConstraintEq-subtyp bevaras vid DB-serialisering")
   @SuppressWarnings("unchecked")
   public void should_preserve_constraint_eq_subtype_through_db_serialisation()
   {
      var constraint = new ConstraintEq();
      constraint.setField(SorteringsordningFieldEq.STATUS);
      constraint.setOperator(ConstraintEq.OperatorEnum.EQ);
      constraint.setValue("NY");

      var sortBy = new SortBy();
      sortBy.setField(SorteringsordningField.SKAPAD);
      sortBy.setDirection(SortBy.DirectionEnum.ASC);

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));
      entry.setSortBy(sortBy);

      var json = converter.convertToDatabaseColumn(List.of(entry));
      var result = converter.convertToEntityAttribute(json);

      assertEquals(1, result.size());
      var resultEntry = result.getFirst();
      assertNotNull(resultEntry.getConstraints());
      assertEquals(1, resultEntry.getConstraints().size());
      List<?> rawConstraints = resultEntry.getConstraints();
      var firstConstraint = rawConstraints.getFirst();
      assertInstanceOf(ConstraintEq.class, firstConstraint);
      var resultConstraint = (ConstraintEq) firstConstraint;
      assertEquals(SorteringsordningFieldEq.STATUS, resultConstraint.getField());
      assertEquals("NY", resultConstraint.getValue());
      assertNotNull(resultEntry.getSortBy());
      assertEquals(SorteringsordningField.SKAPAD, resultEntry.getSortBy().getField());
      assertEquals(SortBy.DirectionEnum.ASC, resultEntry.getSortBy().getDirection());
   }

   @Test
   @DisplayName("OUL-FR-09.5: ConstraintBetween-subtyp bevaras vid DB-serialisering")
   @SuppressWarnings("unchecked")
   public void should_preserve_constraint_between_subtype_through_db_serialisation()
   {
      var constraint = new ConstraintBetween();
      constraint.setField(SorteringsordningFieldDate.SKAPAD);
      constraint.setOperator(ConstraintBetween.OperatorEnum.BETWEEN);
      constraint.setFrom(LocalDate.of(2024, 1, 1));
      constraint.setTo(LocalDate.of(2024, 12, 31));

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));

      var json = converter.convertToDatabaseColumn(List.of(entry));
      var result = converter.convertToEntityAttribute(json);

      List<?> rawConstraints = result.getFirst().getConstraints();
      assertInstanceOf(ConstraintBetween.class, rawConstraints.getFirst());
      var resultConstraint = (ConstraintBetween) rawConstraints.getFirst();
      assertEquals(SorteringsordningFieldDate.SKAPAD, resultConstraint.getField());
      assertEquals(LocalDate.of(2024, 1, 1), resultConstraint.getFrom());
      assertEquals(LocalDate.of(2024, 12, 31), resultConstraint.getTo());
   }

   @Test
   @DisplayName("OUL-FR-09.5: ConstraintContains-subtyp bevaras vid DB-serialisering")
   @SuppressWarnings("unchecked")
   public void should_preserve_constraint_contains_subtype_through_db_serialisation()
   {
      var constraint = new ConstraintContains();
      constraint.setField(SorteringsordningFieldString.STATUS);
      constraint.setOperator(ConstraintContains.OperatorEnum.CONTAINS);
      constraint.setValue("test-value");

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));

      var json = converter.convertToDatabaseColumn(List.of(entry));
      var result = converter.convertToEntityAttribute(json);

      List<?> rawConstraints = result.getFirst().getConstraints();
      assertInstanceOf(ConstraintContains.class, rawConstraints.getFirst());
      var resultConstraint = (ConstraintContains) rawConstraints.getFirst();
      assertEquals(SorteringsordningFieldString.STATUS, resultConstraint.getField());
      assertEquals("test-value", resultConstraint.getValue());
   }

   @Test
   @DisplayName("OUL-FR-09.5: ConstraintOffsetToNow-subtyp bevaras vid DB-serialisering")
   @SuppressWarnings("unchecked")
   public void should_preserve_constraint_offset_to_now_subtype_through_db_serialisation()
   {
      var constraint = new ConstraintOffsetToNow();
      constraint.setField(SorteringsordningFieldDate.PLANERAD_TILL);
      constraint.setOperator(ConstraintOffsetToNow.OperatorEnum.OFFSET_TO_NOW);
      constraint.setOffset("-7d");

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));

      var json = converter.convertToDatabaseColumn(List.of(entry));
      var result = converter.convertToEntityAttribute(json);

      List<?> rawConstraints = result.getFirst().getConstraints();
      assertInstanceOf(ConstraintOffsetToNow.class, rawConstraints.getFirst());
      var resultConstraint = (ConstraintOffsetToNow) rawConstraints.getFirst();
      assertEquals(SorteringsordningFieldDate.PLANERAD_TILL, resultConstraint.getField());
      assertEquals("-7d", resultConstraint.getOffset());
   }

   @Test
   @DisplayName("convertToEntityAttribute: ogiltigt JSON kastar IllegalArgumentException")
   public void should_throw_illegal_argument_on_malformed_json()
   {
      assertThrows(IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute("not-json{{{"));
   }

   @Test
   @DisplayName("convertToEntityAttribute: okänd constraint-operator kastar IllegalArgumentException")
   public void should_throw_illegal_argument_on_unknown_operator()
   {
      var json = "[{\"constraints\":[{\"operator\":\"unknown_op\",\"field\":\"status\"}]}]";

      assertThrows(IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute(json));
   }

   @Test
   @DisplayName("convertToEntityAttribute: null JSON kastar IllegalArgumentException")
   public void should_throw_illegal_argument_on_null_json()
   {
      assertThrows(IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute(null));
   }

   @Test
   @DisplayName("OUL-FR-09.5: Blandade constraint-subtyper och sort_by i samma entry bevaras vid DB-serialisering")
   @SuppressWarnings("unchecked")
   public void should_preserve_mixed_constraints_and_sort_by_in_same_entry()
   {
      var eqConstraint = new ConstraintEq();
      eqConstraint.setField(SorteringsordningFieldEq.STATUS);
      eqConstraint.setOperator(ConstraintEq.OperatorEnum.EQ);
      eqConstraint.setValue("NY");

      var betweenConstraint = new ConstraintBetween();
      betweenConstraint.setField(SorteringsordningFieldDate.SKAPAD);
      betweenConstraint.setOperator(ConstraintBetween.OperatorEnum.BETWEEN);
      betweenConstraint.setFrom(LocalDate.of(2024, 1, 1));
      betweenConstraint.setTo(LocalDate.of(2024, 12, 31));

      var sortBy = new SortBy();
      sortBy.setField(SorteringsordningField.SKAPAD);
      sortBy.setDirection(SortBy.DirectionEnum.DESC);

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(eqConstraint, betweenConstraint));
      entry.setSortBy(sortBy);

      var json = converter.convertToDatabaseColumn(List.of(entry));
      var result = converter.convertToEntityAttribute(json);

      assertEquals(1, result.size());
      var resultEntry = result.getFirst();
      List<?> rawConstraints = resultEntry.getConstraints();
      assertEquals(2, rawConstraints.size());
      assertInstanceOf(ConstraintEq.class, rawConstraints.get(0));
      assertInstanceOf(ConstraintBetween.class, rawConstraints.get(1));
      assertEquals("NY", ((ConstraintEq) rawConstraints.get(0)).getValue());
      assertEquals(LocalDate.of(2024, 1, 1), ((ConstraintBetween) rawConstraints.get(1)).getFrom());
      assertNotNull(resultEntry.getSortBy());
      assertEquals(SortBy.DirectionEnum.DESC, resultEntry.getSortBy().getDirection());
   }

   @Test
   @DisplayName("Okänd operator i JSON ger IllegalArgumentException vid DB-deserialisering")
   public void should_throw_on_unknown_constraint_operator()
   {
      var json = """
            [{"constraints":[{"operator":"UNKNOWN_OP","field":"STATUS"}],"sortBy":null}]
            """;

      assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(json));
   }
}
