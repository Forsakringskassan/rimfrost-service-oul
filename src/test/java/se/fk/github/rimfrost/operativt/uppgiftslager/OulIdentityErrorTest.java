package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class OulIdentityErrorTest extends OulTestBase
{
   private static WireMockServer wireMockServer;

   @BeforeAll
   static void setup()
   {
      wireMockServer = WireMockTestResource.getWireMockServer();
   }

   @Test
   void should_return_401_on_invalid_authorization_header()
   {
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/identity"))
            .willReturn(WireMock.aResponse().withStatus(401)));
      assignTaskToHandlaggare(UUID.randomUUID(), 401);
   }

   @Test
   void should_return_500_on_other_identity_service_error()
   {
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/identity"))
            .willReturn(WireMock.aResponse().withStatus(503)));
      assignTaskToHandlaggare(UUID.randomUUID(), 500);
   }
}
