package com.fincore.platform.infrastructure.auth.service;

import com.fincore.platform.infrastructure.auth.domain.ApiKey;
import com.fincore.platform.infrastructure.auth.dto.*;
import com.fincore.platform.infrastructure.auth.repository.ApiKeyRepository;
import com.fincore.platform.infrastructure.auth.security.ApiKeyAuthFilter;
import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import com.fincore.platform.infrastructure.auth.domain.Usuario;
import com.fincore.platform.infrastructure.tenant.repository.EmpresaRepository;
import com.fincore.platform.infrastructure.auth.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         EmpresaRepository empresaRepository,
                         UsuarioRepository usuarioRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ApiKeyResponse generar(UUID empresaId, UUID usuarioId, GenerarApiKeyRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String apiKeyRaw = "fc_" + UUID.randomUUID().toString().replace("-", "");
        String hash = ApiKeyAuthFilter.hashApiKey(apiKeyRaw);

        ApiKey apiKey = ApiKey.builder()
                .empresa(empresa).creadoPor(usuario).nombre(request.getNombre())
                .claveHash(hash).activa(true).build();
        apiKey = apiKeyRepository.save(apiKey);

        return ApiKeyResponse.builder()
                .id(apiKey.getId().toString()).nombre(apiKey.getNombre())
                .apiKey(apiKeyRaw).activa(true).fechaCreacion(apiKey.getFechaCreacion()).build();
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listar(UUID empresaId) {
        return apiKeyRepository.findByEmpresaIdAndActivaTrue(empresaId)
                .stream().map(ak -> ApiKeyResponse.builder()
                        .id(ak.getId().toString()).nombre(ak.getNombre()).activa(ak.isActiva())
                        .fechaCreacion(ak.getFechaCreacion()).fechaRevocacion(ak.getFechaRevocacion()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void revocar(UUID empresaId, UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RecursoNoEncontradoException("API Key no encontrada"));
        if (!apiKey.getEmpresa().getId().equals(empresaId)) {
            throw new NegocioException("La API Key no pertenece a esta empresa");
        }
        apiKey.setActiva(false);
        apiKey.setFechaRevocacion(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
    }
}
