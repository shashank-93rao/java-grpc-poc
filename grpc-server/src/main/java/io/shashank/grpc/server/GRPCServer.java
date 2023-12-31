package io.shashank.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.shashank.grpc.service.echo.EchoService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    log.info("Starting GRPC Server!");
    Server server = ServerBuilder.forPort(8080).addService(new EchoService())
        .build();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("GRPC Server going down")));
    server.start();
    log.info("Started");
    server.awaitTermination();
  }
}
