package com.heroku.myorchestrator.util.content;

import com.heroku.App;
import com.heroku.definitions.config.enumerate.Kind;
import static com.heroku.definitions.config.enumerate.Kind.google_trends;
import com.heroku.definitions.util.actions.MasterUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@Component
public class GoogleTrendsParsingUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:google_trends_parsing_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:google_trends_parsing_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() {
        from("direct:google_trends_parsing_util_test_0")
                .to("mock:google_trends_parsing_util_test_1");
    }

    @Test
    public void testGoogleTrendsValues() throws InterruptedException {
        /*producer0.sendBody(Kind.google_trends.preMessage());
        consumer1.message(0).body().in((Exchange exchange) -> {
            MasterUtil masterUtil = new MasterUtil(exchange);

            try {
                List<Map<String, Object>> data = DocumentUtil.getData(masterUtil.findOrElseThrow(google_trends))
                        .stream().filter((map) -> map.containsKey("trends"))
                        .collect(Collectors.toList());
                data.forEach(System.out::println);
                System.out.println(data.size());
                data.stream().map((map) -> {
                    Object get = map.get("gender");
                    Map<String, Object> m = (Map<String, Object>) map.get("trends");
                    m.put("gender", map.get("gender"));
                    return m;
                })
                        .filter((map) -> map.get("status").equals("success"))
                        .map((m) -> {
                            List<Map<String, Object>> d = (List<Map<String, Object>>) m.get("data");
                            int sum = d.stream().filter((mm) -> ((String) mm.keySet().iterator().next()).compareTo("1507") > 0)
                                    .map((mm) -> mm.values().iterator().next() + "")
                                    .mapToInt(Integer::parseInt)
                                    .sum();
                            m.put("from1508", sum);
                            return m;
                        })
                        .filter((m)->((String)m.get("from")).compareTo("1206")>0)
                        .forEach((map) -> {
                            System.out.println(map.get("title") + "\t" + map.get("name") + "\t" + map.get("gender") + "\t" + map.get("count") + "\t" + map.get("sum") + "\t" + map.get("avg") + "\t" + map.get("from") + "\t" + map.get("from1508"));
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        });
        MockEndpoint.assertIsSatisfied(camelContext);*/
    }
}
