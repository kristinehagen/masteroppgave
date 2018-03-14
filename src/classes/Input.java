package classes;

import functions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Input {

    private double currentMinute = 8*60;              //Minutes
    private double simulationStopTime = 10 *60;
    private double timeHorizon = 20;
    private SolutionMethod solutionMethod = SolutionMethod.ColumnGenerationLoadInXpress;


    //--------INITIALIZATION--------------
    private int nrStationBranching = 1;             //Create n new routes IN each branching
    private int minLoad = 8;                        //Initial vehicle load må være i intervallet [Min max] for å kunne kjøre til positive og negative stasjoner.
    private int maxLoad = 15;




    //----------COLUMN GENERATION-----------
    //Score
    private double weightTimeToViolation = -0.2;
    private double weightViolationRate = 0.25;
    private double weightDrivingTime = -0.1;
    private double weightOptimalState = 0.25;

    //Xpress objective function
    private double weightViolation = 0.6;
    private double weightDeviation = 0.3;
    private double weightReward = 0.1;
    private double weightDeviationReward  = 0.6;
    private double weightDrivingTimePenalty = 0.4;

    //Xpress
    private String timedependentInoutFile = "timeDependentInputFile.txt";
    private String fixedInputFile = "fixedInputFile.txt";
    private int maxVisit = 2;




    //--------GENETIC ALGORITHM------------
    private int maxNumberOfGenerations = 400;
    private int sizeOfPopulation = 20;
    private int tournamentParticipants = 2;
    private double crossoverProbability = 0.8;
    private double intraMutationProbability = 1;




    //------------Constants----------------
    private double vehicleHandlingTime = 0.25;
    private double vehicleParkingTime = 2;
    private String demandFile = "demand.txt";
    private String initialStationFile = "stationInitial.txt";
    private String vehicleInitialFile = "vehicleInitial.txt";





    private ArrayList<Integer> stationIdList;
    private HashMap<Integer, Station> stations;
    private HashMap<Integer, Vehicle> vehicles;
    private ArrayList<Station> stationListWithDemand;


    //Constructor
    public Input() throws IOException {
        this.stationIdList = ReadStationInitialState.readStationInitialState(initialStationFile);
        this.stations = ReadDemandAndNumberOfBikes.readStationInformation(stationIdList, demandFile, initialStationFile);
        ReadCoordinates.lookUpCoordinates(stations, stationIdList);
        this.vehicles = ReadVehicleInput.readVehicleInput(vehicleInitialFile);
        ReadDistanceMatrix.lookUpDrivingTimes(stations, stationIdList);
    }

    public Input(double hour) throws FileNotFoundException {
        this.stationListWithDemand = ReadDemandAndNumberOfBikes.readDemandInformationForGeneratingInstances(demandFile, hour);
    }

    //Getters and setters

    public double getIntraMutationProbability() {
        return intraMutationProbability;
    }

    public int getTournamentParticipants() {
        return tournamentParticipants;
    }

    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public int getSizeOfPopulation() {
        return sizeOfPopulation;
    }

    public int getNumberOfVehicles() {
        return vehicles.size();
    }

    public int getNumberOfStations(){
        return stations.size();
    }

    public HashMap<Integer, Vehicle> getVehicles() {
        return vehicles;
    }

    public Vehicle getVehicle(int vehicleID) {
        return this.vehicles.get(vehicleID);
    }

    public String getDemandFile() {
        return demandFile;
    }

    public String getInitialStationFile() {
        return initialStationFile;
    }

    public HashMap<Integer, Station> getStations() {
        return stations;
    }

    public ArrayList<Integer> getStationIdList() {
        return stationIdList;
    }

    public int getMaxNumberOfGenerations() {
        return maxNumberOfGenerations;
    }

    public void setMaxNumberOfGenerations(int maxNumberOfGenerations) {
        this.maxNumberOfGenerations = maxNumberOfGenerations;
    }

    public int getNrStationBranching() {
        return nrStationBranching;
    }

    public void setNrStationBranching(int nrStationBranching) {
        this.nrStationBranching = nrStationBranching;
    }

    public double getWeightOptimalState() {
        return weightOptimalState;
    }

    public void setWeightOptimalState(double weightOptimalState) {
        this.weightOptimalState = weightOptimalState;
    }

    public double getWeightDrivingTime() {
        return weightDrivingTime;
    }

    public void setWeightDrivingTime(double weightDrivingTime) {
        this.weightDrivingTime = weightDrivingTime;
    }

    public double getWeightViolationRate() {
        return weightViolationRate;
    }

    public void setWeightViolationRate(double weightViolationRate) {
        this.weightViolationRate = weightViolationRate;
    }

    public double getWeightTimeToViolation() {
        return weightTimeToViolation;
    }

    public void setWeightTimeToViolation(double weightTimeToViolation) {
        this.weightTimeToViolation = weightTimeToViolation;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }

    public int getMinLoad() {
        return minLoad;
    }

    public void setMinLoad(int minLoad) {
        this.minLoad = minLoad;
    }

    public double getCurrentMinute() {
        return currentMinute;
    }

    public void setCurrentMinute(double currentMinute) {
        this.currentMinute = currentMinute;
    }

    public double getTimeHorizon() {
        return timeHorizon;
    }

    public void setTimeHorizon(double timeHorizon) {
        this.timeHorizon = timeHorizon;
    }

    public ArrayList<Station> getStationListWithDemand() {
        return stationListWithDemand;
    }

    public void setStationListWithDemand(ArrayList<Station> stationListWithDemand) {
        this.stationListWithDemand = stationListWithDemand;
    }

    public SolutionMethod getSolutionMethod() {
        return solutionMethod;
    }

    public void setSolutionMethod(SolutionMethod solutionMethod) {
        this.solutionMethod = solutionMethod;
    }

    public double getVehicleHandlingTime() {
        return vehicleHandlingTime;
    }

    public void setVehicleHandlingTime(double vehicleHandlingTime) {
        this.vehicleHandlingTime = vehicleHandlingTime;
    }

    public double getVehicleParkingTime() {
        return vehicleParkingTime;
    }

    public void setVehicleParkingTime(double vehicleParkingTime) {
        this.vehicleParkingTime = vehicleParkingTime;
    }

    public String getTimedependentInoutFile() {
        return timedependentInoutFile;
    }

    public void setTimedependentInoutFile(String timedependentInoutFile) {
        this.timedependentInoutFile = timedependentInoutFile;
    }

    public String getFixedInputFile() {
        return fixedInputFile;
    }

    public void setFixedInputFile(String fixedInputFile) {
        this.fixedInputFile = fixedInputFile;
    }

    public double getWeightViolation() {
        return weightViolation;
    }

    public void setWeightViolation(double weightViolation) {
        this.weightViolation = weightViolation;
    }

    public double getWeightDeviation() {
        return weightDeviation;
    }

    public void setWeightDeviation(double weightDeviation) {
        this.weightDeviation = weightDeviation;
    }

    public double getWeightReward() {
        return weightReward;
    }

    public void setWeightReward(double weightReward) {
        this.weightReward = weightReward;
    }

    public double getWeightDeviationReward() {
        return weightDeviationReward;
    }

    public void setWeightDeviationReward(double weightDeviationReward) {
        this.weightDeviationReward = weightDeviationReward;
    }

    public double getWeightDrivingTimePenalty() {
        return weightDrivingTimePenalty;
    }

    public void setWeightDrivingTimePenalty(double weightDrivingTimePenalty) {
        this.weightDrivingTimePenalty = weightDrivingTimePenalty;
    }

    public int getMaxVisit() {
        return maxVisit;
    }

    public void setMaxVisit(int maxVisit) {
        this.maxVisit = maxVisit;
    }
}
