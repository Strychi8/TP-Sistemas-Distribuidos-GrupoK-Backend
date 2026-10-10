package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.Customer;
import com.rentar.customer.grpc.CustomerList;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;

import java.util.List;

/**
 * Traduce los DTOs de la capa de servicio (Hito 1) a los mensajes protobuf del
 * contrato de H2-4 (proto/customer.proto). Mapper puro: sin lógica de negocio.
 */
public class CustomerGrpcMapper {

    private CustomerGrpcMapper() {
    }

    public static Customer toCustomer(ClienteResponseDTO cliente) {
        return Customer.newBuilder()
                .setIdCliente(cliente.getIdCliente())
                .setNombre(cliente.getNombre())
                .setApellido(cliente.getApellido())
                .setDni(cliente.getDni())
                .setEmail(cliente.getEmail())
                .setActivo(Boolean.TRUE.equals(cliente.getActivo()))
                .build();
    }

    /**
     * NOTA (H2-4, Fase 2): ClienteServiceImpl.listarClientes() todavía no pagina en la
     * base de datos. Hasta que se implemente paginación real, se expone la lista
     * completa como una única página para no bloquear el resto del flujo.
     */
    public static CustomerList toCustomerList(List<ClienteResponseDTO> clientes) {
        CustomerList.Builder builder = CustomerList.newBuilder();

        for (ClienteResponseDTO cliente : clientes) {
            builder.addCustomers(toCustomer(cliente));
        }

        builder.setTotalElements(clientes.size());
        builder.setTotalPages(1);

        return builder.build();
    }
}
