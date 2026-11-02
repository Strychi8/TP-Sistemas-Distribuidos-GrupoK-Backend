package com.rentar.verificacion_service.config;

import com.rentar.verificacion_service.model.PatenteRobada;
import com.rentar.verificacion_service.model.TitularidadVehiculo;
import com.rentar.verificacion_service.repository.IPatenteRobadaRepository;
import com.rentar.verificacion_service.repository.ITitularidadVehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final IPatenteRobadaRepository patenteRobadaRepository;
    private final ITitularidadVehiculoRepository titularidadRepository;

    @Override
    public void run(String... args) {
        inicializarPatentesRobadas();
        inicializarTitularidades();
    }

    private void inicializarPatentesRobadas() {
        try {
            if (patenteRobadaRepository.count() == 0) {
                log.info("[DATA INIT] Sembrando patentes robadas simuladas en MySQL...");
                patenteRobadaRepository.saveAll(List.of(
                        PatenteRobada.builder()
                                .patente("AA111BB")
                                .motivo("Robo calificado con armas en CABA")
                                .fechaDenuncia(LocalDateTime.now().minusDays(10))
                                .build(),
                        PatenteRobada.builder()
                                .patente("ZZ999ZZ")
                                .motivo("Hurto de vehiculo en via publica")
                                .fechaDenuncia(LocalDateTime.now().minusDays(5))
                                .build(),
                        PatenteRobada.builder()
                                .patente("ROB123")
                                .motivo("Denuncia policial activa por sustraccion")
                                .fechaDenuncia(LocalDateTime.now().minusDays(2))
                                .build()
                ));
                log.info("[DATA INIT] Patentes robadas sembradas exitosamente.");
            }
        } catch (Exception e) {
            log.warn("[DATA INIT] No se pudo persistir semillas de patentes robadas en BD (se utilizara memoria): {}", e.getMessage());
        }
    }

    private void inicializarTitularidades() {
        try {
            if (titularidadRepository.count() == 0) {
                log.info("[DATA INIT] Sembrando titularidades vehiculares simuladas en MySQL...");
                titularidadRepository.saveAll(List.of(
                        TitularidadVehiculo.builder()
                                .clienteId(89L)
                                .documento("30123456")
                                .patente("DEF456")
                                .nombreTitular("Ana Martinez")
                                .build(),
                        TitularidadVehiculo.builder()
                                .clienteId(1L)
                                .documento("11111111")
                                .patente("EXT100")
                                .nombreTitular("Juan Perez")
                                .build(),
                        TitularidadVehiculo.builder()
                                .clienteId(2L)
                                .documento("22222222")
                                .patente("EXT200")
                                .nombreTitular("Maria Gomez")
                                .build()
                ));
                log.info("[DATA INIT] Titularidades sembradas exitosamente.");
            }
        } catch (Exception e) {
            log.warn("[DATA INIT] No se pudo persistir semillas de titularidades en BD (se utilizara memoria): {}", e.getMessage());
        }
    }
}
