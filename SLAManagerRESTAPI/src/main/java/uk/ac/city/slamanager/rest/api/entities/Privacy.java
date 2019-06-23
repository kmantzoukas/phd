package uk.ac.city.slamanager.rest.api.entities;

public class Privacy {

    private String trustedIPS;

    public String[] getParameters(){
        return trustedIPS.split(",");
    }

}
