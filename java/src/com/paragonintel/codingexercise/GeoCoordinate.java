package com.paragonintel.codingexercise;

public class GeoCoordinate {
    public static final double EARTH_RADIUS_MILES = 3962.17341;

    private double latitude;
    private double longitude;

    public GeoCoordinate(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public String toString() {
        return "GeoCoordinate{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public boolean hasLocation() {
        return !Double.isNaN(this.latitude) && !Double.isNaN(this.longitude);
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean equals(Object other) {
        if (other == null ||
                !(other instanceof GeoCoordinate)) {
            return false;
        }

        GeoCoordinate otherGeoCoordinate = (GeoCoordinate) other;

        return this.latitude ==  otherGeoCoordinate.latitude &&
                this.longitude == otherGeoCoordinate.longitude;
    }

    // appears to be in statute miles (sm): source: https://www.nhc.noaa.gov/gccalc.shtml
    public double GetDistanceTo(GeoCoordinate other) {
        if (other == null) {
            return Double.NaN;
        }

        var lat1 = this.latitude * (Math.PI / 180.0);
        var long1 = this.longitude * (Math.PI / 180.0);
        var lat2 = other.latitude * (Math.PI / 180.0);
        var long2 = other.longitude * (Math.PI / 180.0);
        var longDistance = long2 - long1;
        var d3 = Math.pow(Math.sin((lat2 - lat1) / 2.0), 2.0) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(longDistance / 2.0), 2.0);
        var distance = EARTH_RADIUS_MILES * (2.0 * Math.atan2(Math.sqrt(d3), Math.sqrt(1.0 - d3)));
        return distance;
    }

    // bearing is the clockwise direction of the line from North to point A and point A to point B
    public double GetBearingTo(GeoCoordinate point) {
        var phi1 = Math.toRadians(this.latitude);
        var phi2 = Math.toRadians(point.latitude);
        var deltaLambda = Math.toRadians(point.longitude - longitude);
        var y = Math.sin(deltaLambda) * Math.cos(phi2);
        var x = Math.cos(phi1) * Math.sin(phi2) -
                Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda);
        var theta = Math.atan2(y, x);

        return (Math.toDegrees(theta) + 360) % 360;
    }
}
