package repositories;

import entities.Medicamentos;
import entities.Paciente;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentosRepository extends JpaRepository<Medicamentos, Long> {
    void deleteByPaciente(Paciente paciente);
    List<Medicamentos> findByPaciente(Paciente paciente);
}
