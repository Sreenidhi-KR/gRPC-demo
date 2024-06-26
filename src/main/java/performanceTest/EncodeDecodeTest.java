package performanceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.proto.person.Person;

public class EncodeDecodeTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        var protoPerson = Person.newBuilder()
                .setLastName("sam")
                .setAge(12)
                .setEmail("sam@gmail.com")
                .setEmployed(true)
                .setSalary(1000.2345)
                .setBio("shjhsdjhkjfsd")
                .setBankAccountNumber(123456789012L)
                .setBalance(-10000)

                .build();
        var jsonPerson = new JsonPerson("sam", 12, "sam@gmail.com", true, 1000.2345, 123456789012L, -10000 , "shjhsdjhkjfsd");

        System.out.println("Running Test");

        for (int i = 0; i < 2; i++) {
            runTest("json", () -> json(jsonPerson , false));
            runTest("proto", () -> proto(protoPerson , false));
        }

        json(jsonPerson , true);
        proto(protoPerson , true );

    }

    private static void proto(Person person , boolean print){
        try {
            var bytes = person.toByteArray();
            if (print) System.out.println("proto bytes length: "+ bytes.length);
            Person.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static void json(JsonPerson person , boolean print){
        try{
            var bytes = mapper.writeValueAsBytes(person);
            if (print) System.out.println("json bytes length: "+ bytes.length);
            mapper.readValue(bytes, JsonPerson.class);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void runTest(String testName, Runnable runnable){
        var start = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            runnable.run();
        }
        var end = System.currentTimeMillis();
        System.out.println("time taken for " + testName + " : " + (end - start) + "ms");
    }

}
