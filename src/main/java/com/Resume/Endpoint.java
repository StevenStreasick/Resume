package com.Resume;

import java.io.ByteArrayInputStream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * Stands up an endpoint which can be called which gets and returns my resume.
 *  
 * This class could easily be swapped out for a simple program that runs and saves the
 * resume to a file. I chose this method for ease of testing on my end.
 */

@Path("Resume")
public class Endpoint {
	@GET
	@Path("/getResume")
	@Produces({"application/pdf"})
	public Response getResume() {
		//Create a resume class
		Resume resumeClass = new Resume();
		//Get my resume
		ByteArrayInputStream resume = resumeClass.createResume();
		
		if(resume == null) {
			//Resume was null
			return Response.serverError().build();
			
		}
		
		return Response.ok(resume)
				.header("Content-Type", "application/pdf")
				.header("Content-Disposition", "Attachment;filename=Steven Streasick Resume.pdf")
				.build();
	
	}
}