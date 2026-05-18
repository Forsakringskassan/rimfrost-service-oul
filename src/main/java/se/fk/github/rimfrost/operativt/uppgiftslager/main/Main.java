package se.fk.github.rimfrost.operativt.uppgiftslager.main;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.vertx.http.runtime.VertxHttpRecorder;
import io.vertx.core.http.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class Main implements QuarkusApplication
{
   Logger logger = LoggerFactory.getLogger(Main.class);

   @ConfigProperty(name = "rimfrost.oul.management.api.host", defaultValue = "0.0.0.0")
   String managementApiHost;

   @ConfigProperty(name = "rimfrost.oul.management.api.port")
   int managementApiPort;

   public static void main(String... args)
   {
      Quarkus.run(Main.class, args);
   }

   @Override
   public int run(String... args) throws Exception
   {
      logger.info("Starting up main");

      Vertx vertx = Vertx.vertx();

      final HttpServerOptions options = new HttpServerOptions();
      options.setHost(managementApiHost);
      options.setPort(managementApiPort);

      logger.info("Starting up HTTP server on port 8082");

      HttpServer server = vertx.createHttpServer(options)
            .requestHandler(VertxHttpRecorder.getRootHandler())
            .listen((result) -> {
               if (result.succeeded())
               {
                  logger.info("Server started on: http://{}:{}", options.getHost(), options.getPort());
               }
               else
               {
                  logger.error("Failed to start server", result.cause());
               }
            });

      logger.info("Waiting for server to finish");
      Quarkus.waitForExit();

      logger.info("Port 8082 server shut down");
      server.close();

      return 0;
   }
}
