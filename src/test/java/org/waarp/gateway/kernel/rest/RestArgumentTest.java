package org.waarp.gateway.kernel.rest;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class RestArgumentTest {

    @Test
    public void testAddFilterNPE() {
        RestArgument ra = new RestArgument(null);
        ra.addFilter(null);

        assertEquals("filters should be an empty ObjectNode",
                     new ObjectNode(JsonNodeFactory.instance), ra.getFilter());
    }
}
