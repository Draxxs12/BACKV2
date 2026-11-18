package com.ferreteria.v1.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "jwt.secret=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=",
        "management.endpoint.health.probes.enabled=true"
})
@AutoConfigureMockMvc
class SecurityConfigAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void readinessProbeIsPublicForInfrastructureChecks() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    void recepcionistaNoPuedeAdministrarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios").with(user("recepcionista").roles("RECEPCIONISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void recepcionistaPuedeConsultarCatalogoDeProductos() throws Exception {
        mockMvc.perform(get("/api/productos").with(user("recepcionista").roles("RECEPCIONISTA")))
                .andExpect(status().isOk());
    }

    @Test
    void recepcionistaNoPuedeCrearProductos() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(user("recepcionista").roles("RECEPCIONISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void recepcionistaNoPuedeActualizarProductos() throws Exception {
        mockMvc.perform(put("/api/productos/1")
                        .with(user("recepcionista").roles("RECEPCIONISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void recepcionistaNoPuedeEliminarProductos() throws Exception {
        mockMvc.perform(delete("/api/productos/1")
                        .with(user("recepcionista").roles("RECEPCIONISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void almaceneroNoPuedeConsultarClientes() throws Exception {
        mockMvc.perform(get("/api/clientes").with(user("almacenero").roles("ALMACENERO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void almaceneroNoPuedeConsultarVentas() throws Exception {
        mockMvc.perform(get("/api/ventas").with(user("almacenero").roles("ALMACENERO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void reenvioMfaNoExigeSesionJwt() throws Exception {
        mockMvc.perform(post("/api/auth/resend-mfa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-inexistente\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "La solicitud de verificación ya no es válida")));
    }
}