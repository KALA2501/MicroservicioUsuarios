package com.usuarios.demo.entities;

import java.io.Serializable;
import java.util.Objects;

public class VinculacionId implements Serializable {

    private String paciente;
    private String medico;

    public VinculacionId() {
    }

    public VinculacionId(String paciente, String medico) {
        this.paciente = paciente;
        this.medico = medico;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VinculacionId))
            return false;
        VinculacionId that = (VinculacionId) o;
        return Objects.equals(paciente, that.paciente) &&
                Objects.equals(medico, that.medico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente, medico);
    }
}
