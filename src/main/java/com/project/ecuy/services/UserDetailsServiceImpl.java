package com.project.ecuy.services;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.ecuy.entities.User;
import com.project.ecuy.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
  @Autowired
    private UserRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usuario = usuarioRepository.findByUsuario(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new org.springframework.security.core.userdetails.User(
            usuario.getUsuario(),
            usuario.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}
