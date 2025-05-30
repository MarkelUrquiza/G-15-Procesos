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
    void testInit_SetsDefaultApiBaseUrlIfNull() throws Exception {
        // Fuerza apiBaseUrl a null antes de llamar a init()
        java.lang.reflect.Field field = RestTemplateServiceProxy.class.getDeclaredField("apiBaseUrl");
        field.setAccessible(true);
        field.set(clientService, null);

        clientService.init();

        String value = (String) field.get(clientService);
        assertEquals("http://127.0.0.1:8080/api", value);
    }

    @Test
    void testInit_SetsDefaultApiBaseUrlIfContainsPlaceholder() throws Exception {
        java.lang.reflect.Field field = RestTemplateServiceProxy.class.getDeclaredField("apiBaseUrl");
        field.setAccessible(true);
        field.set(clientService, "/api.base.url/otro");
        clientService.init();
        String value = (String) field.get(clientService);
        assertEquals("http://127.0.0.1:8080/api", value);
    }

    @Test
    void testInit_DoesNotOverrideValidApiBaseUrl() throws Exception {
        java.lang.reflect.Field field = RestTemplateServiceProxy.class.getDeclaredField("apiBaseUrl");
        field.setAccessible(true);
        field.set(clientService, "http://miapi:9090/api");
        clientService.init();
        String value = (String) field.get(clientService);
        assertEquals("http://miapi:9090/api", value);
    }

    @Test
    void testLogin() {
        String token = "abc123token";
        String url = apiBaseUrl + "/usuario/login?email=juan@example.com&password=password123";

        ResponseEntity<String> mockResponse = new ResponseEntity<>(token, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), isNull(), eq(String.class)))
            .thenReturn(mockResponse);

        String resultado = clientService.login("juan@example.com", "password123");

        assertNotNull(resultado);
        assertEquals(token, resultado);
    }

    @Test
    void testRegistrarUsuario() {
        String url = apiBaseUrl + "/usuario/registrar";

        when(restTemplate.postForObject(url, usuario, Usuario.class))
                .thenReturn(usuario);

        Usuario resultado = clientService.registrarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(restTemplate, times(1)).postForObject(url, usuario, Usuario.class);
    }

    @Test
    void testListarCoches() {
        String url = apiBaseUrl + "/coche";
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
        String url = apiBaseUrl + "/coche/disponibles";
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
        String url = apiBaseUrl + "/coche/buscar?matricula=1234ABC";

        when(restTemplate.getForObject(url, Coche.class))
                .thenReturn(coche);

        Coche resultado = clientService.getCocheByMatricula("1234ABC");

        assertNotNull(resultado);
        assertEquals("1234ABC", resultado.getMatricula());
        verify(restTemplate, times(1)).getForObject(url, Coche.class);
    }

    @Test
    void testCrearCoche() {
        String url = apiBaseUrl + "/coche/crear";

        when(restTemplate.postForObject(url, coche, Coche.class))
                .thenReturn(coche);

        Coche resultado = clientService.crearCoche(coche);

        assertNotNull(resultado);
        assertEquals(coche, resultado);
        verify(restTemplate, times(1)).postForObject(url, coche, Coche.class);
    }

    @Test
    void testActualizarCoche() {
        String url = apiBaseUrl + "/coche/actualizar?matricula=1234ABC";

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
        String url = apiBaseUrl + "/coche/eliminar?matricula=1234ABC";

        doNothing().when(restTemplate).delete(url);

        clientService.eliminarCoche("1234ABC");

        verify(restTemplate, times(1)).delete(url);
    }

    @Test
    void testCrearReserva() {
        String url = apiBaseUrl + "/reservas/pedidos";

        when(restTemplate.postForObject(url, reserva, Reserva.class))
                .thenReturn(reserva);

        Reserva resultado = clientService.crearReserva(reserva);

        assertNotNull(resultado);
        assertEquals(reserva, resultado);
        verify(restTemplate, times(1)).postForObject(url, reserva, Reserva.class);
        verify(restTemplate).postForObject(contains("/reservas/pedidos"), eq(reserva), eq(Reserva.class));
    }

    @Test
    void testObtenerReservasPendientes() {
        String url = apiBaseUrl + "/reservas/pendientes";
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
        String url = apiBaseUrl + "/reservas/compradas";
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
        String url = apiBaseUrl + "/usuario/buscar?email=juan@example.com";

        ResponseEntity<Usuario> mockResponse = new ResponseEntity<>(usuario, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenReturn(mockResponse);

        Usuario resultado = clientService.getUsuarioByEmail("juan@example.com");

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.getEmail());
    }

    @Test
    void testGetUsuarioByEmail_CatchIf404ReturnsNull() {
        String email = "noexiste@example.com";
        String url = apiBaseUrl + "/usuario/buscar?email=" + email;

        // Simula que el servidor responde con 404
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Usuario resultado = clientService.getUsuarioByEmail(email);

        assertNull(resultado);
        verify(restTemplate).getForEntity(eq(url), eq(Usuario.class));
    }

    @Test
    void testBloquearUsuario() {
        String url = apiBaseUrl + "/usuario/bloquear?email=juan@example.com";
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
        String url = apiBaseUrl + "/usuario/desbloquear?email=juan@example.com";
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
        String url = apiBaseUrl + "/reservas/usuario/confirmadas?email=juan@example.com";
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
        String url = apiBaseUrl + "/reservas/usuario/pendientes?email=juan@example.com";
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
        String url = apiBaseUrl + "/usuario/logout?token=" + token;

        when(restTemplate.postForObject(eq(url), isNull(), eq(Void.class)))
                .thenReturn(null);

        clientService.logout(token);

        verify(restTemplate, times(1)).postForObject(eq(url), isNull(), eq(Void.class));
    }

    @Test
    void testMetodoQuePuedeLanzarExcepcion() {
        when(restTemplate.getForObject(contains("/ejemplo"), eq(String.class)))
            .thenThrow(new RestClientException("Error de conexión simulado"));
        
        Exception excepcion = assertThrows(RuntimeException.class, () -> {
            clientService.metodoQuePuedeLanzarExcepcion();
        });
        
        assertTrue(excepcion.getMessage().contains("Error durante la llamada a la API"));
        verify(restTemplate).getForObject(contains("/ejemplo"), eq(String.class));
    }

    @Test
    void testMetodoQuePuedeLanzarExcepcionExitosoSinExcepcion() {
        when(restTemplate.getForObject(contains("/ejemplo"), eq(String.class)))
            .thenReturn("Resultado exitoso");
        
        assertDoesNotThrow(() -> {
            clientService.metodoQuePuedeLanzarExcepcion();
        });
        
        verify(restTemplate).getForObject(contains("/ejemplo"), eq(String.class));
    }

    @Test
    void testMetodoQueDevuelveLista() {
        List<String> datosEsperados = Arrays.asList("elemento1", "elemento2", "elemento3");
        
        when(restTemplate.getForObject(contains("/coleccion"), eq(List.class)))
            .thenReturn(datosEsperados);
        
        List<?> resultado = clientService.metodoQueDevuelveLista();
        
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(restTemplate).getForObject(contains("/coleccion"), eq(List.class));
    }

    @Test
    void testMetodoQueDevuelveListaVacia() {
        when(restTemplate.getForObject(contains("/coleccion"), eq(List.class)))
            .thenReturn(Collections.emptyList());
        
        List<?> resultado = clientService.metodoQueDevuelveLista();
        
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(restTemplate).getForObject(contains("/coleccion"), eq(List.class));
    }

    @Test
    void testActualizarReserva() {
        Integer id = 1;
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setPrecioTotal(100.0);
        
        when(restTemplate.exchange(
                contains("/reservas/actualizar/" + id),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Reserva.class)))
            .thenReturn(ResponseEntity.ok(reserva));
        
        Reserva resultado = clientService.actualizarReserva(id, reserva);
        
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(restTemplate).exchange(
            contains("/reservas/actualizar/" + id),
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
        
        when(restTemplate.postForObject(contains("/reservas/pedidos"), eq(reserva), eq(Reserva.class)))
            .thenReturn(reservaCreada);
        
        Reserva resultado = clientService.hacerPedido(reserva);
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals(150.0, resultado.getPrecioTotal());
        verify(restTemplate).postForObject(contains("/reservas/pedidos"), eq(reserva), eq(Reserva.class));
    }

    @Test
    void testListarUsuariosRegistrados() {
        List<Usuario> usuariosEsperados = Arrays.asList(new Usuario(), new Usuario());
        
        when(restTemplate.getForObject(contains("/usuario"), eq(List.class)))
            .thenReturn(usuariosEsperados);
        
        List<Usuario> resultado = clientService.listarUsuariosResgistrados();
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(restTemplate).getForObject(contains("/usuario"), eq(List.class));
    }

    @Test
    void testEliminarUsuario() {
        doNothing().when(restTemplate).delete(contains("/usuario/eliminar?email=test@example.com"));
        
        clientService.eliminarUsuario("test@example.com");
        
        verify(restTemplate).delete(contains("/usuario/eliminar?email=test@example.com"));
    }

    @Test
    void testEliminarReserva() {
        doNothing().when(restTemplate).delete(contains("/reservas/eliminar/1"));
        
        clientService.eliminarReserva(1);
        
        verify(restTemplate).delete(contains("/reservas/eliminar/1"));
    }

    @Test
    void testMetodoQueDevuelveListaConExcepcion() {
        when(restTemplate.getForObject(contains("/coleccion"), eq(List.class)))
            .thenThrow(new RestClientException("Error al recuperar lista"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.metodoQueDevuelveLista();
        });
        
        assertTrue(exception.getMessage().contains("Error al recuperar la lista"));
    }

    @Test
    void testRegistrarUsuarioConExcepcion() {
        Usuario usuario = new Usuario();
        
        when(restTemplate.postForObject(contains("/usuario/registrar"), eq(usuario), eq(Usuario.class)))
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
    void testRegistrarUsuario_CatchException() {
        String url = apiBaseUrl + "/usuario/registrar";
        Usuario usuario = new Usuario();

        when(restTemplate.postForObject(url, usuario, Usuario.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Datos inválidos"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.registrarUsuario(usuario);
        });

        assertTrue(exception.getMessage().contains("Failed to register user"));
        verify(restTemplate).postForObject(url, usuario, Usuario.class);
    }

    @Test
    void testListarCochesConErrorDeServidor() {
        when(restTemplate.getForObject(contains("/coche"), eq(List.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.ListarCoches();
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve cars"));
    }

    @Test
    void testGetCocheByMatriculaNoEncontrado() {
        when(restTemplate.getForObject(contains("/coche/buscar"), eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Coche no encontrado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getCocheByMatricula("NOEXISTE");
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve car"));
    }

    @Test
    void testActualizarCocheConErrorDeAutorizacion() {
        when(restTemplate.exchange(
                contains("/coche/actualizar"), 
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
        String url = apiBaseUrl + "/usuario/login?email=usuario@ejemplo.com&password=claveincorrecta";
        
        when(restTemplate.postForEntity(eq(url), isNull(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Login failed"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.login("usuario@ejemplo.com", "claveincorrecta");
        });
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Login failed") || 
                   exception.getMessage().contains("401"));
    }

    @Test
    void testLogin_CatchIf400ReturnsNull() {
        String email = "usuario@ejemplo.com";
        String password = "claveincorrecta";
        String url = apiBaseUrl + "/usuario/login?email=" + email + "&password=" + password;

        when(restTemplate.postForEntity(eq(url), isNull(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Login failed"));

        String resultado = clientService.login(email, password);

        assertNull(resultado);
        verify(restTemplate).postForEntity(eq(url), isNull(), eq(String.class));
    }

    @Test
    void testLogin_ThrowsExceptionOnNon2xxStatus() {
        String email = "usuario@ejemplo.com";
        String password = "claveincorrecta";
        String url = apiBaseUrl + "/usuario/login?email=" + email + "&password=" + password;

        ResponseEntity<String> response = new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        when(restTemplate.postForEntity(eq(url), isNull(), eq(String.class)))
            .thenReturn(response);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Login failed"));
        verify(restTemplate).postForEntity(eq(url), isNull(), eq(String.class));
    }

    @Test
    void testCrearReservaConErrorDeDatos() {
        when(restTemplate.postForObject(
                contains("/reservas/pedidos"), 
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
        String url = apiBaseUrl + "/usuario/buscar?email=null@example.com";

        ResponseEntity<Usuario> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenReturn(mockResponse);

        Usuario resultado = clientService.getUsuarioByEmail("null@example.com");

        assertNull(resultado);
    }

    @Test
    void testGetUsuarioByEmailWithNotFoundStatus() {
        String url = apiBaseUrl + "/usuario/buscar?email=noexiste@example.com";

        ResponseEntity<Usuario> mockResponse = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenReturn(mockResponse);

        Usuario resultado = clientService.getUsuarioByEmail("noexiste@example.com");

        assertNull(resultado);
    }

    @Test
    void testListarCochesDisponiblesConErrorDeRed() {
        when(restTemplate.getForObject(contains("/coche/disponibles"), eq(List.class)))
            .thenThrow(new ResourceAccessException("Error de red"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.ListarCochesDisponibles();
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testObtenerReservasPendientesConErrorDeAutorizacion() {
        when(restTemplate.getForObject(contains("/reservas/pendientes"), eq(List.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Acceso denegado"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasPendientes();
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testObtenerReservasCompradasConErrorDeServidor() {
        when(restTemplate.getForObject(contains("/reservas/compradas"), eq(List.class)))
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
            .when(restTemplate).delete(contains("/usuario/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarUsuario("test@example.com");
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testEliminarReservaConErrorDeAutorizacion() {
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Acceso denegado"))
            .when(restTemplate).delete(contains("/reservas/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarReserva(1);
        });
        
        assertTrue(exception.getMessage().contains("Error") || 
                   exception.getMessage().contains("Failed"));
    }

    @Test
    void testActualizarReservaConErrorDeServidor() {
        when(restTemplate.exchange(
                contains("/reservas/actualizar"),
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
                contains("/usuario/bloquear"),
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
                contains("/usuario/desbloquear"),
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
                contains("/usuario/logout"),
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
                contains("/coche/crear"),
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
                contains("/reservas/pedidos"),
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
                contains("/reservas/usuario/confirmadas"),
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
                contains("/reservas/usuario/pendientes"),
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
                contains("/usuario"),
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
            .when(restTemplate).delete(contains("/coche/eliminar"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarCoche("NOEXISTE");
        });
        
        assertTrue(exception.getMessage().contains("Failed to delete car"));
    }

    @Test
    void testGetUsuarioByEmailConErrorDeServidor() {
        String url = apiBaseUrl + "/usuario/buscar?email=error@example.com";
        
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getUsuarioByEmail("error@example.com");
        });
        
        assertNotNull(exception);
    }

    @Test
    void testGetUsuarioByEmailConExcepcionConnectionRefused() {
        String url = apiBaseUrl + "/usuario/buscar?email=exception@example.com";
        
        when(restTemplate.getForEntity(eq(url), eq(Usuario.class)))
            .thenThrow(new ResourceAccessException("Connection refused"));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.getUsuarioByEmail("exception@example.com");
        });
        
        assertNotNull(exception);
    }

    @Test
    void testFiltrarCoches_TodosLosParametros() {
        String url = apiBaseUrl + "/coche/filtrar?marca=Toyota&modelo=Corolla&precioMin=10000.0&precioMax=20000.0";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class)).thenReturn(coches);

        List<Coche> resultado = clientService.filtrarCoches("Toyota", "Corolla", 10000.0, 20000.0);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testFiltrarCoches_SoloMarca() {
        String url = apiBaseUrl + "/coche/filtrar?marca=Toyota&";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class)).thenReturn(coches);

        List<Coche> resultado = clientService.filtrarCoches("Toyota", null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testFiltrarCoches_SoloModelo() {
        String url = apiBaseUrl + "/coche/filtrar?modelo=Corolla&";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class)).thenReturn(coches);

        List<Coche> resultado = clientService.filtrarCoches(null, "Corolla", null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testFiltrarCoches_SoloPrecioMin() {
        String url = apiBaseUrl + "/coche/filtrar?precioMin=10000.0&";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class)).thenReturn(coches);

        List<Coche> resultado = clientService.filtrarCoches(null, null, 10000.0, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testFiltrarCoches_SoloPrecioMax() {
        String url = apiBaseUrl + "/coche/filtrar?precioMax=20000.0";
        List<Coche> coches = Arrays.asList(coche);

        when(restTemplate.getForObject(url, List.class)).thenReturn(coches);

        List<Coche> resultado = clientService.filtrarCoches(null, null, null, 20000.0);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testFiltrarCoches_Excepcion() {
        String url = apiBaseUrl + "/coche/filtrar?marca=Toyota&";
        when(restTemplate.getForObject(url, List.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error de filtrado"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.filtrarCoches("Toyota", null, null, null);
        });

        assertTrue(exception.getMessage().contains("Failed to filter cars"));
    }

    @Test
    void testObtenerMarcas() {
        String url = apiBaseUrl + "/coche/marcas";
        List<String> marcas = Arrays.asList("Toyota", "Honda");

        when(restTemplate.getForObject(url, List.class)).thenReturn(marcas);

        List<String> resultado = clientService.obtenerMarcas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testObtenerMarcas_Excepcion() {
        String url = apiBaseUrl + "/coche/marcas";
        when(restTemplate.getForObject(url, List.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de marcas"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerMarcas();
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve brands"));
    }

    @Test
    void testAplicarDescuento() {
        String url = apiBaseUrl + "/coche/aplicarDescuento?matricula=1234ABC&descuento=10.0";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class)))
            .thenReturn(new ResponseEntity<>(coche, HttpStatus.OK));

        Coche resultado = clientService.aplicarDescuento("1234ABC", 10.0);

        assertNotNull(resultado);
        assertEquals(coche, resultado);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class));
    }

    @Test
    void testAplicarDescuento_Excepcion() {
        String url = apiBaseUrl + "/coche/aplicarDescuento?matricula=1234ABC&descuento=10.0";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error de descuento"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.aplicarDescuento("1234ABC", 10.0);
        });

        assertTrue(exception.getMessage().contains("Failed to apply discount"));
    }

    @Test
    void testEliminarDescuento() {
        String url = apiBaseUrl + "/coche/eliminarDescuento?matricula=1234ABC";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class)))
            .thenReturn(new ResponseEntity<>(coche, HttpStatus.OK));

        Coche resultado = clientService.eliminarDescuento("1234ABC");

        assertNotNull(resultado);
        assertEquals(coche, resultado);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class));
    }

    @Test
    void testEliminarDescuento_Excepcion() {
        String url = apiBaseUrl + "/coche/eliminarDescuento?matricula=1234ABC";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Coche.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error al eliminar descuento"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarDescuento("1234ABC");
        });

        assertTrue(exception.getMessage().contains("Failed to remove discount"));
    }
    
    @Test
    void testListarCochesDisponibles_Excepcion() {
        String url = apiBaseUrl + "/coche/disponibles";
        when(restTemplate.getForObject(url, List.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de servidor"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.ListarCochesDisponibles();
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve available cars"));
    }

    @Test
    void testHacerPedido_Excepcion() {
        String url = apiBaseUrl + "/reservas/pedidos";
        Reserva reserva = new Reserva();

        when(restTemplate.postForObject(url, reserva, Reserva.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Datos inválidos"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.hacerPedido(reserva);
        });

        assertTrue(exception.getMessage().contains("Failed to create reservation"));
    }

    @Test
    void testObtenerReservasPorFecha() {
        String fecha = "2024-05-18";
        String url = apiBaseUrl + "/reservas/filtrar/fecha/" + fecha;
        List<Reserva> reservas = Arrays.asList(new Reserva());

        when(restTemplate.getForObject(url, List.class)).thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasPorFecha(fecha);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testObtenerReservasPorFecha_Excepcion() {
        String fecha = "2024-05-18";
        String url = apiBaseUrl + "/reservas/filtrar/fecha/" + fecha;

        when(restTemplate.getForObject(url, List.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de servidor"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasPorFecha(fecha);
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve reservations by date"));
    }

    @Test
    void testCrearAdmin() {
        String email = "admin@example.com";
        String url = apiBaseUrl + "/usuario/crearadmin?email=" + email;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class)))
            .thenReturn(new ResponseEntity<>(usuario, HttpStatus.OK));

        Usuario resultado = clientService.crearAdmin(email);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class));
    }

    @Test
    void testCrearAdmin_Excepcion() {
        String email = "admin@example.com";
        String url = apiBaseUrl + "/usuario/crearadmin?email=" + email;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error al cambiar rol"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.crearAdmin(email);
        });

        assertTrue(exception.getMessage().contains("Failed to change user rol"));
    }

    @Test
    void testEliminarAdmin() {
        String email = "cliente@example.com";
        String url = apiBaseUrl + "/usuario/eliminaradmin?email=" + email;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class)))
            .thenReturn(new ResponseEntity<>(usuario, HttpStatus.OK));

        Usuario resultado = clientService.eliminarAdmin(email);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class));
    }

    @Test
    void testEliminarAdmin_Excepcion() {
        String email = "cliente@example.com";
        String url = apiBaseUrl + "/usuario/eliminaradmin?email=" + email;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(Usuario.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error al cambiar rol"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.eliminarAdmin(email);
        });

        assertTrue(exception.getMessage().contains("Failed to change user rol"));
    }

    @Test
    void testObtenerReservasPorRangoFechas() {
        String desde = "2024-05-01";
        String hasta = "2024-05-31";
        String url = apiBaseUrl + "/reservas/filtrar/rango?desde=" + desde + "&hasta=" + hasta;
        List<Reserva> reservas = Arrays.asList(reserva);

        when(restTemplate.getForObject(url, List.class)).thenReturn(reservas);

        List<Reserva> resultado = clientService.obtenerReservasPorRangoFechas(desde, hasta);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restTemplate).getForObject(url, List.class);
    }

    @Test
    void testObtenerReservasPorRangoFechas_Excepcion() {
        String desde = "2024-05-01";
        String hasta = "2024-05-31";
        String url = apiBaseUrl + "/reservas/filtrar/rango?desde=" + desde + "&hasta=" + hasta;

        when(restTemplate.getForObject(url, List.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de servidor"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clientService.obtenerReservasPorRangoFechas(desde, hasta);
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve reservations by date range"));
    }
}