package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.PanacheOulDataStorage;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpecWithEqConstraint;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpecWithSortBy;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpec;

@QuarkusTest
public class OulUppgifterSortTest extends OulTestBase
{
   @Inject
   OperativtUppgiftslagerService service;

   @Inject
   PanacheOulDataStorage panacheOulDataStorage;

   @Test
   @DisplayName("GET /uppgifter utan sorteringsordning — returnerar alla uppgifter")
   public void should_return_all_tasks_without_sorteringsordning()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = getUppgifter(50, 0, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("GET /uppgifter med okänt sorteringsordningId — HTTP 404")
   public void should_return_404_for_unknown_sorteringsordning_id()
   {
      getUppgifter(50, UUID.randomUUID(), 404);
   }

   @Test
   @DisplayName("GET /uppgifter med limit — begränsar returnerade items men total är oförändrad")
   public void should_paginate_with_limit()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = getUppgifter(2, 0, null);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("GET /uppgifter med offset — hoppar över uppgifter, total är oförändrad")
   public void should_paginate_with_offset()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = getUppgifter(50, 1, null);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("GET /uppgifter med sorteringsordningId — använder angiven sorteringsordning")
   public void should_use_specified_sorteringsordning_by_id()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var sortering = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      var page = getUppgifter(50, 0, sortering.getId());

      assertEquals(1, page.getTotal());
      assertEquals(1, page.getItems().size());
   }

