package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.*;
import com.rentar.customer_service.service.IAuthService;
import com.rentar.customer_service.service.IClienteService;
import com.rentar.customer_service.service.impl.JwtService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Adapter gRPC de Customer Service (H2-4, puerto 9092).
 *
 * Fase 2: lectura y verificación (GetClientes, GetClienteById,
 * VerificarClienteExistente, VerificarClienteActivo).
 * Fase 4: autenticación y tokens (Login, Logout, VerificarTokenBlacklist).
 * El CRUD (CrearCliente/ActualizarCliente/EliminarCliente) y GetMiPerfil quedan
 * para una fase posterior.
 *
 * La clase es deliberadamente delgada: no contiene reglas de negocio (viven en
 * IClienteService/IAuthService/JwtService) ni mapeo de excepciones a Status
 * (eso lo resuelve GrpcGlobalExceptionHandler, Fase 3); las excepciones de
 * dominio se propagan tal cual.
 */
@GrpcService
@RequiredArgsConstructor
public class CustomerGrpcServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final IClienteService clienteService;
    private final IAuthService authService;
    private final JwtService jwtService;

    @Override
    public void getClientes(GetClientesRequest request, StreamObserver<CustomerList> responseObserver) {
        CustomerList response = CustomerGrpcMapper.toCustomerList(clienteService.listarClientes());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getClienteById(GetClienteByIdRequest request, StreamObserver<Customer> responseObserver) {
        Customer response = CustomerGrpcMapper.toCustomer(
                clienteService.obtenerClientePorId(request.getIdCliente()));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void verificarClienteExistente(VerificarClienteExistenteRequest request,
            StreamObserver<Customer> responseObserver) {
        Customer response = CustomerGrpcMapper.toCustomer(
                clienteService.verificarClienteExistente(request.getDni()));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void verificarClienteActivo(GetClienteByIdRequest request, StreamObserver<Customer> responseObserver) {
        Customer response = CustomerGrpcMapper.toCustomer(
                clienteService.verificarClienteActivo(request.getIdCliente()));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Única excepción del fail-closed (GrpcSecurityConfig.permitAll): no puede haber
     * identidad previa al momento de loguearse. Credenciales inválidas se traducen a
     * UNAUTHENTICATED por GrpcGlobalExceptionHandler.
     */
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        LoginResponse response = AuthGrpcMapper.toLoginResponse(
                authService.login(AuthGrpcMapper.toLoginRequestDTO(request)));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * AuthServiceImpl.logout es defensivo (nunca lanza: ignora tokens ya expirados,
     * ya revocados o malformados), por lo que success siempre es true si la llamada
     * no lanzó una excepción inesperada.
     */
    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        authService.logout(request.getToken());
        responseObserver.onNext(AuthGrpcMapper.toLogoutResponse(true));
        responseObserver.onCompleted();
    }

    @Override
    public void verificarTokenBlacklist(VerificarTokenRequest request,
            StreamObserver<VerificarTokenResponse> responseObserver) {
        boolean revocado = jwtService.esTokenRevocado(request.getToken());
        responseObserver.onNext(AuthGrpcMapper.toVerificarTokenResponse(revocado));
        responseObserver.onCompleted();
    }

    @Override
    public void crearCliente(CrearClienteRequest request, StreamObserver<Customer> responseObserver) {
        var dto = CustomerGrpcMapper.toClienteRequestDTO(request);
        var creado = clienteService.crearCliente(dto);
        responseObserver.onNext(CustomerGrpcMapper.toCustomer(creado));
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarCliente(ActualizarClienteRequest request, StreamObserver<Customer> responseObserver) {
        var dto = CustomerGrpcMapper.toClienteUpdateDTO(request);
        var actualizado = clienteService.actualizarCliente(request.getIdCliente(), dto);
        responseObserver.onNext(CustomerGrpcMapper.toCustomer(actualizado));
        responseObserver.onCompleted();
    }

    @Override
    public void eliminarCliente(EliminarClienteRequest request, StreamObserver<EliminarClienteResponse> responseObserver) {
        clienteService.eliminarCliente(request.getIdCliente());
        responseObserver.onNext(CustomerGrpcMapper.toEliminarClienteResponse(true));
        responseObserver.onCompleted();
    }

    @Override
    public void getMiPerfil(GetMiPerfilRequest request, StreamObserver<GetMiPerfilResponse> responseObserver) {
        var perfil = clienteService.obtenerMiPerfil(request.getEmail());
        responseObserver.onNext(CustomerGrpcMapper.toGetMiPerfilResponse(perfil));
        responseObserver.onCompleted();
    }
}
