package ru.sstu.vak.emotionrecognition.identifyemotion.media.face;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import ru.sstu.vak.emotionrecognition.common.Prediction;

public abstract class MediaFace {

    @JsonProperty("prediction")
    private final Prediction prediction;

    @JsonProperty("location")
    private final Location location;

    protected MediaFace(MediaFace mediaFace) {
        this.prediction = mediaFace.getPrediction();
        this.location = new Location(mediaFace.getLocation());
    }

    protected MediaFace(Prediction prediction, Location location) {
        this.prediction = prediction;
        this.location = location;
    }


    public Prediction getPrediction() {
        return prediction;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaFace)) return false;
        MediaFace mediaFace = (MediaFace) o;
        return getPrediction() == mediaFace.getPrediction() &&
                getLocation().equals(mediaFace.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrediction(), getLocation());
    }

    @Override
    public String toString() {
        return "DataFace{" +
                "prediction=" + prediction +
                ", location=" + location +
                '}';
    }

    public static class Location {

        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public Location(Location location) {
            this.x = location.x;
            this.y = location.y;
            this.width = location.width;
            this.height = location.height;
        }

        public Location(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location)) return false;

            Location location = (Location) o;

            if (x != location.x) return false;
            if (y != location.y) return false;
            if (width != location.width) return false;
            return height == location.height;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "x=" + x +
                    ", y=" + y +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

}
