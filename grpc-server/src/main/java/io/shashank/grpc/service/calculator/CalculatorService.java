package io.shashank.grpc.service.calculator;

import io.grpc.stub.StreamObserver;
import io.shashank.grpc.service.calculator.CalculatorServiceGrpc.CalculatorServiceImplBase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculatorService extends CalculatorServiceImplBase {

  @Override
  public StreamObserver<Request> calculate(StreamObserver<Response> responseObserver) {
    return new StreamObserver<>() {
      boolean init = false;
      double result = 0;

      @Override
      public void onNext(Request request) {
        log.info("Got calculator request {} {}", request.getOp(), request.getNum());
        if (request.getNum() == 0 && request.getOp() == Op.D) {
          responseObserver.onError(new ArithmeticException("Divide by 0"));
          return;
        }
        if (!init) {
          init = true;
          if (request.getOp() == Op.M) {
            result = 1;
          }
        }
        switch (request.getOp()) {
          case A:
            result += request.getNum();
            break;
          case S:
            result -= request.getNum();
            break;
          case M:
            result *= request.getNum();
            break;
          case D:
            result /= request.getNum();
            break;
        }
        log.info("Current result: {}", result);
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("Got error from client", throwable);
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(Response.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
      }
    };
  }
}
