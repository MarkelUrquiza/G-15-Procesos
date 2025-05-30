package com.deustocoches.controller;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.deustocoches.model.Coche;
import com.deustocoches.model.EstadoReserva;
import com.deustocoches.model.Reserva;
import com.deustocoches.model.TipoRol;
import com.deustocoches.model.Usuario;
import com.deustocoches.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
@WebMvcTest(ReservaController.class)
public class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private CocheController cocheController;

    @Autowired
    private ObjectMapper objectMapper;

    private Reserva reserva;
    private Usuario usuario;
    private Coche coche;

    @BeforeEach
    void setUp() {
        // Configurar Usuario
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellido("García");
        usuario.setEmail("juan.garcia@example.com");
        usuario.setPassword("password123");
        usuario.setTlf("666777888");
        usuario.setRol(TipoRol.CLIENTE);

        // Configurar Coche
        coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setMarca("Toyota");
        coche.setModelo("Corolla");
        coche.setAnio(2020);
        coche.setColor("Azul");
        coche.setPrecio(20000.0);
        coche.setDisponible(true);

        // Configurar Reserva
        reserva = new Reserva();
        reserva.setId(1);
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);
        reserva.setFecha("2023-04-23");
        reserva.setPrecioTotal(500.0);
        reserva.setEstado(EstadoReserva.PENDIENTE);
    }
    
    @Test
    void testCrearReserva_exito() throws Exception {
        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1);
        when(reservaService.crearReserva(any(Reserva.class))).thenReturn(reservaGuardada);

        mockMvc.perform(post("/api/reservas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(reservaService).crearReserva(any(Reserva.class));
    }

     @Test
     void testCrearReserva_excepcion() throws Exception {
        when(reservaService.crearReserva(any(Reserva.class))).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/reservas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isBadRequest());

        verify(reservaService).crearReserva(any(Reserva.class));
     }

    @Test
    void testActualizarReserva_exito() throws Exception {
        Reserva detallesReserva = new Reserva();
        Reserva reservaActualizada = new Reserva();
        reservaActualizada.setId(1);
        when(reservaService.actualizarReserva(eq(1), any(Reserva.class))).thenReturn(reservaActualizada);

        mockMvc.perform(put("/api/reservas/actualizar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detallesReserva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(reservaService).actualizarReserva(eq(1), any(Reserva.class));
    }

   @Test
   void testActualizarReserva_excepcion() throws Exception {
        when(reservaService.actualizarReserva(eq(1), any(Reserva.class))).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(put("/api/reservas/actualizar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Reserva())))
                .andExpect(status().isBadRequest());

        verify(reservaService).actualizarReserva(eq(1), any(Reserva.class));
   } 

    @Test
    void testEliminarReserva_exito() throws Exception {
        Reserva reserva = new Reserva();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setDisponible(false);
        reserva.setCoche(coche);

        when(reservaService.obtenerReservaPorId(1)).thenReturn(Optional.of(reserva));
        when(reservaService.eliminarReserva(1)).thenReturn(true);
        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));

        mockMvc.perform(delete("/api/reservas/eliminar/1"))
                .andExpect(status().isNoContent());

        assertTrue(coche.isDisponible());
        verify(reservaService).obtenerReservaPorId(1);
        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).eliminarReserva(1);
    }

    @Test
    void testEliminarReserva_noEncontrada() throws Exception {
        Reserva reserva = new Reserva();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        reserva.setCoche(coche);

        when(reservaService.obtenerReservaPorId(2)).thenReturn(Optional.of(reserva));
        when(reservaService.eliminarReserva(2)).thenReturn(false);
        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));

        mockMvc.perform(delete("/api/reservas/eliminar/2"))
                .andExpect(status().isNotFound());

        verify(reservaService).obtenerReservaPorId(2);
        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).eliminarReserva(2);
    }

    @Test
    void testHacerPedido_exito() throws Exception {
        Usuario usuario = new Usuario();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setDisponible(true);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1);
        reservaGuardada.setUsuario(usuario);
        reservaGuardada.setCoche(coche);

        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));
        when(reservaService.hacerPedido(any(Reserva.class))).thenReturn(reservaGuardada);

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).hacerPedido(any(Reserva.class));
    }

    @Test
    void testHacerPedido_usuarioNull() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setCoche(new Coche());

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHacerPedido_cocheNull() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setUsuario(new Usuario());

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHacerPedido_usuarioOCocheNull() throws Exception {
        Reserva reserva = new Reserva();

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHacerPedido_estadoYFechaNull() throws Exception {
        Usuario usuario = new Usuario();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setDisponible(true);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);
        reserva.setEstado(null);
        reserva.setFecha(null);

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(2);
        reservaGuardada.setUsuario(usuario);
        reservaGuardada.setCoche(coche);

        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));
        when(reservaService.hacerPedido(any(Reserva.class))).thenReturn(reservaGuardada);

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));

        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).hacerPedido(any(Reserva.class));
    }

    @Test
    void testHacerPedido_fechaNull() throws Exception {
        Usuario usuario = new Usuario();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setDisponible(true);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFecha(null);

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1);
        reservaGuardada.setUsuario(usuario);
        reservaGuardada.setCoche(coche);

        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));
        when(reservaService.hacerPedido(any(Reserva.class))).thenReturn(reservaGuardada);

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).hacerPedido(any(Reserva.class));
    }

    @Test
    void testHacerPedido_fechaVacia() throws Exception {
        Usuario usuario = new Usuario();
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setDisponible(true);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setCoche(coche);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFecha(""); // fecha vacía

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(2);
        reservaGuardada.setUsuario(usuario);
        reservaGuardada.setCoche(coche);

        when(cocheController.actualizarCoche(eq("1234ABC"), any(Coche.class))).thenReturn(ResponseEntity.ok(coche));
        when(reservaService.hacerPedido(any(Reserva.class))).thenReturn(reservaGuardada);

        mockMvc.perform(post("/api/reservas/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));

        verify(cocheController).actualizarCoche(eq("1234ABC"), any(Coche.class));
        verify(reservaService).hacerPedido(any(Reserva.class));
    }

    @Test
    void testObtenerReservasPendientes() throws Exception {
        when(reservaService.obtenerPendientes())
                .thenReturn(Arrays.asList(reserva));

        mockMvc.perform(get("/api/reservas/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").exists());

        verify(reservaService, times(1)).obtenerPendientes();
    }

    @Test
    void testObtenerReservasCompradas() throws Exception {
        Reserva reservaComprada = new Reserva();
        reservaComprada.setId(2);
        reservaComprada.setUsuario(usuario);
        reservaComprada.setCoche(coche);
        reservaComprada.setFecha("2023-04-24");
        reservaComprada.setPrecioTotal(600.0);
        reservaComprada.setEstado(EstadoReserva.COMPRADA);

        when(reservaService.obtenerCompradas())
                .thenReturn(Arrays.asList(reservaComprada));

        mockMvc.perform(get("/api/reservas/compradas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].estado").exists());

        verify(reservaService, times(1)).obtenerCompradas();
    }

    @Test
    void testObtenerReservasPendientesPorUsuario() throws Exception {
        when(reservaService.obtenerReservasPendientesPorUsuario("juan.garcia@example.com"))
                .thenReturn(Arrays.asList(reserva));

        mockMvc.perform(get("/api/reservas/usuario/pendientes")
                .param("email", "juan.garcia@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").exists());

        verify(reservaService, times(1)).obtenerReservasPendientesPorUsuario("juan.garcia@example.com");
    }

    @Test
    void testObtenerReservasCompradasPorUsuario() throws Exception {
        Reserva reservaComprada = new Reserva();
        reservaComprada.setId(2);
        reservaComprada.setUsuario(usuario);
        reservaComprada.setCoche(coche);
        reservaComprada.setFecha("2023-04-24");
        reservaComprada.setPrecioTotal(600.0);
        reservaComprada.setEstado(EstadoReserva.COMPRADA);

        when(reservaService.obtenerReservasCompradasPorUsuario("juan.garcia@example.com"))
                .thenReturn(Arrays.asList(reservaComprada));

        mockMvc.perform(get("/api/reservas/usuario/confirmadas")
                .param("email", "juan.garcia@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].estado").exists());

        verify(reservaService, times(1)).obtenerReservasCompradasPorUsuario("juan.garcia@example.com");
    }

    @Test
        void testObtenerReservasPorRangoFechas_ok() throws Exception {
        String desde = "2024-01-01";
        String hasta = "2024-01-31";
        List<Reserva> reservasMock = Arrays.asList(new Reserva(), new Reserva());

        when(reservaService.obtenerReservasPorRangoFechas(desde, hasta)).thenReturn(reservasMock);

        mockMvc.perform(get("/api/reservas/filtrar/rango")
                .param("desde", desde)
                .param("hasta", hasta))
                .andExpect(status().isOk());
        verify(reservaService, times(1)).obtenerReservasPorRangoFechas(desde, hasta);
        
        }

        @Test
        void testObtenerReservasPorRangoFechas_badRequest() throws Exception {
                String desde = "2024-01-01";
                String hasta = "2024-01-31";

                when(reservaService.obtenerReservasPorRangoFechas(desde, hasta)).thenThrow(new RuntimeException("Error"));

                mockMvc.perform(get("/api/reservas/filtrar/rango")
                        .param("desde", desde)
                        .param("hasta", hasta))
                        .andExpect(status().isBadRequest());

                verify(reservaService, times(1)).obtenerReservasPorRangoFechas(desde, hasta);
        }

}