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

package org.opentripplanner.airquality;

import com.fasterxml.jackson.databind.JsonNode;

import spark.utils.StringUtils;

import java.io.File;
import java.util.Arrays;

import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements polling graph updating for the air quality files.
 * 
 * Polling can be configured with following settings:
 * 
 * <pre>
 * type = air-quality
 * frequencySec = 600000
 * airQualityFile = /path/to/file.aqi.nc
 * </pre>
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 */
@SuppressWarnings ("squid:S3457")
public class AirQualityGraphUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(AirQualityGraphUpdater.class);
    private GraphUpdaterManager updaterManager;
    private File airQualityFile;

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {
      airQualityFile = new File (config.path("airQualityFile").asText());
      LOG.info("Configured air quality updater: frequencySec={} and file={}", pollingPeriodSeconds, airQualityFile);
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
      this.updaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) {
      LOG.info("Starting air quality graph updater");
    }

    @Override
    protected void runPolling() {
      updaterManager.execute(graph -> {
        if (!airQualityFile.exists()) {
          LOG.warn("Air quality file {} does not exist", airQualityFile);
          return;
        }
        
        AirQualityUpdater updater = new AirQualityUpdater(Arrays.asList(airQualityFile));
        String errors = updater.checkFiles();
        if (StringUtils.isNotEmpty(errors)) {
          LOG.warn("Errors {} in air quality file {}", errors, airQualityFile);
          return;
        }
        
        LOG.info("Updating graph with air quality data");
        
        updater.updateGraph(graph);
        
        LOG.info("Updated graph with air quality data");
      });
    }

    @Override
    public void teardown() {
      LOG.info("Stopping air quality graph updater");
    }
    
}
