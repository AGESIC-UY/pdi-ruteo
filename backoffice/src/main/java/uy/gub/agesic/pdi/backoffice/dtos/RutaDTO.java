package uy.gub.agesic.pdi.backoffice.dtos;


import uy.gub.agesic.pdi.common.utiles.dtos.BaseDTO;

public class RutaDTO extends BaseDTO {

    private String logical;
    private String physical;
    private String baseURI;
    private Boolean degraded;
    private Long degradeTimeout;
    private Long degradePermits;

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

    public Long getDegradePermits() {
        return degradePermits;
    }

    public void setDegradePermits(Long degradePermits) {
        this.degradePermits = degradePermits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RutaDTO rutaDTO = (RutaDTO) o;

        if (logical != null ? !logical.equals(rutaDTO.logical) : rutaDTO.logical != null) return false;
        if (physical != null ? !physical.equals(rutaDTO.physical) : rutaDTO.physical != null) return false;
        if (baseURI != null ? !baseURI.equals(rutaDTO.baseURI) : rutaDTO.baseURI != null) return false;
        if (degraded != null ? !degraded.equals(rutaDTO.degraded) : rutaDTO.degraded != null) return false;
        if (degradeTimeout != null ? !degradeTimeout.equals(rutaDTO.degradeTimeout) : rutaDTO.degradeTimeout != null)
            return false;
        return degradePermits != null ? degradePermits.equals(rutaDTO.degradePermits) : rutaDTO.degradePermits == null;
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
    public int hashCode() {
        int result = logical != null ? logical.hashCode() : 0;
        result = 31 * result + (physical != null ? physical.hashCode() : 0);
        result = 31 * result + (baseURI != null ? baseURI.hashCode() : 0);
        result = 31 * result + (degraded != null ? degraded.hashCode() : 0);
        result = 31 * result + (degradeTimeout != null ? degradeTimeout.hashCode() : 0);
        result = 31 * result + (degradePermits != null ? degradePermits.hashCode() : 0);
        return result;
    }
}
