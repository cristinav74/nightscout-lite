package com.example.nightscoutlite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "store",
        "units",
        "defaultProfile"
})
class NightscoutProfile {

    @JsonProperty("units")
    private String units;

    @JsonProperty("defaultProfile")
    private String defaultProfile;

    @JsonProperty("store")
    private Store store;

    public String getUnits() {
        return units;
    }

    public Store getStore() {
        return store;
    }

    public Store.DefaultData getData() {
        return store.getData();
    }

    public String getISF() {
        return getStore().getData().getSens();
    }

    public String getICR() {
        return getStore().getData().getCarbratio();
    }

    public String getDIA() {
        return getStore().getData().getDia();
    }

    public List<ProfileRawData> getBasals() {
        return getStore().getData().getBasals();
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    @Override
    public String toString() {
        return "NightscoutProfile{" +
                "units='" + units + '\'' +
                ", store=" + store +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "Default"
    })
    class Store {

        @JsonProperty("Default")
        private DefaultData Default;

        public DefaultData getData() {
            return Default;
        }

        @Override
        public String toString() {
            String valueOfData;
            if (getData() == null)
                valueOfData = null;
            else
                valueOfData = Default.toString();
            return "Store{" +
                    "data=" + valueOfData +
                    '}';
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({
                "dia",
                "sens",
                "basal",
                "carbratio"
        })
        class DefaultData {

            @JsonProperty("dia")
            private String dia;

            @JsonProperty("sens")
            private List<ProfileRawData> sens;

            @JsonProperty("basal")
            private List<ProfileRawData> basal;

            @JsonProperty("carbratio")
            private List<ProfileRawData> carbratio;

            public String getDia() {
                return dia;
            }

            public String getSens() {
                return sens.get(0).getValue();
            }

            public List<ProfileRawData> getBasals() {
                basal.sort(new BasalSorter());
                return basal;
            }

            public String getCarbratio() {
                return carbratio.get(0).getValue();
            }

            @Override
            public String toString() {
                return "DefaultData{" +
                        "dia='" + getDia() + '\'' +
                        ", sens=" + getSens() +
                        ", carbratio=" + getCarbratio() +
                        '}';
            }
        }
    }
}
