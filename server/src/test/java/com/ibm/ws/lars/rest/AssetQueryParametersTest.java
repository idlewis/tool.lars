/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.ws.lars.rest;

import static com.ibm.ws.lars.rest.Condition.Operation.EQUALS;
import static com.ibm.ws.lars.rest.Condition.Operation.NOT_EQUALS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import com.ibm.ws.lars.rest.SortOptions.SortOrder;
import com.ibm.ws.lars.rest.exceptions.InvalidParameterException;

/**
 * Unit tests for the {@link AssetQueryParameters} class
 */
public class AssetQueryParametersTest {

    private static final DummyUriInfo ALL_PARAMS_URI;

    static {
        try {
            ALL_PARAMS_URI = new DummyUriInfo("http://example.org/test", "/foobar?q=wibble&offset=100&name=foo&key=!value&limit=12&fields=a,b,c&sortBy=x&sortOrder=ASC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetFilterMap() throws Exception {
        // Note %7C == '|'
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?a=1&b=!1&c=testX%7CtestY&d=!testA%7CtestB&e=testA%7C!testB");
        AssetQueryParameters params = AssetQueryParameters.create(uriInfo);
        Map<String, List<Condition>> expected = new HashMap<>();
        expected.put("a", asList(new Condition(EQUALS, "1")));
        expected.put("b", asList(new Condition(NOT_EQUALS, "1")));
        expected.put("c", asList(new Condition(EQUALS, "testX"), new Condition(EQUALS, "testY")));
        expected.put("d", asList(new Condition(NOT_EQUALS, "testA"), new Condition(EQUALS, "testB")));
        expected.put("e", asList(new Condition(EQUALS, "testA"))); // NOT_EQUALS conditions are ignored if they are not first in the list
        assertEquals(expected, params.getFilterMap());

        params = AssetQueryParameters.create(ALL_PARAMS_URI);
        expected.clear();
        expected.put("name", asList(new Condition(EQUALS, "foo")));
        expected.put("key", asList(new Condition(NOT_EQUALS, "value")));
        assertEquals(expected, params.getFilterMap());
    }

    @Test
    public void testGetPagination() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?limit=2&offset=5");
        AssetQueryParameters params = AssetQueryParameters.create(uriInfo);
        assertEquals(new PaginationOptions(5, 2), params.getPagination());

        params = AssetQueryParameters.create(ALL_PARAMS_URI);
        assertEquals(new PaginationOptions(100, 12), params.getPagination());
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetPaginationBadLimit() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?limit=foo&offset=5");
        AssetQueryParameters.create(uriInfo).getPagination();
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetPaginationBadOffset() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?limit=3&offset=bar");
        AssetQueryParameters.create(uriInfo).getPagination();
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetPaginationNoOffset() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?limit=3");
        AssetQueryParameters.create(uriInfo).getPagination();
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetPaginationNoLimit() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?offset=5");
        AssetQueryParameters.create(uriInfo).getPagination();
    }

    @Test
    public void testGetSortOptions() throws Exception {
        // Valid options for sortOrder are "ASC" and "DESC" case insensitive
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortBy=foo&sortOrder=aSc");
        AssetQueryParameters params = AssetQueryParameters.create(uriInfo);
        assertEquals(new SortOptions("foo", SortOrder.ASCENDING), params.getSortOptions());

        uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortBy=foo&sortOrder=DeSc");
        params = AssetQueryParameters.create(uriInfo);
        assertEquals(new SortOptions("foo", SortOrder.DESCENDING), params.getSortOptions());

        // Default sortOrder is ascending
        uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortBy=foo");
        params = AssetQueryParameters.create(uriInfo);
        assertEquals(new SortOptions("foo", SortOrder.ASCENDING), params.getSortOptions());

        params = AssetQueryParameters.create(ALL_PARAMS_URI);
        assertEquals(new SortOptions("x", SortOrder.ASCENDING), params.getSortOptions());
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetSortOptionsBadSortBy() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortBy=&sortOrder=DESC");
        AssetQueryParameters.create(uriInfo).getSortOptions();
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetSortOptionsBadSortOrder() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortBy=foo&sortOrder=wibble");
        AssetQueryParameters.create(uriInfo).getSortOptions();
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetSortOptionsNoSortBy() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?sortOrder=DESC");
        AssetQueryParameters.create(uriInfo).getSortOptions();
    }

    @Test
    public void testGetSearchTerm() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?q=TEST");
        AssetQueryParameters params = AssetQueryParameters.create(uriInfo);
        assertEquals("TEST", params.getSearchTerm());

        params = AssetQueryParameters.create(ALL_PARAMS_URI);
        assertEquals("wibble", params.getSearchTerm());
    }

    @Test
    public void testGetFields() throws Exception {
        UriInfo uriInfo = new DummyUriInfo("http://example.org/test", "/foobar?fields=field1,field2,field3");
        AssetQueryParameters params = AssetQueryParameters.create(uriInfo);
        assertEquals("field1,field2,field3", params.getFields());

        params = AssetQueryParameters.create(ALL_PARAMS_URI);
        assertEquals("a,b,c", params.getFields());
    }

}
