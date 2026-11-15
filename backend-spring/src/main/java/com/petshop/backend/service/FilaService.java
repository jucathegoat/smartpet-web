package com.petshop.backend.service;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.model.FilaAtendimento;
import com.petshop.backend.model.ItemFilaDTO;
import com.petshop.backend.model.Pet;
import com.petshop.backend.model.Servico;
import com.petshop.backend.repository.ClienteRepository;
import com.petshop.backend.repository.FilaAtendimentoRepository;
import com.petshop.backend.repository.PetRepository;
import com.petshop.backend.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras da fila de atendimento (RF05), agora persistidas no banco.
 *
 * <p>A versao anterior mantinha tudo em um ArrayList em memoria e inseria
 * tres nomes ficticios no construtor — o que fazia a fila "ressuscitar" a
 * cada reinicio do servidor. Agora cada operacao le e grava na tabela
 * fila_atendimento.</p>
 *
 * <p>Nota: o ArrayList continua sendo usado (RNF04) para montar a resposta
 * que vai para a tela e para a API, mas a fonte da verdade e o banco.</p>
 */
@Service
public class FilaService {

    public static final String EM_ESPERA = "Em espera";
    public static final String EM_ATENDIMENTO = "Em atendimento";
    public static final String CONCLUIDO = "Concluído";
    public static final String CANCELADO = "Cancelado";

    private final FilaAtendimentoRepository filaRepository;
    private final PetRepository petRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public FilaService(FilaAtendimentoRepository filaRepository,
                       PetRepository petRepository,
                       ClienteRepository clienteRepository,
                       ServicoRepository servicoRepository) {
        this.filaRepository = filaRepository;
        this.petRepository = petRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    // ===============================================================
    // Leitura
    // ===============================================================

    /**
     * Lista todos os atendimentos da fila, do mais antigo para o mais novo.
     * O cliente, o pet e o servico sao resolvidos por JOIN manual aqui.
     */
    @Transactional(readOnly = true)
    public List<ItemFilaDTO> listarTodos() {
        List<FilaAtendimento> registros =
                filaRepository.findAllByOrderByDataHoraEntradaAsc();

        // ArrayList exigido pelo RNF04
        List<ItemFilaDTO> lista = new ArrayList<>();

        for (FilaAtendimento f : registros) {
            ItemFilaDTO item = new ItemFilaDTO(
                    f.getIdAtendimento(),
                    nomeDoTutor(f.getIdPet()),
                    nomeDoPet(f.getIdPet()),
                    nomeDoServico(f.getIdServico())
            );
            item.setChegada(f.getDataHoraEntrada() != null
                    ? f.getDataHoraEntrada()
                    : LocalDateTime.now());
            item.setStatus(f.getStatus() != null ? f.getStatus() : EM_ESPERA);
            item.setMotivoCancelamento(f.getMetodoCancelamento());
            lista.add(item);
        }
        return lista;
    }

    // ===============================================================
    // Escrita
    // ===============================================================

    /**
     * Coloca um novo atendimento na fila.
     * O pet e identificado pelo id; se nao existir, o registro e criado
     * sem vinculo de pet (nao inventamos dados).
     */
    @Transactional
    public FilaAtendimento adicionar(Integer idPet, Integer idServico, String criticidade) {
        FilaAtendimento f = new FilaAtendimento();
        f.setIdPet(idPet);
        f.setIdServico(idServico);
        f.setCriticidade(criticidade != null ? criticidade : "Normal");
        f.setDataHoraEntrada(LocalDateTime.now());
        f.setStatus(EM_ESPERA);
        return filaRepository.save(f);
    }

    /**
     * Versao por nome: usada pelo formulario do painel e pela API do app.
     * Procura o pet pelo nome e o servico pelo nome; se nao encontrar,
     * grava o atendimento sem o vinculo em vez de criar dado falso.
     */
    @Transactional
    public FilaAtendimento adicionarPorNome(String nomePet, String nomeServico) {
        Integer idPet = null;
        Integer idServico = null;

        if (nomePet != null && !nomePet.isBlank()) {
            List<Pet> pets = petRepository.findByNomeIgnoreCase(nomePet.trim());
            if (!pets.isEmpty()) {
                idPet = pets.get(0).getId();
            }
        }

        if (nomeServico != null && !nomeServico.isBlank()) {
            List<Servico> servicos = servicoRepository.findAll();
            for (Servico s : servicos) {
                if (s.getNome() != null
                        && s.getNome().equalsIgnoreCase(nomeServico.trim())) {
                    idServico = s.getIdServico();
                    break;
                }
            }
        }
        return adicionar(idPet, idServico, "Normal");
    }

    /** Chama o proximo da fila (RF05). */
    @Transactional
    public ItemFilaDTO chamarProximo() {
        return filaRepository
                .findFirstByStatusOrderByDataHoraEntradaAsc(EM_ESPERA)
                .map(this::marcarComoAtendimento)
                .orElse(null);
    }

    /** Cancela um atendimento exigindo justificativa (RF05). */
    @Transactional
    public boolean cancelar(Integer id, String motivo) {
        return filaRepository.findById(id).map(f -> {
            f.setStatus(CANCELADO);
            f.setMetodoCancelamento(
                    motivo != null && !motivo.isBlank()
                            ? motivo
                            : "Cancelado pela administração");
            filaRepository.save(f);
            return true;
        }).orElse(false);
    }

    /** Marca um atendimento como concluido. */
    @Transactional
    public boolean concluir(Integer id) {
        return filaRepository.findById(id).map(f -> {
            f.setStatus(CONCLUIDO);
            filaRepository.save(f);
            return true;
        }).orElse(false);
    }

    /** Remove o atendimento do banco (usado apenas em correcoes). */
    @Transactional
    public boolean excluir(Integer id) {
        if (!filaRepository.existsById(id)) {
            return false;
        }
        filaRepository.deleteById(id);
        return true;
    }

    // ===============================================================
    // Auxiliares
    // ===============================================================

    private ItemFilaDTO marcarComoAtendimento(FilaAtendimento f) {
        f.setStatus(EM_ATENDIMENTO);
        filaRepository.save(f);

        ItemFilaDTO item = new ItemFilaDTO(
                f.getIdAtendimento(),
                nomeDoTutor(f.getIdPet()),
                nomeDoPet(f.getIdPet()),
                nomeDoServico(f.getIdServico())
        );
        item.setChegada(f.getDataHoraEntrada() != null
                ? f.getDataHoraEntrada()
                : LocalDateTime.now());
        item.setStatus(EM_ATENDIMENTO);
        return item;
    }

    private String nomeDoPet(Integer idPet) {
        if (idPet == null) return "Pet não informado";
        return petRepository.findById(idPet)
                .map(Pet::getNome)
                .orElse("Pet não informado");
    }

    private String nomeDoTutor(Integer idPet) {
        if (idPet == null) return "Cliente não informado";
        Pet pet = petRepository.findById(idPet).orElse(null);
        if (pet == null || pet.getCpfTutor() == null) {
            return "Cliente não informado";
        }
        Cliente tutor = clienteRepository.findById(pet.getCpfTutor()).orElse(null);
        return tutor != null && tutor.getNome() != null
                ? tutor.getNome()
                : "Cliente não informado";
    }

    private String nomeDoServico(Integer idServico) {
        if (idServico == null) return "Atendimento geral";
        return servicoRepository.findById(idServico)
                .map(Servico::getNome)
                .orElse("Atendimento geral");
    }
}
