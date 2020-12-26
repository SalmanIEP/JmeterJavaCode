package jmeter;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.AuthPanel;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static JavaUtil.FileDirectoryUtil.flushDirectory;


public class JMeterEngineUtil {
    private static final Logger LOG = Logger.getLogger(JMeterEngineUtil.class.getName());
    private static StandardJMeterEngine jMeterEngine;
    protected static HashTree testplanTree;
    private static File testplanFile;
    private static Summariser summer;
    protected static HTTPSamplerProxy httpSamplerProxy;
    private static ReportGenerator reportGenerator;
    private static CookieManager cookieManager;
    private static LoopController loopController;
    private static AuthManager authManager;
    protected static ThreadGroup threadGroup;
    protected static TestPlan testPlan;
    private static HeaderManager headerManager;
    private static String reportDirectory = "target/performancereport/";
    private static File jmeterHomePath;
    private static HashTree threadGroupHashTree;


    static {
        jMeterEngine = new StandardJMeterEngine();
    }

    private JMeterEngineUtil() {

    }

    //This method will load and Identify JMeter in your system.
    public static void loadJMeterconfig() {
        jmeterHomePath = null;
        testplanTree = new HashTree();
        jmeterHomePath = new File(System.getProperty("user.dir") + "/src/test/resources/JmeterSetup");
        if (jmeterHomePath.exists()) {
            JMeterUtils.setJMeterHome(jmeterHomePath.getPath());
            JMeterUtils.loadJMeterProperties(jmeterHomePath.getPath() + File.separator + "bin" + File.separator + "jmeter.properties");
            JMeterUtils.initLocale();
            LOG.info("JMeter Home Initialized successfully");
        } else {
            LOG.severe("JMeter Home not set, existing from execution");
            System.exit(1);
        }
    }

    public static void setAuthentication(String url, String username, String password) {
        authManager = new AuthManager();
        Authorization authorization = new Authorization();
        authorization.setURL(url);
        authorization.setUser(username);
        authorization.setPass(password);
        authManager.addAuth(authorization);
        LOG.info("Initalized Authentication Manager");
        authManager.setName(JMeterUtils.getResString("Authorization Manger")); // $NON-NLS-1$
        authManager.setProperty(TestElement.TEST_CLASS, AuthManager.class.getName());
        authManager.setProperty(TestElement.GUI_CLASS, AuthPanel.class.getName());
    }

    public static HeaderManager setHeaderManager() {
        headerManager = new HeaderManager();
        headerManager.setName("Header Manager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return headerManager;
    }

    public static HTTPArgument getHttpArgument() {
        return new HTTPArgument();
    }


    //HTTP Sampler with port
    public static void setHttpSampler(String protocolType, String setDomainName, int setPort, String setPath, String requestType) {
        httpSamplerProxy = new HTTPSamplerProxy();
        httpSamplerProxy.setDomain(setDomainName);
        httpSamplerProxy.setPort(setPort);
        httpSamplerProxy.setPath(setPath);
        httpSamplerProxy.setMethod(requestType);
        httpSamplerProxy.setProtocol(protocolType);
        httpSamplerProxy.setName("Http Sampler");
        LOG.info("Initalized Http Sampler");
        httpSamplerProxy.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSamplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
    }

    //HTTP Sampler without port
    public static void setHttpSampler(String protocolType, String setDomainName, String setPath, String requestType) {
        httpSamplerProxy = new HTTPSamplerProxy();
        httpSamplerProxy.setDomain(setDomainName);
        httpSamplerProxy.setPath(setPath);
        httpSamplerProxy.setMethod(requestType);
        httpSamplerProxy.setProtocol(protocolType);
        httpSamplerProxy.setName("Http Sampler");
        LOG.info("Initalized Http Sampler");
        httpSamplerProxy.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSamplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
    }



    //Loop Controller
    public static void setLoopController(int loopCount) {
        loopController = new LoopController();
        loopController.setLoops(loopCount);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        LOG.info("Initalized Loop Controller");
    }

    //Loop Controller
    public static void setThreadGroup(int noOfThreads, int setRamupNo) {
        threadGroup = new ThreadGroup();
        threadGroup.setName("Thread Group");
        threadGroup.setNumThreads(noOfThreads);
        threadGroup.setRampUp(setRamupNo);
        threadGroup.setSamplerController(loopController);
        LOG.info("Initialized Thread Group");
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
    }
    public  static void createTimer(int rps){
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        long rpsCalc = rps * 60;
        timer.setProperty("throughput", rpsCalc);
        timer.setProperty("calcMode", 2);
        timer.setCalcMode(2);
        timer.setThroughput(rpsCalc);
        timer.setEnabled(true);
        LOG.info("Initialized ConstantThroughputTimer");
        timer.setProperty(TestElement.TEST_CLASS, ConstantThroughputTimer.class.getName());
        timer.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());

    }

    //Cookies Manager
    public static void setCookieManager() {
        cookieManager = new CookieManager();
        cookieManager.setName("Cookie Manager");
        LOG.info("Initialized Cookie Manager");
        cookieManager.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
        cookieManager.setProperty(TestElement.GUI_CLASS, CookiePanel.class.getName());
    }

    //Creating Test Plan
    public static void initializeTestPlan(String testPlanName) {
        testPlan = new TestPlan(testPlanName);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
    }

    public static HashTree configureTestPlan() {
        testplanTree.add("TestPlan", testPlan);
        testplanTree.add("loopController", loopController);
        testplanTree.add("ThreadGroup", threadGroup);
        testplanTree.add("HTTPSamplerProxy", httpSamplerProxy);
        return testplanTree;
    }


    public static void saveTestPlan(String filePath, String fileName) throws IOException {
        SaveService.saveTree(testplanTree, new FileOutputStream(filePath + fileName));

    }



    public static void executeTestWithJMXFile(String reportName, String jmxFilePath) {
        try {
            testplanFile = new File(System.getProperty("testPlan.location", jmxFilePath));
            SaveService.loadProperties();
            testplanTree = SaveService.loadTree(testplanFile);
            LOG.info("Run and Generate Report");
            generateReport(reportName);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        }
    }

    public static List<Map<String, String>> generateReport(String reportName) throws IOException {

            List<Map<String, String>> list;
            JMeterUtils.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", reportDirectory + reportName);
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }
            File report = new File(reportDirectory + reportName);
            File reportFile = new File(reportDirectory + reportName + "\\result.jtl");
            if (report.exists()) {
                flushDirectory(report);
                LOG.info("Report folder deleted");
                if (reportFile.exists()) {
                    boolean delete1 = reportFile.delete();
                    LOG.info("Report File deleted" + delete1);
                }
            }
            ResultCollector logger = new ResultCollector(summer);
            logger.setFilename(reportFile.getPath());
            testplanTree.add(testplanTree.getArray()[0], logger);
            jMeterEngine.configure(testplanTree);
            LOG.info("Performance Execution Started..........");
            jMeterEngine.run();
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            MappingIterator<Map<String, String>> mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(reportFile);
            list = mappingIterator.readAll();
            LOG.info("Report Generated Successfully");

           return list;
    }

    //Example to use in step definitation file
    /*
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
     */

}
