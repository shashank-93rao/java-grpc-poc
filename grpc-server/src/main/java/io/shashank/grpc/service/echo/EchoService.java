package io.shashank.grpc.service.echo;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.shashank.grpc.service.echo.EchoServiceGrpc.EchoServiceImplBase;
import java.util.ArrayList;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoService extends EchoServiceImplBase {

  private final Random r = new Random();

  @Override
  public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
//    super.hello(request, responseObserver); // Don't call this. Will give you not implemented exception
    log.info("got echo request {}", request.getMessage());
    EchoResponse response = EchoResponse.newBuilder()
        .setMessage(request.getMessage()).build();
    responseObserver.onNext(response);
    log.info("responded to {}", request.getMessage());
    responseObserver.onCompleted();
  }

  @Override
  public void serverStreamingEcho(EchoRequest request,
      StreamObserver<EchoResponse> responseObserver) {
    int totalResponse = r.nextInt(10);
    for (int i = 0; i < totalResponse; i++) {
      responseObserver.onNext(
          EchoResponse.newBuilder().setMessage(request.getMessage() + " " + i).build());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<EchoRequest> clientStreamingEcho(
      StreamObserver<EchoResponse> responseObserver) {
    ArrayList<String> messages = new ArrayList<>();
    return new StreamObserver<>() {
      @Override
      public void onNext(EchoRequest echoRequest) {
        log.info("Received streaming echo request {}", echoRequest.getMessage());
        messages.add(echoRequest.getMessage());
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("error", throwable);
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(
            EchoResponse.newBuilder().setMessage(String.join("\t", messages)).build());
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public StreamObserver<EchoRequest> bidirectionalEcho(
      StreamObserver<EchoResponse> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(EchoRequest echoRequest) {
        log.info("Received streaming echo request {}", echoRequest.getMessage());
        responseObserver.onNext(
            EchoResponse.newBuilder().setMessage(echoRequest.getMessage()).build());
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("error", throwable);
      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public void cancellableEcho(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
    log.info(
        "Received an echo request. Waiting for sometime and checking if the request is going to be cancelled");
    Context context = Context.current();
    try {
      for (int i = 0; i < 10; i++) {
        Thread.sleep(300);
        if (context.isCancelled()) {
          log.info("I'm not responding to this request");
          responseObserver.onError(Status.CANCELLED.asException());
          return;
        }
      }
    } catch (InterruptedException e) {

    }

    responseObserver.onNext(EchoResponse.newBuilder().setMessage(request.getMessage()).build());
    log.info("Responded to echo request");


  }
}
