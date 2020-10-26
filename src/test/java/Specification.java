import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class Specification {

    public static RequestSpecification requestSpec() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://currencyconverter.kowabunga.net")
                .log(LogDetail.ALL)
                .build();
        return requestSpec;
    }

    public  static ResponseSpecification responseSpec(int code){
        ResponseSpecification responseSpec = new ResponseSpecBuilder()
                .expectStatusCode(code)
                .build();
        RestAssured.responseSpecification = responseSpec;
        return  responseSpec;
    }

    public static void installSpec(RequestSpecification requestSpec){
        RestAssured.requestSpecification = requestSpec;
    }

    public static void  installSpec(ResponseSpecification responseSpec){
        RestAssured.responseSpecification = responseSpec;
    }

    public static void  installSpec(RequestSpecification requestSpec, ResponseSpecification responseSpec){
        RestAssured.requestSpecification  = requestSpec;
        RestAssured.responseSpecification = responseSpec;
    }
}
