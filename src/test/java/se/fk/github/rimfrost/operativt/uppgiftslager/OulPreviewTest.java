package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpec;

@QuarkusTest
public class OulPreviewTest extends OulTestBase
{
   @Test
   @DisplayName("POST /sorteringsordning/preview — returnerar uppgifter för angiven sorteringsordning utan att spara sorteringsordningen")
   public void should_preview_without_saving()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = sendPreviewRequest(newSorteringsordningSpec(), 50, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
      assertEquals(0, getSorteringsordningar(100).getTotal());
   }

   @Test
   @DisplayName("POST /sorteringsordning/preview — inga uppgifter ger tom sida")
   public void should_return_empty_page_when_no_tasks()
   {
      var page = sendPreviewRequest(newSorteringsordningSpec(), 50, null);

      assertEquals(0, page.getTotal());
      assertEquals(0, page.getItems().size());
   }

   @Test
   @DisplayName("POST /sorteringsordning/preview — limit begränsar items, total är oförändrad")
   public void should_paginate_with_limit()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = sendPreviewRequest(newSorteringsordningSpec(), 2, null);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("POST /sorteringsordning/preview — offset hoppar över uppgifter, total är oförändrad")
   public void should_paginate_with_offset()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      var page = sendPreviewRequest(newSorteringsordningSpec(), 50, 1);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }
}
