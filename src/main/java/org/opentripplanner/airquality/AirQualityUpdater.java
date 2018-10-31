package org.opentripplanner.airquality;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Air quality graph updater
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 */
public class AirQualityUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(AirQualityUpdater.class);
  private AirQualityDataFile dataFile;
  
  /**
   * Constructor
   * 
   * @param aqiNcFiles air quality NetCDF files
   */
  public AirQualityUpdater(String latitudeVariable, String longitudeVariable, String aqiVariable, String timeVariable, File aqiNcFile) {
    this.dataFile = readAirQualityDataFile(latitudeVariable, longitudeVariable, aqiVariable, timeVariable, aqiNcFile);
  }
  
  /**
   * Updates street edges with air quality data
   */
  public void updateGraph(Graph graph) {
    if (LOG.isInfoEnabled()) {
      LOG.info("Updating air quality data starting from {}", this.dataFile.getOriginDate().toString());
    }
    
    Collection<StreetEdge> streetEdges = graph.getStreetEdges();
    this.updateEdges(dataFile, streetEdges);
  }
  
  public String checkFile() {
    return dataFile.getError();
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
   * Loads air quality data file from the disk
   * 
   * @param file file
   * @return air quality data file
   */
  private AirQualityDataFile readAirQualityDataFile(String latitudeVariable, String longitudeVariable, String aqiVariable, String timeVariable, File file) {
    return new AirQualityDataFile(latitudeVariable, longitudeVariable, aqiVariable, timeVariable, file);
  }
}
