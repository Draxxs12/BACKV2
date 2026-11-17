package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.ContadorComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContadorComprobanteRepository extends JpaRepository<ContadorComprobante, String> {

    /**
     * Sincroniza una serie con su histórico. Se ejecuta únicamente durante
     * el arranque, antes de aceptar solicitudes de venta.
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO contador_comprobante (serie, ultimo_numero) "
            + "SELECT :serie, COALESCE(MAX(CAST(SUBSTRING(numero_comprobante, 6) "
            + "AS UNSIGNED)), 0) FROM ventas "
            + "WHERE numero_comprobante LIKE CONCAT(:serie, '-%') "
            + "ON DUPLICATE KEY UPDATE ultimo_numero = "
            + "GREATEST(ultimo_numero, VALUES(ultimo_numero))",
            nativeQuery = true)
    void sincronizarSerieDesdeVentas(@Param("serie") String serie);

    /**
     * Reserva atómicamente el siguiente correlativo de la serie.
     * La fila ya debe existir por la sincronización de inicio.
     */
    @Modifying
    @Query(value = "UPDATE contador_comprobante "
            + "SET ultimo_numero = LAST_INSERT_ID(ultimo_numero + 1) "
            + "WHERE serie = :serie",
            nativeQuery = true)
    int reservarSiguiente(@Param("serie") String serie);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long obtenerNumeroReservado();
}
