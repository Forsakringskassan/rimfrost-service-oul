package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgifterIndividRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class OulIndividTest extends OulTestBase
{
   private static WireMockServer wireMockServer;

   /** Identity type used for the individ searched in all tests. */
   private static final String INDIVID_TYP_ID = "test-individ-typ-id";

   /** Identity value used for the individ searched in all tests. */
   private static final String INDIVID_VARDE = "test-individ-varde";

   /** The yrkanderoll that the stubbed handläggning returns for the test individ. */
   private static final String YRKANDE_ROLL_ID = "SÖKANDE";

   @BeforeAll
   static void setup()
   {
      wireMockServer = WireMockTestResource.getWireMockServer();
   }

   @Test
   @DisplayName("OUL-FR-21.4: Returns 501 when assignable not specified")
   public void should_return_501_when_assignable_not_specified()
   {
      var handlaggareId = UUID.randomUUID();
      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);

      postUppgifterIndivid(request, handlaggareId, 501);
   }

   @Test
   @DisplayName("OUL-FR-21.4: Returns 501 when assignable=true")
   public void should_return_501_when_assignable_true()
   {
      var handlaggareId = UUID.randomUUID();
      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(true);

      postUppgifterIndivid(request, handlaggareId, 501);
   }

   @Test
   @DisplayName("OUL-FR-21.3, OUL-FR-21.5: Returns matched uppgift with yrkanderoll when individ found in rollerIYrkande")
   public void should_return_uppgift_with_yrkanderoll_when_individ_found()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      var createResponse = sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(1, response.getOperativaUppgifter().size());
      var uppgift = response.getOperativaUppgifter().getFirst();
      assertEquals(createResponse.getUppgiftId(), uppgift.getUppgiftId());
      assertEquals(createResponse.getHandlaggningId(), uppgift.getHandlaggningId());
      assertEquals(YRKANDE_ROLL_ID, uppgift.getYrkandeRoll());
   }

   @Test
   @DisplayName("OUL-FR-21.5: Returns empty list when rollerIYrkande contains roles but none match the searched individ")
   public void should_return_empty_when_individ_not_in_rollerIYrkande()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      // Handläggning has a role, but for a different individ — not the one being searched
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId, "other-typ-id", "other-varde"))));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(0, response.getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("OUL-FR-21.6: Excludes SID-märkta uppgift when caller lacks SID-behörighet")
   public void should_exclude_sid_uppgift_when_caller_lacks_sid_behorighet()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));
      wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"sid\":true}")));
      // No hasSidPermission stub for caller — SidBehorighetCheckerService fails open → false

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(0, response.getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("OUL-FR-21.6: Includes SID-märkta uppgift when caller has SID-behörighet")
   public void should_include_sid_uppgift_when_caller_has_sid_behorighet()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));
      wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"sid\":true}")));
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/" + oulHandlaggareTypId + "/" + handlaggareId + "/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("true")));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      var createResponse = sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(1, response.getOperativaUppgifter().size());
      assertEquals(createResponse.getUppgiftId(), response.getOperativaUppgifter().getFirst().getUppgiftId());
   }

   @Test
   @DisplayName("OUL-FR-21.5: Statuses filter — excludes uppgift with non-matching status")
   public void should_exclude_uppgift_with_non_matching_status()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));

      // Uppgift is created with status NY; filtering by TILLDELAD should exclude it
      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);
      request.addStatusesItem(PostUppgifterIndividRequest.StatusesEnum.TILLDELAD);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(0, response.getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("OUL-FR-21.5: Statuses filter — includes uppgift with matching status")
   public void should_include_uppgift_with_matching_status()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      var createResponse = sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);
      request.addStatusesItem(PostUppgifterIndividRequest.StatusesEnum.NY);

      var response = postUppgifterIndivid(request, handlaggareId);

      assertEquals(1, response.getOperativaUppgifter().size());
      assertEquals(createResponse.getUppgiftId(), response.getOperativaUppgifter().getFirst().getUppgiftId());
   }

   @Test
   @DisplayName("OUL-FR-21.5: Returns 500 when handlaggning cannot be read during individ matching")
   public void should_return_500_when_handlaggning_read_fails_during_individ_matching()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      postUppgifterIndivid(request, handlaggareId, 500);
   }

   @Test
   @DisplayName("OUL-FR-21.7: Returns 500 when SID-status cannot be determined")
   public void should_return_500_when_sid_status_unavailable()
   {
      var handlaggareId = UUID.randomUUID();
      var handlaggningId = UUID.randomUUID();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/handlaggning/" + handlaggningId))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(handlaggningWithIndivid(handlaggningId))));
      wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var createRequest = OulTestData.newCreateUppgiftRequest(handlaggningId);
      createRequest.setAssignable(false);
      sendCreateUppgiftRequest(createRequest);

      var request = new PostUppgifterIndividRequest(INDIVID_TYP_ID, INDIVID_VARDE);
      request.setAssignable(false);

      postUppgifterIndivid(request, handlaggareId, 500);
   }

   /**
    * Builds a WireMock response body for a handläggning whose single role contains
    * {@link #INDIVID_TYP_ID} / {@link #INDIVID_VARDE} as a {@link #YRKANDE_ROLL_ID} roll.
    *
    * @param handlaggningId the handläggning ID to embed in the response
    * @return JSON string suitable for a WireMock stub body
    */
   private String handlaggningWithIndivid(UUID handlaggningId)
   {
      return handlaggningWithIndivid(handlaggningId, INDIVID_TYP_ID, INDIVID_VARDE);
   }

   /**
    * Builds a WireMock response body for a handläggning whose single role contains the given individ identity.
    *
    * @param handlaggningId the handläggning ID to embed in the response
    * @param individTypId   the individ type ID for the role
    * @param individVarde   the individ value for the role
    * @return JSON string suitable for a WireMock stub body
    */
   private String handlaggningWithIndivid(UUID handlaggningId, String individTypId, String individVarde)
   {
      return """
            {
              "handlaggning": {
                "id": "%s",
                "version": 1,
                "handlaggningIdTyp": "test-handlaggning-id-typ",
                "yrkande": {
                  "id": "81234d56-7890-4abc-1234-56789abcdef0",
                  "version": 1,
                  "ingangtypId": "test-ingangtyp-id",
                  "yrkandedatum": "2026-01-22T13:30:11.155427+01:00",
                  "yrkandestatus": "YRKAT",
                  "yrkandeFrom": "2025-01-10T16:15:50Z",
                  "yrkandeTom": "2025-01-10T16:15:50Z",
                  "avsikt": "NY",
                  "rollerIYrkande": [
                    {
                      "id": "00000000-0000-0000-0000-000000000001",
                      "individ": {
                        "typId": "%s",
                        "varde": "%s"
                      },
                      "yrkandeRollId": "%s",
                      "avserYrkande": "81234d56-7890-4abc-1234-56789abcdef0"
                    }
                  ],
                  "sakfragorStallningstaganden": [],
                  "beslut": []
                },
                "handlaggningspecifikationId": "71234567-89ab-4cde-9012-3456789abcde",
                "skapadTS": "2026-01-22T13:31:00.874268300+01:00",
                "avslutadTS": null
              }
            }""".formatted(handlaggningId, individTypId, individVarde, YRKANDE_ROLL_ID);
   }
}
