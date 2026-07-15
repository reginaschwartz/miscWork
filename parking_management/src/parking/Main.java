package parking;

import parking.spot.ParkingSpot;
import parking.vehicle.Car;
import parking.vehicle.Motorcycle;
import parking.vehicle.Truck;
import parking.vehicle.Vehicle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        String strs = "{())))}" ;
        isValid(strs);
       // System.out.println(prefix);

    }


    public static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> pairs = Map.of(
                ')', '(',
                '}', '{',
                ']', '['
        );

        for (char c : s.toCharArray()) {
            if (pairs.containsKey(c)) {
                if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                    return false;
                }
            } else {
                stack.push(c);
            }
        }

        return stack.isEmpty();
    }

//    public static String longestCommonPrefix(String[] strs) {
//        if (strs == null || strs.length == 0) {
//            return "";
//        }
//
//        for (int i = 0; i < strs[0].length(); i++) {
//            char c = strs[0].charAt(i);
//
//            for (int j = 1; j < strs.length; j++) {
//                if (i == strs[j].length() || strs[j].charAt(i) != c) {
//                    return strs[0].substring(0, i);
//                }
//            }
//        }
//
//        return strs[0];
//    }
//    public static void main(String[] args) {
//
//        // Create parking lot: 4 wide spots, 4 narrow spots, 4 truck spots
//        ParkingLot lot = new ParkingLot("Central Parking", 4, 4, 4);
//
//        lot.printStatus();
//
//        // --- Fill all wide spots with cars (4/4) ---
//        System.out.println("\n--- Cars Entering (4 wide spots available) ---");
//        Car car1 = new Car("ABC-101");
//        Car car2 = new Car("ABC-102");
//        Car car3 = new Car("ABC-103");
//        Car car4 = new Car("ABC-104");
//        Car car5 = new Car("ABC-105");   // wide spots full — will be rejected
//        assignSpot(lot, car1);
//        assignSpot(lot, car2);
//        assignSpot(lot, car3);
//        assignSpot(lot, car4);
//        assignSpot(lot, car5);           // ERROR: no room for cars
//
//        // --- Fill all narrow spots with motorcycles (4/4) ---
//        System.out.println("\n--- Motorcycles Entering (4 narrow spots available) ---");
//        Motorcycle moto1 = new Motorcycle("M-001");
//        Motorcycle moto2 = new Motorcycle("M-002");
//        Motorcycle moto3 = new Motorcycle("M-003");
//        Motorcycle moto4 = new Motorcycle("M-004");
//        Motorcycle moto5 = new Motorcycle("M-005");   // narrow spots full — will be rejected
//        assignSpot(lot, moto1);
//        assignSpot(lot, moto2);
//        assignSpot(lot, moto3);
//        assignSpot(lot, moto4);
//        assignSpot(lot, moto5);           // ERROR: no room for motorcycles
//
//        // --- Fill all truck spots with trucks (4/4) ---
//        System.out.println("\n--- Trucks Entering (4 truck spots available) ---");
//        Truck truck1 = new Truck("T-001");
//        Truck truck2 = new Truck("T-002");
//        Truck truck3 = new Truck("T-003");
//        Truck truck4 = new Truck("T-004");
//        Truck truck5 = new Truck("T-005");   // truck spots full — will be rejected
//        assignSpot(lot, truck1);
//        assignSpot(lot, truck2);
//        assignSpot(lot, truck3);
//        assignSpot(lot, truck4);
//        assignSpot(lot, truck5);             // ERROR: no room for trucks
//
//        lot.printStatus();
//
//        // --- Some vehicles leave, freeing up spots ---
//        System.out.println("\n--- Vehicles Leaving ---");
//        leave(lot, "ABC-101");
//        leave(lot, "M-001");
//        leave(lot, "T-001");
//
//        lot.printStatus();
//
//        // --- Previously rejected vehicles can now enter ---
//        System.out.println("\n--- New Vehicles Entering After Vacating ---");
//        assignSpot(lot, car5);
//        assignSpot(lot, moto5);
//        assignSpot(lot, truck5);
//
//        lot.printStatus();
//    }

    private static void assignSpot(ParkingLot lot, Vehicle vehicle) {
        Optional<ParkingSpot> spot = lot.enter(vehicle);
        if (spot.isPresent()) {
            System.out.println(vehicle + " -> assigned to " + spot.get().getSpotType() +
                               " Spot #" + spot.get().getSpotNumber());
        } else {
            System.out.println(vehicle + " -> NO available spot! Lot is full for this vehicle type.");
        }
    }

    private static void leave(ParkingLot lot, String licensePlate) {
        Optional<ParkingSpot> spot = lot.exit(licensePlate);
        if (spot.isPresent()) {
            System.out.println("Vehicle XXX [" + licensePlate + "] left Spot #" + spot.get().getSpotNumber());
        } else {
            System.out.println("Vehicle [" + licensePlate + "] not found in lot.");
        }
    }
}
