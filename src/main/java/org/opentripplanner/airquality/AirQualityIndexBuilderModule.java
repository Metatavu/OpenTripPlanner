package org.opentripplanner.airquality;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Graph Builder Module for adding air quality data from NetCDF -files into StreetEdges
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 *
 */
public class AirQualityIndexBuilderModule implements GraphBuilderModule {
  
  private List<AirQualityDataFile> dataFiles;
  
  /**
   * Constructor for air quality index builder module
   * 
   * @param aqiNcFiles air quality NetCDF files
   */
  public AirQualityIndexBuilderModule(List<File> aqiNcFiles) {
    this.dataFiles = loadAirQualityDataFiles(aqiNcFiles);
  }

  /**
   * Updates street edges with air quality data
   */
  @Override
  public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
    Collection<StreetEdge> streetEdges = graph.getStreetEdges();
    this.dataFiles.stream().forEach(dataFile -> this.updateEdges(dataFile, streetEdges));
  }

  /**
   * Checks that all input files are valid
   */
  @Override
  public void checkInputs() {
    String errors = this.dataFiles.stream()
      .filter(dataFile -> dataFile.getError() != null )
      .map(AirQualityDataFile::getError)
      .collect(Collectors.joining(", "));
    
    if (errors != null && !errors.isEmpty()) {
      throw new RuntimeException(errors);
    }
  }
  
  /**
   * Updates street edges with given data file
   * 
   * @param dataFile data file
   * @param streetEdges street edges
   */
  private void updateEdges(AirQualityDataFile dataFile, Collection<StreetEdge> streetEdges) {
    Envelope dataBoundingBox = dataFile.getBoundingBox();
    
    List<StreetEdge> dataEdges = streetEdges.stream().filter(streetEdge -> {
      Envelope edgeEnvelope = new Envelope(streetEdge.getFromVertex().getCoordinate(), streetEdge.getToVertex().getCoordinate());
      return dataBoundingBox.contains(edgeEnvelope);
    }).collect(Collectors.toList());

    AirQualityEdgeUpdater edgeUpdater = new AirQualityEdgeUpdater(dataFile, dataEdges);
    
    edgeUpdater.updateEdges();
  }
  
  /**
   * Loads air quality data files from the disk
   * 
   * @param aqiNcFiles files
   * @return air quality data files
   */
  private List<AirQualityDataFile> loadAirQualityDataFiles(List<File> aqiNcFiles) {
    if (dataFiles == null) {
      dataFiles = aqiNcFiles.stream().map(this::readAirQualityDataFile).collect(Collectors.toList());  
    }
    
    return dataFiles;
  }
  
  /**
   * Loads air quality data file from the disk
   * 
   * @param file file
   * @return air quality data file
   */
  private AirQualityDataFile readAirQualityDataFile(File file) {
    return new AirQualityDataFile(file);
  }
  
}
