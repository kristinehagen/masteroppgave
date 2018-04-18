package classes;

import com.dashoptimization.XPRMCompileException;
import enums.SolutionMethod;
import solutionMethods.*;
import enums.NextEvent;
import functions.NextSimulation;
import xpress.ReadXpressResult;
import xpress.WriteXpressFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Simulation {

    private double numberOfTimesVehicleRouteGenerated = 0;
    private double congestions = 0;
    private double starvations = 0;
    private int totalNumberOfCustomers = 0;
    private int happyCustomers = 0;
    private ArrayList<Double> timeToNextSimulationList = new ArrayList<>();
    private ArrayList<Double> computationalTimesXpress = new ArrayList<>();
    private ArrayList<Double> computationalTimesXpressPlussInitialization = new ArrayList<>();

    //Constructor
    public Simulation() {
    }

    public void run(String simulationFile, Input input) throws IOException, XPRMCompileException, InterruptedException {

        input.setCurrentMinute(input.getSimulationStartTime());

        //Xpress
        WriteXpressFiles.printFixedInput(input);

        //Demand input file
        CustomerArrival nextCustomerArrival = new CustomerArrival();
        File inputFile = new File(simulationFile);
        Scanner in = new Scanner(inputFile);


        // 1 : SIMULATION STOP TIME
        double simulationStopTime = input.getSimulationStopTime();                              //Actual time minutes


        // 2 : TIME FOR NEW VEHICLE ROUTES
        double timeToNewVehicleRoutes = simulationStopTime + 1;
        ArrayList<VehicleArrival> vehicleArrivals = new ArrayList<>();

        //If vehicle routes are to be generated
        if (!input.getSolutionMethod().equals(SolutionMethod.NO_VEHICLES)) {

            //Start timer
            StopWatch stopWatchTotalComputationTime = new StopWatch();
            stopWatchTotalComputationTime.start();

            //Generate routes for service vehicles
            generateVehicleRoute(input);

            if (input.getSolutionMethod().equals(SolutionMethod.HEURISTIC_VERSION_3)) {
                vehicleArrivals = ReadXpressResult.readVehicleArrivalsVersion3(input.getVehicles(), input.getCurrentMinute());
            } else {
                vehicleArrivals = ReadXpressResult.readVehicleArrivals(input.getCurrentMinute());                      //Actual arrival times minutes
            }

            //Stop timer
            stopWatchTotalComputationTime.stop();
            computationalTimesXpressPlussInitialization.add(stopWatchTotalComputationTime.getElapsedTimeSecs());

            System.out.println("Time first Xpress: " + computationalTimesXpress.get(0));
            System.out.println("Time first Xpress + initialization: " + computationalTimesXpressPlussInitialization.get(0));

            //Determine time to generate new vehicle routes
            timeToNewVehicleRoutes = NextSimulation.determineTimeToNextSimulation(vehicleArrivals, input.getTimeHorizon(), input.getReOptimizationMethod(), input.getCurrentMinute());      //Actual time minutes
            this.timeToNextSimulationList.add(timeToNewVehicleRoutes-input.getCurrentMinute());

            System.out.println();
            System.out.println("Remaining time: " + (simulationStopTime - timeToNewVehicleRoutes));

        }


        //3 : NEXT CUSTOMER ARRIVAL
        nextCustomerArrival.updateNextCustomerArrival(in, input.getCurrentMinute(), simulationStopTime);         //Actual time minutes


        // 4 : NEXT VEHICLE ARRIVAL
        int vehicleArrivalCounter = 0;
        VehicleArrival nextVehicleArrival = new VehicleArrival(simulationStopTime);
        if (!input.getSolutionMethod().equals(SolutionMethod.NO_VEHICLES)) {
            if (vehicleArrivals.size() > 0) {
                nextVehicleArrival = vehicleArrivals.get(vehicleArrivalCounter);                        //Actual time minutes
            }
        }





        //Determine next event (simulation stop, new vehicle routes, customer arrival, vehicle arrival)
        boolean stopTimeReached = false;

        while (!stopTimeReached) {

            double nextEventTime = simulationStopTime;
            NextEvent nextEvent = NextEvent.SIMULATION_STOP;

            //If vehicle routes are to be generated
            if (!input.getSolutionMethod().equals(SolutionMethod.NO_VEHICLES)) {
                if (timeToNewVehicleRoutes < nextEventTime) {
                    nextEventTime = timeToNewVehicleRoutes;
                    nextEvent = NextEvent.NEW_VEHICLE_ROUTES;
                }
            }

            if (nextCustomerArrival.getTime() < nextEventTime) {
                nextEventTime = nextCustomerArrival.getTime();
                nextEvent = NextEvent.CUSTOMER_ARRIVAL;
            }

            //If vehicle routes are to be generated
            if (!input.getSolutionMethod().equals(SolutionMethod.NO_VEHICLES)) {
                if (nextVehicleArrival.getTime() < nextEventTime) {
                    nextEventTime = nextVehicleArrival.getTime();
                    nextEvent = NextEvent.VEHICLE_ARRIVAL;
                }
            }



            switch (nextEvent) {

                case SIMULATION_STOP:
                    stopTimeReached = true;
                    break;

                case NEW_VEHICLE_ROUTES:
                    //Generate new routes
                    determineRemainingDrivingTimeAndStation(timeToNewVehicleRoutes, input.getVehicles(), vehicleArrivals );
                    input.setCurrentMinute(timeToNewVehicleRoutes);

                    //Start timer
                    StopWatch stopWatchTotalComputationTime = new StopWatch();
                    stopWatchTotalComputationTime.start();

                    generateVehicleRoute(input);

                    if (input.getSolutionMethod().equals(SolutionMethod.HEURISTIC_VERSION_3)) {
                        vehicleArrivals = ReadXpressResult.readVehicleArrivalsVersion3(input.getVehicles(), input.getCurrentMinute());
                    } else {
                        vehicleArrivals = ReadXpressResult.readVehicleArrivals(input.getCurrentMinute());         //Actual arrival times minutes
                    }

                    //Stop timer
                    stopWatchTotalComputationTime.stop();
                    computationalTimesXpressPlussInitialization.add(stopWatchTotalComputationTime.getElapsedTimeSecs());

                    //Update nextVehicleArrival
                    vehicleArrivalCounter = 0;
                    nextVehicleArrival = vehicleArrivals.get(vehicleArrivalCounter);
                    timeToNewVehicleRoutes = NextSimulation.determineTimeToNextSimulation(vehicleArrivals, input.getTimeHorizon(), input.getReOptimizationMethod(), input.getCurrentMinute());      //Actual time minutes
                    this.timeToNextSimulationList.add(timeToNewVehicleRoutes-input.getCurrentMinute());

                    System.out.println();
                    System.out.println("Remaining time: " + (simulationStopTime - timeToNewVehicleRoutes));
                    break;

                case CUSTOMER_ARRIVAL:
                    upDateLoadAndViolation(nextCustomerArrival, input.getStations());
                    totalNumberOfCustomers ++;
                    nextCustomerArrival.updateNextCustomerArrival(in, input.getCurrentMinute(), simulationStopTime);
                    input.setCurrentMinute(nextCustomerArrival.getTime());
                    break;

                case VEHICLE_ARRIVAL:
                    updateStationAfterVehicleArrival(nextVehicleArrival, input.getStations(), input.getVehicles());
                    vehicleArrivalCounter ++;
                    nextVehicleArrival = updateNextVehicleArrival(vehicleArrivals, vehicleArrivalCounter, simulationStopTime);
                    input.setCurrentMinute(nextVehicleArrival.getTime());
                    break;

            }
        }
    }

    //Find next vehicle arrival
    private VehicleArrival updateNextVehicleArrival(ArrayList<VehicleArrival> vehicleArrivals, int vehicleArrivalCounter, double simulationStopTime) {

        int lengthVehicleArrivals = vehicleArrivals.size();
        if (vehicleArrivalCounter < lengthVehicleArrivals) {
            return vehicleArrivals.get(vehicleArrivalCounter);
        } else {
            return new VehicleArrival(simulationStopTime);
        }
    }

    //Update load and violation when a customer arrives at a station
    private void upDateLoadAndViolation (CustomerArrival nextCustomerArrival, HashMap<Integer, Station> stations) {
        Station station = stations.get(nextCustomerArrival.getStationId());
        if (nextCustomerArrival.getLoad() > 0) {
            //Check for congestion
            if (station.getLoad() + 1 > station.getCapacity() ) {
                this.congestions++;
            } else {
                station.addBikeToStation(1);
                this.happyCustomers ++;
            }
        } else if (nextCustomerArrival.getLoad() < 0) {
            //Check for starvation
            if (station.getLoad() - 1 < 0) {
                this.starvations++;
            } else {
                station.addBikeToStation(-1);
                this.happyCustomers ++;
            }
        }
        this.totalNumberOfCustomers++;
    }

    //Update load when a vehicle arrives at a station
    private void updateStationAfterVehicleArrival (VehicleArrival nextVehicleArrival, HashMap<Integer, Station> stations, HashMap<Integer, Vehicle> vehicles) {
        int vehicleId = nextVehicleArrival.getVehicle();
        Vehicle vehicle = vehicles.get(vehicleId);
        int vehicleLoad = vehicle.getLoad();
        int vehicleCapacity = vehicle.getCapacity();

        int stationId = nextVehicleArrival.getStationId();
        Station station = stations.get(stationId);
        double stationLoad = station.getLoad();
        int stationCapacity = station.getCapacity();
        int load = nextVehicleArrival.getLoad();

        //Load to station
        if (load > 0) {
            if (stationLoad + load > stationCapacity) {
                load = (int) (stationCapacity - stationLoad);
            }
            if (load > vehicleLoad) {
                load = vehicleLoad;
            }
            station.addBikeToStation(load);
            vehicle.addLoad(-load);
        }

        //Unload from station
        else {
            load = -load;
            if (stationLoad < load ) {
                load = (int) (stationLoad);
            }
            if (vehicleCapacity - vehicleLoad < load) {
                load = vehicleCapacity - vehicleLoad;
            }
            station.addBikeToStation(-load);
            vehicle.addLoad(load);
        }
    }

    //Generate new vehicle routes
    private void generateVehicleRoute(Input input) throws IOException, XPRMCompileException {
        numberOfTimesVehicleRouteGenerated ++;
        double computationalTimeXpress;

        switch (input.getSolutionMethod()) {
            case HEURISTIC_VERSION_1:
                HeuristicVersion1 heuristicVersion1 = new HeuristicVersion1(input);
                computationalTimeXpress = heuristicVersion1.getComputationalTimeXpress();
                break;
            case HEURISTIC_VERSION_2:
                HeuristicVersion2 heuristicVersion2 = new HeuristicVersion2(input);
                computationalTimeXpress = heuristicVersion2.getComputationalTimeXpress();
                break;
            case HEURISTIC_VERSION_3:
                HeuristicVersion3 heuristicVersion3 = new HeuristicVersion3(input);
                computationalTimeXpress = heuristicVersion3.getComputationalTimeXpress();
                break;
            case EXACT_METHOD:
                ExactMethod exactMethod = new ExactMethod(input);
                computationalTimeXpress = exactMethod.getComputationalTimeXpress();
                break;
            case CURRENT_SOLUTION_IN_OSLO:
                CurrentSolutionInOslo currentSolutionInOslo = new CurrentSolutionInOslo(input);
                computationalTimeXpress = 0;                                                                //Foreløpig
                break;
            case NO_VEHICLES:                                                                               //Kan muligens droppe denne
                NoVehicles noVehicles = new NoVehicles(input);
                computationalTimeXpress = 0;
                break;
            default:
                computationalTimeXpress = 0;
        }
        computationalTimesXpress.add(computationalTimeXpress);
    }

    //Determine time to next station, works as input to next vehicle route generation
    private void determineRemainingDrivingTimeAndStation(double timeForNewVehicleRoutes, HashMap<Integer, Vehicle> vehicles, ArrayList<VehicleArrival> vehicleArrivals) {
        for (VehicleArrival vehicleArrival : vehicleArrivals) {

            boolean vehicleArrivalBeforeGeneratingNewRoutes = vehicleArrival.getTime() < timeForNewVehicleRoutes;
            boolean nextVehicleArrivalAfterOrAtTimeForGeneratingNewRoutes = vehicleArrival.getTimeNextVisit() >= timeForNewVehicleRoutes;
            boolean vehicleArrivalFirstVisit = vehicleArrival.isFirstvisit();
            boolean vehicleArrivalAfterOrAtTimeForGeneratingNewRoutes = vehicleArrival.getTime() >= timeForNewVehicleRoutes;
            boolean nextStationIsArtificialStation = vehicleArrival.getNextStationId() == 0;

            if ( vehicleArrivalBeforeGeneratingNewRoutes & nextVehicleArrivalAfterOrAtTimeForGeneratingNewRoutes & !nextStationIsArtificialStation) {
                int vehicleId = vehicleArrival.getVehicle();
                Vehicle vehicle = vehicles.get(vehicleId);
                double timeToNextStation = vehicleArrival.getTimeNextVisit()-timeForNewVehicleRoutes;
                vehicle.setTimeToNextStation(timeToNextStation);
                vehicle.setNextStation(vehicleArrival.getNextStationId());

            } else if (vehicleArrivalFirstVisit & vehicleArrivalAfterOrAtTimeForGeneratingNewRoutes){
                int vehicleId = vehicleArrival.getVehicle();
                Vehicle vehicle = vehicles.get(vehicleId);
                double timeToNextStation = vehicleArrival.getTime()-timeForNewVehicleRoutes;
                vehicle.setTimeToNextStation(timeToNextStation);
                vehicle.setNextStation(vehicleArrival.getStationId());

            }  else if (nextStationIsArtificialStation & vehicleArrivalBeforeGeneratingNewRoutes ) {
                int vehicleId = vehicleArrival.getVehicle();
                Vehicle vehicle = vehicles.get(vehicleId);
                vehicle.setTimeToNextStation(0);
                vehicle.setNextStation(vehicleArrival.getStationId());
            }
        }
    }





    //Getters and setters

    public double getNumberOfTimesVehicleRouteGenerated() {
        return numberOfTimesVehicleRouteGenerated;
    }

    public void setNumberOfTimesVehicleRouteGenerated(double numberOfTimesVehicleRouteGenerated) {
        this.numberOfTimesVehicleRouteGenerated = numberOfTimesVehicleRouteGenerated;
    }
    public double getCongestions() {
        return congestions;
    }

    public void setCongestions(double congestions) {
        this.congestions = congestions;
    }

    public double getStarvations() {
        return starvations;
    }

    public void setStarvations(double starvations) {
        this.starvations = starvations;
    }

    public int getTotalNumberOfCustomers() {
        return totalNumberOfCustomers;
    }

    public void setTotalNumberOfCustomers(int totalNumberOfCustomers) {
        this.totalNumberOfCustomers = totalNumberOfCustomers;
    }

    public int getHappyCustomers() {
        return happyCustomers;
    }

    public void setHappyCustomers(int happyCustomers) {
        this.happyCustomers = happyCustomers;
    }

    public ArrayList<Double> getTimeToNextSimulationList() {
        return timeToNextSimulationList;
    }

    public void setTimeToNextSimulationList(ArrayList<Double> timeToNextSimulationList) {
        this.timeToNextSimulationList = timeToNextSimulationList;
    }

    public ArrayList<Double> getComputationalTimesXpress() {
        return computationalTimesXpress;
    }

    public void setComputationalTimesXpress(ArrayList<Double> computationalTimesXpress) {
        this.computationalTimesXpress = computationalTimesXpress;
    }

    public ArrayList<Double> getComputationalTimesXpressPlussInitialization() {
        return computationalTimesXpressPlussInitialization;
    }

    public void setComputationalTimesXpressPlussInitialization(ArrayList<Double> computationalTimesXpressPlussInitialization) {
        this.computationalTimesXpressPlussInitialization = computationalTimesXpressPlussInitialization;
    }
}






















