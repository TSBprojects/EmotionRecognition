package ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sstu.vak.emotionRecognition.common.Emotion;

public abstract class DataFace {

    @JsonProperty("emotion")
    private Emotion emotion;

    @JsonProperty("location")
    private Location location;


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


    public static class Location {

        public int x;
        public int y;
        public int width;
        public int height;

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
