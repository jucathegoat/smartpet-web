package com.petshop.backend.service;

import com.petshop.backend.model.ItemFilaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FilaService {

    // Lista sincronizada em memória baseada na implementação da Fila do seu colega
    private final List<ItemFilaDTO> fila = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger contadorId = new AtomicInteger(1);

    public FilaService() {
        // Inicializa com alguns exemplos caso a fila comece vazia
        adicionar("Carlos Silva", "Thor", "Banho e Tosa");
        adicionar("Ana Souza", "Mel", "Consulta Veterinária");
        adicionar("Marcos Lima", "Bob", "Vacinação");
    }

    public List<ItemFilaDTO> listarTodos() {
        return new ArrayList<>(fila);
    }

    public ItemFilaDTO adicionar(String cliente, String pet, String servico) {
        ItemFilaDTO item = new ItemFilaDTO(
            contadorId.getAndIncrement(),
            cliente,
            pet,
            servico
        );
        fila.add(item);
        return item;
    }

    public ItemFilaDTO chamarProximo() {
        synchronized (fila) {
            for (ItemFilaDTO item : fila) {
                if ("Em espera".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("Em atendimento");
                    return item;
                }
            }
        }
        return null;
    }

    public boolean cancelar(Integer id, String motivo) {
        synchronized (fila) {
            for (ItemFilaDTO item : fila) {
                if (item.getId().equals(id)) {
                    item.setStatus("Cancelado");
                    item.setMotivoCancelamento(motivo);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean concluir(Integer id) {
        synchronized (fila) {
            for (ItemFilaDTO item : fila) {
                if (item.getId().equals(id)) {
                    item.setStatus("Concluído");
                    return true;
                }
            }
        }
        return false;
    }
}
