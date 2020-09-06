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
import com.google.gson.Gson;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {
        String path = "java/src/com/paragonintel/codingexercise/Resources/";

        AirportCollection airportCollection;

        try {
            String jsonFilename = "airports.json";
            String airportsFile = path + jsonFilename;
            airportCollection = AirportCollection.loadFromFile(airportsFile);

            String eventsFilename = "events.txt";
            String eventsFile = path + eventsFilename;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(eventsFile));

            List<Flight> flights = new ArrayList<>();
            // mapping Aircraft ID to Flight
            HashMap<String,Flight> currentFlights = new HashMap<>();
            String json;
            while((json = bufferedReader.readLine()) != null){
                AdsbEvent event = AdsbEvent.fromJson(json);

                String aircraftIdentifier = event.getIdentifier();
                Date currentTime = event.getTimestamp();

                double latitude = event.getLatitude();
                double longitude = event.getLongitude();
                GeoCoordinate eventCoordinate = new GeoCoordinate(latitude,longitude);

                Airport closestAirport = airportCollection.getClosestAirport(eventCoordinate);
                GeoCoordinate airportCoordinate = new GeoCoordinate(closestAirport.getLatitude(),closestAirport.getLongitude());

                boolean onGround = false;

                // i am assuming that any time the plane is on the ground, it is at an airport
                if (Double.isNaN(event.getAltitude()) || event.getAltitude()==closestAirport.getElevation()){
                    onGround = true;
                }

                boolean atAirport = false;

                // but... i will check that we are in a certain margin of error. anything > 100 I will consider bad data
                if (onGround){
                    if (eventCoordinate.GetDistanceTo(airportCoordinate) < 100){
                        atAirport = true;
                    }
                }

                // if that plane is currently in-flight
                if (currentFlights.containsKey(event.getIdentifier())){
                    // if there is a arrival (plane is on the ground and the closest airport is not the one the plane left)
                    if (atAirport){
                        Flight flight = currentFlights.get(aircraftIdentifier);

                        // if we have landed at a new airport
                        if (flight.getDepartureAirport() != closestAirport.getIdentifier()) {

                            // fill in missing details for the flight re:arrival
                            flight.setArrivalAirport(closestAirport.getIdentifier());
                            flight.setArrivalTime(currentTime);

                            // the flight is no longer in progress
                            currentFlights.remove(aircraftIdentifier);
                        }
                        // if we are still at the same airport as before, then overwrite the departure time
                        // because the plan did not depart during the last event
                        else {
                            flight.setDepartureTime(currentTime);
                        }
                    }
                // that plane is not on a flight
                } else {
                    // set up a new flight
                    Flight flight = new Flight();
                    flight.setAircraftIdentifier(event.getIdentifier());

                    // if the plane is at the airport, assume it is preparing for departure
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
