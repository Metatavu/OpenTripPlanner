package org.opentripplanner.airquality;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that is used to store air quailty data for single edge.
 * 
 * This is used only on graph build time
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 *
 */
public class EdgeAirQuality {
  
  private Map<Integer, byte[]> airQualities; 
  
  /**
   * Constructor
   */
  public EdgeAirQuality() {
    airQualities = new HashMap<>();
  }
  
  /**
   * Adds air quality index for given time
   * 
   * @param time time
   * @param airQuality air quality index
   */
  public void addAirQualitySample(int time, byte airQuality) {
    byte[] existing = airQualities.get(time);
    if (existing == null) {
      airQualities.put(time, new byte[] { airQuality });
    } else {
      byte[] updated = new byte[existing.length + 1];
      System.arraycopy(existing, 0, updated, 0, existing.length);
      updated[existing.length] = airQuality;
      airQualities.put(time, updated);
    }
  }
  
  /**
   * Returns air quality average for given time
   * 
   * @param time time
   * @return air quality index
   */
  public byte getAirQuality(int time) {
    return getAverage(getAirQualitiesInTime(time));
  }
  
  /**
   * Returns air quality averages given times
   * 
   * @param times times
   * @return air quality indexes
   */
  public byte[] getAirQualities(int times) {
    byte[] result = new byte[times];
    
    for (int time = 0; time < times; time++) {
      result[time] = getAirQuality(time);
    }
    
    return result;
  }
  
  /**
   * Returns average for values
   * 
   * @param values values
   * @return average
   */
  private byte getAverage(byte[] values) {
    if (values == null) {
      return 0;
    }
    
    double result = 0d;
    
    for (byte value : values) {
      result += value;
    }
    
    return (byte) Math.round(result / values.length);
  }
  
  /**
   * Returns array of air quality indices in time
   * 
   * @param time time
   * @return array of air quality indices
   */
  private byte[] getAirQualitiesInTime(int time) {
    return airQualities.get(time);
  }

}
