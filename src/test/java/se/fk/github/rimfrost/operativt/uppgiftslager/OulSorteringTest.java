package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningPage;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newSorteringsordningSpec;

@QuarkusTest
public class OulSorteringTest extends OulTestBase
{
   @Test
   @DisplayName("OUL-FR-09.1, OUL-FR-09.4: Skapa sorteringsordning — svar innehåller genererat id och skapad-tidpunkt")
   public void should_create_sorteringsordning()
   {
      var response = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      assertNotNull(response.getId());
      assertNotNull(response.getSkapad());
      assertEquals(1, response.getEntries().size());
   }

   @Test
   @DisplayName("OUL-FR-09.3: Skapad-tidsstämpel är korrekt och bevaras via DB round-trip")
   public void should_preserve_skapad_timestamp_through_db_round_trip()
   {
      var before = OffsetDateTime.now().minusSeconds(1);
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var after = OffsetDateTime.now().plusSeconds(1);
      var fetched = getSorteringsordning(created.getId());

      assertTrue(created.getSkapad().isAfter(before), "skapad should be after test start");
      assertTrue(created.getSkapad().isBefore(after), "skapad should be before test end");
      assertEquals(created.getSkapad(), fetched.getSkapad());
   }

   @Test
   @DisplayName("OUL-FR-09.2: Skapa sorteringsordning två gånger — första blir default, andra ändrar inte default")
   public void should_keep_first_as_default_on_second_create()
   {
      var first = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      var defaultResponse = getDefaultSorteringsordning();
      assertEquals(first.getId(), defaultResponse.getId());
   }

   @Test
   @DisplayName("OUL-FR-11.1, OUL-FR-11.3: Lista sorteringsordningar — två poster efter två skapelser")
   public void should_list_two_sorteringsordningar_after_two_creates()
   {
      var first = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var second = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      var page = getSorteringsordningar(100);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
      var ids = page.getItems().stream().map(s -> s.getId()).toList();
      assertTrue(ids.contains(first.getId()));
      assertTrue(ids.contains(second.getId()));
   }

   @Test
   @DisplayName("OUL-FR-10.2: Hämta default sorteringsordning — HTTP 404 när ingen finns")
   public void should_return_404_for_default_when_none_exists()
   {
      getDefaultSorteringsordning(404);
   }

   @Test
   @DisplayName("OUL-FR-10.1, OUL-FR-09.3: Hämta default sorteringsordning — returnerar aktiv sorteringsordning efter skapelse")
   public void should_get_default_sorteringsordning_after_create()
   {
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var defaultResponse = getDefaultSorteringsordning();

      assertEquals(created.getId(), defaultResponse.getId());
   }

   @Test
   @DisplayName("OUL-FR-11.1, OUL-FR-11.3: Lista sorteringsordningar — tom lista när ingen finns")
   public void should_list_sorteringsordningar_empty()
   {
      var page = getSorteringsordningar(100);

      assertNotNull(page);
      assertEquals(0, page.getTotal());
      assertTrue(page.getItems().isEmpty());
   }

   @Test
   @DisplayName("OUL-FR-11.1, OUL-FR-11.3: Lista sorteringsordningar — en post efter skapelse")
   public void should_list_one_sorteringsordning_after_create()
   {
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var page = getSorteringsordningar(100);

      assertEquals(1, page.getTotal());
      assertEquals(created.getId(), page.getItems().getFirst().getId());
   }

   @Test
   @DisplayName("OUL-FR-10.3: Hämta sorteringsordning via id — returnerar rätt post")
   public void should_get_sorteringsordning_by_id()
   {
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var fetched = getSorteringsordning(created.getId());

      assertEquals(created.getId(), fetched.getId());
      assertEquals(created.getSkapad(), fetched.getSkapad());
   }

   @Test
   @DisplayName("OUL-FR-10.4: Hämta sorteringsordning via id — HTTP 404 när id inte matchar")
   public void should_return_404_for_unknown_sorteringsordning_id()
   {
      getSorteringsordning(UUID.randomUUID(), 404);
   }

   @Test
   @DisplayName("OUL-FR-13.1: Ta bort sorteringsordning — lyckas med 204 efter att ny default satts")
   public void should_delete_non_default_sorteringsordning()
   {
      var first = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var second = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      setDefaultSorteringsordning(second.getId());

      deleteSorteringsordning(first.getId());

      getSorteringsordning(first.getId(), 404);
   }

