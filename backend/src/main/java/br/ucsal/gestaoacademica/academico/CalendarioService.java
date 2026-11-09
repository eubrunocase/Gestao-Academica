package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarioService {

    private final CalendarioAcademicoRepository calendarioRepository;
    private final AuditoriaService auditoriaService;

    public CalendarioService(CalendarioAcademicoRepository calendarioRepository,
            AuditoriaService auditoriaService) {
        this.calendarioRepository = calendarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<CalendarioResponse> listar() {
        return calendarioRepository.findAllByOrderByPeriodoLetivoDesc().stream()
                .map(CalendarioResponse::from)
                .toList();
    }

    @Transactional
    public CalendarioResponse criar(CalendarioRequest request) {
        if (calendarioRepository.findByPeriodoLetivo(request.periodoLetivo()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Periodo letivo ja cadastrado");
        }
        validarDatas(request);
        CalendarioAcademico calendario = new CalendarioAcademico();
        aplicar(calendario, request);
        calendarioRepository.save(calendario);
        auditoriaService.registrarAuditoria("CRIAR", "CalendarioAcademico", calendario.getId(),
                calendario.getPeriodoLetivo());
        return CalendarioResponse.from(calendario);
    }

    @Transactional
    public CalendarioResponse atualizar(Long id, CalendarioRequest request) {
        CalendarioAcademico calendario = calendarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Calendario nao encontrado"));
        calendarioRepository.findByPeriodoLetivo(request.periodoLetivo())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "Periodo letivo ja cadastrado");
                });
        validarDatas(request);
        aplicar(calendario, request);
        calendarioRepository.save(calendario);
        auditoriaService.registrarAuditoria("ATUALIZAR", "CalendarioAcademico", id,
                calendario.getPeriodoLetivo());
        return CalendarioResponse.from(calendario);
    }

    private void aplicar(CalendarioAcademico calendario, CalendarioRequest request) {
        calendario.setPeriodoLetivo(request.periodoLetivo());
        calendario.setInicioMatricula(request.inicioMatricula());
        calendario.setFimMatricula(request.fimMatricula());
        calendario.setFimPeriodo(request.fimPeriodo());
    }

    private void validarDatas(CalendarioRequest request) {
        if (request.fimMatricula().isBefore(request.inicioMatricula())) {
            throw new BusinessException("Fim da matricula deve ser posterior ao inicio");
        }
        if (request.fimPeriodo().isBefore(request.fimMatricula())) {
            throw new BusinessException("Fim do periodo deve ser posterior ao fim da matricula");
        }
    }
}
