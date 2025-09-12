package br.com.naildesigner.servico_service.repositories;

import br.com.naildesigner.servico_service.models.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
