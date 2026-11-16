package pe.edu.idat.tecnicos.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Publica el servicio SOAP en /ws y genera el WSDL a partir del contrato tecnicos.xsd
 * (enfoque contract-first). WSDL: http://localhost:8082/ws/tecnicos.wsdl
 */
@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext ctx) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(ctx);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "tecnicos")
    public DefaultWsdl11Definition tecnicosWsdl(XsdSchema tecnicosSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("TecnicosPort");
        wsdl.setServiceName("ServicioTecnicos");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace("http://idat.edu.pe/tecnicos");
        wsdl.setSchema(tecnicosSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema tecnicosSchema() {
        return new SimpleXsdSchema(new ClassPathResource("tecnicos.xsd"));
    }
}
