package repositories;

import entities.Vinculacion;
import entities.VinculacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VinculacionRepository extends JpaRepository<Vinculacion, VinculacionId> {
    List<Vinculacion> findByMedico_TarjetaProfesional(String tarjetaProfesional);
}
