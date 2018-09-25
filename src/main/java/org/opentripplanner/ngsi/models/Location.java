
package org.opentripplanner.ngsi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "value"
})
public class Location {

    @JsonProperty("type")
    private String type;
    @JsonProperty("value")
    private Point value;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("value")
    public Point getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Point value) {
        this.value = value;
    }

}
