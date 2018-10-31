package org.opentripplanner.ngsi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opentripplanner.ngsi.models.NoiseLevelObserved;

public class NgsiClient {

  public List<NoiseLevelObserved> listNoiseLevelObserved(String serverUrl, List<NameValuePair> parameters) throws IOException, URISyntaxException {
    HttpGet request = new HttpGet();
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      URIBuilder uriBuilder = new URIBuilder(serverUrl);
      uriBuilder.addParameter("type", "NoiseLevelObserved");
      uriBuilder.addParameters(parameters);
      request.setURI(uriBuilder.build());
      try (CloseableHttpResponse response = client.execute(request)) {
        if (response.getStatusLine().getStatusCode() == 200) {
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          return objectMapper.readValue(response.getEntity().getContent(), new TypeReference<List<NoiseLevelObserved>>() {});
        }
      }
    }

    return null;
  }

}