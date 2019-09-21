package org.bremersee.web.reactive.function.client.proxy;

import static org.bremersee.web.reactive.function.client.proxy.app.ControllerOne.OK_RESPONSE;

import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.web.reactive.function.client.WebClientException;
import org.bremersee.web.reactive.function.client.proxy.app.ControllerOne;
import org.bremersee.web.reactive.function.client.proxy.app.ControllerTwo;
import org.bremersee.web.reactive.function.client.proxy.app.ProxyTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;


/**
 * @author Christian Bremer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {ProxyTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"security.basic.enabled=false"})
@AutoConfigureWebTestClient
@Slf4j
public class WebClientProxyBuilderTest {

  @LocalServerPort
  private int port;

  @Autowired
  private WebTestClient webClient;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
        .build();
  }

  private ControllerOne newControllerOneClient() {
    return WebClientProxyBuilder.defaultBuilder()
        .webClient(newWebClient())
        .build(ControllerOne.class);
  }

  private ControllerTwo newControllerTwoClient() {
    return WebClientProxyBuilder.defaultBuilder()
        .webClient(newWebClient())
        .build(ControllerTwo.class);
  }

  @Test
  public void callWithWebTestClient() {
    webClient.get().uri("/").exchange().expectStatus().isOk().expectBody(String.class)
        .isEqualTo(OK_RESPONSE);
  }

  @Test
  public void callWithWebClient() {
    StepVerifier
        .create(newWebClient().get().uri(b -> b.build()).retrieve().bodyToMono(String.class))
        .assertNext(response -> Assert.assertEquals(OK_RESPONSE, response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void simpleGet() {
    StepVerifier.create(newControllerOneClient().simpleGet())
        .assertNext(response -> Assert.assertEquals(OK_RESPONSE, response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void doGet() {
    StepVerifier.create(newControllerOneClient().getOks())
        .assertNext(ok -> Assert.assertEquals("OK_0", ok.get("value")))
        .assertNext(ok -> Assert.assertEquals("OK_1", ok.get("value")))
        .assertNext(ok -> Assert.assertEquals("OK_2", ok.get("value")))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void doPost() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("value", "ok");
    StepVerifier.create(newControllerOneClient().addOk(form))
        .assertNext(response -> Assert.assertEquals(OK_RESPONSE, response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void doPut() {
    StepVerifier.create(newControllerOneClient().updateOk("value", "ok"))
        .assertNext(response -> Assert.assertEquals("value=ok", response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void doPatch() {
    StepVerifier.create(newControllerOneClient().patchOk("name", "suffix", "payload"))
        .expectNextCount(0)
        .verifyComplete();

    StepVerifier.create(newControllerOneClient().patchOk("name", "exception", "payload"))
        .expectError(WebClientException.class)
        .verifyThenAssertThat();
  }

  @Test
  public void doDelete() {
    StepVerifier.create(newControllerOneClient().deleteOk("value"))
        .assertNext(Assert::assertTrue)
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void upload() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("k0", "v0");
    data.add("k1", "v1");

    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("x-ok-flag", "a-flag");
    map.put("last", "a-value");
    map.putAll(data);

    StepVerifier.create(newControllerOneClient().upload("a-flag", "a-value", data))
        .assertNext(response -> Assert.assertEquals(map, response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void sayHelloWithName() {
    StepVerifier.create(newControllerTwoClient().sayHello("Anna"))
        .assertNext(response -> Assert.assertEquals("Hello Anna", response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void sayHelloWithoutName() {
    StepVerifier.create(newControllerTwoClient().sayHello(null))
        .assertNext(response -> Assert.assertEquals("Hello Tom", response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void sayHelloTo() {
    StepVerifier.create(newControllerTwoClient().sayHelloTo("Anna Livia"))
        .assertNext(response -> Assert.assertEquals("Hello Anna Livia", response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void setName() {
    StepVerifier.create(newControllerTwoClient().setName("Anna Livia"))
        .assertNext(response -> Assert.assertEquals("Anna Livia", response))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  public void setNoName() {
    StepVerifier.create(newControllerTwoClient().setName(null))
        .assertNext(response -> Assert.assertEquals("null", response))
        .expectNextCount(0)
        .verifyComplete();
  }

}