package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.TeamService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

/**
 * Unit tests for {@link se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.TeamApiService#harSidBehorighet}
 * (FKPOC-933), using mocked HTTP responses from the team API.
 */
@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
class TeamApiServiceTest
{
   private static WireMockServer wireMockServer;

   @Inject
   TeamService teamService;

   @BeforeAll
   static void setup()
   {
      wireMockServer = WireMockTestResource.getWireMockServer();
   }

   @BeforeEach
   void resetStubs()
   {
      wireMockServer.resetToDefaultMappings();
   }

   @Test
   void harSidBehorighet_returnsTrue_whenBehorigheterContainsSid()
   {
      var varde = "a1a1a1a1-0000-0000-0000-000000000010";
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + varde + "/behorigheter"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"behorigheter\":[\"SID\"]}")));

      var handlaggare = ImmutableIdtyp.builder().typId(oulHandlaggareTypId).varde(varde).build();

      assertTrue(teamService.harSidBehorighet(handlaggare));
   }

   @Test
   @DisplayName("OUL-FR-04.6: returns false when behörigheter list is empty")
   void harSidBehorighet_returnsFalse_whenBehorigheterIsEmpty()
   {
      var varde = "a1a1a1a1-0000-0000-0000-000000000011";
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + varde + "/behorigheter"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"behorigheter\":[]}")));

      var handlaggare = ImmutableIdtyp.builder().typId(oulHandlaggareTypId).varde(varde).build();

      assertFalse(teamService.harSidBehorighet(handlaggare));
   }

   @Test
   void harSidBehorighet_returnsFalse_whenBehorigheterFieldIsNull()
   {
      var varde = "a1a1a1a1-0000-0000-0000-000000000014";
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + varde + "/behorigheter"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"behorigheter\":null}")));

      var handlaggare = ImmutableIdtyp.builder().typId(oulHandlaggareTypId).varde(varde).build();

      assertFalse(teamService.harSidBehorighet(handlaggare));
   }

   @Test
   @DisplayName("OUL-FR-04.6: returns false when handläggare is not found (404)")
   void harSidBehorighet_returnsFalse_whenHandlaggareNotFound()
   {
      var varde = "a1a1a1a1-0000-0000-0000-000000000012";
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + varde + "/behorigheter"))
            .willReturn(WireMock.aResponse().withStatus(404)));

      var handlaggare = ImmutableIdtyp.builder().typId(oulHandlaggareTypId).varde(varde).build();

      assertFalse(teamService.harSidBehorighet(handlaggare));
   }

   @Test
   void harSidBehorighet_throws_whenTeamApiUnavailable()
   {
      var varde = "a1a1a1a1-0000-0000-0000-000000000013";
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + varde + "/behorigheter"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var handlaggare = ImmutableIdtyp.builder().typId(oulHandlaggareTypId).varde(varde).build();

      // Matches the existing isSameTeam/teamIds pattern: only a 404 is swallowed into a
      // definite answer. A real backend failure propagates rather than being silently
      // treated as "no behörighet" — OUL's top-level exception handling turns this into
      // a proper 500 response instead of an uncontrolled crash.
      assertThrows(WebApplicationException.class, () -> teamService.harSidBehorighet(handlaggare));
   }
}
