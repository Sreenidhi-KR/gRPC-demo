package demo.server;


import com.proto.square.SquareRequest;
import com.proto.square.SquareResponse;
import com.proto.square.SquareServiceGrpc;
import io.grpc.stub.StreamObserver;

public class SquareServerImpl extends SquareServiceGrpc.SquareServiceImplBase {

    @Override
    public void square(SquareRequest request, StreamObserver<SquareResponse> responseObserver) {
        responseObserver.onNext(SquareResponse.newBuilder()
                .setNumber(request.getNumber())
                .setNumberSquare(request.getNumber() * request.getNumber())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<SquareRequest> squareBiDirectionStream(StreamObserver<SquareResponse> responseObserver) {
        return new StreamObserver<SquareRequest>() {
            @Override
            public void onNext(SquareRequest request) {
                responseObserver.onNext(SquareResponse.newBuilder()
                        .setNumber(request.getNumber())
                        .setNumberSquare(request.getNumber() * request.getNumber())
                        .build());
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
