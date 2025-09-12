package br.com.naildesigner.agendamento_service_AgendeNail.controllers;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.BloqueioHorarioDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.services.BloqueioHorarioService; // Importe o NOVO serviço
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController // Indica que esta classe é um controlador REST
@RequestMapping("/api/bloqueios") // Define o caminho base para todos os endpoints deste controlador
public class BloqueioHorarioController {

    @Autowired
    private BloqueioHorarioService bloqueioHorarioService; // Injeta o NOVO serviço de bloqueios

    @PostMapping
    public ResponseEntity<BloqueioHorarioDTO> criarBloqueio(@RequestBody BloqueioHorarioDTO dto) {
        try {
            BloqueioHorarioDTO novoBloqueio = bloqueioHorarioService.salvarBloqueioHorario(dto);
            return new ResponseEntity<>(novoBloqueio, HttpStatus.CREATED); // Retorna 201 Created
        } catch (IllegalArgumentException e) {
            // Captura erros de validação como conflito de horários de bloqueio
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Retorna 400 Bad Request
        }
    }

    @GetMapping
    public ResponseEntity<List<BloqueioHorarioDTO>> listarBloqueios() {
        List<BloqueioHorarioDTO> bloqueios = bloqueioHorarioService.listarTodosBloqueiosHorarios();
        return new ResponseEntity<>(bloqueios, HttpStatus.OK); // Retorna 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloqueioHorarioDTO> buscarBloqueioPorId(@PathVariable Long id) {
        try {
            BloqueioHorarioDTO bloqueio = bloqueioHorarioService.buscarBloqueioHorarioPorId(id);
            return new ResponseEntity<>(bloqueio, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Retorna 404 Not Found
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BloqueioHorarioDTO> atualizarBloqueio(@PathVariable Long id, @RequestBody BloqueioHorarioDTO dto) {
        try {
            BloqueioHorarioDTO bloqueioAtualizado = bloqueioHorarioService.atualizarBloqueioHorario(id, dto);
            return new ResponseEntity<>(bloqueioAtualizado, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Retorna 404 Not Found
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Retorna 400 Bad Request
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirBloqueio(@PathVariable Long id) {
        try {
            bloqueioHorarioService.excluirBloqueioHorario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Retorna 204 No Content
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Retorna 404 Not Found
        }
    }

    // Você pode adicionar outros métodos conforme a necessidade, como buscar bloqueios por profissional e data
    @GetMapping("/profissional/{profissionalId}/data/{dataBloqueio}")
    public ResponseEntity<List<BloqueioHorarioDTO>> buscarBloqueiosPorProfissionalEData(
            @PathVariable UUID profissionalId,
            @PathVariable LocalDate dataBloqueio) {
        List<BloqueioHorarioDTO> bloqueios = bloqueioHorarioService.buscarBloqueiosPorProfissionalEData(profissionalId, dataBloqueio);
        return new ResponseEntity<>(bloqueios, HttpStatus.OK);
    }
}
