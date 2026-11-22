package com.autobots.automanager.filtros;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.autobots.automanager.jwt.ProvedorJwt;

class AutenticadorJwt {
	private String jwt;
	private ProvedorJwt provedorJwt;
	private UserDetailsService servico;
	
	public AutenticadorJwt(String jwt, ProvedorJwt provedorJwt, UserDetailsService servico) {
		this.jwt = jwt;
		this.provedorJwt = provedorJwt;
		this.servico = servico;
	}

	public UsernamePasswordAuthenticationToken obterAutenticacao() {
		if (provedorJwt.validarJwt(jwt)) {
			String nomeUsuario = provedorJwt.obterNomeUsuario(jwt);
			try {
				UserDetails usuario = servico.loadUserByUsername(nomeUsuario);
				if (usuario.isEnabled()) {
					return new UsernamePasswordAuthenticationToken(usuario, usuario.getPassword(),
							usuario.getAuthorities());
				}
			} catch (UsernameNotFoundException e) {
				return null;
			}
		}
		return null;
	}
}