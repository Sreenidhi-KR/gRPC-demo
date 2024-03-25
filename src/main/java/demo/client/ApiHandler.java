package demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proto.demo.DemoRequest;
import com.proto.demo.DemoResponse;
import com.proto.demo.DemoServiceGrpc;
import com.proto.square.SquareRequest;
import com.proto.square.SquareResponse;
import com.proto.square.SquareServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ApiHandler {

    private static void callDemoAPI(ManagedChannel channel){
        System.out.println("Calling demo API Stub");
        DemoServiceGrpc.DemoServiceBlockingStub stub = DemoServiceGrpc.newBlockingStub(channel);
        DemoResponse response = stub.demo(DemoRequest.newBuilder().setName("John").build());
        System.out.println("Response " + response.getRes());
    }



    private static void callDemoServerStreamAPI(ManagedChannel channel){

        System.out.println("Calling server stream API Stub");
        DemoServiceGrpc.DemoServiceBlockingStub stub = DemoServiceGrpc.newBlockingStub(channel);
        stub.demoServerStream(DemoRequest.newBuilder().setName("John").build()).forEachRemaining((response) ->{
            System.out.println("Response " + response.getRes());
        });
    }

    private static void callDemoClientStreamAPI(ManagedChannel channel) throws InterruptedException {

        System.out.println("Calling client stream API Stub");
        DemoServiceGrpc.DemoServiceStub stub = DemoServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<DemoResponse> resStream = new StreamObserver<DemoResponse>() {
            @Override
            public void onNext(DemoResponse response) {
                System.out.println(response.getRes());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        //get back a stream from server to use
        StreamObserver<DemoRequest> reqStream = stub.demoClientStream(resStream);

        //call the api on server using request stream
        Arrays.asList("Clement", "Marie", "Test").forEach(name ->

                reqStream.onNext(DemoRequest.newBuilder().setName(name).build())
        );

        //will call resStream.onNext and resStream.onCompleted
        reqStream.onCompleted();

        //noinspection ResultOfMethodCallIgnored
        latch.await(3, TimeUnit.SECONDS);


    }

    private static void callDemoBiDirectionStreamAPI(ManagedChannel channel) throws InterruptedException {

        DemoServiceGrpc.DemoServiceStub stub = DemoServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<DemoResponse> resStream = new StreamObserver<DemoResponse>() {
            @Override
            public void onNext(DemoResponse response) {
                System.out.println(response.getRes());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error" + t);
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<DemoRequest> reqStream = stub.demoBiDirectionStream(resStream);

        Arrays.asList("Clement", "Marie", "Test").forEach(name ->
                reqStream.onNext(DemoRequest.newBuilder().setName(name).build())
        );


        //will call resStream.onNext and resStream.onCompleted
        reqStream.onCompleted();

        //noinspection ResultOfMethodCallIgnored
        latch.await(3, TimeUnit.SECONDS);


    }

    private static Map<Integer, Integer> callSquareAPI(ManagedChannel channel , int number){

        Map<Integer, Integer> result = new HashMap<>();

        SquareServiceGrpc.SquareServiceBlockingStub stub = SquareServiceGrpc.newBlockingStub(channel);

        for(int i = 1 ; i<= number ;i++){

            SquareResponse response = stub.square(SquareRequest.newBuilder().setNumber(i).build());
            //System.out.println(response.getNumberSquare());
            result.put(response.getNumber() , response.getNumberSquare());

        }


        return result;


    }

    private static Map<Integer, Integer> callSquareBiDirectionStreamAPI(ManagedChannel channel , int number) throws InterruptedException {


        SquareServiceGrpc.SquareServiceStub stub = SquareServiceGrpc.newStub(channel);

        Map<Integer, Integer> result = new HashMap<>();

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<SquareResponse> resStream = new StreamObserver<SquareResponse>() {
            @Override
            public void onNext(SquareResponse response) {
                result.put(response.getNumber() , response.getNumberSquare());
//                System.out.println(response.getNumberSquare());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error" + t);
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<SquareRequest> reqStream = stub.squareBiDirectionStream(resStream);


        IntStream.rangeClosed(1, number).forEach(i ->
                reqStream.onNext(SquareRequest.newBuilder().setNumber(i).build())
        );


        //will call resStream.onNext and resStream.onCompleted
        reqStream.onCompleted();

        //noinspection ResultOfMethodCallIgnored
        latch.await(10, TimeUnit.SECONDS);

        return result;


    }

    public static Map<Integer, Integer> getSquare(int number ) throws InterruptedException {

        int port = 50052;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost" , port).usePlaintext().build();

        Map<Integer, Integer>  res =  callSquareAPI(channel , number);

//        System.out.println("Shutting down");

        channel.shutdown();

        return res;
    }

    public static Map<Integer, Integer> getSquareStream(int number ) throws InterruptedException {

        int port = 50052;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost" , port).usePlaintext().build();

        Map<Integer, Integer>  res =  callSquareBiDirectionStreamAPI(channel , number);

//        System.out.println("Shutting down");

        channel.shutdown();

        return res;
    }

    public static Map<Integer, Integer> squareRestClient(String url , int number) throws IOException {
        Map<Integer, Integer> res = new HashMap<>();
        try{
        for(int i =1 ; i<=number ;i++){
            Map<String , Object> r = callSquareRestApi(url + i);
            res.put( (Integer) r.get("number") , (Integer) r.get("numberSquare"));
        }}
        catch (Exception e){
            System.out.println(e);
        }
        return res;
    }

    public static Map<String, Object> callSquareRestApi(String urlStr) throws IOException {
        // Create a URL object
        URL url = new URL(urlStr);

        // Open a connection to the REST API server
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method
        connection.setRequestMethod("GET");


        // Read the response body
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.readValue(connection.getInputStream(), Map.class);

        // Disconnect the connection
        connection.disconnect();

        return response;
    }

    public static void main(String[] args) throws InterruptedException {

        if(args.length == 0){
            System.out.println("Need atleast 1 argument");
            return;
        }
        int port = 50052;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost" , port).usePlaintext().build();

        switch(args[0]){
            case "normal-demo": callDemoAPI(channel); break;
            case "serverStream-demo" : callDemoServerStreamAPI(channel);break;
            case "clientStream-demo" : callDemoClientStreamAPI(channel);break;
            case "BiDirectionStream-demo" : callDemoBiDirectionStreamAPI(channel);break;
            case "square" : callSquareAPI(channel , 1000);break;
            case "suareStream" : callSquareBiDirectionStreamAPI(channel , 1000);break;
            default:
                System.out.println("Invalid");
        }

        System.out.println("Shutting down");

        channel.shutdown();
    }
}
