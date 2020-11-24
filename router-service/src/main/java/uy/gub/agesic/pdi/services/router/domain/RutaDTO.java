package uy.gub.agesic.pdi.services.router.domain;

import java.io.Serializable;

public class RutaDTO implements Serializable, Comparable<RutaDTO> {

    // Direccion logica de la ruta, viene dentro de los cabezales de addressing
    private String logical;

    // Direccion fisica hacia la que se hace el ruteo
    private String physical;

    // Direccion base a utilizar para enviar a DATAPOWER
    private String baseURI;

    // Indicador de si esta ruta esta en estado degradado
    private Boolean degraded;

    // Es el timeout que causa el pasaje a estado degradado
    private Long degradeTimeout;

    // Cantidad de permisos de acceso
    private Integer degradePermits;

    public String getLogical() {
        return logical;
    }

    public void setLogical(String logical) {
        this.logical = logical;
    }

    public String getPhysical() {
        return physical;
    }

    public void setPhysical(String physical) {
        this.physical = physical;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public Boolean getDegraded() {
        return degraded;
    }

    public void setDegraded(Boolean degraded) {
        this.degraded = degraded;
    }

    public Long getDegradeTimeout() {
        return degradeTimeout;
    }

    public void setDegradeTimeout(Long degradeTimeout) {
        this.degradeTimeout = degradeTimeout;
    }

    public Integer getDegradePermits() {
        return degradePermits;
    }

    public void setDegradePermits(Integer degradePermits) {
        this.degradePermits = degradePermits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RutaDTO route = (RutaDTO) o;

        if (logical != null ? !logical.equals(route.logical) : route.logical != null) return false;
        if (physical != null ? !physical.equals(route.physical) : route.physical != null) return false;
        if (baseURI != null ? !baseURI.equals(route.baseURI) : route.baseURI != null) return false;
        if (degraded != null ? !degraded.equals(route.degraded) : route.degraded != null) return false;
        if (degradeTimeout != null ? !degradeTimeout.equals(route.degradeTimeout) : route.degradeTimeout != null)
            return false;
        return degradePermits != null ? degradePermits.equals(route.degradePermits) : route.degradePermits == null;
    }

    @Override
    public int hashCode() {
        int result = logical != null ? logical.hashCode() : 0;
        result = 31 * result + (physical != null ? physical.hashCode() : 0);
        result = 31 * result + (baseURI != null ? baseURI.hashCode() : 0);
        result = 31 * result + (degraded != null ? degraded.hashCode() : 0);
        result = 31 * result + (degradeTimeout != null ? degradeTimeout.hashCode() : 0);
        result = 31 * result + (degradePermits != null ? degradePermits.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RutaDTO{" +
                "logical='" + logical + '\'' +
                ", physical='" + physical + '\'' +
                ", baseURI='" + baseURI + '\'' +
                ", degraded=" + degraded +
                ", degradeTimeout=" + degradeTimeout +
                ", degradePermits=" + degradePermits +
                '}';
    }

    @Override
    public int compareTo(RutaDTO o) {

        if(logical.compareTo(o.getLogical()) < 0)
            return -1;
        else if(logical.compareTo(o.getLogical()) > 0)
                return 1;
        else return 0;
    }

}
