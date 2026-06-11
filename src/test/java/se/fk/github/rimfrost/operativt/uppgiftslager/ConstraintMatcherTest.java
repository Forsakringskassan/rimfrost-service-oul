package se.fk.github.rimfrost.operativt.uppgiftslager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.ConstraintMatcher;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableErbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableUppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintBetween;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintContains;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintOffsetToNow;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldDate;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldString;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintMatcherTest
{
   ConstraintMatcher matcher = new ConstraintMatcher();

   @Test
   @DisplayName("eq: matchar när strängar är exakt lika")
   public void eq_matches_string_field_when_equal()
   {
      var uppgift = defaultUppgift().beskrivning("hund").build();
      var constraint = eqConstraint(SorteringsordningFieldEq.BESKRIVNING, "hund");

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("eq: matchar inte när strängar skiljer sig")
   public void eq_does_not_match_when_field_differs()
   {
      var uppgift = defaultUppgift().beskrivning("hund").build();
      var constraint = eqConstraint(SorteringsordningFieldEq.BESKRIVNING, "katt");

      assertFalse(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("eq: matchar på status")
   public void eq_matches_status_field()
   {
      var uppgift = defaultUppgift().status(UppgiftStatus.TILLDELAD).build();
      var constraint = eqConstraint(SorteringsordningFieldEq.STATUS, "TILLDELAD");

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("contains: matchar när strängen innehåller")
   public void contains_matches_when_substring_present()
   {
      var uppgift = defaultUppgift().beskrivning("hundens mat").build();
      var constraint = containsConstraint(SorteringsordningFieldString.BESKRIVNING, "hund");

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("contains: matchar inte när sträng saknas")
   public void contains_does_not_match_when_substring_absent()
   {
      var uppgift = defaultUppgift().beskrivning("kattens mat").build();
      var constraint = containsConstraint(SorteringsordningFieldString.BESKRIVNING, "hund");

      assertFalse(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("between: matchar när datum är inom intervallet")
   public void between_matches_when_date_within_range()
   {
      var uppgift = defaultUppgift().skapad(LocalDate.of(2026, 5, 15)).build();
      var constraint = betweenConstraint(SorteringsordningFieldDate.SKAPAD,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("between: matchar på datumintervall")
   public void between_matches_on_boundary_dates()
   {
      var uppgift = defaultUppgift().skapad(LocalDate.of(2026, 5, 1)).build();
      var constraint = betweenConstraint(SorteringsordningFieldDate.SKAPAD,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("between: matchar inte utanför datumintervallet")
   public void between_does_not_match_outside_range()
   {
      var uppgift = defaultUppgift().skapad(LocalDate.of(2026, 4, 30)).build();
      var constraint = betweenConstraint(SorteringsordningFieldDate.SKAPAD,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

      assertFalse(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("between: matchar inte när datumfältet är null")
   public void between_does_not_match_when_date_field_is_null()
   {
      var uppgift = defaultUppgift().build();
      var constraint = betweenConstraint(SorteringsordningFieldDate.PLANERAD_TILL,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

      assertFalse(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("offset_to_now: matchar när datum är inom negativ offset från nu")
   public void offset_to_now_matches_when_within_offset()
   {
      var uppgift = defaultUppgift().planeradTill(LocalDate.now().minusDays(3)).build();
      var constraint = offsetToNowConstraint(SorteringsordningFieldDate.PLANERAD_TILL, "-7d");

      assertTrue(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("offset_to_now: matchar inte när datum är utanför offset")
   public void offset_to_now_does_not_match_when_outside_offset()
   {
      var uppgift = defaultUppgift().planeradTill(LocalDate.now().minusDays(10)).build();
      var constraint = offsetToNowConstraint(SorteringsordningFieldDate.PLANERAD_TILL, "-7d");

      assertFalse(matcher.matches(uppgift, List.of(constraint)));
   }

   @Test
   @DisplayName("catch-all: null constraints matchar alltid")
   public void null_constraints_is_catch_all()
   {
      var uppgift = defaultUppgift().build();

      assertTrue(matcher.matches(uppgift, null));
   }

   @Test
   @DisplayName("catch-all: tom lista matchar alltid")
   public void empty_constraints_is_catch_all()
   {
      var uppgift = defaultUppgift().build();

      assertTrue(matcher.matches(uppgift, List.of()));
   }

   @Test
   @DisplayName("AND-semantik: alla constraints måste matcha")
   public void all_constraints_must_match()
   {
      var uppgift = defaultUppgift().regel("regel-a").roll("roll-x").build();
      var constraints = List.of(
            eqConstraint(SorteringsordningFieldEq.REGEL, "regel-a"),
            eqConstraint(SorteringsordningFieldEq.ROLL, "roll-x"));

      assertTrue(matcher.matches(uppgift, constraints));
   }

   @Test
   @DisplayName("AND-semantik: returnerar false om ett constraint inte matchar")
   public void returns_false_when_one_constraint_fails()
   {
      var uppgift = defaultUppgift().regel("regel-a").roll("roll-y").build();
      var constraints = List.of(
            eqConstraint(SorteringsordningFieldEq.REGEL, "regel-a"),
            eqConstraint(SorteringsordningFieldEq.ROLL, "roll-x"));

      assertFalse(matcher.matches(uppgift, constraints));
   }

   // --- builders ---

   private static ImmutableUppgiftDto.Builder defaultUppgift()
   {
      return ImmutableUppgiftDto.builder()
            .uppgiftId(UUID.randomUUID())
            .handlaggningId(UUID.randomUUID())
            .skapad(LocalDate.now())
            .status(UppgiftStatus.NY)
            .regel("testregel")
            .beskrivning("testbeskrivning")
            .verksamhetslogik("testverksamhetslogik")
            .roll("testroll")
            .url("http://localhost")
            .subTopic("test-sub-topic")
            .replyTopic("test-reply-topic")
            .cloudeventAttributes(Map.of())
            .erbjudande(ImmutableErbjudande.builder().id("e1").namn("test").build())
            .individer(new se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp[0]);
   }

   private static ConstraintEq eqConstraint(SorteringsordningFieldEq field, String value)
   {
      var c = new ConstraintEq();
      c.setField(field);
      c.setValue(value);
      return c;
   }

   private static ConstraintContains containsConstraint(SorteringsordningFieldString field, String value)
   {
      var c = new ConstraintContains();
      c.setField(field);
      c.setValue(value);
      return c;
   }

   private static ConstraintBetween betweenConstraint(SorteringsordningFieldDate field, LocalDate from, LocalDate to)
   {
      var c = new ConstraintBetween();
      c.setField(field);
      c.setFrom(from);
      c.setTo(to);
      return c;
   }

   private static ConstraintOffsetToNow offsetToNowConstraint(SorteringsordningFieldDate field, String offset)
   {
      var c = new ConstraintOffsetToNow();
      c.setField(field);
      c.setOffset(offset);
      return c;
   }
}
