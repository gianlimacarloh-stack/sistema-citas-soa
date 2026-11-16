package pe.edu.idat.citas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

/** Un cliente SOAP (WebServiceTemplate) por cada servicio que orquesta Citas. */
@Configuration
public class SoapClientConfig {

    @Bean
    public WebServiceTemplate clientesTemplate(@Value("${servicio.clientes.url}") String url) {
        return template("pe.edu.idat.citas.cliente.clientes", url);
    }

    @Bean
    public WebServiceTemplate tecnicosTemplate(@Value("${servicio.tecnicos.url}") String url) {
        return template("pe.edu.idat.citas.cliente.tecnicos", url);
    }

    private WebServiceTemplate template(String paquete, String url) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(paquete);
        WebServiceTemplate template = new WebServiceTemplate(marshaller);
        template.setDefaultUri(url);
        return template;
    }
}
