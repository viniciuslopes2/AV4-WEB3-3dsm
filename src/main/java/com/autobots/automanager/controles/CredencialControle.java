package com.autobots.automanager.controles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.modelo.CredencialSelecionador;
import com.autobots.automanager.modelo.UsuarioLogadoSelecionador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.modelo.VerificadorPermissao;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/credencial")
public class CredencialControle {
    @Autowired
    private CredencialRepositorio repositorio;
    @Autowired
    private CredencialSelecionador selecionador;
    @Autowired
    private AdicionadorLinkCredencial adicionadorLink;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private UsuarioLogadoSelecionador usuarioLogadoSelecionador;
    @Autowired
    private VerificadorPermissao verificador;

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @GetMapping("/credencial/{id}")
    public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
        Credencial credencial = selecionador.selecionar(repositorio, id);
        if (credencial == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!podeGerenciar(credencial)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        adicionadorLink.adicionarLink(credencial);
        return new ResponseEntity<>(credencial, HttpStatus.FOUND);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @GetMapping("/credenciais")
    public ResponseEntity<List<Credencial>> obterCredenciais() {
        List<Credencial> credenciais = new ArrayList<>();
        for (Credencial credencial : repositorio.findAll()) {
            if (podeGerenciar(credencial)) {
                credenciais.add(credencial);
            }
        }
        if (credenciais.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(credenciais);
        return new ResponseEntity<>(credenciais, HttpStatus.FOUND);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @PostMapping("/cadastro/{idUsuario}")
    @Transactional
    public ResponseEntity<?> cadastrarCredencial(@PathVariable long idUsuario, @RequestBody Credencial credencial) {
        if (credencial.getId() != null || jaExiste(credencial)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Usuario usuario = usuarioSelecionador.selecionar(usuarioRepositorio, idUsuario);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!verificador.gerenciaUsuario(usuarioLogadoSelecionador.selecionar(), usuario.getPerfis())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (credencial.getCriacao() == null) {
            credencial.setCriacao(new Date());
        }
        if (credencial instanceof CredencialUsuarioSenha usuarioSenha) {
            usuarioSenha.setSenha(new BCryptPasswordEncoder().encode(usuarioSenha.getSenha()));
        }
        Credencial salva = repositorio.save(credencial);
        usuario.getCredenciais().add(salva);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarCredencial(@RequestBody Credencial atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Credencial credencial = selecionador.selecionar(repositorio, atualizacao.getId());
        if (credencial != null && !podeGerenciar(credencial)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (credencial != null) {
            CredencialAtualizador atualizador = new CredencialAtualizador();
            atualizador.atualizar(credencial, atualizacao);
            repositorio.save(credencial);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirCredencial(@RequestBody Credencial exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Credencial credencial = selecionador.selecionar(repositorio, exclusao.getId());
        if (credencial != null && !podeGerenciar(credencial)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (credencial != null) {
            Long id = credencial.getId();
            boolean vinculada = false;
            for (Usuario usuario : usuarioRepositorio.findAll()) {
                if (usuario.getCredenciais().removeIf(item -> item.getId().equals(id))) {
                    vinculada = true;
                }
            }
            if (!vinculada) {
                repositorio.delete(credencial);
            }
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    private boolean podeGerenciar(Credencial credencial) {
        Usuario logado = usuarioLogadoSelecionador.selecionar();
        for (Usuario usuario : usuarioRepositorio.findAll()) {
            for (Credencial item : usuario.getCredenciais()) {
                if (item.getId().equals(credencial.getId())) {
                    return verificador.gerenciaUsuario(logado, usuario.getPerfis());
                }
            }
        }
        return logado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN);
    }

    private boolean jaExiste(Credencial nova) {
        for (Credencial existente : repositorio.findAll()) {
            if (nova instanceof CredencialUsuarioSenha novaUsuarioSenha
                    && existente instanceof CredencialUsuarioSenha usuarioSenha
                    && usuarioSenha.getNomeUsuario().equals(novaUsuarioSenha.getNomeUsuario())) {
                return true;
            }
            if (nova instanceof CredencialCodigoBarra novoCodigoBarra
                    && existente instanceof CredencialCodigoBarra codigoBarra
                    && codigoBarra.getCodigo() == novoCodigoBarra.getCodigo()) {
                return true;
            }
        }
        return false;
    }
}
