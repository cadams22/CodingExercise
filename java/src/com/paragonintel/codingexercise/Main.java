package com.paragonintel.codingexercise;

import com.paragonintel.codingexercise.Airports.*;
import com.paragonintel.codingexercise.Events.*;

import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {
        // change to a cleaner relative path
        String path = "/Users/courtneyadams/Documents/gitProjects/CodingExercise/java/src/com/paragonintel/codingexercise/Resources/";
        String jsonFilename = "airports.json";
        String airportsFile = path + jsonFilename;

        AirportCollection airportCollection = null;

        try {
            airportCollection = AirportCollection.loadFromFile(airportsFile);

            String eventsFilename = "events.txt";
            String eventsFile = path + eventsFilename;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(eventsFile));

            List<Flight> flights = new ArrayList<Flight>();
            // mapping Aircraft ID to Flight
            HashMap<String,Flight> currentFlights = new HashMap<String,Flight>();
            String json = null;
            while((json = bufferedReader.readLine()) != null){
                AdsbEvent event = AdsbEvent.fromJson(json);

                String aircraftIdentifier = event.getIdentifier();
                Date currentTime = event.getTimestamp();

                double latitude = event.getLatitude();
                double longitude = event.getLongitude();
                GeoCoordinate eventCoordinate = new GeoCoordinate(latitude,longitude);

                Airport closestAirport = airportCollection.getClosestAirport(eventCoordinate);
                GeoCoordinate airportCoordinate = new GeoCoordinate(closestAirport.getLatitude(),closestAirport.getLongitude());

                boolean atAirport = false;
                boolean onGround = false;

                if (Double.isNaN(event.getAltitude()) || event.getAltitude()==closestAirport.getElevation()){
                    atAirport = true;
                }

                System.out.println("Altitude=" + event.getAltitude());
                System.out.println("onGround?=" + (Double.isNaN(event.getAltitude()) || event.getAltitude()==closestAirport.getElevation()));
                System.out.println("eventCoordinate=" + eventCoordinate + ";airportCoordinate=" + airportCoordinate);
                System.out.println("Distance=" + eventCoordinate.GetDistanceTo(airportCoordinate) + "\n");

                // if that plane is currently in-flight
                if (currentFlights.containsKey(event.getIdentifier())){
                    // if there is a arrival (plane is on the ground and the closest airport is not the one the plane left)
                    if (atAirport && currentFlights.get(aircraftIdentifier).getDepartureAirport() != closestAirport.getIdentifier()) {
                        Flight flight = currentFlights.get(aircraftIdentifier);
                        // fill in missing details for the flight re:arrival
                        flight.setArrivalAirport(closestAirport.getIdentifier());
                        flight.setArrivalTime(currentTime);

                        // the flight is no longer in progress
                        currentFlights.remove(aircraftIdentifier);
                    }
                // that plane is not on a flight
                } else {
                    // set up a new flight
                    Flight flight = new Flight();
                    flight.setAircraftIdentifier(event.getIdentifier());

                    // if the plane is on the ground, assume it is preparing for departure
                    if (atAirport) {
                        flight.setDepartureAirport(closestAirport.getIdentifier());
                        flight.setDepartureTime(currentTime);
                    }

                    // add to current flight list and complete flight list
                    currentFlights.put(event.getIdentifier(), flight);
                    flights.add(flight);
                }
            }

            bufferedReader.close();

            String filePath = path + "flights.json";
            FileWriter fileWriter = new FileWriter(filePath);
            Gson gson = new Gson();

            gson.toJson(flights,fileWriter);
            fileWriter.close();
        }
        catch(FileNotFoundException e) {
            System.err.println("Caught FileNotFoundException: " + e.getMessage());
        }
        catch(IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

    }
}
