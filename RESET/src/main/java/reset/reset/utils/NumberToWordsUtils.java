package reset.reset.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Slf4j
@Component
public class NumberToWordsUtils {

    // ==================== CONSTANTES ====================

    private static final String[] UNIDADES = {
            "zero", "um", "dois", "três", "quatro", "cinco",
            "seis", "sete", "oito", "nove"
    };

    private static final String[] DEZENAS = {
            "", "dez", "vinte", "trinta", "quarenta", "cinquenta",
            "sessenta", "setenta", "oitenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "cento", "duzentos", "trezentos", "quatrocentos",
            "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"
    };

    private static final String[] ESPECIAIS = {
            "", "onze", "doze", "treze", "catorze", "quinze",
            "dezasseis", "dezassete", "dezoito", "dezanove"
    };

    private static final String[] ORDENS_MAIORES = {
            "", "mil", "milhão", "bilhão", "trilhão"
    };

    private static final String[] ORDENS_MAIORES_PLURAL = {
            "", "mil", "milhões", "bilhões", "trilhões"
    };

    private static final String MOEDA_SINGULAR = "metical";
    private static final String MOEDA_PLURAL = "meticais";
    private static final String CENTAVO_SINGULAR = "centavo";
    private static final String CENTAVO_PLURAL = "centavos";
    private static final String CONECTOR_E = " e ";
    private static final String CONECTOR_DE = " de ";

    // ==================== MÉTODOS PÚBLICOS ====================

    public static String convert(BigDecimal valor) {
        if (valor == null) {
            return "zero " + MOEDA_PLURAL;
        }

        try {
            // Arredonda para 2 casas decimais para evitar problemas com floats
            BigDecimal valorArredondado = valor.setScale(2, RoundingMode.HALF_EVEN);

            // Separa parte inteira e centavos
            long parteInteira = valorArredondado.longValue();
            int centavos = valorArredondado.remainder(BigDecimal.ONE)
                    .multiply(new BigDecimal(100))
                    .intValue();

            String extenso = buildExtenso(parteInteira, centavos);
            return extenso.substring(0, 1).toUpperCase() + extenso.substring(1);

        } catch (Exception e) {
            log.error("Erro ao converter valor para extenso: {}", e.getMessage(), e);
            return "ERRO NA CONVERSÃO";
        }
    }

    public static String convert(double valor) {
        return convert(BigDecimal.valueOf(valor));
    }

