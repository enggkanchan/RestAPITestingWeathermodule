package api.Usecases;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.Assert;

public class validatetestcas {
	
	  ExtentReports extent = new ExtentReports();;
	    ExtentTest test;

	    @BeforeClass
	    public void setup() {
	    	ExtentSparkReporter htmlReporter = new ExtentSparkReporter("extentReport.html");
	        htmlReporter.config().setTheme(Theme.STANDARD);
	        htmlReporter.config().setDocumentTitle("API Test Report");
	        htmlReporter.config().setReportName("Weather and Air Pollution Test Report");

	        extent.attachReporter(htmlReporter);
	        extent.createTest("FirstTest");
	        extent.setSystemInfo("Tester", "Kanchan");
	        extent.setSystemInfo("Environment", "QA");
	    }
	 
	    @Test
	    public void TestReult() {
	 
	        for (String city : Url.CITIES) {
	        	
	            // Fetch weather data to get coordinates
	        	test = extent.createTest("City: " + city);
	        	try {
	            Response weatherResponse = RestAssured.given()
	            		.contentType(ContentType.JSON)
	            		.queryParam("q", city)
	                    .queryParam("appid", Url.API_KEY)
	                    .when()
	                    .get(Url.BASE_URL + "/weather")
	                    .then()
	                    .statusCode(200)
	                    .extract()
	                    .response();
	            
	            float lat = weatherResponse.path("coord.lat");
	            float lon = weatherResponse.path("coord.lon");
	            
	            test.log(Status.INFO, "Latitude: " + lat + ", Longitude: " + lon);

	            Response air_pollution = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", Url.API_KEY)
                        .when()
                        .get(Url.BASE_URL + "/air_pollution/forecast")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
				
				 
	            List<Integer> aqiList = air_pollution.jsonPath().getList("list.main.aqi", Integer.class);
                test.log(Status.INFO, "AQI List: " + aqiList);

                // Verify if any AQI is 2 or above
                boolean isAqiHigh = aqiList.stream().anyMatch(aqi -> aqi >= 2);

                String actualResult = "AQI values: " + aqiList.toString();
                String expectedResult = "At least one AQI value should be 2 or above";

                if (isAqiHigh) 
                {
                    test.log(Status.PASS, "City: " + city+  " - " + expectedResult );
                }  
                else 
                {
                    test.log(Status.FAIL, "City: " + city + " - " + " AQI value is less than 2" + actualResult);
                }

                System.out.println("City: " + city + " - " + expectedResult + ": " + isAqiHigh);
                
            } catch (Exception e) 
	        {
            	test.log(Status.FAIL, "Test failed for city: " + city + " - " + e.getMessage());
                
                System.out.println("Test failed for city: " + city + " - " + e.getMessage());
            }
        }
     }
	    
	@AfterClass
	public void tearDown() 
	{
	     extent.flush();
	}
}