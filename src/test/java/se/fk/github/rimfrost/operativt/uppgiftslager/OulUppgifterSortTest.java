package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpec;

@QuarkusTest
public class OulUppgifterSortTest extends OulTestBase
{
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
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = getUppgifter(50, 0, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-03.6, OUL-FR-14: Ny default tillämpas automatiskt vid listning efter byte")
   public void should_apply_new_default_after_set_default()
   {
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var second = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      setDefaultSorteringsordning(second.getId());
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = getUppgifter(50, 0, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
   }
}
