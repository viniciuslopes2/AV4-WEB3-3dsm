package com.autobots.automanager.adaptadores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UsuarioRepositorio repositorio;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		for (Usuario usuario : repositorio.findAll()) {
			for (Credencial credencial : usuario.getCredenciais()) {
				if (credencial instanceof CredencialUsuarioSenha usuarioSenha
						&& usuarioSenha.getNomeUsuario().equals(username)) {
					return new UserDetailsImpl(usuario, usuarioSenha);
				}
			}
		}
		throw new UsernameNotFoundException(username);
	}
}
