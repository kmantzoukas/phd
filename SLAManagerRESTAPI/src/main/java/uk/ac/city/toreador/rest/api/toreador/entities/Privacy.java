package uk.ac.city.toreador.rest.api.toreador.entities;

public class Privacy {

    private String trustedIPS;

    public String[] getParameters(){
        return trustedIPS.split(",");
    }

}
