package org.bremersee.web.reactive.function.client.proxy;

import org.junit.Test;
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