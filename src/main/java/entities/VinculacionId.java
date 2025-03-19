package entities;

import java.io.Serializable;
import java.util.Objects;

public class VinculacionId implements Serializable {
    private Long paciente;
    private Long medico;

    public VinculacionId() {}

    public VinculacionId(Long paciente, Long medico) {
        this.paciente = paciente;
        this.medico = medico;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VinculacionId that = (VinculacionId) o;
        return Objects.equals(paciente, that.paciente) &&
               Objects.equals(medico, that.medico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente, medico);
    }
}
