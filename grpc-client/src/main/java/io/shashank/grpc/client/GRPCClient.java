package io.shashank.grpc.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.shashank.grpc.service.hello.Hello;
import io.shashank.grpc.service.hello.HelloResponse;
import io.shashank.grpc.service.hello.HelloServiceGrpc;
import io.shashank.grpc.service.hello.HelloServiceGrpc.HelloServiceBlockingStub;
import io.shashank.grpc.service.hello.HelloServiceGrpc.HelloServiceFutureStub;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCClient {

  public static void main(String[] args) throws InterruptedException {
    helloSync();
    helloAsync();
    Thread.sleep(99999999999999999L);
  }

  private static void helloSync() {
    log.info("Sending a hello request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
    HelloServiceBlockingStub helloService = HelloServiceGrpc.newBlockingStub(channel).withde;
    HelloResponse response = helloService.hello(
        Hello.newBuilder().setName("shashank").build());
    log.info("got response: "+ response.getGreeting());
    channel.shutdown();
  }

  private static void helloAsync() {
    log.info("Sending a hello async request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
    HelloServiceFutureStub helloService = HelloServiceGrpc.newFutureStub(channel);
    ListenableFuture<HelloResponse> response = helloService.hello(
        Hello.newBuilder().setName("shashank").build());
    Futures.addCallback(response, new FutureCallback<>() {
      @Override
      public void onSuccess(HelloResponse result) {
        log.info("got async response: "+ result.getGreeting());
      }

      @Override
      public void onFailure(Throwable t) {
        log.error("Gor error", t);
      }
    }, Executors.newSingleThreadExecutor());
    channel.shutdown();
  }
}
