package uy.gub.agesic.pdi.services.router;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        String mensajeA = "Antes >> <<zz:aa del mensaje original<PEPE:Body>Cuerpo del mensaje original</PEPE:Body>Despues del mensaje original";
        String mensajeB = "Antes <<vv:nbdel mensaje procesado<XXXX:Body>Cuerpo del mensaje procesado</XXXX:Body>Despues del mensaje procesado";

        String regexp = "<(.*:)?body>(.*)</\\1?body>";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // Buscamos el body original
        Matcher matcherA = pattern.matcher(mensajeA);
        Matcher matcherB = pattern.matcher(mensajeB);

        if (matcherA.find()) {
            // 1 y 3 son los namespaces de los tags en los bodys del mensaje original
            String body = matcherA.group(2);

            if (matcherB.find()) {
                int start = matcherB.start(2);
                int end = matcherB.end(2);

                StringBuilder sb = new StringBuilder(mensajeB).replace(start, end, body);

                System.out.println(sb.toString());
            }
        }
    }

}
