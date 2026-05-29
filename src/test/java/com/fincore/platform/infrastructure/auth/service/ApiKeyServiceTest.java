package com.fincore.platform.infrastructure.auth.service;

import com.fincore.platform.infrastructure.auth.domain.ApiKey;
import com.fincore.platform.infrastructure.auth.dto.GenerarApiKeyRequest;
import com.fincore.platform.infrastructure.auth.repository.*;
import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.Empresa;
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
class ApiKeyServiceTest {

    @Mock private ApiKeyRepository apiKeyRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private ApiKeyService apiKeyService;

    @Test
    void generarApiKey() {
        var empresa = Empresa.builder().id(UUID.randomUUID()).nombre("X").build();
        var usuario = com.fincore.platform.infrastructure.auth.domain.Usuario.builder()
                .id(UUID.randomUUID()).nombre("J").build();

        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> {
            ApiKey ak = inv.getArgument(0); ak.setId(UUID.randomUUID()); return ak;
        });

        var resp = apiKeyService.generar(empresa.getId(), usuario.getId(),
                new GenerarApiKeyRequest("mi-key"));
        assertTrue(resp.getApiKey().startsWith("fc_"));
        assertTrue(resp.isActiva());
    }

    @Test
    void listarVacio() {
        when(apiKeyRepository.findByEmpresaIdAndActivaTrue(any())).thenReturn(List.of());
        assertTrue(apiKeyService.listar(UUID.randomUUID()).isEmpty());
    }

    @Test
    void revocarNoPertenece() {
        var otraEmpresa = Empresa.builder().id(UUID.randomUUID()).build();
        var apiKey = ApiKey.builder().id(UUID.randomUUID()).empresa(otraEmpresa).build();
        when(apiKeyRepository.findById(apiKey.getId())).thenReturn(Optional.of(apiKey));

        assertThrows(NegocioException.class,
                () -> apiKeyService.revocar(UUID.randomUUID(), apiKey.getId()));
    }
}
