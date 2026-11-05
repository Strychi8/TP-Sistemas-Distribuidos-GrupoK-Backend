package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.*;
import com.rentar.customer_service.service.IAuthService;
import com.rentar.customer_service.service.IClienteService;
import com.rentar.customer_service.service.impl.JwtService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Adapter gRPC de Customer Service (puerto 9092).
 * Implementa los métodos RPC definidos en customer.proto cumpliendo con la
 * convención RPC_REQUEST_RESPONSE_UNIQUE de Buf Lint.
 */
@GrpcService
@RequiredArgsConstructor
public class CustomerGrpcServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final IClienteService clienteService;
    private final IAuthService authService;
    private final JwtService jwtService;

    @Override
    public void getClientes(GetClientesRequest request, StreamObserver<GetClientesResponse> responseObserver) {
        GetClientesResponse response = GetClientesResponse.newBuilder()
                .addAllCustomers(CustomerGrpcMapper.toCustomerList(clienteService.listarClientes()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getClienteById(GetClienteByIdRequest request, StreamObserver<GetClienteByIdResponse> responseObserver) {
        Customer customer = CustomerGrpcMapper.toCustomer(
                clienteService.obtenerClientePorId(request.getIdCliente()));
        GetClienteByIdResponse response = GetClienteByIdResponse.newBuilder()
                .setCustomer(customer)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMiPerfil(GetMiPerfilRequest request, StreamObserver<GetMiPerfilResponse> responseObserver) {
        var perfil = clienteService.obtenerMiPerfil(request.getEmail());
        responseObserver.onNext(CustomerGrpcMapper.toGetMiPerfilResponse(perfil));
        responseObserver.onCompleted();
    }

    @Override
    public void verificarClienteExistente(VerificarClienteExistenteRequest request,
            StreamObserver<VerificarClienteExistenteResponse> responseObserver) {
        Customer customer = CustomerGrpcMapper.toCustomer(
                clienteService.verificarClienteExistente(request.getDni()));
        VerificarClienteExistenteResponse response = VerificarClienteExistenteResponse.newBuilder()
                .setCustomer(customer)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void verificarClienteActivo(VerificarClienteActivoRequest request,
            StreamObserver<VerificarClienteActivoResponse> responseObserver) {
        Customer customer = CustomerGrpcMapper.toCustomer(
                clienteService.verificarClienteActivo(request.getIdCliente()));
        VerificarClienteActivoResponse response = VerificarClienteActivoResponse.newBuilder()
                .setCustomer(customer)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void verificarTokenBlacklist(VerificarTokenBlacklistRequest request,
            StreamObserver<VerificarTokenBlacklistResponse> responseObserver) {
        boolean revocado = jwtService.esTokenRevocado(request.getToken());
        responseObserver.onNext(AuthGrpcMapper.toVerificarTokenBlacklistResponse(revocado));
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        LoginResponse response = AuthGrpcMapper.toLoginResponse(
                authService.login(AuthGrpcMapper.toLoginRequestDTO(request)));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        authService.logout(request.getToken());
        responseObserver.onNext(AuthGrpcMapper.toLogoutResponse(true));
        responseObserver.onCompleted();
    }

    @Override
    public void crearCliente(CrearClienteRequest request, StreamObserver<CrearClienteResponse> responseObserver) {
        var dto = CustomerGrpcMapper.toClienteRequestDTO(request);
        var creado = clienteService.crearCliente(dto);
        CrearClienteResponse response = CrearClienteResponse.newBuilder()
                .setCustomer(CustomerGrpcMapper.toCustomer(creado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarCliente(ActualizarClienteRequest request, StreamObserver<ActualizarClienteResponse> responseObserver) {
        var dto = CustomerGrpcMapper.toClienteUpdateDTO(request);
        var actualizado = clienteService.actualizarCliente(request.getIdCliente(), dto);
        ActualizarClienteResponse response = ActualizarClienteResponse.newBuilder()
                .setCustomer(CustomerGrpcMapper.toCustomer(actualizado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void eliminarCliente(EliminarClienteRequest request, StreamObserver<EliminarClienteResponse> responseObserver) {
        clienteService.eliminarCliente(request.getIdCliente());
        responseObserver.onNext(CustomerGrpcMapper.toEliminarClienteResponse(true));
        responseObserver.onCompleted();
    }
}
