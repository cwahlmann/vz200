package jemu.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
class WebConfig extends WebMvcConfigurationSupport {
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		addDefaultHttpMessageConverters(converters);
        converters.add(new AbstractHttpMessageConverter<InputStream>(MediaType.APPLICATION_OCTET_STREAM) {
            protected boolean supports(Class<?> clazz) {
                return InputStream.class.isAssignableFrom(clazz);
            }

            protected InputStream readInternal(Class<? extends InputStream> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                return inputMessage.getBody();
            }

            protected void writeInternal(InputStream inputStream, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                IOUtils.copy(inputStream, outputMessage.getBody());
            }
        });
        super.configureMessageConverters(converters);
    }
}
