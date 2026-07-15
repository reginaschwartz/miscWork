package parking.spot;

import parking.vehicle.Vehicle;
import parking.vehicle.VehicleType;

public class WideSpot extends ParkingSpot {

    public WideSpot(int spotNumber) {
        super(spotNumber);
    }

    @Override
    public boolean canFit(Vehicle vehicle) {
        return (vehicle.getType() == VehicleType.CAR || vehicle.getType() == VehicleType.MOTORCYCLE);
    }

    @Override
    public String getSpotType() {
        return "Wide";
    }
}
