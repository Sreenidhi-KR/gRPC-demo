package demo.server;

import com.proto.demo.DemoRequest;
import com.proto.demo.DemoResponse;
import com.proto.demo.DemoServiceGrpc;
import io.grpc.stub.StreamObserver;

public class DemoServerImpl extends DemoServiceGrpc.DemoServiceImplBase {

    @Override
    public void demo(DemoRequest request, StreamObserver<DemoResponse> responseObserver) {
        responseObserver.onNext(DemoResponse.newBuilder().setRes("Hello " + request.getName()).build());
        responseObserver.onCompleted();
    }


    @Override
    public void demoServerStream(DemoRequest request, StreamObserver<DemoResponse> responseObserver) {

        for(int i =0 ; i<10 ;i++){
            DemoResponse response = DemoResponse.newBuilder().setRes(i + " Hello " + request.getName()).build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DemoRequest> demoClientStream(StreamObserver<DemoResponse> responseObserver) {
            StringBuilder sb = new StringBuilder();

            return new StreamObserver<DemoRequest>() {
                @Override
                public void onNext(DemoRequest request) {
                    sb.append("Hello ").append(request.getName()).append("!\n");
                }

                @Override
                public void onError(Throwable throwable) {
                    responseObserver.onError(throwable);

                }

                @Override
                public void onCompleted() {
                    //calls response stream's onNext and onCompleted
                    responseObserver.onNext(DemoResponse.newBuilder().setRes(sb.toString()).build());
                    responseObserver.onCompleted();
                }
            };
    }

    @Override
    public StreamObserver<DemoRequest> demoBiDirectionStream(StreamObserver<DemoResponse> responseObserver) {
        return new StreamObserver<DemoRequest>() {
            @Override
            public void onNext(DemoRequest request) {
                responseObserver.onNext(DemoResponse.newBuilder().setRes("hello" + request.getName()).build());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
