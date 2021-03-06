package com.fasterxml.jackson.datatype.threetenbp.deser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.threetenbp.MockObjectConfiguration;
import com.fasterxml.jackson.datatype.threetenbp.ModuleTestBase;

import org.junit.Test;

import java.io.IOException;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.Temporal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OffsetTimeDeserTest extends ModuleTestBase
{
    // for [datatype-jsr310#45]
    static class  Pojo45s {
        public String name;
        public List<Pojo45> objects;
    }

    static class Pojo45 { 
        public org.threeten.bp.LocalDate partDate;
        public org.threeten.bp.OffsetTime starttime;
        public org.threeten.bp.OffsetTime endtime;
        public String comments;
    }

    private final ObjectReader READER = newMapper().readerFor(OffsetTime.class);

    @Test
    public void testDeserializationAsTimestamp01() throws Exception
    {
        OffsetTime time = OffsetTime.of(15, 43, 0, 0, ZoneOffset.of("+0300"));
        OffsetTime value = READER
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[15,43,\"+0300\"]");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp02() throws Exception
    {
        OffsetTime time = OffsetTime.of(9, 22, 57, 0, ZoneOffset.of("-0630"));
        OffsetTime value = READER.readValue("[9,22,57,\"-06:30\"]");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp03Nanoseconds() throws Exception
    {
        OffsetTime time = OffsetTime.of(9, 22, 0, 57, ZoneOffset.of("-0630"));
        OffsetTime value = READER
                .with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[9,22,0,57,\"-06:30\"]");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp03Milliseconds() throws Exception
    {
        OffsetTime time = OffsetTime.of(9, 22, 0, 57000000, ZoneOffset.of("-0630"));
        OffsetTime value = READER
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[9,22,0,57,\"-06:30\"]");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp04Nanoseconds() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 829837, ZoneOffset.of("+1100"));
        OffsetTime value = READER
                .with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[22,31,5,829837,\"+11:00\"]");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp04Milliseconds01() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 829837, ZoneOffset.of("+1100"));
        OffsetTime value = READER
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[22,31,5,829837,\"+11:00\"]");
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationAsTimestamp04Milliseconds02() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 829000000, ZoneOffset.of("+1100"));
        OffsetTime value = READER
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
               .readValue("[22,31,5,829,\"+11:00\"]");
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationFromString01() throws Exception
    {
        OffsetTime time = OffsetTime.of(15, 43, 0, 0, ZoneOffset.of("+0300"));
        OffsetTime value = READER.readValue('"' + time.toString() + '"');
        assertEquals("The value is not correct.", time, value);

        time = OffsetTime.of(9, 22, 57, 0, ZoneOffset.of("-0630"));
        value = READER.readValue('"' + time.toString() + '"');
        assertEquals("The value is not correct.", time, value);

        time = OffsetTime.of(22, 31, 5, 829837, ZoneOffset.of("+1100"));
        value = READER.readValue('"' + time.toString() + '"');
        assertEquals("The value is not correct.", time, value);

        expectSuccess(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), "'12:00Z'");
    }

    @Test
    public void testBadDeserializationFromString01() throws Throwable
    {
        expectFailure(quote("notanoffsettime"));
    }
    
    @Test
    public void testDeserializationWithTypeInfo01() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 829837, ZoneOffset.of("+1100"));

        final ObjectMapper mapper = newMapper();
        mapper.addMixIn(Temporal.class, MockObjectConfiguration.class);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
        Temporal value = mapper.readValue(
                "[\"" + OffsetTime.class.getName() + "\",[22,31,5,829837,\"+11:00\"]]", Temporal.class
                );

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a OffsetTime.", value instanceof OffsetTime);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationWithTypeInfo02() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 422000000, ZoneOffset.of("+1100"));

        final ObjectMapper mapper = newMapper();
        mapper.addMixIn(Temporal.class, MockObjectConfiguration.class);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        Temporal value = mapper.readValue(
                "[\"" + OffsetTime.class.getName() + "\",[22,31,5,422,\"+11:00\"]]", Temporal.class
                );

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a OffsetTime.", value instanceof OffsetTime);
        assertEquals("The value is not correct.", time, value);
    }

    @Test
    public void testDeserializationWithTypeInfo03() throws Exception
    {
        OffsetTime time = OffsetTime.of(22, 31, 5, 829837, ZoneOffset.of("+1100"));

        final ObjectMapper mapper = newMapper();
        mapper.addMixIn(Temporal.class, MockObjectConfiguration.class);
        Temporal value = mapper.readValue(
                "[\"" + OffsetTime.class.getName() + "\",\"" + time.toString() + "\"]", Temporal.class
                );
        assertTrue("The value should be a OffsetTime.", value instanceof OffsetTime);
        assertEquals("The value is not correct.", time, value);
    }

    // for [datatype-jsr310#45]
    @Test
    public void testDeserOfArrayOf() throws Exception
    {
        final String JSON = aposToQuotes
                ("{'name':'test','objects':[{'partDate':[2015,10,13],'starttime':[15,7,'+0'],'endtime':[2,14,'+0'],'comments':'in the comments'}]}");
        Pojo45s result = READER.forType(Pojo45s.class).readValue(JSON);
        assertNotNull(result);
        assertNotNull(result.objects);
        assertEquals(1, result.objects.size());
    }

    @Test
    public void testDeserializationAsArrayDisabled() throws Throwable
    {
        try {
            read("['12:00Z']");
    	        fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
           // OK
        }
    }
    
    @Test
    public void testDeserializationAsEmptyArrayDisabled() throws Throwable
    {
        // works even without the feature enabled
        assertNull(read("[]"));
    }
    
    @Test
    public void testDeserializationAsArrayEnabled() throws Throwable
    {
        OffsetTime value = READER.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
    			.readValue(aposToQuotes("['12:00Z']"));
        expect(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), value);
    }

    @Test
    public void testDeserializationAsEmptyArrayEnabled() throws Throwable
    {
        OffsetTime value = READER.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS,
                    DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .readValue("[]");
        assertNull(value);
    }

    private void expectFailure(String json) throws Throwable {
        try {
            read(json);
            fail("expected DateTimeParseException");
        } catch (JsonProcessingException e) {
            if (e.getCause() == null) {
                throw e;
            }
            if (!(e.getCause() instanceof DateTimeParseException)) {
                throw e.getCause();
            }
        }
    }

    private void expectSuccess(Object exp, String json) throws IOException {
        final OffsetTime value = read(json);
        notNull(value);
        expect(exp, value);
    }

    private OffsetTime read(final String json) throws IOException {
        return READER.readValue(aposToQuotes(json));
    }

    private static void notNull(Object value) {
        assertNotNull("The value should not be null.", value);
    }

    private static void expect(Object exp, Object value) {
        assertEquals("The value is not correct.", exp,  value);
    }
}
