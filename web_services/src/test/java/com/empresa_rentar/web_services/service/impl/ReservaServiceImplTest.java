package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.ReservaRequestDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.exception.custom.BusinessException;
import com.empresa_rentar.web_services.exception.custom.ResourceNotFoundException;
import com.empresa_rentar.web_services.mapper.ReservaMapper;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Reserva;
import com.empresa_rentar.web_services.model.Vehiculo;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.repository.IReservaRepository;
import com.empresa_rentar.web_services.repository.IVehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private IVehiculoRepository vehiculoRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaServiceImpl reservaServiceImpl;

    @Test
    @DisplayName("CP01: Debe lanzar BusinessException cuando fechafin es anterior o igual a fechaInicio")
    void crearReserva_FechaFinInvalida_LanzaBusinessException() {
        // Arrange (Preparaición)
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFinInvalida = fechaInicio.minusHours(1); // 1 hora antes

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                1L,
                1L,
                fechaInicio,
                fechaFinInvalida

        );

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest)
        );

        // validamos el mensaje de erro de negocio
        assertEquals("La fecha de fin debe ser posterior a la fecha de inicio", exception.getMessage());

        // Verificamos que NINGÚN repositorio haya sido consultado (aislamiento y eficiencia)
        verifyNoInteractions(clienteRepository, vehiculoRepository, reservaRepository);
    }

    @Test
    @DisplayName("CP02: Debe lanzar ResourceNotFoundException cuando el cliente no existe")
    void crearReserva_ClienteNoExiste_LanzaResourceNotFoundException() {
        // Arrange (Preparación)
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);
        Long idClienteInexistente = 2L;

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idClienteInexistente, // idCliente
                1L,                  // idVehiculo
                fechaInicio,         // fechaInicio
                fechaFin            //fechaFin
        );

        //STUBBING: Le enseñamos a Mockito cómo debe responder la interfaz del repositorio
        when(clienteRepository.findById(idClienteInexistente)).thenReturn(Optional.empty());

        // Act & Assert (Ejecución y Verificación)
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest)
        );

        // Validamos el mensaje devuelto
        assertEquals("Cliente no encontrado con ID: 2", exception.getMessage());

        // Verificamos que se haya llamdo a cleinteRepository.finById una vez
        verify(clienteRepository, times(1)).findById(idClienteInexistente);

        // Verificamos que NO se haya intentado consultar vehículo ni guardar reservas
        verifyNoInteractions(vehiculoRepository, reservaRepository);

    }

    @Test
    @DisplayName("CP03: Debe lanzar BusinessException cuando el cliente está inactivo")
    void crearReserva_ClienteInactivo_LanzaBusinessException() {
        // Arrange (Preparación)
        Long idCliente = 1L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idCliente,
                1L,
                fechaInicio,
                fechaFin
        );

        // Instanciamos un cliente inactivo utilizando el patrón Builder de Lombock
        Cliente clienteInactivo = Cliente.builder()
                .idCliente(idCliente)
                .nombre("Diego")
                .apellido("Fernandez")
                .activo(false)
                .build();

        // STUBBING: Retornamos un Optional poblado con el cliente inactivo
        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteInactivo));

        // Act & Assert (Ejecución y Verificación)
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest));

        // Verificamos que el mensaje corresponda a la regla de negocio
        assertEquals("El cliente seleccionado se encuentra inactivo y no puede realizar alquileres",
                exception.getMessage());

        // Verificamos que se consultó al cliente pero no se continuó hacia vehículos o reservas
        verify(clienteRepository, times(1)).findById(idCliente);
        verifyNoInteractions(vehiculoRepository, reservaRepository);
    }

    @Test
    @DisplayName("CP4: Debe lanzar ResourceNotException cuando el vehículo no existe")
    void crearReserva_VehiculoNoExiste_LanzaResourceNotFoundException() {
        // Arrange (Preparación)
        Long idCliente = 1L;
        Long idVehiculoInexistente = 2L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idCliente,
                idVehiculoInexistente,
                fechaInicio,
                fechaFin
        );

        // STUBBING: Retornamos un cliente activo para pasar la validación de cliente
        Cliente clienteActivo = Cliente.builder()
                .idCliente(idCliente)
                .nombre("Diego")
                .apellido("Fernandez")
                .activo(true)
                .build();

        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteActivo));

        // STUBBING: Retornamos un Optional vacío para simular que el vehículo no existe
        when(vehiculoRepository.findById(idVehiculoInexistente)).thenReturn(Optional.empty());

        // Act & Assert (Ejecución y Verificación)
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest));

        // Verificamos que el mensaje corresponda a la regla de negocio
        assertEquals("Vehículo no encontrado con ID: 2", exception.getMessage());

        // Verificamos que se consultó al cliente y luego al vehículo, pero no se continuó hacia reservas
        verify(clienteRepository, times(1)).findById(idCliente);
        verify(vehiculoRepository, times(1)).findById(idVehiculoInexistente);
        // Confirmamos que NO se llegó a la búsqueda de solapamientos ni a guardar la reserva
        verifyNoInteractions(reservaRepository);
    }

    @Test
    @DisplayName("CP05: Debe lanzar BusinessException cuando el vehículo está inactivo")
    void crearReserva_VehiculoInactivo_LanzaBusinessException() {
        // Arrange (Preparación)
        Long idCliente = 1L;
        Long idVehiculo = 2L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idCliente,
                idVehiculo,
                fechaInicio,
                fechaFin
        );

        Cliente clienteActivo = Cliente.builder()
                .idCliente(idCliente)
                .activo(true)
                .build();
        // Instanciamos un vehículo inactivo (activo = false)
        Vehiculo vehiculoInactivo = Vehiculo.builder()
                .idVehiculo(idVehiculo)
                .patente("AA123CD")
                .activo(false)
                .build();
        // STUBBING de las búsquedas secuenciales
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteActivo));

        when(vehiculoRepository.findById(idVehiculo)).thenReturn(Optional.of(vehiculoInactivo));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest)
        );

        assertEquals("El vehículo seleccionado se encuentra inactivo",
                exception.getMessage());

        //Verificamos las búsquedas y confirmamos que no se consultaron solapamientos
        verify(clienteRepository, times(1)).findById(idCliente);
        verify(vehiculoRepository, times(1)).findById(idVehiculo);
        verifyNoInteractions(reservaRepository);
    }

    @Test
    @DisplayName("CP:06 Debe lanzar BusinessException cuando el vehículo ya tiene una reserva confirmada en las fechas elejidas ")
    void crearReserba_VehiculoConSolapaiento_LanzaBusinessException() {
        // Arrange
        Long idCliente = 1L;
        Long idVehiculo = 2L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idCliente,
                idVehiculo,
                fechaInicio,
                fechaFin
        );

        Cliente clienteActivo = Cliente.builder()
                .idCliente(idCliente)
                .activo(true)
                .build();

        Vehiculo vehiculoActivo = Vehiculo.builder()
                .idVehiculo(idVehiculo)
                .patente("AA123CD")
                .activo(true)
                .build();

        // STUBBING de las búsquedas previas
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteActivo));

        when(vehiculoRepository.findById(idVehiculo)).thenReturn(Optional.of(vehiculoActivo));

        // STUBBING del método de solapamiento en el repositorio de reservas
        when(reservaRepository.existeSolapamiento(
                eq(idVehiculo),
                eq(fechaInicio),
                eq(fechaFin),
                eq(EstadoReserva.CONFIRMADA)
        )).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservaServiceImpl.crearReserva(dtoRequest)
        );

        // Validamos el mensaje de negocio
        assertEquals("El vehículo ya posee una reserva confirmada dentro del periodo solicitado", exception.getMessage());

        // Verificamos que se ejecutaron las tres comprobaciones en orden
        verify(clienteRepository, times(1)).findById(idCliente);
        verify(vehiculoRepository, times(1)).findById(idVehiculo);
        verify(reservaRepository, times(1)).existeSolapamiento(idVehiculo, fechaInicio, fechaFin, EstadoReserva.CONFIRMADA);

        // Verificamos que no se haya intentado guardar la reserva
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP07: Debe crear y retornar la reserva exitosamente cuando todas las validaciones son correctas")
    void crearReserva_DatosValidos_RetornaReservaResponseDTO() {
        // Arrange
        Long idCliente = 1L;
        Long idVehiculo = 2L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3); // 3 días de alquiler

        ReservaRequestDTO dtoRequest = new ReservaRequestDTO(
                idCliente,
                idVehiculo,
                fechaInicio,
                fechaFin
        );

        Cliente clienteActivo = crearClienteValido();

        Vehiculo vehiculoActivo = crearVehiculoValido();

        Reserva reservaGuardada = Reserva.builder()
                .idReserva(1L)
                .cliente(clienteActivo)
                .vehiculo(vehiculoActivo)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .precioDiario(vehiculoActivo.getPrecioDiario())
                .importeTotal(vehiculoActivo.getPrecioDiario().multiply(new java.math.BigDecimal("3"))) // 3 días
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        ReservaResponseDTO responseEsperado = new ReservaResponseDTO(
                reservaGuardada.getIdReserva(),
                clienteActivo.getIdCliente(),
                clienteActivo.getNombre() + " " + clienteActivo.getApellido(),
                vehiculoActivo.getIdVehiculo(),
                vehiculoActivo.getPatente(),
                vehiculoActivo.getModelo(),
                fechaInicio,
                fechaFin,
                vehiculoActivo.getPrecioDiario(),
                new BigDecimal("300.00"), // 3 días * 100.00
                EstadoReserva.CONFIRMADA
        );

        // STUBBING del flujo completo
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteActivo));
        when(vehiculoRepository.findById(idVehiculo)).thenReturn(Optional.of(vehiculoActivo));
        when(reservaRepository.existeSolapamiento(idVehiculo, fechaInicio, fechaFin, EstadoReserva.CONFIRMADA)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoActivo);
        when(reservaMapper.mapToResponseDTO(any(Reserva.class))).thenReturn(responseEsperado);

        //assert
        ReservaResponseDTO resultado = reservaServiceImpl.crearReserva(dtoRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.idReserva());
        assertEquals(EstadoReserva.CONFIRMADA, resultado.estadoReserva());
        assertEquals(new BigDecimal("300.00"), resultado.importeTotal());

        //Verificamos que se ejecuto persistencia y el mapeo
        verify(reservaRepository, times(1)).save(any(Reserva.class));
        // Verificamos que el vehículo se actualizó a estado RESERVADO
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("CP08: Debe lanzar ResourceNotFoundException cuando la reserva a cancelar no existe")
    void cancelarReserva_ReservaNoExiste_LanzaResourceNotFoundException() {
        // Arrange
        Long idReservaInexistente = 1L;

        // STUBBING: Retornamos un Optional vacío para simular que la reserva no existe
        when(reservaRepository.findById(idReservaInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaServiceImpl.cancelarReserva(idReservaInexistente)
        );

        // Validamos el mensaje devuelto
        assertEquals("Reserva no encontrada con ID: 1", exception.getMessage());

        // Verificamos que se haya llamado a reservaRepository.findById una vez
        verify(reservaRepository, times(1)).findById(idReservaInexistente);

        // Verificamos que NO se haya intentado guardar la reserva cancelada
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP09: Debe lanzar BusinessException cuando la reserva ya comenzó o transcurrio")
    void cancelarReserva_PeriodoIniciado_LanzaBusinessException() {
        // Arrange
        Long idReserva = 1L;

        // Simulamos una reserva cuyo inicio ocurrió hace 2 horas (periodo iniciado)
        LocalDateTime fechaInicioPasada = LocalDateTime.now().minusHours(2);
        LocalDateTime fechaFinPasada = LocalDateTime.now().minusHours(3);

        Reserva reservaIniciada = Reserva.builder()
                .idReserva(idReserva)
                .fechaInicio(fechaInicioPasada)
                .fechaFin(fechaFinPasada)
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        //  STUBBING: Retornamos la reserva cuya fecha de inicio ya transcurrió
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaIniciada));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservaServiceImpl.cancelarReserva(idReserva));

        // Validamos el mensaje de la regla de negocio
        assertEquals("No se puede cancelar una reserva cuyo período de alquiler ya transcurrió", exception.getMessage());

        // Verificamos la busqueda en el repositorio
        verify(reservaRepository, times(1)).findById(idReserva);
        // Confirmamos que no se intento guardar ningun cambio
        verify(reservaRepository, never()).save(any(Reserva.class));

    }

    @Test
    @DisplayName("CP10: Debe lanzar BussinesException cuando la reserva ya se encuentra cancelada")
    void cacelarReserva_ReservaYaCancelada_LanzaBussinesException() {
        // Arrange
        Long idReserva = 1L;

        // Fecha futura para superar la validación temporal (CP9) y evaluar el estado
        LocalDateTime fechaInicioFutura = LocalDateTime.now().plusDays(5);
        LocalDateTime fechaFinFutura = fechaInicioFutura.plusDays(6);

        Reserva reservaCancelada = Reserva.builder()
                .idReserva(idReserva)
                .fechaInicio(fechaInicioFutura)
                .fechaFin(fechaFinFutura)
                .estado(EstadoReserva.CANCELADA) // Condición clave a testear
                .build();

        // STUBBING: Retornamos la reserva que ya está cancelada
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaCancelada));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> reservaServiceImpl.cancelarReserva(idReserva));

        // Validamos el mensaje de la regla de negocio
        assertEquals("La reserva ya se encuentra cancelada", exception.getMessage());

        // Verificamos que se haya llamado a reservaRepository.findById una vez
        verify(reservaRepository, times(1)).findById(idReserva);
        // Verificamos que NO se haya intentado guardar la reserva cancelada
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP11: Debe realizar la baja lógica cambiando el estado a CANCELAD y retornar el DTO cuando los datos son válidos")
    void cancelarReserva_DatosValidos_RetornaReservaResponseDTO() {
        // Arrange
        Long idReserva = 1L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = fechaInicio.plusDays(3); // 3 días de alquiler

        Cliente clienteActivo = crearClienteValido();
        Vehiculo vehiculoActivo = crearVehiculoValido();
        Reserva reservaGuardada = Reserva.builder()
                .idReserva(idReserva)
                .cliente(clienteActivo)
                .vehiculo(vehiculoActivo)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .precioDiario(vehiculoActivo.getPrecioDiario())
                .importeTotal(vehiculoActivo.getPrecioDiario().multiply(new java.math.BigDecimal("3"))) // 3 días
                .estado(EstadoReserva.CONFIRMADA)
                .build();
        Reserva reservaActualizada = Reserva.builder()
                .idReserva(idReserva)
                .cliente(clienteActivo)
                .vehiculo(vehiculoActivo)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .precioDiario(vehiculoActivo.getPrecioDiario())
                .importeTotal(vehiculoActivo.getPrecioDiario().multiply(new java.math.BigDecimal("3"))) // 3 días
                .estado(EstadoReserva.CANCELADA)
                .build();

        ReservaResponseDTO responseEsperado = new ReservaResponseDTO(
                reservaActualizada.getIdReserva(),
                clienteActivo.getIdCliente(),
                clienteActivo.getNombre() + " " + clienteActivo.getApellido(),
                vehiculoActivo.getIdVehiculo(),
                vehiculoActivo.getPatente(),
                vehiculoActivo.getModelo(),
                fechaInicio,
                fechaFin,
                vehiculoActivo.getPrecioDiario(),
                new BigDecimal("300.00"), // 3 días * 100.00
                EstadoReserva.CANCELADA
        );
        //STUBBING del flujo completo de cancelación
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaGuardada));

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaActualizada);

        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoActivo);

        when(reservaMapper.mapToResponseDTO(any(Reserva.class))).thenReturn(responseEsperado);

        // Act
        ReservaResponseDTO resultado = reservaServiceImpl.cancelarReserva(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(EstadoReserva.CANCELADA, resultado.estadoReserva());

        // Verificamos las llamadas al repositoria y al mapper
        verify(reservaRepository, times(1)).findById(1L);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
        // Verificamos que el vehículo se revirtió a estado DISPONIBLE
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
        verify(reservaMapper, times(1)).mapToResponseDTO(any(Reserva.class));
    }

    @Test
    @DisplayName("CP14: Debe lanzar BusinessException cuando la reserva ya está finalizada")
    void cancelarReserva_ReservaFinalizada_LanzaBusinessException() {
        // Arrange
        Long idReserva = 1L;

        // Fecha futura para superar la validación temporal (CP9) y evaluar el estado
        LocalDateTime fechaInicioFutura = LocalDateTime.now().plusDays(5);
        LocalDateTime fechaFinFutura = fechaInicioFutura.plusDays(6);

        Reserva reservaFinalizada = Reserva.builder()
                .idReserva(idReserva)
                .fechaInicio(fechaInicioFutura)
                .fechaFin(fechaFinFutura)
                .estado(EstadoReserva.FINALIZADA)
                .build();

        // STUBBING: Retornamos la reserva que ya está finalizada
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaFinalizada));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservaServiceImpl.cancelarReserva(idReserva));

        // Validamos el mensaje de la regla de negocio
        assertEquals("No se puede cancelar una reserva que ya fue finalizada", exception.getMessage());

        // Verificamos que se haya llamado a reservaRepository.findById una vez
        verify(reservaRepository, times(1)).findById(idReserva);
        // Verificamos que NO se haya intentado guardar la reserva cancelada
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CP12: Debe lanzar ResourceNotFounException cuando se busca un ID de reserva inexistente")
    void obtenerPorId_ReservaInexistente_LanzaResourceNotFoundException() {
        // Arrange
        Long idReservaInexistente = 1L;

        // STUBBING: Retornamos un Optional vacío para simular que la reserva no existe
        when(reservaRepository.findById(idReservaInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaServiceImpl.obtenerPorId(idReservaInexistente)
        );

        // Validamos el mensaje devuelto
        assertEquals("Reserva no encontrada con ID: 1", exception.getMessage());

        // Verificamos que se haya llamado a reservaRepository.findById una vez
        verify(reservaRepository, times(1)).findById(idReservaInexistente);
        verifyNoInteractions(reservaMapper);
    }

    @Test
    @DisplayName("CP13: Debe retornar el ReservaResponseDTO correctamente cuando la reserva existe por ID")
    void obtenerPorId_ReservaExistente_RetornaReservaResponseDTO() {
        // Arrange
        Long idReserva = 1L;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(2);
        LocalDateTime fechaFin = fechaInicio.plusDays(3);

        // Instanciamos los objetos completos
        Cliente clienteValido = crearClienteValido();
        Vehiculo vehiculoValido = crearVehiculoValido();

        Reserva reservaValida = Reserva.builder()
                .idReserva(idReserva)
                .cliente(clienteValido)
                .vehiculo(vehiculoValido)
                .precioDiario(vehiculoValido.getPrecioDiario())
                .importeTotal(vehiculoValido.getPrecioDiario().multiply(new java.math.BigDecimal("3"))) // 3 días
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();
        ReservaResponseDTO responseEsperado = new ReservaResponseDTO(
                reservaValida.getIdReserva(),
                clienteValido.getIdCliente(),
                clienteValido.getNombre() + " " + clienteValido.getApellido(),
                vehiculoValido.getIdVehiculo(),
                vehiculoValido.getPatente(),
                vehiculoValido.getModelo(),
                fechaInicio,
                fechaFin,
                vehiculoValido.getPrecioDiario(),
                new BigDecimal("300.00"), // 3 días * 100.00
                EstadoReserva.CONFIRMADA
        );

        // STUBBING
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaValida));

        when(reservaMapper.mapToResponseDTO(any(Reserva.class))).thenReturn(responseEsperado);

        // Act
        ReservaResponseDTO resultado = reservaServiceImpl.obtenerPorId(idReserva);

        //Assert
        assertNotNull(resultado);
        assertEquals(idReserva, resultado.idReserva());
        assertEquals(EstadoReserva.CONFIRMADA, resultado.estadoReserva());
        assertEquals(new BigDecimal("300.00"), resultado.importeTotal());

        // Verificamos llamadas al repositorio y al mapper
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(reservaMapper, times(1)).mapToResponseDTO(any(Reserva.class));
    }

    private Cliente crearClienteValido() {

        return Cliente.builder()
                .idCliente(1L)
                .nombre("Diego")
                .apellido("Fernandez")
                .activo(true)
                .build();
    }

    private Vehiculo crearVehiculoValido() {
        return Vehiculo.builder()
                .idVehiculo(2L)
                .patente("AA123CD")
                .precioDiario(new java.math.BigDecimal("100.00"))
                .activo(true)
                .build();
    }

}