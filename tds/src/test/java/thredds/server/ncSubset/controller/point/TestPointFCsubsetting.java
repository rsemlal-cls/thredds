/*
 *
 *  * Copyright 1998-2013 University Corporation for Atmospheric Research/Unidata
 *  *
 *  *  Portions of this software were developed by the Unidata Program at the
 *  *  University Corporation for Atmospheric Research.
 *  *
 *  *  Access and use of this software shall impose the following obligations
 *  *  and understandings on the user. The user is granted the right, without
 *  *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  *  this software, and any derivative works thereof, and its supporting
 *  *  documentation for any purpose whatsoever, provided that this entire
 *  *  notice appears in all copies of the software, derivative works and
 *  *  supporting documentation.  Further, UCAR requests that the user credit
 *  *  UCAR/Unidata in any publications that result from the use of this
 *  *  software or in any product that includes this software. The names UCAR
 *  *  and/or Unidata, however, may not be used in any advertising or publicity
 *  *  to endorse or promote any products or commercial entity unless specific
 *  *  written permission is obtained from UCAR/Unidata. The user also
 *  *  understands that UCAR/Unidata is not obligated to provide the user with
 *  *  any support, consulting, training or assistance of any kind with regard
 *  *  to the use, operation and performance of this software nor to provide
 *  *  the user with any updates, revisions, new versions or "bug fixes."
 *  *
 *  *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package thredds.server.ncSubset.controller.point;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner;
import thredds.mock.web.MockTdsContextLoader;
import thredds.server.ncSubset.format.SupportedFormat;
import thredds.server.ncSubset.format.SupportedOperation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Test ncss on point feature collections
 *
 * @author caron
 * @since 10/18/13
 */
@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "/WEB-INF/applicationContext-tdsConfig.xml" }, loader = MockTdsContextLoader.class)
public class TestPointFCsubsetting {

	@Autowired
	private WebApplicationContext wac;

	private String dataset =  "/ncss/testBuoyFeatureCollection/Surface_Buoy_Point_Data_fc.cdmr";
	private String req =  "?req=point&var=ICE&var=PRECIP_amt&var=PRECIP_amt24&var=T&north=40&west=-170&east=-100&south=-40&time_start=2013-08-04T00:00:00Z&time_end=2013-08-07T00:00:00Z&accept=";
	private MockMvc mockMvc;

  @SpringJUnit4ParameterizedClassRunner.Parameters
   public static List<Object[]> getTestParameters() {

    List<Object[]>  result = new ArrayList<Object[]>(10);

    for (SupportedFormat f : SupportedOperation.POINT_REQUEST.getSupportedFormats()) {
      result.add( new Object[]{f});
    }

    return result;
 	}


	@Before
	public void setup(){
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

  SupportedFormat format;
  public TestPointFCsubsetting(SupportedFormat format) {
    this.format = format;
  }

	@Test
	public void getSubsettedData() throws Exception{
		RequestBuilder rb = MockMvcRequestBuilders.get(dataset + req + format.getFormatName()).servletPath(dataset);
    MvcResult result = this.mockMvc.perform( rb )
            .andExpect(MockMvcResultMatchers.status().isOk())
    		  	.andExpect(MockMvcResultMatchers.content().contentType(format.getResponseContentType()))
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    System.out.printf("format=%s status = %d type=%s%n", format, response.getStatus(), response.getContentType());
    if (response.getStatus() == 200) {
      assertTrue(format.isAlias(response.getContentType()));
    } else {
      System.out.printf(" return = %s%n", response.getContentAsString());
    }
  }

  @Test
  public void testInvalidDateRangeOnStationDataset() throws Exception{
 		RequestBuilder rb = MockMvcRequestBuilders.get(dataset).servletPath(dataset)
 				.param("accept", "netcdf")
 				.param("var", "air_temperature", "dew_point_temperature")
 				.param("subset", "bb")
 				.param("north", "43.0")
 				.param("south", "38.0")
 				.param("west", "-107.0")
 				.param("east", "-103.0")
 				.param("time_start","2013-08-25T06:00:00Z")
 				.param("time_end","2013-08-26T06:00:00Z");

    org.springframework.test.web.servlet.MvcResult result = this.mockMvc.perform( rb ).andExpect(MockMvcResultMatchers.status().is(400))
            .andReturn();
		System.out.printf("%s%n", result.getResponse().getContentAsString());
 	}

  public void getSubsetOnStationDataset() throws Exception{
 		RequestBuilder rb = MockMvcRequestBuilders.get(dataset).servletPath(dataset)
 				.param("accept", "netcdf")
 				.param("var", "air_temperature", "dew_point_temperature")
 				.param("subset", "bb")
 				.param("north", "43.0")
 				.param("south", "38.0")
 				.param("west", "-107.0")
 				.param("east", "-103.0")
 				.param("time_start","2006-03-25T00:00:00Z")
 				.param("time_end","2006-03-28T00:00:00Z");

 		this.mockMvc.perform( rb ).andExpect(MockMvcResultMatchers.status().isOk())
 			.andExpect(MockMvcResultMatchers.content().contentType( SupportedFormat.NETCDF3.getResponseContentType() ));
 	}

	public void getAllStnsOnStationDataset() throws Exception{
		RequestBuilder rb = MockMvcRequestBuilders.get(dataset).servletPath(dataset)
				.param("accept", "netcdf")
				.param("subset", "stns")
				.param("stns", "all")
				.param("var", "air_temperature", "dew_point_temperature")
        .param("time_start","2006-03-25T00:00:00Z")
        .param("time_end","2006-03-26T00:00:00Z");


		this.mockMvc.perform( rb ).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType( SupportedFormat.NETCDF3.getResponseContentType() ));

	}


}

