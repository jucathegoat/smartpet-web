package com.petshop.backend.service;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Value("${app.admin.username:adm}")
    private String adminUsername;

    @Value("${app.admin.password:adm123}")
    private String adminPassword;

    private final ClienteRepository clienteRepository;

    public AuthService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public boolean validarAdmin(String usuario, String senha) {
        if (usuario == null || senha == null) return false;
        return adminUsername.trim().equalsIgnoreCase(usuario.trim()) && adminPassword.equals(senha);
    }

    public Optional<Cliente> autenticarUsuario(String usuario, String senha) {
        if (usuario == null || senha == null) return Optional.empty();
        Optional<Cliente> opt = clienteRepository.findByUsuario(usuario);
        if (opt.isPresent() && senha.equals(opt.get().getSenha())) {
            return opt;
        }
        return Optional.empty();
    }
}