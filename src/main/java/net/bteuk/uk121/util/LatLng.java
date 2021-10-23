package net.bteuk.uk121.util;

//Directly copied from terraplusplus
public class LatLng {
    private final Double lat;
    private final Double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng() {
        this.lat = null;
        this.lng = null;
    }

    public Double getLat() {
        return this.lat;
    }

    public Double getLng() {
        return this.lng;
    }
}
