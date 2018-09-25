
package org.opentripplanner.ngsi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "type",
    "location",
    "dateObservedFrom",
    "dateObservedTo",
    "LAeq",
    "LAmax",
    "LAS",
    "LAeq_d"
})
public class NoiseLevelObserved {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("location")
    private Location location;
    @JsonProperty("dateObservedFrom")
    private DateObservedFrom dateObservedFrom;
    @JsonProperty("dateObservedTo")
    private DateObservedTo dateObservedTo;
    @JsonProperty("LAeq")
    private LAeq lAeq;
    @JsonProperty("LAmax")
    private LAmax lAmax;
    @JsonProperty("LAS")
    private LAS lAS;
    @JsonProperty("LAeq_d")
    private LAeqD lAeqD;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Location location) {
        this.location = location;
    }

    @JsonProperty("dateObservedFrom")
    public DateObservedFrom getDateObservedFrom() {
        return dateObservedFrom;
    }

    @JsonProperty("dateObservedFrom")
    public void setDateObservedFrom(DateObservedFrom dateObservedFrom) {
        this.dateObservedFrom = dateObservedFrom;
    }

    @JsonProperty("dateObservedTo")
    public DateObservedTo getDateObservedTo() {
        return dateObservedTo;
    }

    @JsonProperty("dateObservedTo")
    public void setDateObservedTo(DateObservedTo dateObservedTo) {
        this.dateObservedTo = dateObservedTo;
    }

    @JsonProperty("LAeq")
    public LAeq getLAeq() {
        return lAeq;
    }

    @JsonProperty("LAeq")
    public void setLAeq(LAeq lAeq) {
        this.lAeq = lAeq;
    }

    @JsonProperty("LAmax")
    public LAmax getLAmax() {
        return lAmax;
    }

    @JsonProperty("LAmax")
    public void setLAmax(LAmax lAmax) {
        this.lAmax = lAmax;
    }

    @JsonProperty("LAS")
    public LAS getLAS() {
        return lAS;
    }

    @JsonProperty("LAS")
    public void setLAS(LAS lAS) {
        this.lAS = lAS;
    }

    @JsonProperty("LAeq_d")
    public LAeqD getLAeqD() {
        return lAeqD;
    }

    @JsonProperty("LAeq_d")
    public void setLAeqD(LAeqD lAeqD) {
        this.lAeqD = lAeqD;
    }

}
