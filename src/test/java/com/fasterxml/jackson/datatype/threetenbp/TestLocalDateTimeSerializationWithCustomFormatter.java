package com.fasterxml.jackson.datatype.threetenbp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateTimeSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestLocalDateTimeSerializationWithCustomFormatter {
    private final DateTimeFormatter formatter;

    public TestLocalDateTimeSerializationWithCustomFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Test
    public void testSerialization() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now();
        assertThat(serializeWith(dateTime, formatter), containsString(dateTime.format(formatter)));
    }

    private String serializeWith(LocalDateTime dateTime, DateTimeFormatter f) throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new SimpleModule().addSerializer(new LocalDateTimeSerializer(f)));
        return mapper.writeValueAsString(dateTime);
    }

    @Test
    public void testDeserialization() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now();
        assertThat(deserializeWith(dateTime.format(formatter), formatter), equalTo(dateTime));
    }

    private LocalDateTime deserializeWith(String json, DateTimeFormatter f) throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new SimpleModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(f)));
        return mapper.readValue("\"" + json + "\"", LocalDateTime.class);
    }

    @Parameters
    public static Collection<Object[]> customFormatters() {
        Collection<Object[]> formatters = new ArrayList<Object[]>();
        formatters.add(new Object[]{DateTimeFormatter.ISO_DATE_TIME});
        formatters.add(new Object[]{DateTimeFormatter.ISO_LOCAL_DATE_TIME});
        return formatters;
    }
}