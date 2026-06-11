package se.fk.github.rimfrost.operativt.uppgiftslager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.ConstraintMatcher;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.SortOrderApplier;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableErbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableUppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortOrderApplierTest
{
   SortOrderApplier applier = new SortOrderApplier(new ConstraintMatcher());

   @Test
   @DisplayName("Tom lista returnerar tom sida med total 0")
   public void empty_list_returns_empty_page()
   {
      var sorteringsordning = sorteringsordning(List.of(catchAll()));

      var result = applier.apply(List.of(), sorteringsordning, 10, 0);
      assertEquals(0, result.total());
      assertEquals(0, result.items().size());
   }

   @Test
   @DisplayName("Catch-all entry returnerar alla uppgifter")
   public void catch_all_entry_returns_all_tasks()
   {
      var uppgifter = List.of(defaultUppgift(), defaultUppgift(), defaultUppgift());
      var sorteringsordning = sorteringsordning(List.of(catchAll()));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 0);
      assertEquals(3, result.total());
      assertEquals(3, result.items().size());
   }

   @Test
   @DisplayName("Uppgifter som matchar en tidig entry placeras före uppgifter som matchar en sen entry")
   public void earlier_entry_tasks_appear_before_later_entry_tasks()
   {
      var prioriterad = defaultUppgiftBuilder().regel("viktig").build();
      var vanlig = defaultUppgiftBuilder().regel("normal").build();
      var uppgifter = List.<UppgiftDto> of(vanlig, prioriterad);

      var prioriteradEntry = entry(List.of(eqConstraint(SorteringsordningFieldEq.REGEL, "viktig")), null);
      var catchAllEntry = catchAll();
      var sorteringsordning = sorteringsordning(List.of(prioriteradEntry, catchAllEntry));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 0);
      assertEquals(prioriterad.uppgiftId(), result.items().get(0).uppgiftId());
      assertEquals(vanlig.uppgiftId(), result.items().get(1).uppgiftId());
   }

   @Test
   @DisplayName("sort_by datum asc sorterar uppgifter inom entry i stigande ordning")
   public void sort_by_date_asc_orders_tasks_within_entry()
   {
      var tidig = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 1, 1)).build();
      var sen = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 6, 1)).build();
      var uppgifter = List.<UppgiftDto> of(sen, tidig);

      var entry = entry(null, new SortBy(SorteringsordningField.SKAPAD, SortBy.DirectionEnum.ASC));
      var sorteringsordning = sorteringsordning(List.of(entry));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 0);
      assertEquals(tidig.uppgiftId(), result.items().get(0).uppgiftId());
      assertEquals(sen.uppgiftId(), result.items().get(1).uppgiftId());
   }

   @Test
   @DisplayName("sort_by datum desc sorterar uppgifter inom entry i fallande ordning")
   public void sort_by_date_desc_orders_tasks_within_entry()
   {
      var tidig = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 1, 1)).build();
      var sen = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 6, 1)).build();
      var uppgifter = List.<UppgiftDto> of(tidig, sen);

      var entry = entry(null, new SortBy(SorteringsordningField.SKAPAD, SortBy.DirectionEnum.DESC));
      var sorteringsordning = sorteringsordning(List.of(entry));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 0);
      assertEquals(sen.uppgiftId(), result.items().get(0).uppgiftId());
      assertEquals(tidig.uppgiftId(), result.items().get(1).uppgiftId());
   }

   @Test
   @DisplayName("limit begränsar antalet returnerade uppgifter")
   public void limit_restricts_returned_items()
   {
      var uppgifter = List.of(defaultUppgift(), defaultUppgift(), defaultUppgift());
      var sorteringsordning = sorteringsordning(List.of(catchAll()));

      var result = applier.apply(uppgifter, sorteringsordning, 2, 0);
      assertEquals(3, result.total());
      assertEquals(2, result.items().size());
   }

   @Test
   @DisplayName("offset hoppar över uppgifter")
   public void offset_skips_items()
   {
      var forsta = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 1, 1)).build();
      var andra = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 1, 2)).build();
      var tredje = defaultUppgiftBuilder().skapad(LocalDate.of(2026, 1, 3)).build();
      var uppgifter = List.<UppgiftDto> of(forsta, andra, tredje);

      var entry = entry(null, new SortBy(SorteringsordningField.SKAPAD, SortBy.DirectionEnum.ASC));
      var sorteringsordning = sorteringsordning(List.of(entry));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 1);
      assertEquals(3, result.total());
      assertEquals(2, result.items().size());
      assertEquals(andra.uppgiftId(), result.items().get(0).uppgiftId());
   }

   @Test
   @DisplayName("total återspeglar alla uppgifter, inte bara sidstorleken")
   public void total_reflects_all_tasks_not_page_size()
   {
      var uppgifter = List.of(defaultUppgift(), defaultUppgift(), defaultUppgift(), defaultUppgift(), defaultUppgift());
      var sorteringsordning = sorteringsordning(List.of(catchAll()));

      var result = applier.apply(uppgifter, sorteringsordning, 2, 0);
      assertEquals(5, result.total());
   }

   @Test
   @DisplayName("Uppgifter utan matchande entry hamnar sist i ursprungsordning")
   public void unmatched_tasks_appear_last_in_original_order()
   {
      var matchande = defaultUppgiftBuilder().regel("viktig").build();
      var omatchad1 = defaultUppgift();
      var omatchad2 = defaultUppgift();
      var uppgifter = List.of(omatchad1, matchande, omatchad2);

      var prioriteradEntry = entry(List.of(eqConstraint(SorteringsordningFieldEq.REGEL, "viktig")), null);
      var sorteringsordning = sorteringsordning(List.of(prioriteradEntry));

      var result = applier.apply(uppgifter, sorteringsordning, 10, 0);
      assertEquals(matchande.uppgiftId(), result.items().get(0).uppgiftId());
      assertEquals(omatchad1.uppgiftId(), result.items().get(1).uppgiftId());
      assertEquals(omatchad2.uppgiftId(), result.items().get(2).uppgiftId());
   }

   // --- builders ---

   private static UppgiftDto defaultUppgift()
   {
      return defaultUppgiftBuilder().build();
   }

   private static ImmutableUppgiftDto.Builder defaultUppgiftBuilder()
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
            .subTopic("test")
            .cloudeventAttributes(Map.of())
            .erbjudande(ImmutableErbjudande.builder().id("e1").namn("test").build())
            .individer(new se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp[0]);
   }

   private static SorteringsordningEntity sorteringsordning(List<SorteringsordningEntry> entries)
   {
      return new SorteringsordningEntity(UUID.randomUUID(), OffsetDateTime.now(), entries);
   }

   private static SorteringsordningEntry catchAll()
   {
      return entry(null, null);
   }

   @SuppressWarnings("unchecked")
   private static SorteringsordningEntry entry(List<?> constraints, SortBy sortBy)
   {
      var e = new SorteringsordningEntry();
      if (constraints != null)
      {
         e.setConstraints(
               (List<se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint>) (List<?>) constraints);
      }
      e.setSortBy(sortBy);
      return e;
   }

   private static ConstraintEq eqConstraint(SorteringsordningFieldEq field, String value)
   {
      var c = new ConstraintEq();
      c.setField(field);
      c.setValue(value);
      return c;
   }
}
