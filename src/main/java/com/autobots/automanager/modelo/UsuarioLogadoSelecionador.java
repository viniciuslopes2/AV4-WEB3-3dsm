package com.autobots.automanager.modelo;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;

@Component
public class UsuarioLogadoSelecionador {
    public Usuario selecionar() {
        UserDetailsImpl logado = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return logado.getUsuario();
    }
}
