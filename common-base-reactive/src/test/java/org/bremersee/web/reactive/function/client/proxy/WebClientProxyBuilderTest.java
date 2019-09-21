package org.bremersee.web.reactive.function.client.proxy;

import static org.bremersee.web.reactive.function.client.proxy.app.ControllerOne.OK_RESPONSE;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.web.reactive.function.client.proxy.app.ControllerOne;
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

}