package com.rentar.vehicle_service.repository;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.model.Vehiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VehiculoRepositoryTest {

    @Autowired
    private IVehiculoRepository vehiculoRepository;

    @Test
    @DisplayName("findByActivoTrueAndInhabilitadoFalse excluye vehículos inhabilitados")
    void testFindByActivoTrueAndInhabilitadoFalseExcluyeInhabilitados() {
        Vehiculo v1 = Vehiculo.builder()
                .patente("AAA111")
                .marca("Ford")
                .modelo("Fiesta")
                .anio(2020)
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.valueOf(40))
                .activo(true)
                .inhabilitado(false)
                .build();

        Vehiculo v2 = Vehiculo.builder()
                .patente("BBB222")
                .marca("Chevrolet")
                .modelo("Corsa")
                .anio(2019)
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.valueOf(35))
                .activo(true)
                .inhabilitado(true) // Inhabilitado por robo
                .build();

        vehiculoRepository.save(v1);
        vehiculoRepository.save(v2);

        List<Vehiculo> activos = vehiculoRepository.findByActivoTrueAndInhabilitadoFalse();

        assertFalse(activos.isEmpty());
        assertTrue(activos.stream().anyMatch(v -> v.getPatente().equals("AAA111")));
        assertFalse(activos.stream().anyMatch(v -> v.getPatente().equals("BBB222")), "Vehículo inhabilitado no debe aparecer");
    }

    @Test
    @DisplayName("buscarConFiltros excluye vehículos inhabilitados")
    void testBuscarConFiltrosExcluyeInhabilitados() {
        Vehiculo v1 = Vehiculo.builder()
                .patente("CCC333")
                .marca("Renault")
                .modelo("Clio")
                .anio(2021)
                .tipoVehiculo(TipoVehiculo.HATCHBACK)
                .precioDiario(BigDecimal.valueOf(45))
                .activo(true)
                .inhabilitado(false)
                .build();

        Vehiculo v2 = Vehiculo.builder()
                .patente("DDD444")
                .marca("Renault")
                .modelo("Megane")
                .anio(2021)
                .tipoVehiculo(TipoVehiculo.HATCHBACK)
                .precioDiario(BigDecimal.valueOf(50))
                .activo(true)
                .inhabilitado(true) // Inhabilitado
                .build();

        vehiculoRepository.save(v1);
        vehiculoRepository.save(v2);

        List<Vehiculo> filtrados = vehiculoRepository.buscarConFiltros(TipoVehiculo.HATCHBACK, "Renault", null, null, null);

        assertTrue(filtrados.stream().anyMatch(v -> v.getPatente().equals("CCC333")));
        assertFalse(filtrados.stream().anyMatch(v -> v.getPatente().equals("DDD444")), "Filtros no deben incluir inhabilitados");
    }
}
