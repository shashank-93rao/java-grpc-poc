package io.shashank.grpc.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Context;
import io.grpc.Context.CancellableContext;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.shashank.grpc.service.echo.EchoRequest;
import io.shashank.grpc.service.echo.EchoResponse;
import io.shashank.grpc.service.echo.EchoServiceGrpc;
import io.shashank.grpc.service.echo.EchoServiceGrpc.EchoServiceBlockingStub;
import io.shashank.grpc.service.echo.EchoServiceGrpc.EchoServiceFutureStub;
import io.shashank.grpc.service.echo.EchoServiceGrpc.EchoServiceStub;
import java.util.Iterator;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCClient {

  public static void main(String[] args) {
    try {
//      echoSync();
//      echoAsync();
//      echoClientStreaming();
//      echoServerStreaming();
//      echoBidirectionalStreaming();
      echoSyncWithCancel();
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void echoClientStreaming() throws InterruptedException {
    log.info("Sending a streaming echo request. ");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceStub service = EchoServiceGrpc.newStub(channel);
    StreamObserver<EchoRequest> requestObserver = service.clientStreamingEcho(
        new StreamObserver<>() {
          @Override
          public void onNext(EchoResponse echoResponse) {
            log.info("Got response from server for streaming request {}",
                echoResponse.getMessage());
          }

          @Override
          public void onError(Throwable throwable) {
            log.info("Got error from server for streaming request", throwable);
          }

          @Override
          public void onCompleted() {
            log.info("Completed");
          }
        });

    for (int i = 0; i < 10; i++) {
      log.info("Sending streaming write");
      requestObserver.onNext(
          EchoRequest.newBuilder().setMessage(i + "").build());
      Thread.sleep(1000);
    }
    requestObserver.onCompleted();
    Thread.sleep(10000);
    channel.shutdownNow();
  }

  private static void echoBidirectionalStreaming() throws InterruptedException {
    log.info("Sending a bi-directional streaming echo request. ");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceStub service = EchoServiceGrpc.newStub(channel);
    StreamObserver<EchoRequest> requestObserver = service.bidirectionalEcho(
        new StreamObserver<>() {
          @Override
          public void onNext(EchoResponse echoResponse) {
            log.info("Got response from server for bi-directional streaming request {}",
                echoResponse.getMessage());
          }

          @Override
          public void onError(Throwable throwable) {
            log.info("Got error from server for bi-directional streaming request", throwable);
          }

          @Override
          public void onCompleted() {
            log.info("Completed");
          }
        });

    for (int i = 0; i < 10; i++) {
      log.info("Sending bi-directional streaming write");
      requestObserver.onNext(
          EchoRequest.newBuilder().setMessage(i + "").build());
      Thread.sleep(1000);
    }
    requestObserver.onCompleted();
    Thread.sleep(100000);
    channel.shutdownNow();
  }


  private static void echoServerStreaming() {
    log.info("Sending an echo request. Expecting streaming response");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceBlockingStub service = EchoServiceGrpc.newBlockingStub(channel);
    Iterator<EchoResponse> responses = service.serverStreamingEcho(
        EchoRequest.newBuilder().setMessage("hello").build());
    log.info("received responses printed below");
    while (responses.hasNext()) {
      log.info(responses.next().getMessage());
    }
    channel.shutdownNow();
  }

  private static void echoSyncWithCancel() throws InterruptedException {
    log.info("Sending an echo request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceBlockingStub echoService = EchoServiceGrpc.newBlockingStub(channel);
    try (CancellableContext context = Context.current().withCancellation()) {
      new Thread(() -> {
        try {
          Thread.sleep(2000);
          context.cancel(new RuntimeException("Cancelling for demonstration"));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }).start();
      context.run(() -> {
        EchoResponse response = echoService.cancellableEcho(
            EchoRequest.newBuilder().setMessage("hello").build());
        log.info("got response: " + response.getMessage());
      });
    }
    Thread.sleep(10000);
    channel.shutdownNow();
  }

  private static void echoSync() {
    log.info("Sending an echo request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceBlockingStub echoService = EchoServiceGrpc.newBlockingStub(channel);
    EchoResponse response = echoService.echo(EchoRequest.newBuilder().setMessage("hello").build());
    log.info("got response: " + response.getMessage());
    channel.shutdownNow();
  }

  private static void echoAsync() throws InterruptedException {
    log.info("Sending an async echo request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    EchoServiceFutureStub echoService = EchoServiceGrpc.newFutureStub(channel);
    ListenableFuture<EchoResponse> response = echoService.echo(
        EchoRequest.newBuilder().setMessage("hello").build());
    Futures.addCallback(response, new FutureCallback<>() {
      @Override
      public void onSuccess(EchoResponse result) {
        log.info("got async response: " + result.getMessage());
      }

      @Override
      public void onFailure(Throwable t) {
        log.error("Gor error", t);
      }
    }, Executors.newSingleThreadExecutor());
    log.info("I'm not waiting for the response");
    Thread.sleep(10000);
    channel.shutdownNow();
  }
}
