package org.opentripplanner.airquality;

import java.io.File;
import java.io.IOException;

import com.vividsolutions.jts.geom.Envelope;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayByte.D3;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Class for representing single air quality data file
 * 
 * @author Antti Leppä <antti.leppa@metatavu.fi>
 * @author Heikki Kurhinen <heikki.kurhinen@metatavu.fi>
 *
 */
public class AirQualityDataFile {

  private String error;
  private Array timeArray;
  private Array latitudeArray;
  private Array longitudeArray;
  private ArrayByte.D3 aqiArray;

  /**
   * Constructor for the class.
   * 
   * @param file NetCDF file containing air quality data
   */
  public AirQualityDataFile(File file) {
    error = null;

    try {
      NetcdfFile netcdfFile = readNetcdfFile(file);
      Variable time = netcdfFile.findVariable("time");
      Variable latitude = netcdfFile.findVariable("latitude");
      Variable longitude = netcdfFile.findVariable("longitude");
      Variable aqi = netcdfFile.findVariable("AQI");
      
      if (time == null) {
        error = String.format("Missing time variable from %s file", file.getAbsolutePath());
        return;
      }

      if (latitude == null) {
        error = String.format("Missing latitude variable from %s file", file.getAbsolutePath());
        return;
      }

      if (longitude == null) {
        error = String.format("Missing longitude variable from %s file", file.getAbsolutePath());
        return;
      }

      if (aqi == null) {
        error = String.format("Missing AQI variable from %s file", file.getAbsolutePath());
        return;
      }

      timeArray = time.read();
      latitudeArray = latitude.read();
      longitudeArray = longitude.read();
      aqiArray = (D3) aqi.read(new int[3], aqi.getShape());
    } catch (IOException | InvalidRangeException e) {
      error = e.getMessage();
    }
  }
  
  /**
   * Returns air quality data bounding box.
   * 
   * @return air quality data bounding box
   */
  public Envelope getBoundingBox() {
    double latitude1 = latitudeArray.getDouble(0);
    double longitude1 = longitudeArray.getDouble(0);
    double latitude2 = latitudeArray.getDouble((int) latitudeArray.getSize() - 1);
    double longitude2 = longitudeArray.getDouble((int) longitudeArray.getSize() - 1);
    return new Envelope(longitude1, longitude2, latitude1, latitude2);
  }
  
  /**
   * Returns whether the file was valid or not
   * 
   * @return whether the file was valid or not
   */
  public boolean isValid() {
    return error == null;
  }

  /**
   * Returns error if one is present in the file, null otherwise
   * 
   * @return error if one is present in the file.
   */
  public String getError() {
    return error;
  }

  /**
   * Returns time array
   * 
   * @return time array
   */
  public Array getTimeArray() {
    return timeArray;
  }

  /**
   * Returns latitude array
   * 
   * @return latitude array
   */
  public Array getLatitudeArray() {
    return latitudeArray;
  }

  /**
   * Returns longitude array
   * 
   * @return longitude array
   */
  public Array getLongitudeArray() {
    return longitudeArray;
  }

  /**
   * Returns air quality data array
   * 
   * @return air quality data array
   */
  public ArrayByte.D3 getAqiArray() {
    return aqiArray;
  }

  /**
   * Reads the NetCDF file
   * 
   * @param file file
   * @return Read file
   * @throws IOException throws Exception if file could not be read correctly
   */
  private NetcdfFile readNetcdfFile(File file) throws IOException {
    return NetcdfFile.open(file.getAbsolutePath());
  }

}
