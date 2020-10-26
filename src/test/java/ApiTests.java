import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.tika.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.form;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ApiTests {

    public static String nodeToString(Node node) throws Exception{
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
        t.setOutputProperty(OutputKeys.INDENT,"yes");
        t.transform(new DOMSource(node),new StreamResult(sw));

        return sw.toString();

    }

    public static String documentToString(Document doc){
        try {
            StringWriter sw = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
            t.setOutputProperty(OutputKeys.METHOD,"xml");
            t.setOutputProperty(OutputKeys.INDENT,"yes");
            t.setOutputProperty(OutputKeys.ENCODING,"UTF-8");

            t.transform(new DOMSource(doc),new StreamResult(sw));

            return sw.toString();
        } catch (Exception e){
            throw new RuntimeException("Error converting to String", e);
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/params.csv")
        public void test1(String currencyFrom, String currencyTo, String date, String amount) throws Exception{
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateNow = new Date();
        date = sdf.format(dateNow);

        String filePath = ".//getConversionAmount.xml";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(filePath);

        Node nodeCurrencyFrom = doc.getElementsByTagName("tem:CurrencyFrom").item(0);
        Node nodeCurrencyTo = doc.getElementsByTagName("tem:CurrencyTo").item(0);
        Node nodeRateDate = doc.getElementsByTagName("tem:RateDate").item(0);
        Node nodeAmount = doc.getElementsByTagName("tem:Amount").item(0);


        System.out.println(nodeToString(nodeCurrencyFrom));
        System.out.println(nodeToString(nodeCurrencyTo));
        System.out.println(nodeToString(nodeRateDate));
        System.out.println(nodeToString(nodeAmount));

        nodeCurrencyFrom.setTextContent(currencyFrom);
        nodeCurrencyTo.setTextContent(currencyTo);
        nodeRateDate.setTextContent(date);
        nodeAmount.setTextContent(amount);

        String xmlTorequest = nodeToString(doc);
        System.out.println(xmlTorequest);
       RestAssured.baseURI="http://currencyconverter.kowabunga.net";
        Response response = given()
                .spec(Specification.requestSpec())
                .header("Content-Type","text/xml")
                .body(xmlTorequest)
                .when()
                .post("/converter.asmx")
                .then()
                .log().all().extract().response();

        XmlPath xmlPath =new XmlPath(response.asString());
        String rate = xmlPath.getString("GetConversionAmountResult");
        System.out.println(rate);
        Assertions.assertFalse(xmlPath.get("GetConversionAmountResult").toString().isEmpty(),"amount is NULL");
        System.out.println("amount is "+rate);
    }

    @Test
    @Description(value = "Позитивный тест GetCurrencies")
    public void getCurrenciesSuccessTest() throws Exception {
        RequestSpecification getSpec = Specification.requestSpec();
        ResponseSpecification checkSpec = Specification.responseSpec(200);
        Specification.installSpec(getSpec, checkSpec);
        Steps.test2(".//getCultureInfo.xml", "GetCultureInfoResult", 200, "USD");

    }
}
