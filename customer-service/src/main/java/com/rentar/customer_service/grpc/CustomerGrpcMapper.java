package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.ActualizarClienteRequest;
import com.rentar.customer.grpc.CrearClienteRequest;
import com.rentar.customer.grpc.Customer;
import com.rentar.customer.grpc.CustomerList;
import com.rentar.customer.grpc.EliminarClienteResponse;
import com.rentar.customer.grpc.GetMiPerfilResponse;
import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.request.ClienteUpdateDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Traduce los DTOs de la capa de servicio (Hito 1) a los mensajes protobuf del
 * contrato de H2-4 (proto/customer.proto). Mapper puro: sin lógica de negocio.
 */
public class CustomerGrpcMapper {

    private CustomerGrpcMapper() {
    }

    public static Customer toCustomer(ClienteResponseDTO cliente) {
        Customer.Builder builder = Customer.newBuilder()
                .setIdCliente(cliente.getIdCliente())
                .setNombre(cliente.getNombre())
                .setApellido(cliente.getApellido())
                .setDni(cliente.getDni())
                .setEmail(cliente.getEmail())
                .setActivo(Boolean.TRUE.equals(cliente.getActivo()))
                .setTelefono(cliente.getTelefono() != null ? cliente.getTelefono() : "");


        if (cliente.getFechaNacimiento() != null) {
            builder.setFechaNacimiento(toGoogleDate(cliente.getFechaNacimiento()));
        }

        return builder.build();
    }

    public static CustomerList toCustomerList(List<ClienteResponseDTO> clientes) {
        CustomerList.Builder builder = CustomerList.newBuilder();

        for (ClienteResponseDTO cliente : clientes) {
            builder.addCustomers(toCustomer(cliente));
        }

        return builder.build();
    }

    public static ClienteRequestDTO toClienteRequestDTO(CrearClienteRequest req) {
        return ClienteRequestDTO.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .dni(req.getDni())
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .telefono(req.getTelefono())
                .fechaNacimiento(parseLocalDate(req.getFechaNacimiento()))
                .build();
    }

    public static ClienteUpdateDTO toClienteUpdateDTO(ActualizarClienteRequest req) {
        return ClienteUpdateDTO.builder()
                .email(req.getEmail())
                .password(req.hasPassword() ? req.getPassword() : null)
                .dni(req.getDni())
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .telefono(req.getTelefono())
                .fechaNacimiento(parseLocalDate(req.getFechaNacimiento()))
                .build();
    }

    public static EliminarClienteResponse toEliminarClienteResponse(boolean success) {
        return EliminarClienteResponse.newBuilder()
                .setSuccess(success)
                .build();
    }

    public static GetMiPerfilResponse toGetMiPerfilResponse(ClienteResponseDTO cliente) {
        GetMiPerfilResponse.Builder builder = GetMiPerfilResponse.newBuilder()
                .setIdCliente(cliente.getIdCliente() != null ? cliente.getIdCliente() : 0L)
                .setIdUsuario(cliente.getIdUsuario() != null ? cliente.getIdUsuario() : 0L)
                .setEmail(cliente.getEmail() != null ? cliente.getEmail() : "")
                .setDni(cliente.getDni() != null ? cliente.getDni() : "")
                .setNombre(cliente.getNombre() != null ? cliente.getNombre() : "")
                .setApellido(cliente.getApellido() != null ? cliente.getApellido() : "")
                .setTelefono(cliente.getTelefono() != null ? cliente.getTelefono() : "")
                .setActivo(Boolean.TRUE.equals(cliente.getActivo()));

        if (cliente.getFechaNacimiento() != null) {
            builder.setFechaNacimiento(toGoogleDate(cliente.getFechaNacimiento()));
        }

        return builder.build();
    }

    private static LocalDate parseLocalDate(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDate.parse(str.trim());
    }

    // Helper reutilizable (LocalDate -> google.type.Date)
    private static com.google.type.Date toGoogleDate(LocalDate fecha) {
        return com.google.type.Date.newBuilder()
                .setYear(fecha.getYear())
                .setMonth(fecha.getMonthValue())
                .setDay(fecha.getDayOfMonth())
                .build();
    }
}
