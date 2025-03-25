package entities;

import java.io.Serializable;
import java.util.Objects;

public class VinculacionId implements Serializable {
    
    private String paciente; // Este es el ID de Paciente
    private String medico;   // Este es el ID de Medico

    public VinculacionId() {}

    public VinculacionId(String paciente, String medico) {
        this.paciente = paciente;
        this.medico = medico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VinculacionId)) return false;
        VinculacionId that = (VinculacionId) o;
        return Objects.equals(paciente, that.paciente) &&
               Objects.equals(medico, that.medico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente, medico);
    }
}
