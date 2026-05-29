package com.fincore.platform.infrastructure.tenant.service;

import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import com.fincore.platform.infrastructure.tenant.dto.*;
import com.fincore.platform.infrastructure.tenant.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public EmpresaResponse registrar(RegistroEmpresaRequest request) {
        if (empresaRepository.existsByEmail(request.getEmail())) {
            throw new NegocioException("Ya existe una empresa con ese email");
        }
        Empresa empresa = Empresa.builder()
                .nombre(request.getNombre()).email(request.getEmail())
                .telefono(request.getTelefono()).direccion(request.getDireccion())
                .activa(true).build();
        empresa = empresaRepository.save(empresa);
        return mapear(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        return mapear(empresa);
    }

    @Transactional
    public MensajeResponse desactivar(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        empresa.setActiva(false);
        empresaRepository.save(empresa);
        return MensajeResponse.builder().mensaje("Empresa desactivada correctamente").build();
    }

    private EmpresaResponse mapear(Empresa e) {
        return EmpresaResponse.builder()
                .id(e.getId().toString()).nombre(e.getNombre()).email(e.getEmail())
                .telefono(e.getTelefono()).direccion(e.getDireccion())
                .activa(e.isActiva()).fechaCreacion(e.getFechaCreacion()).build();
    }
}
