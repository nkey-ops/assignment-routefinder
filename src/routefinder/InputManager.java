package routefinder;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InputManager {
    private final IRouteFinder routeFinder = new RouteFinder();

    private static final String DESTINATION_LETTER_REQUEST_MESSAGE =
            "Please enter a letter that your destinations start with ";
    private static final String DESTINATION_NAME_REQUEST_MESSAGE =
            "Please enter your destination: ";

    private static final String BUS_TRIPS_LENGTHS =
            "\uD83D\uDE0D Bus Trips Lengths in Minutes are: ";
    private static final String NOT_FOUND =
            "Nothing was found. Try again.";
    private static final String CONTINUE_OR_EXIT =
            "Do you want to check different destination? " +
                    "Please type Y to continue or press any other key to exit ";


    public void start() {
        do {
            char destinationLetter = getDestinationLetter();

            Map<String, Map<String, String>> busRoutes =
                    routeFinder.getBusRoutesUrls(destinationLetter);

            if (busRoutes.isEmpty()) {
                System.out.println(NOT_FOUND);
                start();
                return;
            }

            printBusRoutes(busRoutes);

            String destinationName = getDestinationName();
            if (!busRoutes.containsKey(destinationName)) {
                System.out.println(NOT_FOUND);
                start();
                return;
            }

            Map<String, String> routTrips = busRoutes.get(destinationName);
            Map<String, List<Long>> routTripsWithLength =
                    routeFinder.getBusRouteTripsLengthsInMinutesToAndFromDestination(routTrips);

            printRoutTrips(routTripsWithLength);

        } while (isContinue());
    }

    private boolean isContinue() {
        System.out.print(CONTINUE_OR_EXIT);
        Scanner scanner = new Scanner(System.in);
        
        return scanner.next().equalsIgnoreCase("Y");
    }   

    private char getDestinationLetter() {
        Scanner scanner = new Scanner(System.in);

        System.out.print(DESTINATION_LETTER_REQUEST_MESSAGE);

        String input;
        while (!(input = scanner.next()).matches("^\\w$")) {
            System.out.print("\n" + DESTINATION_LETTER_REQUEST_MESSAGE);
        }

        return input.charAt(0);
    }

    private void printBusRoutes(Map<String, Map<String, String>> busRoutesUrls) {
        for (Map.Entry<String, Map<String, String>> entry : busRoutesUrls.entrySet()) {
            System.out.println("Destination: " + entry.getKey());

            for (Map.Entry<String, String> routeDetailsEntry : entry.getValue().entrySet()) {
                System.out.println("Bus number: " + routeDetailsEntry.getKey());
            }
            System.out.println("+".repeat(40));
        }
    }

    private String getDestinationName() {
        Scanner scanner = new Scanner(System.in);

        System.out.print(DESTINATION_NAME_REQUEST_MESSAGE);
        String destination = scanner.nextLine();
        System.out.println();

        return destination;
    }

    private void printRoutTrips(Map<String, List<Long>> routTrips) {
        System.out.println(BUS_TRIPS_LENGTHS);
        System.out.println(routTrips + "\n");
    }
}
