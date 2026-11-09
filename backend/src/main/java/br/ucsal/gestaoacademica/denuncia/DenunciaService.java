package br.ucsal.gestaoacademica.denuncia;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final DenunciaAndamentoRepository andamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public DenunciaService(DenunciaRepository denunciaRepository,
            DenunciaAndamentoRepository andamentoRepository, UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {
        this.denunciaRepository = denunciaRepository;
        this.andamentoRepository = andamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public DenunciaCriadaResponse criar(DenunciaRequest request, Long usuarioId) {
        Denuncia denuncia = new Denuncia();
        denuncia.setAnonima(request.anonima());
        if (!request.anonima()) {
            denuncia.setDenunciante(usuarioRepository.getReferenceById(usuarioId));
        }
        denuncia.setCategoria(request.categoria());
        denuncia.setDescricao(request.descricao());
        denuncia.setDataAproximada(request.dataAproximada());
        denuncia.setStatus(StatusDenuncia.RECEBIDA);
        denuncia.setProtocolo(gerarProtocolo());
        denunciaRepository.save(denuncia);

        auditoriaService.registrarAuditoria("CRIAR", "Denuncia", denuncia.getId(),
                request.anonima() ? "Anonima" : "Identificada");
        return new DenunciaCriadaResponse(denuncia.getProtocolo(), denuncia.getStatus(),
                denuncia.isAnonima(), denuncia.getCriadoEm());
    }

    @Transactional(readOnly = true)
    public DenunciaResponse consultarPorProtocolo(String protocolo) {
        Denuncia denuncia = denunciaRepository.findByProtocoloIgnoreCase(protocolo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Protocolo nao encontrado"));
        List<AndamentoResponse> andamentos = andamentoRepository
                .findByDenunciaIdOrderByCriadoEmAsc(denuncia.getId()).stream()
                .map(AndamentoResponse::from)
                .toList();
        return DenunciaResponse.semDenunciante(denuncia, andamentos);
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponse> listar() {
        return denunciaRepository.findAllByOrderByCriadoEmDesc().stream()
                .map(denuncia -> DenunciaResponse.completo(denuncia, andamentos(denuncia.getId())))
                .toList();
    }

    @Transactional
    public DenunciaResponse adicionarAndamento(Long denunciaId, AndamentoRequest request,
            Long autorId) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Denuncia nao encontrada"));

        DenunciaAndamento andamento = new DenunciaAndamento();
        andamento.setDenuncia(denuncia);
        andamento.setEtapa(request.etapa());
        andamento.setDescricao(request.descricao());
        andamento.setAutor(usuarioRepository.getReferenceById(autorId));
        andamentoRepository.save(andamento);

        StatusDenuncia novoStatus = request.status() != null
                ? request.status() : statusPorEtapa(request.etapa());
        if (novoStatus != null) {
            denuncia.setStatus(novoStatus);
            denunciaRepository.save(denuncia);
        }

        auditoriaService.registrarAuditoria("ANDAMENTO", "Denuncia", denunciaId,
                request.etapa());
        return DenunciaResponse.completo(denuncia, andamentos(denunciaId));
    }

    @Transactional(readOnly = true)
    public EstatisticasDenunciaResponse estatisticas() {
        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (Object[] linha : denunciaRepository.contarPorStatus()) {
            porStatus.put(((StatusDenuncia) linha[0]).name(), (Long) linha[1]);
        }
        Map<String, Long> porCategoria = new LinkedHashMap<>();
        for (Object[] linha : denunciaRepository.contarPorCategoria()) {
            porCategoria.put((String) linha[0], (Long) linha[1]);
        }
        return new EstatisticasDenunciaResponse(denunciaRepository.count(),
                denunciaRepository.countByAnonimaTrue(), porStatus, porCategoria);
    }

    private List<AndamentoResponse> andamentos(Long denunciaId) {
        return andamentoRepository.findByDenunciaIdOrderByCriadoEmAsc(denunciaId).stream()
                .map(AndamentoResponse::from)
                .toList();
    }

    private String gerarProtocolo() {
        int ano = LocalDate.now().getYear();
        long sequencia = denunciaRepository.count() + 1;
        String protocolo;
        do {
            protocolo = String.format("DEN-%d-%06d", ano, sequencia++);
        } while (denunciaRepository.existsByProtocolo(protocolo));
        return protocolo;
    }

    private StatusDenuncia statusPorEtapa(String etapa) {
        try {
            return StatusDenuncia.valueOf(etapa.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
