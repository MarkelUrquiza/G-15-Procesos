package com.deustocoches.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.deustocoches.model.Coche;
import com.deustocoches.model.EstadoReserva;
import com.deustocoches.model.Reserva;
import com.deustocoches.model.TipoRol;
import com.deustocoches.model.Usuario;

class ClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestTemplateServiceProxy clientService;

    private String apiBaseUrl = "http://localhost:8080";
    private Usuario usuario;
    private Coche coche;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        try {
            java.lang.reflect.Field field = RestTemplateServiceProxy.class.getDeclaredField("apiBaseUrl");
            field.setAccessible(true);
            field.set(clientService, apiBaseUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        coche.setColor("Rojo");
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
    void testLogin() {
        String token = "abc123token";
        String url = apiBaseUrl + "/api/usuario/login?email=juan@example.com&password=password123";

        when(restTemplate.postForObject(url, null, String.class))
                .thenReturn(token);

        String resultado = clientService.login("juan@example.com", "password123");

        assertNotNull(resultado);
        assertEquals(token, resultado);
        verify(restTemplate, times(1)).postForObject(url, null, String.class);
    }

    @Test
    void testRegistrarUsuario() {
        String url = apiBaseUrl + "/api/usuario/registrar";

        when(restTemplate.postForObject(url, usuario, Usuario.class))
                .thenReturn(usuario);

        Usuario resultado = clientService.registrarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(restTemplate, times(1)).postForObject(url, usuario, Usuario.class);
    }

    @Test
    void testListarCoches() {
        String url = apiBaseUrl + "/api/coche";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(coches);

        List<Coche> resultado = clientService.ListarCoches();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testListarCochesDisponibles() {
        String url = apiBaseUrl + "/api/coche/disponibles";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(coches);

        List<Coche> resultado = clientService.ListarCochesDisponibles();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testGetCocheByMatricula() {
        String url = apiBaseUrl + "/api/coche/buscar?matricula=1234ABC";

        when(restTemplate.getForObject(url, Coche.class))
                .thenReturn(coche);

        Coche resultado = clientService.getCocheByMatricula("1234ABC");

        assertNotNull(resultado);
        assertEquals("1234ABC", resultado.getMatricula());
        verify(restTemplate, times(1)).getForObject(url, Coche.class);
    }

    @Test
    void testCrearCoche() {
        String url = apiBaseUrl + "/api/coche/crear";

        when(restTemplate.postForObject(url, coche, Coche.class))
                .thenReturn(coche);

        Coche resultado = clientService.crearCoche(coche);

        assertNotNull(resultado);
        assertEquals(coche, resultado);
        verify(restTemplate, times(1)).postForObject(url, coche, Coche.class);
    }

    @Test
    void testActualizarCoche() {
        String url = apiBaseUrl + "/api/coche/actualizar?matricula=1234ABC";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Coche.class)))
                .thenReturn(new ResponseEntity<>(coche, HttpStatus.OK));

        Coche resultado = clientService.actualizarCoche("1234ABC", coche);

        assertNotNull(resultado);
        assertEquals(coche, resultado);
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Coche.class));
    }

    @Test
    void testEliminarCoche() {
        String url = apiBaseUrl + "/api/coche/eliminar?matricula=1234ABC";

        doNothing().when(restTemplate).delete(url);

        clientService.eliminarCoche("1234ABC");

        verify(restTemplate, times(1)).delete(url);
    }

    @Test
    void testCrearReserva() {
        String url = apiBaseUrl + "/api/reservas/pedidos";

        when(restTemplate.postForObject(url, reserva, Reserva.class))
                .thenReturn(reserva);

        Reserva resultado = clientService.crearReserva(reserva);

        assertNotNull(resultado);
        assertEquals(reserva, resultado);
        verify(restTemplate, times(1)).postForObject(url, reserva, Reserva.class);
    }

    @Test
    void testObtenerReservasPendientes() {
        String url = apiBaseUrl + "/api/reservas/pendientes";
        List<Reserva> reservas = Arrays.asList(reserva);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasPendientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testObtenerReservasCompradas() {
        String url = apiBaseUrl + "/api/reservas/compradas";
        reserva.setEstado(EstadoReserva.COMPRADA);
        List<Reserva> reservas = Arrays.asList(reserva);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasCompradas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testGetUsuarioByEmail() {
        String url = apiBaseUrl + "/api/usuario/buscar?email=juan@example.com";

        when(restTemplate.getForObject(url, Usuario.class))
                .thenReturn(usuario);

        Usuario resultado = clientService.getUsuarioByEmail("juan@example.com");

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.getEmail());
        verify(restTemplate, times(1)).getForObject(url, Usuario.class);
    }

    @Test
    void testBloquearUsuario() {
        String url = apiBaseUrl + "/api/usuario/bloquear?email=juan@example.com";
        usuario.setBloqueado(true);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class)))
                .thenReturn(new ResponseEntity<>(usuario, HttpStatus.OK));

        Usuario resultado = clientService.bloquearUsuario("juan@example.com");

        assertNotNull(resultado);
        assertTrue(resultado.isBloqueado());
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class));
    }

    @Test
    void testDesbloquearUsuario() {
        String url = apiBaseUrl + "/api/usuario/desbloquear?email=juan@example.com";
        usuario.setBloqueado(false);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class)))
                .thenReturn(new ResponseEntity<>(usuario, HttpStatus.OK));

        Usuario resultado = clientService.desbloquearUsuario("juan@example.com");

        assertNotNull(resultado);
        assertFalse(resultado.isBloqueado());
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class));
    }

    @Test
    void testObtenerReservasConfirmadasPorUsuario() {
        String url = apiBaseUrl + "/api/reservas/usuario/confirmadas?email=juan@example.com";
        reserva.setEstado(EstadoReserva.COMPRADA);
        List<Reserva> reservas = Arrays.asList(reserva);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasConfirmadasPorUsuario("juan@example.com");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testObtenerReservasPendientesPorUsuario() {
        String url = apiBaseUrl + "/api/reservas/usuario/pendientes?email=juan@example.com";
        List<Reserva> reservas = Arrays.asList(reserva);

        when(restTemplate.getForObject(url, List.class))
                .thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasPendientesPorUsuario("juan@example.com");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate, times(1)).getForObject(url, List.class);
    }

    @Test
    void testLogout() {
        String token = "abc123token";
        String url = apiBaseUrl + "/api/usuario/logout?token=" + token;

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
                .thenReturn(null);

        clientService.logout(token);

        verify(restTemplate, times(1)).postForObject(eq(url), isNull(), eq(Void.class));
    }

    @Test
    void testMetodoQuePuedeLanzarExcepcion() {
        when(restTemplate.getForObject(contains("/api/ejemplo"), eq(String.class)))
            .thenThrow(new RestClientException("Error de conexión simulado"));
        
        Exception excepcion = assertThrows(RuntimeException.class, () -> {
            clientService.metodoQuePuedeLanzarExcepcion();
        });
        
        assertTrue(excepcion.getMessage().contains("Error durante la llamada a la API"));
        verify(restTemplate).getForObject(contains("/api/ejemplo"), eq(String.class));
    }

    @Test
    void testMetodoQuePuedeLanzarExcepcionExitosoSinExcepcion() {
        when(restTemplate.getForObject(contains("/api/ejemplo"), eq(String.class)))
            .thenReturn("Resultado exitoso");
        
        assertDoesNotThrow(() -> {
            clientService.metodoQuePuedeLanzarExcepcion();
        });
        
        verify(restTemplate).getForObject(contains("/api/ejemplo"), eq(String.class));
    }

    @Test
    void testMetodoQueDevuelveLista() {
        List<String> datosEsperados = Arrays.asList("elemento1", "elemento2", "elemento3");
        
        when(restTemplate.getForObject(contains("/api/coleccion"), eq(List.class)))
            .thenReturn(datosEsperados);
        
        List<?> resultado = clientService.metodoQueDevuelveLista();
        
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(restTemplate).getForObject(contains("/api/coleccion"), eq(List.class));
    }

    @Test
    void testMetodoQueDevuelveListaVacia() {
        when(restTemplate.getForObject(contains("/api/coleccion"), eq(List.class)))
            .thenReturn(Collections.emptyList());
        
        List<?> resultado = clientService.metodoQueDevuelveLista();
        
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(restTemplate).getForObject(contains("/api/coleccion"), eq(List.class));
    }

    @Test
    void testActualizarReserva() {
        Integer id = 1;
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setPrecioTotal(100.0);
        
        when(restTemplate.exchange(
                contains("/api/reservas/actualizar/" + id),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Reserva.class)))
            .thenReturn(ResponseEntity.ok(reserva));
        
        Reserva resultado = clientService.actualizarReserva(id, reserva);
        
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(restTemplate).exchange(
            contains("/api/reservas/actualizar/" + id),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(Reserva.class));
    }

    @Test
    void testHacerPedido() {
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(150.0);
        
        Reserva reservaCreada = new Reserva();
        reservaCreada.setId(1);
        reservaCreada.setPrecioTotal(150.0);
        
        when(restTemplate.postForObject(contains("/api/reserva/pedido"), eq(reserva), eq(Reserva.class)))
            .thenReturn(reservaCreada);
        
        Reserva resultado = clientService.hacerPedido(reserva);
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals(150.0, resultado.getPrecioTotal());
        verify(restTemplate).postForObject(contains("/api/reserva/pedido"), eq(reserva), eq(Reserva.class));
    }

    @Test
    void testListarUsuariosRegistrados() {
        List<Usuario> usuariosEsperados = Arrays.asList(new Usuario(), new Usuario());
        
        when(restTemplate.getForObject(contains("/api/usuario"), eq(List.class)))
            .thenReturn(usuariosEsperados);
        
        List<Usuario> resultado = clientService.listarUsuariosResgistrados();
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(restTemplate).getForObject(contains("/api/usuario"), eq(List.class));
    }

    @Test
    void testEliminarUsuario() {
        doNothing().when(restTemplate).delete(contains("/api/usuario/eliminar?email=test@example.com"));
        
        clientService.eliminarUsuario("test@example.com");
        
        verify(restTemplate).delete(contains("/api/usuario/eliminar?email=test@example.com"));
    }

    @Test
    void testEliminarReserva() {
        doNothing().when(restTemplate).delete(contains("/api/reservas/eliminar/1"));
        
        clientService.eliminarReserva(1);
        
        verify(restTemplate).delete(contains("/api/reservas/eliminar/1"));
    }

    @Test
    void testGetUsuarioByEmailConExcepcion() {
        when(restTemplate.getForObject(contains("/api/usuario/buscar"), eq(Usuario.class)))
            .thenThrow(new RestClientException("Error al buscar usuario"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getUsuarioByEmail("error@example.com");
        });
        


        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed") ||
                   exception.getMessage().contains("usuario"));
    }

    @Test
    void testMetodoQueDevuelveListaConExcepcion() {
        when(restTemplate.getForObject(contains("/api/coleccion"), eq(List.class)))
            .thenThrow(new RestClientException("Error al recuperar lista"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.metodoQueDevuelveLista();
        });
        
        assertTrue(exception.getMessage().contains("Error al recuperar la lista"));
    }

    @Test
    void testRegistrarUsuarioConExcepcion() {
        Usuario usuario = new Usuario();
        
        when(restTemplate.postForObject(contains("/api/usuario/registrar"), eq(usuario), eq(Usuario.class)))
            .thenThrow(new RestClientException("Error al registrar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.registrarUsuario(usuario);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed") ||
                   exception.getMessage().contains("registrar"));
    }

    @Test
    void testListarCochesConErrorDeServidor() {
        when(restTemplate.getForObject(contains("/api/coche"), eq(List.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.ListarCoches();
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve cars"));
    }

    @Test
    void testGetCocheByMatriculaNoEncontrado() {
        when(restTemplate.getForObject(contains("/api/coche/buscar"), eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Coche no encontrado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getCocheByMatricula("NOEXISTE");
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve car"));
    }

    @Test
    void testActualizarCocheConErrorDeAutorizacion() {
        when(restTemplate.exchange(
                contains("/api/coche/actualizar"), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "No autorizado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.actualizarCoche("1234ABC", coche);
        });
        
        assertTrue(exception.getMessage().contains("Failed to update car"));
    }

    @Test
    void testLoginFallido() {
        when(restTemplate.postForObject(
                contains("/api/usuario/login"), 
                isNull(), 
                eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.login("usuario@ejemplo.com", "claveincorrecta");
        });
        
        assertTrue(exception.getMessage().contains("Login failed"));
    }

    @Test
    void testCrearReservaConErrorDeDatos() {
        when(restTemplate.postForObject(
                contains("/api/reservas/pedidos"), 
                any(Reserva.class), 
                eq(Reserva.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Datos inválidos"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.crearReserva(new Reserva());
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(
            exception.getMessage().contains("Error") || 
            exception.getMessage().contains("Failed") ||
            exception.getMessage().contains("pedido") ||
            exception.getMessage().contains("reserva")
        );
    }

    @Test
    void testGetUsuarioByEmailWithNullResponse() {
        when(restTemplate.getForObject(contains("/api/usuario/buscar"), eq(Usuario.class)))
            .thenReturn(null);
        
        Usuario result = clientService.getUsuarioByEmail("noexiste@example.com");
        
        assertNull(result);
    }

    @Test
    void testGetUsuarioByEmailWithNotFoundStatus() {
        when(restTemplate.getForObject(contains("/api/usuario/buscar"), eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        
        Usuario result = clientService.getUsuarioByEmail("noexiste@example.com");
        
        assertNull(result);
    }

    @Test
    void testListarCochesDisponiblesConErrorDeRed() {
        when(restTemplate.getForObject(contains("/api/coche/disponibles"), eq(List.class)))
            .thenThrow(new ResourceAccessException("Error de red"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.ListarCochesDisponibles();
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testObtenerReservasPendientesConErrorDeAutorizacion() {
        when(restTemplate.getForObject(contains("/api/reservas/pendientes"), eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Acceso denegado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasPendientes();
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testObtenerReservasCompradasConErrorDeServidor() {
        when(restTemplate.getForObject(contains("/api/reservas/compradas"), eq(List.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasCompradas();
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testEliminarUsuarioConErrorDeServidor() {
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor"))
            .when(restTemplate).delete(contains("/api/usuario/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarUsuario("test@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testEliminarReservaConErrorDeAutorizacion() {
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Acceso denegado"))
            .when(restTemplate).delete(contains("/api/reservas/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarReserva(1);
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testActualizarReservaConErrorDeServidor() {
        when(restTemplate.exchange(
                contains("/api/reservas/actualizar"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Reserva.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.actualizarReserva(1, reserva);
        });
        
        assertTrue(exception.getMessage().contains("Failed to update reservation"));
    }

    @Test
    void testBloquearUsuarioConErrorDeDatos() {
        when(restTemplate.exchange(
                contains("/api/usuario/bloquear"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Email inválido"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.bloquearUsuario("email_invalido");
        });
        
        assertTrue(exception.getMessage().contains("Failed to block user"));
    }

    @Test
    void testDesbloquearUsuarioConErrorDeAcceso() {
        when(restTemplate.exchange(
                contains("/api/usuario/desbloquear"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Permisos insuficientes"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.desbloquearUsuario("juan@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Failed to unblock user"));
    }

    @Test
    void testLogoutConErrorDeServidor() {
        when(restTemplate.postForObject(
                contains("/api/usuario/logout"),
                isNull(),
                eq(Void.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.logout("token123");
        });
        
        assertTrue(exception.getMessage().contains("Logout failed") || 
                   exception.getMessage().contains("Error"));
    }

    @Test
    void testCrearCocheConErrorDeDatos() {
        when(restTemplate.postForObject(
                contains("/api/coche/crear"),
                eq(coche),
                eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Datos de coche inválidos"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.crearCoche(coche);
        });
        
        assertTrue(exception.getMessage().contains("Failed to create car"));
    }

    @Test
    void testHacerPedidoConErrorDeConexion() {
        when(restTemplate.postForObject(
                contains("/api/reserva/pedido"),
                any(Reserva.class),
                eq(Reserva.class)))
            .thenThrow(new ResourceAccessException("Error de conexión"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.hacerPedido(reserva);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(
            exception.getMessage().contains("Error") || 
            exception.getMessage().contains("Failed") ||
            exception.getMessage().contains("pedido") ||
            exception.getMessage().contains("reserva")
        );
    }

    @Test
    void testObtenerReservasConfirmadasPorUsuarioConErrorDeServidor() {
        when(restTemplate.getForObject(
                contains("/api/reservas/usuario/confirmadas"),
                eq(List.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasConfirmadasPorUsuario("juan@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve"));
    }

    @Test
    void testObtenerReservasPendientesPorUsuarioConError404() {
        when(restTemplate.getForObject(
                contains("/api/reservas/usuario/pendientes"),
                eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasPendientesPorUsuario("noexiste@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve"));
    }

    @Test
    void testListarUsuariosRegistradosConErrorDeAutenticacion() {
        when(restTemplate.getForObject(
                contains("/api/usuario"),
                eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.listarUsuariosResgistrados();
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve users"));
    }

    @Test
    void testEliminarCocheConError404() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Coche no encontrado"))
            .when(restTemplate).delete(contains("/api/coche/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarCoche("NOEXISTE");
        });
        
        assertTrue(exception.getMessage().contains("Failed to delete car"));
    }

    @Test
    void testGetUsuarioByEmailConErrorDeServidor() {
        when(restTemplate.getForObject(contains("/api/usuario/buscar"), eq(Usuario.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getUsuarioByEmail("test@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Failed") || 
                   exception.getMessage().contains("Error") ||
                   exception.getMessage().contains("user"));
    }

}