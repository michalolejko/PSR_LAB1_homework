
public class Course {

    private String destination, departure;
    private int departureHour, arrivalHour;

    public Course(String destination, String departure, int departureHour, int arrivalHour) {
        this.departureHour = departureHour;
        this.arrivalHour = arrivalHour;
        this.departure = departure;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Course from " + departure + " to " + destination + " at " + departureHour + " to " + arrivalHour;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public int getDepartureHour() {
        return departureHour;
    }

    public void setDepartureHour(int departureHour) {
        this.departureHour = departureHour;
    }

    public int getArrivalHour() {
        return arrivalHour;
    }

    public void setArrivalHour(int arrivalHour) {
        this.arrivalHour = arrivalHour;
    }
}