   @Test
   @DisplayName("OUL-FR-13.2: Ta bort sorteringsordning — HTTP 409 om det är default")
   public void should_return_409_when_deleting_default_sorteringsordning()
   {
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      deleteSorteringsordning(created.getId(), 409);
   }

   @Test
   @DisplayName("OUL-FR-13.3: Ta bort sorteringsordning — HTTP 404 om id inte finns")
   public void should_return_404_when_deleting_unknown_sorteringsordning()
   {
      deleteSorteringsordning(UUID.randomUUID(), 404);
   }

   @Test
   @DisplayName("OUL-FR-14.1: Ange default — getDefault returnerar ny default")
   public void should_update_default_sorteringsordning()
   {
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var second = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      setDefaultSorteringsordning(second.getId());

      var defaultResponse = getDefaultSorteringsordning();
      assertEquals(second.getId(), defaultResponse.getId());
   }

   @Test
   @DisplayName("OUL-FR-14.2: Ange default — HTTP 404 om id inte finns")
   public void should_return_404_when_setting_unknown_default()
   {
      setDefaultSorteringsordning(UUID.randomUUID(), 404);
   }

   @Test
   @DisplayName("OUL-FR-12.1: Preview sorteringsordning — returnerar uppgifter utan att persistera spec")
   public void should_preview_sorteringsordning_without_persisting()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      UppgiftPage page = sendPreviewRequest(newSorteringsordningSpec(), 10, null);

      assertEquals(2, page.getTotal());
      assertEquals(2, page.getItems().size());
      assertEquals(0, getSorteringsordningar(10).getTotal());
   }

   @Test
   @DisplayName("OUL-FR-12.2: Preview sorteringsordning — limit begränsar returnerade items men total är oförändrad")
   public void should_preview_sorteringsordning_with_limit()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      UppgiftPage page = sendPreviewRequest(newSorteringsordningSpec(), 2, null);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-12.3: Preview sorteringsordning — offset hoppar över uppgifter men total är oförändrad")
   public void should_preview_sorteringsordning_with_offset()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      UppgiftPage page = sendPreviewRequest(newSorteringsordningSpec(), 10, 1);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-11.2: Lista sorteringsordningar — limit begränsar returnerade items men total är oförändrad")
   public void should_list_sorteringsordningar_with_limit()
   {
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      SorteringsordningPage page = getSorteringsordningar(2);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-11.2: Lista sorteringsordningar — offset hoppar över poster men total är oförändrad")
   public void should_list_sorteringsordningar_with_offset()
   {
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      SorteringsordningPage page = getSorteringsordningar(100, 1);

      assertEquals(3, page.getTotal());
      assertEquals(2, page.getItems().size());
   }

   @Test
   @DisplayName("OUL-FR-11.2: Lista sorteringsordningar — saknat limit-param ger HTTP 400")
   public void should_return_400_when_limit_is_missing()
   {
      getSorteringsordningar(null, 400);
   }

   @Test
   @DisplayName("OUL-FR-11.2: Lista sorteringsordningar — limit=0 ger HTTP 400")
   public void should_return_400_when_limit_is_zero()
   {
      getSorteringsordningar(0, 400);
   }

   @Test
   @DisplayName("OUL-FR-09.4, OUL-FR-09.5: Entries med sort_by överlever DB round-trip")
   public void should_preserve_entries_with_sort_by_after_round_trip()
   {
      var sortBy = new SortBy();
      sortBy.setField(SorteringsordningField.SKAPAD);
      sortBy.setDirection(SortBy.DirectionEnum.ASC);
      var entry = new SorteringsordningEntry();
      entry.setSortBy(sortBy);
      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));

      var created = sendCreateSorteringsordningRequest(spec);
      var fetched = getSorteringsordning(created.getId());

      assertEquals(1, fetched.getEntries().size());
      var fetchedEntry = fetched.getEntries().getFirst();
      assertNotNull(fetchedEntry.getSortBy());
      assertEquals(SorteringsordningField.SKAPAD, fetchedEntry.getSortBy().getField());
      assertEquals(SortBy.DirectionEnum.ASC, fetchedEntry.getSortBy().getDirection());
   }
}
