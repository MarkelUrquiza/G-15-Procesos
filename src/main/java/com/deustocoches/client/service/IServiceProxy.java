package com.deustocoches.client.service;

import java.util.List;

import com.deustocoches.model.Coche;
import com.deustocoches.model.Reserva;
import com.deustocoches.model.Usuario;

public interface IServiceProxy {
	// CRUD operations for Usuario
	void eliminarUsuario(String email);
	Usuario getUsuarioByEmail(String email);
	List<Usuario> listarUsuariosResgistrados();
	Usuario registrarUsuario(Usuario usuario);
	String login(String email, String password);
	void logout(String token);
	Usuario bloquearUsuario(String email);
	Usuario desbloquearUsuario(String email);
	Usuario crearAdmin(String email);
	Usuario eliminarAdmin(String email);

	// CRUD operations for Coche
	List<Coche> ListarCoches();
	List<Coche> ListarCochesDisponibles();
	Coche getCocheByMatricula(String matricula);
	Coche actualizarCoche(String matricula, Coche coche);
	void eliminarCoche(String matricula);
	Coche crearCoche(Coche coche);
	List<Coche> filtrarCoches(String marca, String modelo, Double precioMin, Double precioMax);
	List<String> obtenerMarcas();
	Coche aplicarDescuento(String matricula, Double descuento);
	Coche eliminarDescuento(String matricula);

	// CRUD operations for Reserva
	Reserva crearReserva(Reserva reserva);
	Reserva actualizarReserva(Integer id, Reserva detallesReserva);
	void eliminarReserva(Integer id);
	Reserva hacerPedido(Reserva reserva);
	List<Reserva> obtenerReservasConfirmadasPorUsuario(String email);
	List<Reserva> obtenerReservasPendientesPorUsuario(String email);
	List<Reserva> obtenerReservasCompradas();
	List<Reserva> obtenerReservasPendientes();
	List<Reserva> obtenerReservasPorFecha(String fecha);
	List<Reserva> obtenerReservasPorRangoFechas(String desde, String hasta);
}