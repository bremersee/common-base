package org.bremersee.web.reactive.function.client.proxy;

import static org.junit.Assert.*;

import org.bremersee.web.reactive.function.client.proxy.app.ControllerOneImpl;
import org.bremersee.web.reactive.function.client.proxy.app.ProxyTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Christian Bremer
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {ProxyTestConfiguration.class}, webEnvironment = WebEnvironment.RANDOM_PORT) //, ControllerOneImpl.class}
//@AutoConfigureMockMvc
//@WebFluxTest
//@AutoConfigureWebTestClient
//@Import(ProxyTestConfiguration.class)
public class WebClientProxyBuilderTest {

  //@LocalServerPort
  int port;

  //@Autowired
  private WebTestClient webClient;

  ///@Test
  public void build() {
    // TODO
    webClient.get().uri("/").exchange().expectStatus().isOk().expectBody(String.class)
        .isEqualTo("Hello");

  }

  @Test
  public void foo() {

  }
}