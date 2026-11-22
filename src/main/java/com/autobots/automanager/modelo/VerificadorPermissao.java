package com.autobots.automanager.modelo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@Component
public class VerificadorPermissao {

    public boolean gerenciaUsuario(Usuario logado, Set<PerfilUsuario> perfisAlvo) {
        if (logado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)) {
            return true;
        }
        Set<PerfilUsuario> permitidos = new HashSet<>();
        if (logado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
            permitidos.add(PerfilUsuario.ROLE_GERENTE);
            permitidos.add(PerfilUsuario.ROLE_VENDEDOR);
            permitidos.add(PerfilUsuario.ROLE_CLIENTE);
        }
        if (logado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
            permitidos.add(PerfilUsuario.ROLE_CLIENTE);
        }
        return !perfisAlvo.isEmpty() && permitidos.containsAll(perfisAlvo);
    }

    public boolean lerUsuario(Usuario logado, Usuario alvo) {
        if (gerenciaUsuario(logado, alvo.getPerfis())) {
            return true;
        }
        return logado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE) && alvo.getId().equals(logado.getId());
    }

    public boolean lerVenda(Usuario logado, Venda venda) {
        if (logado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                || logado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
            return true;
        }
        if (logado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR) && venda.getFuncionario() != null
                && venda.getFuncionario().getId().equals(logado.getId())) {
            return true;
        }
        return logado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE) && venda.getCliente() != null
                && venda.getCliente().getId().equals(logado.getId());
    }
}
