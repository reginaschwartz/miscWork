package parking;

import parking.spot.NarrowSpot;
import parking.spot.ParkingSpot;
import parking.spot.TruckSpot;
import parking.spot.WideSpot;
import parking.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParkingLot {
    private final String name;
    private final List<ParkingSpot> spots = new ArrayList<>();

    public ParkingLot(String name, int wideSpots, int narrowSpots, int truckSpots) {
        this.name = name;

        int spotNumber = 1;
        for (int i = 0; i < wideSpots; i++) {
            spots.add(new WideSpot(spotNumber++));
        }
        for (int i = 0; i < narrowSpots; i++) {
            spots.add(new NarrowSpot(spotNumber++));
        }
        for (int i = 0; i < truckSpots; i++) {
            spots.add(new TruckSpot(spotNumber++));
        }
    }

    public Optional<ParkingSpot> enter(Vehicle vehicle) {
        Optional<ParkingSpot> available = spots.stream()
                .filter(spot -> !spot.isOccupied() && spot.canFit(vehicle))
                .findFirst();

        available.ifPresent(spot -> spot.park(vehicle));
        return available;
    }

    public Optional<ParkingSpot> exit(String licensePlate) {
        Optional<ParkingSpot> occupiedSpot = spots.stream()
                .filter(spot -> spot.isOccupied() &&
                        spot.getParkedVehicle().getLicensePlate().equals(licensePlate))
                .findFirst();

        occupiedSpot.ifPresent(ParkingSpot::vacate);
        return occupiedSpot;
    }

    public void printStatus() {
        System.out.println("\n=== " + name + " Status ===");
        spots.forEach(System.out::println);
        System.out.println("Total: " + spots.size() +
                " | Occupied: " + spots.stream().filter(ParkingSpot::isOccupied).count() +
                " | Free: " + spots.stream().filter(s -> !s.isOccupied()).count());
    }
}
