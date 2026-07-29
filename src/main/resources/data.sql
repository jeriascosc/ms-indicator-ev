-- Datos de ejemplo que cubren los cuatro cuadrantes del analisis CPI vs SPI,
-- el caso de igualdad a 1 y el caso de indicadores no calculables (divisor cero).
-- BAC = 10000 en todos los registros para que las cifras sean faciles de contrastar.

-- CPI = 6000/5000 = 1.20 (>1) | SPI = 6000/5000 = 1.20 (>1) -> Proyecto ideal
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Cimentacion', 10000.0000, 0.5000, 0.6000, 5000.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);

-- CPI = 4000/5000 = 0.80 (<1) | SPI = 4000/5000 = 0.80 (<1) -> Proyecto critico
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Estructura metalica', 10000.0000, 0.5000, 0.4000, 5000.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);

-- CPI = 6000/8000 = 0.75 (<1) | SPI = 6000/5000 = 1.20 (>1) -> Proyecto con mayor gasto y retrasado
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Instalaciones electricas', 10000.0000, 0.5000, 0.6000, 8000.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);

-- CPI = 4000/3200 = 1.25 (>1) | SPI = 4000/5000 = 0.80 (<1) -> Rapido avance a mayor costo
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Acabados interiores', 10000.0000, 0.5000, 0.4000, 3200.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);

-- CPI = 5000/5000 = 1.00 | SPI = 5000/5000 = 1.00 -> Proyecto conforme a lo planeado
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Urbanismo', 10000.0000, 0.5000, 0.5000, 5000.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);

-- AC = 0 y PV = 0 -> CPI, SPI, EAC y VAC no calculables (null)
INSERT INTO activity (name, total_planned_budget, porcent_planned, porcent_complete, actual_cost,
                      created, create_date, create_update, create_date_update)
VALUES ('Actividad no iniciada', 10000.0000, 0.0000, 0.0000, 0.0000,
        'system', CURRENT_TIMESTAMP, NULL, NULL);
