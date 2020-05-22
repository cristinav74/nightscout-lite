package com.example.nightscoutlite;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sgv",
        "direction",
        "date",
        "delta"
})
class NightscoutData {

    @JsonProperty("sgv")
    private Integer sgv;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("delta")
    private Float delta;

    @JsonProperty("sgv")
    public Integer getSgv() {
        return sgv;
    }

    @JsonProperty("sgv")
    public void setSgv(Integer sgv) {
        this.sgv = sgv;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(String direction) {
        this.direction = direction;
    }

    @JsonProperty("date")
    public Long getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(Long date) {
        this.date = date;
    }

    @JsonProperty("delta")
    public Float getDelta() {
        return delta;
    }

    @JsonProperty("delta")
    public void setDelta(Float delta) {
        this.delta = delta;
    }

    @Override
    public String toString() {
        return "NightscoutData{" +
                "sgv=" + sgv +
                ", direction='" + direction + '\'' +
                ", date=" + date +
                ", delta=" + delta +
                '}';
    }

}
