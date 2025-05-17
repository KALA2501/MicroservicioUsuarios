package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.ContactoEmergencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ContactoEmergenciaRepositoryTest {

    @Autowired
    private ContactoEmergenciaRepository repository;

    private ContactoEmergencia crearContactoEjemplo() {
        ContactoEmergencia contacto = new ContactoEmergencia();
        contacto.setPkId(1L);
        contacto.setNombre("Juan Emergencias");
        contacto.setApellido("Pérez");
        contacto.setTelefono("3012345678");
        contacto.setRelacion("Hermano");
        contacto.setDireccion("Carrera 9 # 99-99");
        contacto.setEmail("juan@prueba.com");
        return contacto;
    }

    @Test
    @DisplayName("Debe encontrar un contacto por teléfono si existe")
    void testFindByTelefono_existente() {
        ContactoEmergencia contacto = crearContactoEjemplo();
        repository.save(contacto);

        Optional<ContactoEmergencia> encontrado = repository.findByTelefono("3012345678");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Juan Emergencias");
    }

    @Test
    @DisplayName("Debe retornar vacío si no existe contacto con ese teléfono")
    void testFindByTelefono_inexistente() {
        Optional<ContactoEmergencia> resultado = repository.findByTelefono("0000000000");

        assertThat(resultado).isEmpty();
    }
}