   @Test
   @DisplayName("GET /uppgifter med sorteringsordningId — returnerar alla uppgifter via catch-all sorteringsordning")
   public void should_apply_sorteringsordning_by_id()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var sortering = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      var page = getUppgifter(50, 0, sortering.getId());

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-03.6: Default sorteringsordning tillämpas automatiskt vid listning utan sorteringsordningId")
   public void should_apply_default_sorteringsordning_automatically()
   {
      // Default: STATUS=NY matched tasks sort before unmatched (TILLDELAD)
      sendCreateSorteringsordningRequest(newStatusConstraintSpec("NY"));

      var task1 = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var task2 = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assigned = assignTaskToHandlaggare(UUID.randomUUID());
      var assignedId = assigned.getOperativUppgift().getUppgiftId();
      var remainingId = task1.getUppgiftId().equals(assignedId) ? task2.getUppgiftId() : task1.getUppgiftId();

      var page = getUppgifter(50, 0, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
      // STATUS=NY sorts first (matched); TILLDELAD (assigned) sorts last (unmatched)
      assertEquals(remainingId, page.getItems().get(0).getUppgiftId());
      assertEquals(assignedId, page.getItems().get(1).getUppgiftId());
   }

   @Test
   @DisplayName("OUL-FR-03.6, OUL-FR-14: Ny default tillämpas automatiskt vid listning efter byte")
   public void should_apply_new_default_after_set_default()
   {
      // First default: STATUS=NY matched first
      sendCreateSorteringsordningRequest(newStatusConstraintSpec("NY"));
      // New default: STATUS=TILLDELAD matched first
      var second = sendCreateSorteringsordningRequest(newStatusConstraintSpec("TILLDELAD"));
      setDefaultSorteringsordning(second.getId());

      var task1 = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var task2 = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assigned = assignTaskToHandlaggare(UUID.randomUUID());
      var assignedId = assigned.getOperativUppgift().getUppgiftId();
      var remainingId = task1.getUppgiftId().equals(assignedId) ? task2.getUppgiftId() : task1.getUppgiftId();

      var page = getUppgifter(50, 0, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
      // STATUS=TILLDELAD sorts first (matched); NY (remaining) sorts last (unmatched)
      assertEquals(assignedId, page.getItems().get(0).getUppgiftId());
      assertEquals(remainingId, page.getItems().get(1).getUppgiftId());
   }

   /**
    * Builds a {@link SorteringsordningSpec} with a single entry constraining on STATUS = {@code status}.
    *
    * @param status the status value to match (e.g. "NY", "TILLDELAD")
    * @return a spec with one STATUS equality constraint and no sort order
    */
   @SuppressWarnings("unchecked")
   private static SorteringsordningSpec newStatusConstraintSpec(String status)
   {
      var constraint = new ConstraintEq();
      constraint.setField(SorteringsordningFieldEq.STATUS);
      constraint.setOperator(ConstraintEq.OperatorEnum.EQ);
      constraint.setValue(status);

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));

      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));
      return spec;
   }

   @Test
   @DisplayName("Count-cache: två konsekutiva anrop utan mutation returnerar samma total (cache hit)")
   public void should_return_consistent_total_on_consecutive_calls_cache_hit()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var first = getUppgifter(50, 0, null);
      var second = getUppgifter(50, 0, null);

      assertEquals(2, first.getTotal());
      assertEquals(2, second.getTotal());
   }

   @Test
   @DisplayName("Count-cache: invalidering tvingar nytt DB-anrop och returnerar korrekt total")
   public void should_return_fresh_total_after_cache_invalidation()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      // Populate the cache
      var before = getUppgifter(50, 0, null);
      assertEquals(2, before.getTotal());

      // Invalidate cache to simulate TTL expiry
      panacheOulDataStorage.invalidateCountCache();

      // Should issue a fresh COUNT(*) and still return the correct total
      var after = getUppgifter(50, 0, null);
      assertEquals(2, after.getTotal());
   }

   @Test
   @DisplayName("DB-sortering: eq-constraint placerar matchande uppgift före icke-matchande")
   public void should_sort_matching_eq_constraint_before_non_matching()
   {
      var normalRequest = newCreateUppgiftRequest(UUID.randomUUID());
      normalRequest.setRegel("Normal Regel");
      sendCreateUppgiftRequest(normalRequest);

      var priorityRequest = newCreateUppgiftRequest(UUID.randomUUID());
      priorityRequest.setRegel("Prioriterad Regel");
      sendCreateUppgiftRequest(priorityRequest);

      // The HTTP API has a known Jackson polymorphism limitation with ConstraintEq over the wire.
      // Calling the service directly bypasses that and tests the DB sort path end-to-end.
      var sortering = service.createSorteringsordning(
            newSorteringsordningSpecWithEqConstraint(SorteringsordningFieldEq.REGEL, "Prioriterad Regel"));

      var page = getUppgifter(50, 0, sortering.id());

      assertEquals(2, page.getTotal());
      assertEquals("Prioriterad Regel", page.getItems().get(0).getRegel());
      assertEquals("Normal Regel", page.getItems().get(1).getRegel());
   }

   @Test
   @DisplayName("DB-sortering: sort_by ASC sorterar uppgifter i stigande ordning inom gruppen")
   public void should_sort_by_field_ascending_within_group()
   {
      var requestZ = newCreateUppgiftRequest(UUID.randomUUID());
      requestZ.setRoll("Z Roll");
      sendCreateUppgiftRequest(requestZ);

      var requestA = newCreateUppgiftRequest(UUID.randomUUID());
      requestA.setRoll("A Roll");
      sendCreateUppgiftRequest(requestA);

      var sortering = sendCreateSorteringsordningRequest(
            newSorteringsordningSpecWithSortBy(SorteringsordningField.ROLL, SortBy.DirectionEnum.ASC));

      var page = getUppgifter(50, 0, sortering.getId());

      assertEquals(2, page.getTotal());
      assertEquals("A Roll", page.getItems().get(0).getRoll());
      assertEquals("Z Roll", page.getItems().get(1).getRoll());
   }

   @Test
   @DisplayName("DB-sortering: sort_by DESC sorterar uppgifter i fallande ordning inom gruppen")
   public void should_sort_by_field_descending_within_group()
   {
      var requestA = newCreateUppgiftRequest(UUID.randomUUID());
      requestA.setRoll("A Roll");
      sendCreateUppgiftRequest(requestA);

      var requestZ = newCreateUppgiftRequest(UUID.randomUUID());
      requestZ.setRoll("Z Roll");
      sendCreateUppgiftRequest(requestZ);

      var sortering = sendCreateSorteringsordningRequest(
            newSorteringsordningSpecWithSortBy(SorteringsordningField.ROLL, SortBy.DirectionEnum.DESC));

      var page = getUppgifter(50, 0, sortering.getId());

      assertEquals(2, page.getTotal());
      assertEquals("Z Roll", page.getItems().get(0).getRoll());
      assertEquals("A Roll", page.getItems().get(1).getRoll());
   }
}
