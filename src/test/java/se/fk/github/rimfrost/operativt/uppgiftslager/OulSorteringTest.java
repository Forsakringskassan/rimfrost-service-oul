package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
   @DisplayName("OUL-FR-09.2, OUL-FR-09.4: Skapa sorteringsordning två gånger — andra skapelsen ersätter den första och får nytt id")
   public void should_replace_sorteringsordning_on_second_post()
   {
      var first = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var second = sendCreateSorteringsordningRequest(newSorteringsordningSpec());

      assertNotEquals(first.getId(), second.getId());
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
   @DisplayName("OUL-FR-11.1, OUL-FR-11.2: Lista sorteringsordningar — tom lista när ingen finns")
   public void should_list_sorteringsordningar_empty()
   {
      var list = getSorteringsordningar();

      assertNotNull(list);
      assertEquals(0, list.size());
   }

   @Test
   @DisplayName("OUL-FR-11.2, OUL-FR-09.3: Lista sorteringsordningar — en post efter skapelse")
   public void should_list_one_sorteringsordning_after_create()
   {
      var created = sendCreateSorteringsordningRequest(newSorteringsordningSpec());
      var list = getSorteringsordningar();

      assertEquals(1, list.size());
      assertEquals(created.getId(), list.getFirst().getId());
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
}
