package com.usuarios.demo.entities;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VinculacionId implements Serializable {

    private String paciente;
    private String medico;

    public VinculacionId() {
    }

    public VinculacionId(String paciente, String medico) {
        this.paciente = paciente;
        this.medico = medico;
    }
}
