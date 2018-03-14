package main;

import classes.*;
import functions.WriteXpressFiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class ColumnGenerationLoadInXpress {

    private Population population;
    private Individual bestGlobalSolution;
    private HashMap<Integer, Station> stations;

    //Constructor
    public  ColumnGenerationLoadInXpress(Input input) throws FileNotFoundException, UnsupportedEncodingException {
        WriteXpressFiles.printFixedInput(input);
        init(input);
    }



    private static void init(Input input) throws FileNotFoundException, UnsupportedEncodingException {

        //Initialize routes for each vehicle
        for (Vehicle vehicle: input.getVehicles().values()) {
            vehicle.createRoutes(input);
        }

        System.out.println("Initial routes created");

        GraphViewer graphViewer = new GraphViewer();
        graphViewer.displayInitiatedRoutes(input, true);

        //Write time dependent input
        WriteXpressFiles.printTimeDependentInput(input);


        //Print initiated routes
        int counter = 1;
        for (Vehicle vehicle: input.getVehicles().values()) {
            for (ArrayList<StationVisit> route : vehicle.getInitializedRoutes()) {
                //Route id
                System.out.print("Route " + counter + ": ");
                counter ++;
                //Station ids
                for (StationVisit stationVisit : route) {
                    System.out.print(stationVisit.getStation().getId() + " ");
                }
                //Total time
                System.out.println(", total time: " + route.get(route.size()-1).getVisitTime());
            }
        }

    }






}
