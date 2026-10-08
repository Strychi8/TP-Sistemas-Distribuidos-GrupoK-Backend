package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.exception.custom.RolNotFoundException;
import com.rentar.customer_service.model.Rol;
import com.rentar.customer_service.repository.IRolRepository;
import com.rentar.customer_service.service.IRolService;
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
