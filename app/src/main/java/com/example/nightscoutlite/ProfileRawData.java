package com.example.nightscoutlite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "time",
        "value",
        "timeAsSeconds"
})
class ProfileRawData {

    @JsonProperty("time")
    private String time;

    @JsonProperty("value")
    private String value;

    @JsonProperty("timeAsSeconds")
    private String timeAsSeconds;

    public String getTime() {
        return time;
    }

    public String getValue() {
        return value;
    }

    public int getTimeAsSeconds() {
        return new Integer(timeAsSeconds);
    }
}
