package com.deustocoches.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.deustocoches.client.service.RestTemplateServiceProxy;
import com.deustocoches.model.Coche;
import com.deustocoches.model.EstadoReserva;
import com.deustocoches.model.Reserva;
import com.deustocoches.model.TipoRol;
import com.deustocoches.model.Usuario;

@WebMvcTest(com.deustocoches.client.controller.ClientController.class) 
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplateServiceProxy serviceProxy;


    private Usuario usuario;
    private Coche coche;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellido("García");
        usuario.setEmail("juan@example.com");
        usuario.setPassword("password123");
        usuario.setTlf("666777888");
        usuario.setRol(TipoRol.CLIENTE);
        
        coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setMarca("Toyota");
        coche.setModelo("Corolla");
        coche.setAnio(2020);
        coche.setColor("Azul");
        coche.setPrecio(20000.0);
        coche.setDisponible(true);
        
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);
        reserva.setFecha("2023-04-23");
        reserva.setPrecioTotal(500.0);
        reserva.setEstado(EstadoReserva.PENDIENTE);
    }

    @Test
    void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testPerformLoginClient() throws Exception {
        when(serviceProxy.login("juan@example.com", "password123"))
                .thenReturn("token123");
        
        when(serviceProxy.getUsuarioByEmail("juan@example.com"))
                .thenReturn(usuario);

        mockMvc.perform(post("/login")
                .param("email", "juan@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/coches/disponibles"));

        verify(serviceProxy, times(1)).login("juan@example.com", "password123");
        verify(serviceProxy, times(1)).getUsuarioByEmail("juan@example.com");
    }
    
    @Test
    void testPerformLoginAdmin() throws Exception {
        Usuario admin = new Usuario();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        admin.setRol(TipoRol.ADMIN);
        
        when(serviceProxy.login("admin@example.com", "admin123"))
                .thenReturn("admintoken123");
        
        when(serviceProxy.getUsuarioByEmail("admin@example.com"))
                .thenReturn(admin);

        mockMvc.perform(post("/login")
                .param("email", "admin@example.com")
                .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(serviceProxy, times(1)).login("admin@example.com", "admin123");
        verify(serviceProxy, times(1)).getUsuarioByEmail("admin@example.com");
    }

    @Test
    void testPerformLoginFailed() throws Exception {
        when(serviceProxy.login("juan@example.com", "wrongpassword"))
                .thenReturn(null);

        mockMvc.perform(post("/login")
                .param("email", "juan@example.com")
                .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(serviceProxy, times(1)).login("juan@example.com", "wrongpassword");
    }

    @Test
    void testPerformLogout() throws Exception {
        doNothing().when(serviceProxy).logout(any(String.class));

        mockMvc.perform(post("/logout")
                .sessionAttr("token", "token123")) 
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).logout(any(String.class));
        
    }

    @Test
    void testShowRegisterPage() throws Exception {
        mockMvc.perform(get("/usuario/registrar"))
                .andExpect(status().isOk())
                .andExpect(view().name("registrar"));
    }

    @Test
    void testRegistrarUsuario() throws Exception {
        when(serviceProxy.registrarUsuario(any(Usuario.class)))
                .thenReturn(usuario);

        mockMvc.perform(post("/usuario/registrar")
                .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).registrarUsuario(any(Usuario.class));
    }

    @Test
    void testRegistrarUsuarioFallido() throws Exception {
        when(serviceProxy.registrarUsuario(any(Usuario.class)))
                .thenThrow(new RuntimeException("Error al registrar"));

        mockMvc.perform(post("/usuario/registrar")
                .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuario/registrar"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(serviceProxy, times(1)).registrarUsuario(any(Usuario.class));
    }

    @Test
    void testListarCochesDisponibles() throws Exception {
        List<Coche> coches = Arrays.asList(coche);
        when(serviceProxy.ListarCochesDisponibles())
                .thenReturn(coches);

        mockMvc.perform(get("/coches/disponibles"))
                .andExpect(status().isOk())
                .andExpect(view().name("coches"))
                .andExpect(model().attribute("coches", coches));

        verify(serviceProxy, times(1)).ListarCochesDisponibles();
    }

    @Test
    void testListarUsuarios() throws Exception {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(serviceProxy.listarUsuariosResgistrados())
                .thenReturn(usuarios);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuariosADMIN"))
                .andExpect(model().attribute("usuarios", usuarios));

        verify(serviceProxy, times(1)).listarUsuariosResgistrados();
    }

    @Test
    void testEliminarUsuario() throws Exception {
        mockMvc.perform(post("/usuario/eliminar")
                .param("email", "juan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).eliminarUsuario("juan@example.com");
    }

    @Test
    void testListarCoches() throws Exception {
        List<Coche> coches = Arrays.asList(coche);
        when(serviceProxy.ListarCoches())
                .thenReturn(coches);

        mockMvc.perform(get("/coches"))
                .andExpect(status().isOk())
                .andExpect(view().name("cochesADMIN"))
                .andExpect(model().attribute("coches", coches));

        verify(serviceProxy, times(1)).ListarCoches();
    }

    @Test
    void testGetCocheByMatricula() throws Exception {
        when(serviceProxy.getCocheByMatricula("1234ABC"))
                .thenReturn(coche);

        mockMvc.perform(get("/coche")
                .param("matricula", "1234ABC"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalleCocheADMIN"))
                .andExpect(model().attribute("coche", coche));

        verify(serviceProxy, times(1)).getCocheByMatricula("1234ABC");
    }

    @Test
    void testCrearCoche() throws Exception {
        when(serviceProxy.crearCoche(any(Coche.class)))
                .thenReturn(coche);

        mockMvc.perform(post("/coche/crear")
                .flashAttr("coche", coche))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/coches"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).crearCoche(any(Coche.class));
    }

    @Test
    void testActualizarCoche() throws Exception {
        when(serviceProxy.actualizarCoche(eq("1234ABC"), any(Coche.class)))
                .thenReturn(coche);

        mockMvc.perform(post("/coche/actualizar")
                .param("matricula", "1234ABC")
                .flashAttr("coche", coche))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/coches"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).actualizarCoche(eq("1234ABC"), any(Coche.class));
    }

    @Test
    void testEliminarCoche() throws Exception {
        mockMvc.perform(post("/coche/eliminar")
                .param("matricula", "1234ABC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/coches"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).eliminarCoche("1234ABC");
    }

    @Test
    void testBloquearUsuario() throws Exception {
        when(serviceProxy.bloquearUsuario("juan@example.com"))
                .thenReturn(usuario);

        mockMvc.perform(post("/usuario/bloquear")
                .param("email", "juan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).bloquearUsuario("juan@example.com");
    }

    @Test
    void testDesbloquearUsuario() throws Exception {
        when(serviceProxy.desbloquearUsuario("juan@example.com"))
                .thenReturn(usuario);

        mockMvc.perform(post("/usuario/desbloquear")
                .param("email", "juan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(serviceProxy, times(1)).desbloquearUsuario("juan@example.com");
    }

    @Test
    void testMostrarReservasConfirmadasPorUsuario() throws Exception {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(serviceProxy.obtenerReservasConfirmadasPorUsuario("juan@example.com"))
                .thenReturn(reservas);

        mockMvc.perform(get("/reservas/usuario/confirmadas")
                .param("email", "juan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas"))
                .andExpect(model().attribute("reservas", reservas));

        verify(serviceProxy, times(1)).obtenerReservasConfirmadasPorUsuario("juan@example.com");
    }

    @Test
    void testMostrarReservasPendientesPorUsuario() throws Exception {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(serviceProxy.obtenerReservasPendientesPorUsuario("juan@example.com"))
                .thenReturn(reservas);

        mockMvc.perform(get("/reservas/usuario/pendientes")
                .param("email", "juan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas"))
                .andExpect(model().attribute("reservas", reservas));

        verify(serviceProxy, times(1)).obtenerReservasPendientesPorUsuario("juan@example.com");
    }

    @Test
    void testMostrarReservasCompradas() throws Exception {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(serviceProxy.obtenerReservasCompradas())
                .thenReturn(reservas);

        mockMvc.perform(get("/reservas/compradas"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservasADMIN"))
                .andExpect(model().attribute("reservas", reservas));

        verify(serviceProxy, times(1)).obtenerReservasCompradas();
    }

    @Test
    void testMostrarReservasPendientes() throws Exception {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(serviceProxy.obtenerReservasPendientes())
                .thenReturn(reservas);

        mockMvc.perform(get("/reservas/pendientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservasADMIN"))
                .andExpect(model().attribute("reservas", reservas));

        verify(serviceProxy, times(1)).obtenerReservasPendientes();
    }
}