    public static String convert(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                return "zero " + MOEDA_PLURAL;
            }
            // Remove caracteres não numéricos exceto ponto e vírgula
            String clean = valor.replaceAll("[^0-9,.]", "")
                    .replace(',', '.');
            return convert(new BigDecimal(clean));
        } catch (NumberFormatException e) {
            log.error("Erro ao converter string para número: {}", e.getMessage());
            return "zero " + MOEDA_PLURAL;
        }
    }

    public static String convertNumber(long numero) {
        if (numero == 0) {
            return "zero";
        }
        return converterNumero(numero);
    }

    public static String convertNumber(int numero) {
        return convertNumber((long) numero);
    }

    public static String convertCapitalized(BigDecimal valor) {
        String extenso = convert(valor);
        if (extenso == null || extenso.isEmpty()) {
            return "Zero " + MOEDA_PLURAL;
        }
        return extenso.substring(0, 1).toUpperCase() + extenso.substring(1);
    }

    public static String convertIntegerPart(BigDecimal valor) {
        if (valor == null) {
            return "zero";
        }
        long parteInteira = valor.setScale(2, RoundingMode.HALF_EVEN).longValue();
        return converterNumero(parteInteira);
    }

    public static String convertCentsPart(BigDecimal valor) {
        if (valor == null) {
            return "zero centavos";
        }

        BigDecimal arredondado = valor.setScale(2, RoundingMode.HALF_EVEN);
        int centavos = arredondado.remainder(BigDecimal.ONE)
                .multiply(new BigDecimal(100))
                .intValue();

        if (centavos == 0) {
            return "zero centavos";
        }

        return converterNumero(centavos) + " " + (centavos == 1 ? CENTAVO_SINGULAR : CENTAVO_PLURAL);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private static String buildExtenso(long parteInteira, int centavos) {
        StringBuilder resultado = new StringBuilder();

        // Caso especial: zero
        if (parteInteira == 0 && centavos == 0) {
            return "zero " + MOEDA_PLURAL;
        }

        // Parte inteira
        if (parteInteira > 0) {
            resultado.append(converterNumero(parteInteira));
            resultado.append(" ");
            resultado.append(parteInteira == 1 ? MOEDA_SINGULAR : MOEDA_PLURAL);
        }

        // Centavos
        if (centavos > 0) {
            if (parteInteira > 0) {
                resultado.append(CONECTOR_E);
            }
            resultado.append(converterNumero(centavos));
            resultado.append(" ");
            resultado.append(centavos == 1 ? CENTAVO_SINGULAR : CENTAVO_PLURAL);
        }

        return resultado.toString();
    }

    private static String converterNumero(long numero) {
        if (numero < 0) {
            return "menos " + converterNumero(Math.abs(numero));
        }

        if (numero < 10) {
            return UNIDADES[(int) numero];
        }

        if (numero < 20) {
            return ESPECIAIS[(int) (numero - 10)];
        }

        if (numero < 100) {
            int dezena = (int) (numero / 10);
            int unidade = (int) (numero % 10);
            if (unidade == 0) {
                return DEZENAS[dezena];
            }
            return DEZENAS[dezena] + CONECTOR_E + UNIDADES[unidade];
        }

        if (numero < 1000) {
            int centena = (int) (numero / 100);
            int resto = (int) (numero % 100);

            // Caso especial: 100
            if (centena == 1 && resto == 0) {
                return "cem";
            }

            // Caso especial: centena + resto
            if (resto == 0) {
                return CENTENAS[centena];
            }

            // Se o centena for 1 e tiver resto, usa "cento"
            if (centena == 1) {
                return "cento" + CONECTOR_E + converterNumero(resto);
            }

            return CENTENAS[centena] + CONECTOR_E + converterNumero(resto);
        }

        // Milhares
        if (numero < 1_000_000) {
            return converterMilhares(numero);
        }

        // Milhões
        if (numero < 1_000_000_000) {
            return converterMilhoes(numero);
        }

        // Bilhões
        if (numero < 1_000_000_000_000L) {
            return converterBilhoes(numero);
        }

        // Trilhões
        return converterTrilhoes(numero);
    }

    private static String converterMilhares(long numero) {
        int milhares = (int) (numero / 1000);
        int resto = (int) (numero % 1000);

        StringBuilder resultado = new StringBuilder();

        if (milhares == 1) {
            resultado.append("mil");
        } else {
            resultado.append(converterNumero(milhares));
            resultado.append(" mil");
        }

        if (resto > 0) {
            resultado.append(CONECTOR_E);
            resultado.append(converterNumero(resto));
        }

        return resultado.toString();
    }

    private static String converterMilhoes(long numero) {
        int milhoes = (int) (numero / 1_000_000);
        int resto = (int) (numero % 1_000_000);

        StringBuilder resultado = new StringBuilder();

        if (milhoes == 1) {
            resultado.append("um milhão");
        } else {
            resultado.append(converterNumero(milhoes));
            resultado.append(" milhões");
        }

        if (resto > 0) {
            resultado.append(CONECTOR_E);
            resultado.append(converterNumero(resto));
        }

        return resultado.toString();
    }

    private static String converterBilhoes(long numero) {
        long bilhoes = numero / 1_000_000_000L;
        long resto = numero % 1_000_000_000L;

        StringBuilder resultado = new StringBuilder();

        if (bilhoes == 1) {
            resultado.append("um bilhão");
        } else {
            resultado.append(converterNumero(bilhoes));
            resultado.append(" bilhões");
        }

        if (resto > 0) {
            resultado.append(CONECTOR_E);
            resultado.append(converterNumero(resto));
        }

        return resultado.toString();
    }

    private static String converterTrilhoes(long numero) {
        long trilhoes = numero / 1_000_000_000_000L;
        long resto = numero % 1_000_000_000_000L;

        StringBuilder resultado = new StringBuilder();

        if (trilhoes == 1) {
            resultado.append("um trilhão");
        } else {
            resultado.append(converterNumero(trilhoes));
            resultado.append(" trilhões");
        }

        if (resto > 0) {
            resultado.append(CONECTOR_E);
            resultado.append(converterNumero(resto));
        }

        return resultado.toString();
    }

    // ==================== MÉTODOS DE FORMATAÇÃO ====================

    public static String formatCurrency(BigDecimal valor) {
        if (valor == null) {
            return "0,00 MZN";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "MZ"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,##0.00 ¤", symbols);
        format.setCurrency(java.util.Currency.getInstance("MZN"));

        return format.format(valor);
    }

    public static String formatNumber(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "MZ"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(valor);
    }

    // ==================== MÉTODOS DE VALIDAÇÃO ====================

    public static boolean isValidForConversion(BigDecimal valor) {
        if (valor == null) {
            return false;
        }

        try {
            // Verifica se o valor é finito e dentro dos limites
            return valor.compareTo(new BigDecimal("999999999999.99")) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static BigDecimal normalize(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }
}
