package ru.sstu.vak.emotionrecognition.identifyemotion.dataface;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import ru.sstu.vak.emotionrecognition.common.Emotion;

public abstract class DataFace {

    @JsonProperty("emotion")
    private Emotion emotion;

    @JsonProperty("location")
    private Location location;

    public DataFace(DataFace dataFace) {
        this.emotion = dataFace.getEmotion();
        this.location = new Location(dataFace.getLocation());
    }

    public DataFace(Emotion emotion, Location location) {
        this.emotion = emotion;
        this.location = location;
    }


    public Emotion getEmotion() {
        return emotion;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataFace)) return false;
        DataFace dataFace = (DataFace) o;
        return getEmotion() == dataFace.getEmotion() &&
                getLocation().equals(dataFace.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmotion(), getLocation());
    }

    @Override
    public String toString() {
        return "DataFace{" +
                "emotion=" + emotion +
                ", location=" + location +
                '}';
    }

    public static class Location {

        public int x;
        public int y;
        public int width;
        public int height;

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
