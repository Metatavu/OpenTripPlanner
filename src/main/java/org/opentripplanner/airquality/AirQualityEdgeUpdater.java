package org.opentripplanner.airquality;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.referencing.GeodeticCalculator;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

import ucar.ma2.Array;

/**
 * Class that updates air quality data from single air quality data file into all street edges.
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 *
 */
public class AirQualityEdgeUpdater {
  
  private static final Logger LOG = LoggerFactory.getLogger(AirQualityEdgeUpdater.class);
  private static final int REPORT_EVERY_N_EDGE = 10000;

  private AirQualityDataFile airQualityDataFile;
  private Collection<StreetEdge> streetEdges;
  private int edgesUpdated;
  
  /**
   * Constructor for air quality edge updater
   * 
   * @param airQualityDataFile data file
   * @param streetEdges street edges
   */
  public AirQualityEdgeUpdater(AirQualityDataFile airQualityDataFile, Collection<StreetEdge> streetEdges) {
    super();
    this.airQualityDataFile = airQualityDataFile;
    this.streetEdges = streetEdges;
    this.edgesUpdated = 0;
  }

  /**
   * Updates air quality data to street edges
   */
  public void updateEdges() {
    streetEdges.stream().forEach(this::updateEdge);
  }
  
  /**
   * Updates air quality data into single edge
   * 
   * @param streetEdge edge
   */
  private void updateEdge(StreetEdge streetEdge) {
    Vertex fromVertex = streetEdge.getFromVertex();
    Vertex toVertex = streetEdge.getToVertex();
    Coordinate fromCoordinate = fromVertex.getCoordinate();
    Coordinate toCoordinate = toVertex.getCoordinate();
    float[] aqi = getAverageAq(fromCoordinate.x, fromCoordinate.y, toCoordinate.x, toCoordinate.y);
    streetEdge.setAqi(aqi);
    streetEdge.setAqiTime(this.airQualityDataFile.getOriginDate().toInstant().toEpochMilli());
    
    edgesUpdated++;
    
    if (LOG.isInfoEnabled() && (edgesUpdated % REPORT_EVERY_N_EDGE) == 0) {
      LOG.info(String.format("%d / %d street edges updated", edgesUpdated, streetEdges.size()));
    }
  }
  
  /**
   * Returns average air quality sample datas near given line. 
   * 
   * Each cell of returned array represent an average of air quality index in time
   * 
   * @param fromLongitude from longitude
   * @param fromLatitude from latitude
   * @param toLongitude to longitude
   * @param toLatitude to latitude
   * @return array of air quality index samples in time
   */
  private float[] getAverageAq(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
    EdgeAirQuality edgeAirQuality = new EdgeAirQuality();
    
    getClosestSamples(fromLongitude, fromLatitude, toLongitude, toLatitude).stream().forEach(sample -> {
      for (int time = 0; time < sample.length; time++) {
        edgeAirQuality.addAirQualitySample(time, sample[time]);
      }
    });
    
    return edgeAirQuality.getAirQualities((int) airQualityDataFile.getTimeArray().getSize());
  }
  
  /**
   * Returns closest air quality samples for given line 
   * 
   * Each list cell represent an average of air quality index in time
   * 
   * @param fromLongitude from longitude
   * @param fromLatitude from latitude
   * @param toLongitude to longitude
   * @param toLatitude to latitude
   * @return closest air quality samples for given line 
   */
  private List<float[]> getClosestSamples(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
    List<float[]> result = new ArrayList<>();
    double azimuth = getAzimuth(fromLongitude, fromLatitude, toLongitude, toLatitude);
    double distance = getDistance(fromLongitude, fromLatitude, toLongitude, toLatitude);
    double spacing = 12d;
    
    for (int i = 0; i < distance / spacing; i++) {
      Point2D samplePoint = moveTo(fromLongitude, fromLatitude, azimuth, i * spacing);
      result.add(getClosestAqi(samplePoint));
    }
    
    return result;
  }
  
  /**
   * Returns closest air quality index for given point. 
   * 
   * Returned array represent an air quality index in time
   * 
   * @param samplePoint point
   * @return closest air quality index for given point
   */
  private float[] getClosestAqi(Point2D samplePoint) {
    double lon = samplePoint.getX();
    double lat = samplePoint.getY();
   
    int lonIndex = getClosestIndex(airQualityDataFile.getLongitudeArray(), lon);
    int latIndex = getClosestIndex(airQualityDataFile.getLatitudeArray(), lat);

    int timeSize = (int) airQualityDataFile.getTimeArray().getSize();
    float[] result = new float[timeSize];
    for (int timeIndex = 0; timeIndex < timeSize; timeIndex++) {
      result[timeIndex] = airQualityDataFile.getAqiArray().get(timeIndex, latIndex, lonIndex);
    }

    return result;
  }
  
  /**
   * Returns closest index for a value from given array
   * 
   * @param array array
   * @param value value
   * @return closest index
   */
  private int getClosestIndex(Array array, double value) {
    double distance = Double.MAX_VALUE;
    
    for (int i = 0; i < array.getSize(); i++) {
      double current = array.getDouble(i);
      double currentDistance = Math.abs(current - value);
      if (currentDistance < distance) {
        distance = currentDistance;
      } else {
        return i - 1;
      }
    }
    
    return (int) (array.getSize() - 1);
  }

  /**
   * Returns azimuth for given coordinates
   * 
   * @param fromLongitude from longitude
   * @param fromLatitude from latitude
   * @param toLongitude to longitude
   * @param toLatitude to latitude
   * @return azimuth
   */
  private double getAzimuth(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
    GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
    
    geodeticCalculator.setStartingGeographicPoint(fromLongitude, fromLatitude);
    geodeticCalculator.setDestinationGeographicPoint(toLongitude, toLatitude);

    return geodeticCalculator.getAzimuth();
  }

  /**
   * Returns distance between given coordinates
   * 
   * @param fromLongitude from longitude
   * @param fromLatitude from latitude
   * @param toLongitude to longitude
   * @param toLatitude to latitude
   * @return distance between given coordinates
   */
  private double getDistance(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
    GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
    
    geodeticCalculator.setStartingGeographicPoint(fromLongitude, fromLatitude);
    geodeticCalculator.setDestinationGeographicPoint(toLongitude, toLatitude);

    return geodeticCalculator.getOrthodromicDistance();
  }
  
  /**
   * Moves point a given amount from given coordinate towards given azimuth 
   * 
   * @param longitude longitude
   * @param latitude latitude
   * @param azimuth azimuth
   * @param amount amount
   * @return moved point
   */
  private Point2D moveTo(double longitude, double latitude, double azimuth, double amount) {
    GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
    geodeticCalculator.setStartingGeographicPoint(longitude, latitude);
    geodeticCalculator.setDirection(azimuth, amount);
    return geodeticCalculator.getDestinationGeographicPoint();
  }
  
}
