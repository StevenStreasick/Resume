package com.Resume;

import java.io.ByteArrayInputStream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("Resume")
public class Endpoint {
	@GET
	@Path("/getResume")
	@Produces({"application/pdf"})
	public Response getResume() {
		Resume resumeClass = new Resume();
		ByteArrayInputStream resume = resumeClass.createResume();
		
		if(resume != null) {
			ResponseBuilder rb = Response.ok(resume);
			rb.header("Content-Type", "application/pdf");
			rb.header("Content-Disposition", "inline;filename=Steven Streasick Resume.pdf");
			
			return rb.build();
		}
		
		return Response.serverError().build();
	}
}