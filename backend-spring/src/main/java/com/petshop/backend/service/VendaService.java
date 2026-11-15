package com.petshop.backend.service;

import com.petshop.backend.model.Caixa;
import com.petshop.backend.model.ItemVenda;
import com.petshop.backend.model.Produto;
import com.petshop.backend.model.Receita;
import com.petshop.backend.model.Venda;
import com.petshop.backend.repository.CaixaRepository;
import com.petshop.backend.repository.ItemVendaRepository;
import com.petshop.backend.repository.ProdutoRepository;
import com.petshop.backend.repository.ReceitaRepository;
import com.petshop.backend.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RF06 — Registro de venda de produtos.
 *
 * <p>Uma venda percorre tres tabelas:
 * venda (cabecalho) -> item_venda (linhas) -> receita (entrada no caixa).</p>
 *
 * <p>Regras aplicadas:</p>
 * <ul>
 *   <li>nao vende sem caixa aberto (a venda precisa entrar no fluxo de caixa);</li>
 *   <li>nao vende mais do que o estoque disponivel;</li>
 *   <li>o preco vem sempre do banco, nunca do formulario;</li>
 *   <li>o estoque e debitado a cada item vendido;</li>
 *   <li>o total da venda vira uma receita vinculada ao caixa.</li>
 * </ul>
 */
@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ProdutoRepository produtoRepository;
    private final CaixaRepository caixaRepository;
    private final ReceitaRepository receitaRepository;

    public VendaService(VendaRepository vendaRepository,
                        ItemVendaRepository itemVendaRepository,
                        ProdutoRepository produtoRepository,
                        CaixaRepository caixaRepository,
                        ReceitaRepository receitaRepository) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.produtoRepository = produtoRepository;
        this.caixaRepository = caixaRepository;
        this.receitaRepository = receitaRepository;
    }

    /** Resultado de uma tentativa de venda. */
    public static class ResultadoVenda {
        public boolean sucesso;
        public String mensagem;
        public Venda venda;

        static ResultadoVenda erro(String msg) {
            ResultadoVenda r = new ResultadoVenda();
            r.sucesso = false;
            r.mensagem = msg;
            return r;
        }

        static ResultadoVenda ok(Venda v, String msg) {
            ResultadoVenda r = new ResultadoVenda();
            r.sucesso = true;
            r.venda = v;
            r.mensagem = msg;
            return r;
        }
    }

    /**
     * Registra uma venda de um unico produto.
     *
     * @param idProduto     produto vendido
     * @param quantidade    unidades (maior que zero)
     * @param formaPagamento Dinheiro, Cartao, Pix...
     */
    @Transactional
    public ResultadoVenda registrarVenda(Integer idProduto, Integer quantidade,
                                         String formaPagamento) {

        if (idProduto == null) {
            return ResultadoVenda.erro("Selecione um produto.");
        }
        if (quantidade == null || quantidade <= 0) {
            return ResultadoVenda.erro("A quantidade precisa ser maior que zero.");
        }

        Caixa caixa = caixaRepository
                .findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc()
                .orElse(null);
        if (caixa == null) {
            return ResultadoVenda.erro(
                    "Não há caixa aberto. Abra o caixa antes de registrar vendas.");
        }

        Produto produto = produtoRepository.findById(idProduto).orElse(null);
        if (produto == null) {
            return ResultadoVenda.erro("Produto não encontrado.");
        }

        int estoque = produto.getEstoque() != null ? produto.getEstoque() : 0;
        if (estoque < quantidade) {
            return ResultadoVenda.erro(
                    "Estoque insuficiente. Disponível: " + estoque + " un.");
        }

        BigDecimal preco = produto.getPreco();
        BigDecimal total = preco.multiply(BigDecimal.valueOf(quantidade));

        // 1) Cabecalho da venda
        Venda venda = new Venda();
        venda.setSubtotal(total);
        venda.setFormaPagamento(
                formaPagamento != null && !formaPagamento.isBlank()
                        ? formaPagamento : "Dinheiro");
        venda.setDataHora(LocalDateTime.now());
        venda.setSituacao("Concluída");
        venda.setStatus("Concluída");
        venda.setIdCaixa(caixa.getIdCaixa());
        vendaRepository.save(venda);

        // 2) Linha da venda
        ItemVenda item = new ItemVenda(idProduto, quantidade, preco, total);
        item.setIdVenda(venda.getIdVenda());
        itemVendaRepository.save(item);

        // 3) Baixa de estoque
        produto.setEstoque(estoque - quantidade);
        produtoRepository.save(produto);

        // 4) Entrada no caixa (RF07)
        Receita receita = new Receita(
                "Venda de Produto",
                quantidade + "x " + produto.getNome(),
                total,
                LocalDateTime.now(),
                caixa.getIdCaixa()
        );
        receitaRepository.save(receita);

        return ResultadoVenda.ok(venda,
                "Venda registrada: " + produto.getNome()
                        + " — R$ " + total);
    }

    // ===============================================================
    // Consultas
    // ===============================================================

    @Transactional(readOnly = true)
    public List<Venda> listarTodas() {
        List<Venda> vendas = vendaRepository.findAll();
        // mais recente primeiro
        List<Venda> ordenadas = new ArrayList<>(vendas);
        ordenadas.sort((a, b) -> {
            if (a.getDataHora() == null) return 1;
            if (b.getDataHora() == null) return -1;
            return b.getDataHora().compareTo(a.getDataHora());
        });
        return ordenadas;
    }

    @Transactional(readOnly = true)
    public List<ItemVenda> itensDaVenda(Integer idVenda) {
        return itemVendaRepository.findByIdVenda(idVenda);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalVendido() {
        BigDecimal soma = BigDecimal.ZERO;
        for (Venda v : vendaRepository.findAll()) {
            if (v.getSubtotal() != null) {
                soma = soma.add(v.getSubtotal());
            }
        }
        return soma;
    }
}
