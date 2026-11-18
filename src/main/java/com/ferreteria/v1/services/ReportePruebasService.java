package com.ferreteria.v1.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ReportePruebasService {

    private static final String API = "https://api.github.com";
    private static final String REPOSITORIO = "Draxxs12/BACKV2";
    private static final String WORKFLOW = "reporte-pruebas.yml";

    private final RestClient cliente;
    private final String token;

    public ReportePruebasService(
            RestClient.Builder builder,
            @Value("${github.reportes.token:}") String token) {
        this.cliente = builder.baseUrl(API).build();
        this.token = token;
    }

    public Map<String, Object> ejecutar() {
        validarToken();
        cliente.post()
                .uri("/repos/{repo}/actions/workflows/{workflow}/dispatches", REPOSITORIO, WORKFLOW)
                .headers(this::configurarHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ref", "main"))
                .retrieve()
                .toBodilessEntity();

        return Map.of(
                "mensaje", "Ejecución solicitada. Las pruebas de backend y frontend se ejecutarán en GitHub Actions.",
                "estado", "EN_COLA",
                "url", "https://github.com/" + REPOSITORIO + "/actions/workflows/" + WORKFLOW
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> estado() {
        validarToken();
        Map<String, Object> respuesta = cliente.get()
                .uri("/repos/{repo}/actions/workflows/{workflow}/runs?event=workflow_dispatch&per_page=1",
                        REPOSITORIO, WORKFLOW)
                .headers(this::configurarHeaders)
                .retrieve()
                .body(Map.class);

        if (respuesta == null || !(respuesta.get("workflow_runs") instanceof List<?> ejecuciones)
                || ejecuciones.isEmpty()) {
            return Map.of("estado", "SIN_EJECUCIONES", "mensaje", "Todavía no hay reportes generados.");
        }

        Map<String, Object> ultima = (Map<String, Object>) ejecuciones.get(0);
        return Map.of(
                "estado", ultima.getOrDefault("status", "desconocido"),
                "resultado", ultima.getOrDefault("conclusion", "pendiente"),
                "ejecucion", ultima.getOrDefault("run_number", ""),
                "url", ultima.getOrDefault("html_url", ""),
                "iniciada", ultima.getOrDefault("created_at", ""),
                "actualizada", ultima.getOrDefault("updated_at", "")
        );
    }

    private void configurarHeaders(HttpHeaders headers) {
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.valueOf("application/vnd.github+json")));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
    }

    private void validarToken() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable GITHUB_REPORTES_TOKEN con un token de GitHub autorizado.");
        }
    }
}
