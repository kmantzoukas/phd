package uk.ac.city.toreador.rest.api.toreador.entities;

public class Availability {

    private int time;
    private TimeUnit units;

    public int getParameter(){
        switch (units){
            case SECONDS:
                return (time*1000);
            case MINUTES:
                return (time*1000*60);
            case HOURS:
                return (time*1000*60*60);
            case DAYS:
                return (time*1000*60*60*24);
            default:
                return 0;
        }
    }
}
