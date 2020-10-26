import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.apache.tika.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.util.List;

import static io.restassured.RestAssured.given;

public class Steps {
    @Step("проверка GetCultureInfo")
    public static void test2(String converterSoap, String result, int code, String currency) throws Exception{

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(converterSoap);

        Node nodeCurrency = doc.getElementsByTagName("tem:Currency").item(0);
        nodeCurrency.setTextContent(currency);
        String xmlTorequest = ApiTests.nodeToString(doc);
        System.out.println(xmlTorequest);
        RestAssured.baseURI="http://currencyconverter.kowabunga.net";
        Response response =
                given()
                        .spec(Specification.requestSpec())
                        .contentType("text/xml")
                        .body(xmlTorequest)
                        .when()
                        .post("/converter.asmx")
                        .then()
                        .spec(Specification.responseSpec(code))
                        .log().all().extract().response();
        XmlPath xmlPath = new XmlPath(response.asString());
        String resp = xmlPath.getString(result);
        System.out.println(resp);
        Assertions.assertFalse(resp.toString().isEmpty(),"currency is null");



    }

}
