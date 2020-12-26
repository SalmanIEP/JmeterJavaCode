package Steps;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static jmeter.JMeterEngineUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class PerformanceSteps {
    private static HeaderManager headerManager;
   private static final Logger LOG = Logger.getLogger(PerformanceSteps.class.getName());
    String Domain;
    String Path;
    String Host;
    @Given("Gettookit Api")
    public void gettookitApi() {

           Domain ="";
           Path ="";
           Host=null;
    }



    @When("^Load of  user is added to with a Rampup Period of  and think time is <(\\d+)> Seconds$")
    public void loadOfUserIsAddedToWithARampupPeriodOfAndThinkTimeIsSeconds(int arg0) throws URISyntaxException, IOException {

        loadJMeterconfig();
        headerManager = setHeaderManager();
        headerManager.add(new Header("Content-Type", "application/json"));
        headerManager.add(new Header("Accept", "application/json"));
        setHttpSampler("https", Domain, Path, "GET");
        setLoopController(1);
        setThreadGroup(10, 5);
        initializeTestPlan("Get list of the toolkits");
        configureTestPlan();

        List<Map<String, String>> Results= generateReport("SimpleHTTPRESTAPIReport");
        System.out.println("Performance Results for" +Results);
        for (Map<String, String> map : Results) {
          long responsetime = Long.parseLong(map.get("elapsed"));
          long expectedresponse = Long.parseLong(String.valueOf(100));
            assertThat(responsetime, is(lessThan(responsetime)));
        }
    }
}
