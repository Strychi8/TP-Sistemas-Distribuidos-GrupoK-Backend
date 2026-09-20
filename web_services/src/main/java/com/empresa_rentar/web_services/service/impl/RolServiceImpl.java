package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.RolNotFoundException;
import com.empresa_rentar.web_services.model.Rol;
import com.empresa_rentar.web_services.repository.IRolRepository;
import com.empresa_rentar.web_services.service.IRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("rolService")
public class RolServiceImpl implements IRolService {
    private final IRolRepository rolRepository;

    @Override
    public boolean existsByNombreRol(NombreRol nombreRol) {
        return rolRepository.existsByNombreRol(nombreRol);
    }

    @Override
    public Rol findByNombreRol(NombreRol nombreRol) {
        return rolRepository.findByNombreRol(nombreRol).orElseThrow(RolNotFoundException::new);
    }
}
