package com.trycore.msindicatorev.service;

import com.trycore.msindicatorev.domain.Activity;
import com.trycore.msindicatorev.web.dto.IndicatorResponse;
import com.trycore.msindicatorev.web.dto.InterpretationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculo de los indicadores de Earned Value Management y su interpretacion.
 *
 * <p>Formulas aplicadas, con {@code BAC = totalPlannedBudget} y {@code AC = actualCost}:
 * <pre>
 *   PV  = porcentPlanned  * BAC
 *   EV  = porcentComplete * BAC
 *   CV  = EV - AC
 *   SV  = EV - PV
 *   CPI = EV / AC
 *   SPI = EV / PV
 *   EAC = BAC / CPI
 *   VAC = BAC - EAC
 * </pre>
 *
 * <p>Cuando un divisor es cero el indicador correspondiente es indefinido y se devuelve
 * {@code null}, propagandose como "No calculable" en la interpretacion.
 */
@Service
public class IndicatorService {

    /** Escala decimal aplicada a todos los indicadores. */
    public static final int SCALE = 4;

    static final String CPI_ON_PLAN = "Gasto conforme a lo planeado";
    static final String CPI_EFFICIENT = "Eficiencia de Costo";
    static final String CPI_POOR = "Mal desempeño Financiero";

    static final String SPI_ON_PLAN = "Avanza conforme a lo planeado";
    static final String SPI_AHEAD = "Avanza más de lo previsto";
    static final String SPI_BEHIND = "Retrazado";

    static final String ANALYSIS_IDEAL = "Proyecto ideal";
    static final String ANALYSIS_CRITICAL = "Proyecto crítico";
    static final String ANALYSIS_OVERSPENT_BEHIND = "Proyecto con mayor gasto y retrasado";
    static final String ANALYSIS_FAST_COSTLY = "Rápido avance a mayor costo";
    /** Cierre para los casos con CPI o SPI exactamente igual a 1, no cubiertos por los cuatro cuadrantes. */
    static final String ANALYSIS_ON_PLAN = "Proyecto conforme a lo planeado";

    static final String NOT_COMPUTABLE = "No calculable";

    /**
     * Calcula los ocho indicadores EVM de una actividad.
     */
    public IndicatorResponse calculate(Activity activity) {
        BigDecimal bac = activity.getTotalPlannedBudget();
        BigDecimal actualCost = activity.getActualCost();

        BigDecimal pv = scale(activity.getPorcentPlanned().multiply(bac));
        BigDecimal ev = scale(activity.getPorcentComplete().multiply(bac));
        BigDecimal cv = scale(ev.subtract(actualCost));
        BigDecimal sv = scale(ev.subtract(pv));
        BigDecimal cpi = divide(ev, actualCost);
        BigDecimal spi = divide(ev, pv);
        BigDecimal eac = divide(bac, cpi);
        BigDecimal vac = (eac == null) ? null : scale(bac.subtract(eac));

        return new IndicatorResponse(
                activity.getId(), activity.getName(), scale(bac), pv, ev, cv, sv, cpi, spi, eac, vac);
    }

    public List<IndicatorResponse> calculateAll(List<Activity> activities) {
        return activities.stream().map(this::calculate).toList();
    }

    /**
     * Traduce los indices CPI y SPI de una actividad a su lectura de negocio.
     */
    public InterpretationResponse interpret(Activity activity) {
        IndicatorResponse indicators = calculate(activity);
        BigDecimal cpi = indicators.cpi();
        BigDecimal spi = indicators.spi();

        return new InterpretationResponse(
                activity.getId(),
                activity.getName(),
                cpi,
                spi,
                cpiStatus(cpi),
                spiStatus(spi),
                analyze(cpi, spi));
    }

    public List<InterpretationResponse> interpretAll(List<Activity> activities) {
        return activities.stream().map(this::interpret).toList();
    }

    String cpiStatus(BigDecimal cpi) {
        if (cpi == null) {
            return NOT_COMPUTABLE;
        }
        int comparison = cpi.compareTo(BigDecimal.ONE);
        if (comparison == 0) {
            return CPI_ON_PLAN;
        }
        return (comparison > 0) ? CPI_EFFICIENT : CPI_POOR;
    }

    String spiStatus(BigDecimal spi) {
        if (spi == null) {
            return NOT_COMPUTABLE;
        }
        int comparison = spi.compareTo(BigDecimal.ONE);
        if (comparison == 0) {
            return SPI_ON_PLAN;
        }
        return (comparison > 0) ? SPI_AHEAD : SPI_BEHIND;
    }

    String analyze(BigDecimal cpi, BigDecimal spi) {
        if (cpi == null || spi == null) {
            return NOT_COMPUTABLE;
        }
        int cpiComparison = cpi.compareTo(BigDecimal.ONE);
        int spiComparison = spi.compareTo(BigDecimal.ONE);

        if (cpiComparison > 0 && spiComparison > 0) {
            return ANALYSIS_IDEAL;
        }
        if (cpiComparison < 0 && spiComparison < 0) {
            return ANALYSIS_CRITICAL;
        }
        if (cpiComparison < 0 && spiComparison > 0) {
            return ANALYSIS_OVERSPENT_BEHIND;
        }
        if (cpiComparison > 0 && spiComparison < 0) {
            return ANALYSIS_FAST_COSTLY;
        }
        return ANALYSIS_ON_PLAN;
    }

    private BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return dividend.divide(divisor, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
