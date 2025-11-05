package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;
// package se.fk.github.quarkustemplate.integration;

// import jakarta.enterprise.context.ApplicationScoped;
// import se.fk.github.quarkustemplate.integration.dto.*;
// import se.fk.gradle.examples.jaxrsspec.controllers.generatedsource.model.AlternativesResponse;
// import se.fk.gradle.examples.jaxrsspec.controllers.generatedsource.model.OmbudRequest;
// import se.fk.gradle.examples.jaxrsspec.controllers.generatedsource.model.OmbudResponse;

// import java.util.Collections;

// @ApplicationScoped
// public class IntegrationMapper
// {
//    public IntegrationAlternativesResponse fromExternalApi(AlternativesResponse externalResponse)
//    {
//       if (externalResponse.getAlternatives() == null)
//       {
//          return ImmutableIntegrationAlternativesResponse.builder()
//                .alternatives(Collections.emptyList())
//                .build();
//       }

//       return ImmutableIntegrationAlternativesResponse.builder()
//             .alternatives(
//                   externalResponse.getAlternatives().stream()
//                         .map(alt -> ImmutableIntegrationAlternative.builder()
//                               .name(alt.getName())
//                               .id(alt.getId())
//                               .build())
//                         .toList())
//             .build();
//    }

//    public OmbudRequest toExternalApi(IntegrationOmbudRequest integrationRequest)
//    {
//       OmbudRequest request = new OmbudRequest();
//       request.setPersonnummer(integrationRequest.personnummer());
//       return request;
//    }

//    public IntegrationOmbudResponse fromExternalApi(OmbudResponse externalResponse)
//    {
//       return ImmutableIntegrationOmbudResponse.builder()
//             .name(externalResponse.getName())
//             .build();
//    }
// }
