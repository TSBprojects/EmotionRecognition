package ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Endpoint {

    private String name;

    private String url;

    protected Endpoint() {
    }

    protected Endpoint(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public static Endpoint of(String name, String url) {
        return new Endpoint(name, url);
    }

    @Override
    public String toString() {
        return name + "[" + url + "]";
    }
}
