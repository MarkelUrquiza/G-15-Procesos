package com.deustocoches.repository;
import com.deustocoches.model.Coche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface CocheRepository extends JpaRepository<Coche, String> {
        Coche findByMatricula(String matricula) ;
}