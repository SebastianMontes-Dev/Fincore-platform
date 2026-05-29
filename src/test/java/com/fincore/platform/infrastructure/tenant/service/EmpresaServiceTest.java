package com.fincore.platform.infrastructure.tenant.service;

import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import com.fincore.platform.infrastructure.tenant.dto.*;
import com.fincore.platform.infrastructure.tenant.repository.EmpresaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @InjectMocks private EmpresaService empresaService;

    @Test
    void registrarEmpresaExitosa() {
        var req = new RegistroEmpresaRequest("Test SA", "test@test.com", "555", "Dir");
        when(empresaRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(empresaRepository.save(any())).thenAnswer(inv -> {
            Empresa e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        var resp = empresaService.registrar(req);
        assertEquals("Test SA", resp.getNombre());
        assertTrue(resp.isActiva());
    }

    @Test
    void registrarEmailDuplicado() {
        var req = new RegistroEmpresaRequest("X", "dup@test.com", null, null);
        when(empresaRepository.existsByEmail("dup@test.com")).thenReturn(true);
        assertThrows(NegocioException.class, () -> empresaService.registrar(req));
    }

    @Test
    void obtenerNoExistente() {
        when(empresaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> empresaService.obtenerPorId(UUID.randomUUID()));
    }

    @Test
    void desactivarEmpresa() {
        var empresa = Empresa.builder().id(UUID.randomUUID()).nombre("X").activa(true).build();
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any())).thenReturn(empresa);

        var resp = empresaService.desactivar(empresa.getId());
        assertTrue(resp.isExitoso());
        assertFalse(empresa.isActiva());
    }
}
