package com.deustocoches.client.service;

import com.deustocoches.model.Reserva;
import com.deustocoches.model.Usuario;
import com.deustocoches.model.Coche;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RestTemplateServiceProxy implements IServiceProxy {

    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public RestTemplateServiceProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Usuario> listarUsuariosResgistrados() {
        String url = apiBaseUrl + "/api/usuario";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve users: " + e.getStatusText());
        }
    }

    @Override
    public Usuario getUsuarioByEmail(String email) {
        String url = apiBaseUrl + "/api/usuario/buscar?email=" + email;
        try {
            Usuario usuario = restTemplate.getForObject(url, Usuario.class);
            return usuario;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw new RuntimeException("Failed to retrieve user by email: " + e.getStatusText());
        }
    }

    @Override
    public void eliminarUsuario(String email) {
        String url = apiBaseUrl + "/api/usuario/eliminar?email=" + email;
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to delete user: " + e.getStatusText());
        }
    }

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        String url = apiBaseUrl + "/api/usuario/registrar";
        try {
            return restTemplate.postForObject(url, usuario, Usuario.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to register user: " + e.getStatusText());
        }
    }

    @Override
    public String login(String email, String password) {
        try {
            // URL con parámetros en la query string
            String url = apiBaseUrl + "/api/usuario/login?email=" + email + "&password=" + password;
            
            // Usar postForObject en lugar de postForEntity
            return restTemplate.postForObject(url, null, String.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Login failed: " + e.getStatusText());
        }
    }

    @Override
    public void logout(String token) {
        String url = apiBaseUrl + "/api/usuario/logout?token=" + token;
        try {
            restTemplate.postForObject(url, null, Void.class);

        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Logout failed: " + e.getStatusText());
        }
    }
    @Override
    public Reserva crearReserva(Reserva reserva) {
        String url = apiBaseUrl + "/api/reservas/pedidos";
        try {
            return restTemplate.postForObject(url, reserva, Reserva.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to create reservation: " + e.getStatusText());
        }
    }

    @Override
    public Reserva actualizarReserva(Integer id, Reserva detallesReserva) {
        String url = apiBaseUrl + "/api/reservas/actualizar/" + id;
        try {
            return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(detallesReserva), Reserva.class)
                    .getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to update reservation: " + e.getStatusText());
        }
    }

    @Override
    public void eliminarReserva(Integer id) {
        String url = apiBaseUrl + "/api/reservas/eliminar/" + id;
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to delete reservation: " + e.getStatusText());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Coche> ListarCoches() {
        String url = apiBaseUrl + "/api/coche";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve cars: " + e.getStatusText());
        }
    }

    @Override
    public Coche getCocheByMatricula(String matricula) {
        String url = apiBaseUrl + "/api/coche/buscar?matricula=" + matricula;
        try {
            Coche coche = restTemplate.getForObject(url, Coche.class);
            return coche;
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve car by license plate: " + e.getStatusText());
        }
    }

    @Override
    public Coche crearCoche(Coche coche) {
        String url = apiBaseUrl + "/api/coche/crear";
        try {
            return restTemplate.postForObject(url, coche, Coche.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to create car: " + e.getStatusText());
        }
    }

    @Override
    public Coche actualizarCoche(String matricula, Coche coche) {
        String url = apiBaseUrl + "/api/coche/actualizar?matricula=" + matricula;
        try {
            return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(coche), Coche.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to update car: " + e.getStatusText());
        }
    }

    @Override
    public void eliminarCoche(String matricula) {
        String url = apiBaseUrl + "/api/coche/eliminar?matricula=" + matricula;
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to delete car: " + e.getStatusText());
        }
    }

    public List<Coche> filtrarCoches(String marca, String modelo, Double precioMin, Double precioMax) {
        String url = apiBaseUrl + "/api/coche/filtrar?";
        if (marca != null) url += "marca=" + marca + "&";
        if (modelo != null) url += "modelo=" + modelo + "&";
        if (precioMin != null) url += "precioMin=" + precioMin + "&";
        if (precioMax != null) url += "precioMax=" + precioMax;
        
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to filter cars: " + e.getStatusText());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Coche> ListarCochesDisponibles() {
        String url = apiBaseUrl + "/api/coche/disponibles";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve available cars: " + e.getStatusText());
        }
    }

    @Override
    public Reserva hacerPedido(Reserva reserva) {
        String url = apiBaseUrl + "/api/reserva/pedido";
        try {
            return restTemplate.postForObject(url, reserva, Reserva.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to create reservation: " + e.getStatusText());
        }
    }
          

    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasPorFecha(String fecha) {
        String url = apiBaseUrl + "/api/reservas/filtrar/fecha/" + fecha;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve reservations by date: " + e.getStatusText());
        }
    }

    @Override
    public Usuario bloquearUsuario(String email) {
        String url = apiBaseUrl + "/api/usuario/bloquear?email=" + email;
        try {
            return restTemplate.exchange(url, HttpMethod.PUT, null, Usuario.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to block user: " + e.getStatusText());
        }
    }

    @Override
    public Usuario desbloquearUsuario(String email) {
        String url = apiBaseUrl + "/api/usuario/desbloquear?email=" + email;
        try {
            return restTemplate.exchange(url, HttpMethod.PUT, null, Usuario.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to unblock user: " + e.getStatusText());
        }
    }

    // Obtener reservas confirmadas por usuario
    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasConfirmadasPorUsuario(String email) {
        String url = apiBaseUrl + "/api/reservas/usuario/confirmadas?email=" + email;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve confirmed reservations by user: " + e.getStatusText());
        }
    }

    // Obtener reservas pendientes por usuario
    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasPendientesPorUsuario(String email) {
        String url = apiBaseUrl + "/api/reservas/usuario/pendientes?email=" + email;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve pending reservations by user: " + e.getStatusText());
        }
    }

    // Obtener todas las reservas compradas
    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasCompradas() {
        String url = apiBaseUrl + "/api/reservas/compradas";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve bought reservations: " + e.getStatusText());
        }
    }

    // Obtener todas las reservas pendientes
    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasPendientes() {
        String url = apiBaseUrl + "/api/reservas/pendientes";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve pending reservations: " + e.getStatusText());
        }
    }

        /**
     * Método que podría lanzar una excepción al realizar una operación remota.
     * Se utiliza para probar el manejo de errores en la aplicación.
     * @throws RuntimeException Si ocurre un error durante la llamada a la API
     */
    public void metodoQuePuedeLanzarExcepcion() {
        try {
            // Lógica que podría lanzar una excepción
            restTemplate.getForObject(apiBaseUrl + "/api/ejemplo", String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error durante la llamada a la API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Método que devuelve una lista genérica de elementos.
     * Se utiliza para probar la recuperación de colecciones de datos.
     * @return Lista de elementos recuperados del endpoint
     * @throws RuntimeException Si ocurre un error durante la llamada a la API
     */
    public List<?> metodoQueDevuelveLista() {
        String url = apiBaseUrl + "/api/coleccion";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al recuperar la lista de elementos: " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<Reserva> obtenerReservasPorRangoFechas(String desde, String hasta) {
        String url = apiBaseUrl + "/api/reservas/filtrar/rango?desde=" + desde + "&hasta=" + hasta;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Failed to retrieve reservations by date range: " + e.getStatusText());
        }
    }

}