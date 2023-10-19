package io.shashank.grpc.service.hello;

import io.grpc.stub.StreamObserver;
import io.shashank.grpc.service.hello.HelloServiceGrpc.HelloServiceImplBase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloService extends HelloServiceImplBase {

  @Override
  public void hello(Hello request, StreamObserver<HelloResponse> responseObserver) {
//    super.hello(request, responseObserver); // Don't call this. Will give you not implemented exception
    log.info("got hello request from {}", request.getName());
    HelloResponse response = HelloResponse.newBuilder()
        .setGreeting(String.format("Hello %s", request.getName())).build();
    responseObserver.onNext(response);
    log.info("responded to {}", request.getName());
    responseObserver.onCompleted();
  }
}
