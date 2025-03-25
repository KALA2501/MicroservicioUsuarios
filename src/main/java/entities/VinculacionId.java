package entities;

import java.io.Serializable;
import java.util.Objects;

public class VinculacionId implements Serializable {

    private String paciente;           // ID del paciente
    private String medico;            // ID del médico
    private Long tipoVinculacion;     // ID del tipo de vinculación

    public VinculacionId() {}

    public VinculacionId(String pacienteId, String medicoId, Long tipoVinculacionId) {
        this.paciente = pacienteId;
        this.medico = medicoId;
        this.tipoVinculacion = tipoVinculacionId;
    }

    // Getters y setters (opcionalmente necesarios para JPA)
    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public Long getTipoVinculacion() {
        return tipoVinculacion;
    }

    public void setTipoVinculacion(Long tipoVinculacion) {
        this.tipoVinculacion = tipoVinculacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VinculacionId)) return false;
        VinculacionId that = (VinculacionId) o;
        return Objects.equals(paciente, that.paciente) &&
               Objects.equals(medico, that.medico) &&
               Objects.equals(tipoVinculacion, that.tipoVinculacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente, medico, tipoVinculacion);
    }
}
