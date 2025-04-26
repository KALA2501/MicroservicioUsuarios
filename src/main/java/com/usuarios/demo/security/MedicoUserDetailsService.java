package com.usuarios.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;

import java.util.Optional;

@Service
public class MedicoUserDetailsService implements UserDetailsService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Optional<Medico> optional = medicoRepository.findByCorreo(correo);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("No se encontró el médico con el correo: " + correo);
        }

        Medico medico = optional.get();

        return User.builder()
                .username(medico.getCorreo())
                .password("{noop}medico123") // usa médico.getPassword() si estás guardando la real
                .roles("MEDICO")
                .build();
    }
}
