package com.paragonintel.codingexercise.Airports;

import com.google.gson.Gson;
import com.paragonintel.codingexercise.GeoCoordinate;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.Set.*;
import java.util.Iterator;
import java.util.Map;

public class AirportCollection {

    // I decided to use a hashmap because I want the GeoCoordinates to be available
    // at O(1) time complexity. Therefore, geoc that as the key
    private HashMap<GeoCoordinate,Airport> airportCollection;

    public AirportCollection(Airport[] airports) {

        this.setAirportCollection(airports);
    }

    public String toString(){
        String airports = "";
        Iterator airportIterator = this.airportCollection.entrySet().iterator();
        while  (airportIterator.hasNext()){
            Map.Entry airport = (Map.Entry)airportIterator.next();
            airports += airport.toString() + "\n";
        }
        return airports;
    }

    public HashMap<GeoCoordinate,Airport> getAirportCollection() {
        return this.airportCollection;
    }

    public void setAirportCollection(Airport[] airports) {

        this.airportCollection = new HashMap<GeoCoordinate,Airport>();
        for(Airport airport : airports){
            double latitude = airport.getLatitude();
            double longitude = airport.getLongitude();
            this.airportCollection.put(new GeoCoordinate(latitude,longitude),airport);
        }

    }

    public static AirportCollection loadFromFile(String filePath) throws IOException {
        var file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        Reader reader = new FileReader(filePath);
        Gson gson = new Gson();
        Airport[] airports = gson.fromJson(reader, Airport[].class);
        return new AirportCollection(airports);
    }

    public Airport getClosestAirport(GeoCoordinate coordinate) {
        Double minDistance = null;
        Airport closestAiport = null;

        for (HashMap.Entry<GeoCoordinate,Airport> airport : this.getAirportCollection().entrySet()){
            if (minDistance == null || coordinate.GetDistanceTo(airport.getKey()) < minDistance){
                minDistance = coordinate.GetDistanceTo(airport.getKey());
                closestAiport = airport.getValue();
            }
        }
        return closestAiport;
    }
}
