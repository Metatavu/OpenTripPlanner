package org.opentripplanner.airquality;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.graph.Graph;

/**
 * Graph Builder Module for adding air quality data from NetCDF -files into StreetEdges
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 *
 */
public class AirQualityIndexBuilderModule implements GraphBuilderModule {
  
  private AirQualityUpdater airQualityUpdater;
  
  /**
   * Constructor for air quality index builder module
   * 
   * @param aqiNcFiles air quality NetCDF files
   */
  public AirQualityIndexBuilderModule(List<File> aqiNcFiles) {
    this.airQualityUpdater = new AirQualityUpdater(aqiNcFiles);
  }

  /**
   * Updates street edges with air quality data
   */
  @Override
  public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
    this.airQualityUpdater.updateGraph(graph);
  }

  /**
   * Checks that all input files are valid
   */
  @Override
  public void checkInputs() {
    String errors = this.airQualityUpdater.checkFiles();
    if (errors != null && !errors.isEmpty()) {
      throw new RuntimeException(errors);
    }
  }
  
}
