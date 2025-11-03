package tests;

import common.TestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import se.fk.github.quarkustemplate.integration.IntegrationService;
import se.fk.github.quarkustemplate.integration.dto.ImmutableIntegrationOmbudRequest;
import se.fk.github.quarkustemplate.integration.dto.IntegrationAlternativesResponse;
import se.fk.github.quarkustemplate.integration.dto.IntegrationOmbudRequest;
import se.fk.github.quarkustemplate.integration.dto.IntegrationOmbudResponse;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class IntegrationTest
{

   @Inject
   IntegrationService fullmaktsIntegration;

   @Test
   void testGetAlternatives()
   {
      IntegrationAlternativesResponse response = fullmaktsIntegration.getAlternatives();

      assertThat(response).isNotNull();
      assertThat(response.alternatives()).hasSize(2);
      assertThat(response.alternatives().get(0).uppgiftId()).isEqualTo("1");
      assertThat(response.alternatives().get(0).name()).isEqualTo("Alt 1");
      assertThat(response.alternatives().get(1).uppgiftId()).isEqualTo("2");
      assertThat(response.alternatives().get(1).name()).isEqualTo("Alt 2");
   }

   @Test
   void testCheckOmbud()
   {
      IntegrationOmbudRequest request = ImmutableIntegrationOmbudRequest.builder()
            .personnummer(TestDataFactory.PNR1)
            .build();

      IntegrationOmbudResponse response = fullmaktsIntegration.checkOmbud(request);

      assertThat(response).isNotNull();
      assertThat(response.name()).isEqualTo("Test Testsson");
   }
}
