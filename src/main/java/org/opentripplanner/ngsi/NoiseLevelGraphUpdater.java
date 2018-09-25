/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.ngsi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.opentripplanner.ngsi.models.Location;
import org.opentripplanner.ngsi.models.NoiseLevelObserved;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements polling graph updating based on noise level from NGSI broker.
 * 
 * Polling can be configured with following settings:
 * 
 * <pre>
 * type = noise-level
 * frequencySec = 300
 * serverUrl = https://ngsi.example.com
 * </pre>
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 */
@SuppressWarnings ("squid:S3457")
public class NoiseLevelGraphUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(NoiseLevelGraphUpdater.class);
    private GraphUpdaterManager updaterManager;
    private String serverUrl;

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {
      serverUrl = config.path("serverUrl").asText();
      
      LOG.info("Configured noise level updater: server-url={}", serverUrl);
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
      this.updaterManager = updaterManager;
    }
    

    @Override
    public void setup() {
      LOG.info("Starting noise level graph updater");
    }

    @Override
    protected void runPolling() {
      updaterManager.execute(graph -> {
        if (serverUrl == null) {
          LOG.warn("Noise level server url is not configured");
          return;
        }

        LOG.info("Updating noise levels");

        NgsiClient ngsiClient = new NgsiClient();
        OffsetDateTime updatesFrom = OffsetDateTime.now().minusMinutes(5l);
        List<NameValuePair> parameters = Arrays.asList(new BasicNameValuePair("q", String.format("dateModified>%s", updatesFrom.format(DateTimeFormatter.ISO_DATE_TIME))));

        try {
          Long updatedEdgesCount = 0l;
          List<NoiseLevelObserved> noiseLevels = ngsiClient.listNoiseLevelObserved(serverUrl, parameters);
          if (noiseLevels == null) {
            LOG.error("Error fething noise levels from {}", serverUrl);
            return;
          }

          Envelope noiseLevelArea = getNoiseLevelArea(noiseLevels);
          List<StreetEdge> dataEdges = getEdgesInEnvelope(graph, noiseLevelArea);

          LOG.info("Found {} edges available for noise updates", dataEdges.size());
          
          for(StreetEdge edge : dataEdges) {
            edge.setNoiseLevel(0d);

            for(NoiseLevelObserved noiseLevel : noiseLevels) {
              if (noiseLevel.getLAmax() == null || noiseLevel.getLAmax().getValue() == null) {
                continue;
              }

              Coordinate noiseCoordinate = getNoiseCoordinate(noiseLevel);
              if (edgeNearNoiseSource(noiseCoordinate, edge.getGeometry())) {
                double noiseObserved = noiseLevel.getLAmax().getValue();
                double edgeNoiseLevel = edge.getNoiseLevel();
                if (noiseObserved > edgeNoiseLevel) {
                  updatedEdgesCount++;
                  edge.setNoiseLevel(noiseObserved);
                }
                
              }
            }
          }

          LOG.info("Updated {} edges with noise data", updatedEdgesCount);
        } catch (IOException e) {
          LOG.error("Error updating noise levels", e);
        } catch (URISyntaxException e) {
          LOG.error("Error updating noise levels", e);
        }
      });
    }

    private List<StreetEdge> getEdgesInEnvelope(Graph graph, Envelope envelope) {
      return graph.getStreetEdges().stream().filter(streetEdge -> {
        Envelope edgeEnvelope = new Envelope(streetEdge.getFromVertex().getCoordinate(), streetEdge.getToVertex().getCoordinate());
        return envelope.contains(edgeEnvelope);
      }).collect(Collectors.toList());
    }

    private Envelope getNoiseLevelArea(List<NoiseLevelObserved> noiseLevels) {
      Double minLat = getMinLatitude(noiseLevels); 
      Double minLng = getMinLongitude(noiseLevels);
      Double maxLat = getMaxLatitude(noiseLevels);
      Double maxLng = getMaxLongitude(noiseLevels);
      return new Envelope(minLat, minLng, maxLat, maxLng);
    }

    private Double getMinLatitude(List<NoiseLevelObserved> noiseLevels) {
      return getCoordinateList(0, noiseLevels).stream().filter(Objects::nonNull).min(Double::compare).orElse(0d);
    }

    private Double getMaxLatitude(List<NoiseLevelObserved> noiseLevels) {
      return getCoordinateList(0, noiseLevels).stream().filter(Objects::nonNull).max(Double::compare).orElse(0d);
    }

    private Double getMinLongitude(List<NoiseLevelObserved> noiseLevels) {
      return getCoordinateList(1, noiseLevels).stream().filter(Objects::nonNull).min(Double::compare).orElse(0d);
    }

    private Double getMaxLongitude(List<NoiseLevelObserved> noiseLevels) {
      return getCoordinateList(1, noiseLevels).stream().filter(Objects::nonNull).max(Double::compare).orElse(0d);
    }

    private List<Double> getCoordinateList(int coordinateIndex, List<NoiseLevelObserved> noiseLevels) {
      return noiseLevels.stream()
        .filter(Objects::nonNull)
        .map((noiseLevel) -> {
          Location location = noiseLevel.getLocation();
          if (location == null ||
              location.getValue() == null || 
              location.getValue().getCoordinates() == null ||
              location.getValue().getCoordinates().get(coordinateIndex) == null) {
            
            return null;
          } else {
            return location.getValue().getCoordinates().get(coordinateIndex);
          }
        }).collect(Collectors.toList());
    }

    private Coordinate getNoiseCoordinate(NoiseLevelObserved noiseLevel) {
      List<Double> coordinates = noiseLevel.getLocation().getValue().getCoordinates();
      return new Coordinate(coordinates.get(0), coordinates.get(1));
    }

    private boolean edgeNearNoiseSource(Coordinate noiseSource, LineString edgeGeometry) {
      if(noiseSource.distance(edgeGeometry.getStartPoint().getCoordinate()) < 0.005) {
        return true;
      }

      return noiseSource.distance(edgeGeometry.getEndPoint().getCoordinate()) < 0.005;
    }

    @Override
    public void teardown() {
      LOG.info("Stopping noise level graph updater");
    }
    
}
