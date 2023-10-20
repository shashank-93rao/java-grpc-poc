package io.shashank.grpc.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.shashank.grpc.service.calculator.CalculatorServiceGrpc;
import io.shashank.grpc.service.calculator.CalculatorServiceGrpc.CalculatorServiceStub;
import io.shashank.grpc.service.calculator.Op;
import io.shashank.grpc.service.calculator.Request;
import io.shashank.grpc.service.calculator.Response;
import io.shashank.grpc.service.hello.Hello;
import io.shashank.grpc.service.hello.HelloResponse;
import io.shashank.grpc.service.hello.HelloServiceGrpc;
import io.shashank.grpc.service.hello.HelloServiceGrpc.HelloServiceBlockingStub;
import io.shashank.grpc.service.hello.HelloServiceGrpc.HelloServiceFutureStub;
import java.util.Random;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCClient {

  public static void main(String[] args) throws InterruptedException {
    streamingClientCalculator();
//    helloSync();
//    helloAsync();
    Thread.sleep(99999999999999999L);
  }

  private static void streamingClientCalculator() {
    log.info("Sending stream of calculator requests");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    CalculatorServiceStub service = CalculatorServiceGrpc.newStub(channel);
    StreamObserver<Request> request = service.calculate(
        new StreamObserver<>() {
          @Override
          public void onNext(Response response) {
            log.info("Calculated number: {}", response.getResult());
          }

          @Override
          public void onError(Throwable throwable) {
            log.error("Server threw an error", throwable);
            channel.shutdown();
          }

          @Override
          public void onCompleted() {
            channel.shutdown();
          }
        });

    Op[] ops = new Op[]{Op.A, Op.S, Op.M, Op.D};
    Random randomizer = new Random();
    for (int i = 0; i < 10; i++) {
      Op op = ops[randomizer.nextInt(ops.length)];
      double num = randomizer.nextInt() * 1.0d;
      log.info("{} {}", op, num);
      request.onNext(Request.newBuilder().setOp(op).setNum(num).build());
    }
    log.info("All calculation requests complete");
    request.onCompleted();
  }

  private static void helloSync() {
    log.info("Sending a hello request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    HelloServiceBlockingStub helloService = HelloServiceGrpc.newBlockingStub(channel);
    HelloResponse response = helloService.hello(
        Hello.newBuilder().setName("shashank").build());
    log.info("got response: " + response.getGreeting());
    channel.shutdown();
  }

  private static void helloAsync() {
    log.info("Sending a hello async request");
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
        .build();
    HelloServiceFutureStub helloService = HelloServiceGrpc.newFutureStub(channel);
    ListenableFuture<HelloResponse> response = helloService.hello(
        Hello.newBuilder().setName("shashank").build());
    Futures.addCallback(response, new FutureCallback<>() {
      @Override
      public void onSuccess(HelloResponse result) {
        log.info("got async response: " + result.getGreeting());
      }

      @Override
      public void onFailure(Throwable t) {
        log.error("Gor error", t);
      }
    }, Executors.newSingleThreadExecutor());
    channel.shutdown();
  }
}
