package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.ContadorComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContadorComprobanteRepository extends JpaRepository<ContadorComprobante, String> {

    /**
     * Inicializa la serie una sola vez tomando el último correlativo existente.
     * La inicialización se separa del incremento para no mezclar el escaneo de
     * ventas con la actualización concurrente de la fila del contador.
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO contador_comprobante (serie, ultimo_numero) "
            + "SELECT :serie, COALESCE(MAX(CAST(SUBSTRING(numero_comprobante, 6) "
            + "AS UNSIGNED)), 0) FROM ventas "
            + "WHERE numero_comprobante LIKE CONCAT(:serie, '-%')",
            nativeQuery = true)
    void inicializarSiNoExiste(@Param("serie") String serie);

    /**
     * Reserva el siguiente número actualizando únicamente la fila de la serie.
     * LAST_INSERT_ID permite recuperar el valor reservado en esta conexión.
     */
    @Modifying
    @Query(value = "UPDATE contador_comprobante "
            + "SET ultimo_numero = LAST_INSERT_ID(ultimo_numero + 1) "
            + "WHERE serie = :serie",
            nativeQuery = true)
    void reservarSiguiente(@Param("serie") String serie);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long obtenerNumeroReservado();
}
