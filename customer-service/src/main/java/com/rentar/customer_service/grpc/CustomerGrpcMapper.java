package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.ActualizarClienteRequest;
import com.rentar.customer.grpc.CrearClienteRequest;
import com.rentar.customer.grpc.Customer;
import com.rentar.customer.grpc.EliminarClienteResponse;
import com.rentar.customer.grpc.GetMiPerfilResponse;
import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.request.ClienteUpdateDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduce los DTOs de la capa de servicio a los mensajes protobuf del
 * contrato de proto/customer.proto. Mapper puro: sin lógica de negocio.
 */
public class CustomerGrpcMapper {

    private CustomerGrpcMapper() {
    }

    public static Customer toCustomer(ClienteResponseDTO cliente) {
        Customer.Builder builder = Customer.newBuilder()
                .setIdCliente(cliente.getIdCliente() != null ? cliente.getIdCliente() : 0L)
                .setNombre(cliente.getNombre() != null ? cliente.getNombre() : "")
                .setApellido(cliente.getApellido() != null ? cliente.getApellido() : "")
                .setDni(cliente.getDni() != null ? cliente.getDni() : "")
                .setEmail(cliente.getEmail() != null ? cliente.getEmail() : "")
                .setActivo(Boolean.TRUE.equals(cliente.getActivo()))
                .setTelefono(cliente.getTelefono() != null ? cliente.getTelefono() : "");

        if (cliente.getFechaNacimiento() != null) {
            builder.setFechaNacimiento(toGoogleDate(cliente.getFechaNacimiento()));
        }

        return builder.build();
    }

    public static List<Customer> toCustomerList(List<ClienteResponseDTO> clientes) {
        return clientes.stream()
                .map(CustomerGrpcMapper::toCustomer)
                .collect(Collectors.toList());
    }

    public static ClienteRequestDTO toClienteRequestDTO(CrearClienteRequest req) {
        return ClienteRequestDTO.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .dni(req.getDni())
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .telefono(req.getTelefono())
                .fechaNacimiento(req.hasFechaNacimiento() ? toLocalDate(req.getFechaNacimiento()) : null)
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
                .fechaNacimiento(req.hasFechaNacimiento() ? toLocalDate(req.getFechaNacimiento()) : null)
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

    // Helper (google.type.Date -> LocalDate)
    public static LocalDate toLocalDate(com.google.type.Date date) {
        if (date == null || date.getYear() == 0) {
            return null;
        }
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    // Helper (LocalDate -> google.type.Date)
    public static com.google.type.Date toGoogleDate(LocalDate fecha) {
        if (fecha == null) {
            return com.google.type.Date.getDefaultInstance();
        }
        return com.google.type.Date.newBuilder()
                .setYear(fecha.getYear())
                .setMonth(fecha.getMonthValue())
                .setDay(fecha.getDayOfMonth())
                .build();
    }
}
