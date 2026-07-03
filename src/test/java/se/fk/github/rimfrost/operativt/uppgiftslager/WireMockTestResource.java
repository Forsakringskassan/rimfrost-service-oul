package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.*;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SuppressFBWarnings(value =
{
      "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "MS_EXPOSE_REP"
})
public class WireMockTestResource implements QuarkusTestResourceLifecycleManager
{
   private static WireMockServer server;

   private static final ObjectMapper mapper = new ObjectMapper()
         .registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

   public static WireMockServer getWireMockServer()
   {
      return server;
   }

   @Override
   public Map<String, String> start()
   {
      server = new WireMockServer(options().dynamicPort().usingFilesUnderDirectory("src/test/resources"));
      server.start();
      return wiremockMapping(server);
   }

   @Override
   public void stop()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   protected Map<String, String> wiremockMapping(WireMockServer server)
   {
      Map<String, String> map = new HashMap<>();
      map.put("handlaggning.api.base-url", server.baseUrl());
      map.put("sid.api.base-url", server.baseUrl());
      return map;
   }
}
