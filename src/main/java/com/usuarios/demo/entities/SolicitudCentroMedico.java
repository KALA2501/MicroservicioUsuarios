package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solicitud_centro_medico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCentroMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_id")
    private Long id;

    private String nombre;
    private String direccion;
    private String correo;
    private String telefono;

    @Column(name = "url_logo")
    private String urlLogo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false)
    private EstadoSolicitud estadoSolicitud = EstadoSolicitud.PENDIENTE;

    @Column(name = "procesado", nullable = false)
    private boolean procesado;

    // Getter and Setter for procesado
    public boolean isProcesado() {
        return procesado;
    }

    public void setProcesado(boolean procesado) {
        this.procesado = procesado;
    }

    public enum EstadoSolicitud {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA
    }
}
