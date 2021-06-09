package ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Endpoint {

    private String name;

    private String ip;

    private String port;

    protected Endpoint() {
    }

    protected Endpoint(String name, String ip, String port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public static Endpoint of(String name, String ip, String port) {
        return new Endpoint(name, ip, port);
    }

    @Override
    public String toString() {
        return name + "[" + ip + ":" + port + "]";
    }
}
