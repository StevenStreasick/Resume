package com.Resume;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDocument;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import io.nayuki.qrcodegen.QrCode;


public class Resume  {
	Font titleFont; //This is the font that will be used within the title
	Font contactInfoFont; //The personal info such as git/email/etc
	Font sectionFont; //The large 
	Font subsectionFont; //The entries in each section
	Font subsectionDescriptorFont; //The line under the subsection (Mainly for education portion)
	Font bulletFont; //This is the font that will be used for any bulleted information.
	Font zapFont;
	Font dateLocationFont; //The font any date of employment or location of employment will use
	
	float sectionPadding = 10f;
	float subsectionPadding = 5f;
	
	private final static String FONTPATH = "C:\\Users\\Bees\\Downloads";
	private final static String REPOURI = "https://github.com/StevenStreasick/Resume";
	
	//@return success
	private boolean createFonts() {
		//First register font. Then
		try {
			BaseFont fontToUse = BaseFont.createFont(FONTPATH + File.separator + "Roboto-Light.ttf", BaseFont.CP1250, BaseFont.EMBEDDED); 
//			BaseFont boldFont = BaseFont.createFont(FONTPATH + File.separator + "Roboto-Bold.ttf", BaseFont.CP1250, BaseFont.EMBEDDED);
			
			titleFont = new Font(fontToUse, 24, Font.BOLD, Color.black); //Set width of this line to bold levels
			contactInfoFont = new Font(fontToUse, 10, Font.NORMAL, Color.black);
			sectionFont = new Font(fontToUse, 14, Font.BOLD, Color.black); //Set width of this line to small
			subsectionFont = new Font(fontToUse, 10.5f, Font.NORMAL, Color.black);
			subsectionDescriptorFont = new Font(fontToUse, subsectionFont.getSize() - 2, Font.ITALIC, subsectionFont.getColor());
			bulletFont = new Font(fontToUse, 10, Font.NORMAL, Color.black);
			zapFont = new Font(Font.ZAPFDINGBATS, bulletFont.getSize() - 6, bulletFont.getStyle(), bulletFont.getColor());
			dateLocationFont = new Font(fontToUse, 9, Font.NORMAL, Color.gray);
			
			return true;
		} catch (DocumentException de) { 
			//TODO: Figure out what could cause a documentException
			de.printStackTrace();
		} catch (IOException ioe) {
			//Likely just a could not find the file error
			ioe.printStackTrace();
			
		}
		
		return false;
	}
	
	private static BufferedImage createQRCode(String URI) {
		 int scalar = 16;
		 
		 QrCode.Ecc errorCorrectionLevel = QrCode.Ecc.LOW;
	     QrCode qrCode = QrCode.encodeText(REPOURI, errorCorrectionLevel);
         
	     int matrixSize = qrCode.size * scalar;
         
	     BufferedImage image = new BufferedImage(matrixSize, matrixSize, BufferedImage.TYPE_INT_RGB);
         image.createGraphics();
         
         //-----Fill the background with white------
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         graphics.setColor(Color.WHITE);
         graphics.fillRect(0, 0, matrixSize, matrixSize);
         
         
         //------Add the black dots to make up the QR code
         graphics.setColor(Color.BLACK);

         for (int x = 0; x < qrCode.size; x++) {
             for (int y = 0; y < qrCode.size; y++) {
                 if (!qrCode.getModule(x, y)) { continue; }
                 
                 graphics.fillRect(x * scalar, y * scalar, scalar, scalar);
              
             }
         }
         
		 return image;
	}
	
	private PdfPCell createSectionCell(String rawText) {
		String titleText = rawText.toUpperCase();
		
		Chunk titleChunk = new Chunk(titleText, sectionFont);
		titleChunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, .05f, sectionFont.getColor());
		
		PdfPCell title = new PdfPCell(new Phrase(titleChunk));
		title.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		title.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		title.setBorder(PdfPCell.NO_BORDER);
		title.setPaddingBottom(sectionPadding / 2f);
		
		return title;
	}
	
	private PdfPCell createBulletCell(String text) {
		//NOTE: I chose to use this chunk method over fontSelector so I can utilize multiple font sizes.
		Chunk bullet = new Chunk(String.valueOf((char) 108) + "  ", zapFont);
		Chunk textChunk = new Chunk(text, bulletFont);
		Paragraph paragraph = new Paragraph();
		
		bullet.setTextRise(2f);
		
		paragraph.add(bullet);
		paragraph.add(textChunk);
		
		PdfPCell bulletCell = new PdfPCell(paragraph);
		bulletCell.setBorder(PdfPCell.NO_BORDER);
		bulletCell.setUseAscender(true);
		bulletCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		bulletCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		
		bulletCell.setPaddingBottom(4f);
		return bulletCell;
	}
	
	private PdfPTable createSubsectionTable(String subsectionTitle, String subsectionDate, String subsectionDesc) {
		PdfPTable experienceHeader = new PdfPTable(2);
		
//		experienceHeader.setSpacingAfter(subsectionPadding);
		
		PdfPCell subsectionTitleCell = new PdfPCell(new Phrase(subsectionTitle, subsectionFont));
		subsectionTitleCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		subsectionTitleCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		subsectionTitleCell.setBorder(PdfPCell.NO_BORDER);
		subsectionTitleCell.setPaddingLeft(0);
		subsectionTitleCell.setUseAscender(true);
		
		PdfPCell subsectionDateCell = new PdfPCell(new Phrase(subsectionDate, dateLocationFont));
		subsectionDateCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		subsectionDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		subsectionDateCell.setBorder(PdfPCell.NO_BORDER);
		subsectionDateCell.setPaddingRight(0);
		subsectionDateCell.setUseAscender(true);
		
		experienceHeader.addCell(subsectionTitleCell);
		experienceHeader.addCell(subsectionDateCell);
		
		if(subsectionDesc != null && !subsectionDesc.isBlank()) {
			PdfPCell subsectionDescCell = new PdfPCell(new Phrase(subsectionDesc, subsectionDescriptorFont));
			subsectionDescCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			subsectionDescCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			subsectionDescCell.setBorder(PdfPCell.NO_BORDER);
			subsectionDescCell.setPaddingLeft(0);
			subsectionDescCell.setUseAscender(true);
			subsectionDescCell.setColspan(2);
			
			experienceHeader.addCell(subsectionDescCell);
		}
		experienceHeader.setSpacingBefore(3);
		experienceHeader.setSpacingAfter(3);
		return experienceHeader;
	}
	
	private PdfPTable createHeader() {
		//TODO: Figure out how I want to implement bold fonts/stroke width.
		//I used setTextRenderMode(int mode, float strokeWidth, Color strokeColor)
		
		PdfPTable header = new PdfPTable(3);
		header.setWidthPercentage(100f);
		header.setWidths(new float[] {1f, .5f, 1f});		
		header.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

		
		//This is the title column
		PdfPTable leftColumn = new PdfPTable(1);
		leftColumn.setWidthPercentage(100f);
		
		PdfPTable qrcodeColumn = new PdfPTable(1);
		qrcodeColumn.setWidthPercentage(100f);
		PdfPTable rightColumn = new PdfPTable(1);
		rightColumn.setWidthPercentage(100f);
		
		Chunk name = new Chunk("Steven Streasick", titleFont);
		name.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, .01f, sectionFont.getColor());
		
		PdfPCell title = new PdfPCell(new Phrase(name));
		title.setBorder(PdfPCell.NO_BORDER);
		title.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		title.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		title.setPaddingBottom(6f);
		title.setPaddingLeft(0);
		title.setUseAscender(true);
		
		PdfPCell email = new PdfPCell(new Phrase("StevenStreasick@gmail.com", contactInfoFont));
		email.setBorder(PdfPCell.NO_BORDER);
		email.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		email.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		email.setPaddingBottom(6f);
		email.setPaddingLeft(0);
		email.setUseAscender(true);
		
		Anchor gitURL = new Anchor("GitHub.com/StevenStreasick", contactInfoFont);
		gitURL.setName("Git");
		gitURL.setReference("https://GitHub.com/StevenStreasick");
		
		PdfPCell git = new PdfPCell(gitURL);
		git.setBorder(PdfPCell.NO_BORDER);
		git.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		git.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		git.setPadding(0);
		git.setUseAscender(true);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage qrCodeBuffImage = createQRCode(REPOURI);
		PdfPCell qrCode = new PdfPCell(new Phrase(""));
		
		try {
			ImageIO.write(qrCodeBuffImage, "jpg", baos);
			Image qrCodeImage = Image.getInstance(baos.toByteArray());
			qrCodeImage.scalePercent(9.6f);//9
			qrCode = new PdfPCell(qrCodeImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		qrCode.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		qrCode.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
		qrCode.setBorder(PdfPCell.NO_BORDER);
		qrCode.setPaddingTop(4f);
		
		PdfPCell phoneNumber = new PdfPCell(new Phrase("231-357-5974", contactInfoFont));
		phoneNumber.setBorder(PdfPCell.NO_BORDER);
		phoneNumber.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		phoneNumber.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		phoneNumber.setPaddingTop(12f);
		phoneNumber.setPaddingRight(0f);
		phoneNumber.setPaddingBottom(5.1f);
		phoneNumber.setUseAscender(true);
		
		PdfPCell address = new PdfPCell(new Phrase("Allendale, MI 49401", contactInfoFont));
		address.setBorder(PdfPCell.NO_BORDER);
		address.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		address.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		address.setPaddingRight(0);
		address.setPaddingBottom(5.1f); 
		address.setUseAscender(true);
		
		Anchor linkedinURL = new Anchor("LinkedIn.com/in/Steven-Streasick", contactInfoFont);
		linkedinURL.setName("LinkedIn");
		linkedinURL.setReference("Https://LinkedIn.com/in/Steven-Streasick");
		
		PdfPCell linkedin = new PdfPCell(linkedinURL);
		linkedin.setBorder(PdfPCell.NO_BORDER);
		linkedin.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		linkedin.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		linkedin.setPaddingRight(0);
		linkedin.setPaddingBottom(0f);
		linkedin.setUseAscender(true);
		
		PdfPCell paddingCell = new PdfPCell(new Phrase(""));
		paddingCell.setBorder(PdfPCell.NO_BORDER);
		
		leftColumn.addCell(title);
		leftColumn.addCell(email);
		leftColumn.addCell(git);
		leftColumn.addCell(paddingCell);
		
		header.addCell(leftColumn);
		
		qrcodeColumn.addCell(qrCode);
		qrcodeColumn.addCell(paddingCell);
		header.addCell(qrcodeColumn);
		
		rightColumn.addCell(phoneNumber);
		rightColumn.addCell(address);
		rightColumn.addCell(linkedin);
		rightColumn.addCell(paddingCell);
		
		header.addCell(rightColumn);
		
		return header;
	}
	
	
	
	private PdfPTable createEducationSection() {
		PdfPTable educationSection = new PdfPTable(1);
		educationSection.setWidthPercentage(100f);
		educationSection.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		educationSection.setSpacingBefore(sectionPadding / 3);
		educationSection.setSpacingAfter(sectionPadding);
		
		PdfPTable educationTable = new PdfPTable(3);
		educationTable.setWidthPercentage(100f);
		educationTable.setWidths(new float[] {.92f, 1f, .3f});
		
		PdfPCell educationTitle = createSectionCell("Education");
		
		PdfPCell schoolCell = new PdfPCell(new Phrase("Grand Valley State University, Padnos College of Engineering and Computing | Honors College", 
				subsectionFont));
		schoolCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		schoolCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		schoolCell.setColspan(2);
		schoolCell.setBorder(PdfPCell.NO_BORDER);
		schoolCell.setPaddingLeft(0);
		schoolCell.setUseAscender(true);
		//TODO: Add more spacing between this and graduation date
		
		PdfPCell location = new PdfPCell(new Phrase("Allendale, MI", dateLocationFont));
		location.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		location.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		location.setBorder(PdfPCell.NO_BORDER);
		location.setPaddingRight(0);
		location.setUseAscender(true);
		
		PdfPCell degree = new PdfPCell(new Phrase("Masters of Science, Applied Computer Science", subsectionDescriptorFont));
		degree.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		degree.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		degree.setBorder(PdfPCell.NO_BORDER);
		degree.setPaddingLeft(0);
		degree.setUseAscender(true);
		
		PdfPCell minor = new PdfPCell(new Phrase("Minor in Mathematics", subsectionDescriptorFont));
		minor.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		minor.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		minor.setBorder(PdfPCell.NO_BORDER);
		minor.setUseAscender(true);
		
		PdfPCell date = new PdfPCell(new Phrase("December 2025", dateLocationFont));
		date.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		date.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		date.setBorder(PdfPCell.NO_BORDER);
		date.setPaddingRight(0);
		date.setUseAscender(true);
		
		PdfPCell bullet1 = createBulletCell("GPA of 3.855");
		
		PdfPCell bullet2 = createBulletCell("Dean's List: Fall 2022, Winter 2022, Fall 2023, Winter 2023");
		
		PdfPCell bullet3 = createBulletCell("Relevant Classwork: Applied AI, "
				+ "Software Engineering, Database, Structure of Programming Languages");
		
		educationTable.addCell(schoolCell);
		educationTable.addCell(location);
		educationTable.addCell(degree);
		educationTable.addCell(minor);
		educationTable.addCell(date);
		
		educationSection.addCell(educationTitle);
		educationSection.addCell(educationTable);
		educationSection.addCell(bullet1);
		educationSection.addCell(bullet2);
		educationSection.addCell(bullet3);
		
		return educationSection;
	}
	
	private PdfPTable createExperienceSection() {
		PdfPTable experience = new PdfPTable(1);
		experience.setWidthPercentage(100f);
		experience.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		experience.setSpacingAfter(sectionPadding);
		
		PdfPCell experienceTitle = createSectionCell("EXPERIENCE");
		
		PdfPTable experience1Header = createSubsectionTable("EJ Full Stack Software-Developer", "May 2024 – Now", null);
		
		PdfPCell ejSWDBullet1 = createBulletCell("Developed and optimized PDF solutions using OpenPDF in Java, streamlining processes "
				+ "in Salesforce");
		
		PdfPCell ejSWDBullet2 = createBulletCell("Identified and resolved bugs in OpenPDF, contributing pull requests to improve "
				+ "functionality");
		
		PdfPCell ejSWDBullet3 = createBulletCell("Improved Salesforce processes with custom Java solutions for better data management "
				+ "and workflow efficiency");
		
		PdfPTable experience2Header = createSubsectionTable("Freelance Programmer", "August 2015 – Now", null);
		
		PdfPCell flpBullet1 = createBulletCell("Proactively stayed at the forefront of Roblox’s ever-changing landscape, consistently "
				+ "integrating leading technologies");
		
		PdfPCell flpBullet2 = createBulletCell("Completed various Hidden Developer Contracts, showcasing integrity while fostering relations "
				+ "within the community");
		
		PdfPTable experience3Header = createSubsectionTable("Cafe Santé Head Pantry Chef", "June 2022 - August 2023", null);
		
		PdfPCell pcBullet1 = createBulletCell("Reduced overall ticket time for pantry orders by 13%");
		
		PdfPCell pcBullet2 = createBulletCell("Led six pantry cooks, ensuring clear communication and efficient operations for consistent customer experiences");
		
		ejSWDBullet3.setPaddingBottom(subsectionPadding);
		
		experience.addCell(experienceTitle);
		experience.addCell(experience1Header);
		experience.addCell(ejSWDBullet1);
		experience.addCell(ejSWDBullet2);
		experience.addCell(ejSWDBullet3);
		
		flpBullet2.setPaddingBottom(subsectionPadding);
		
		experience.addCell(experience2Header);
		experience.addCell(flpBullet1);
		experience.addCell(flpBullet2);
		
		experience.addCell(experience3Header);
		experience.addCell(pcBullet1);
		experience.addCell(pcBullet2);

		return experience;
	}
	
	private PdfPTable createProjects() {
		PdfPTable projects = new PdfPTable(1);
		projects.setWidthPercentage(100f);
		projects.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		projects.setSpacingAfter(sectionPadding);
		
		PdfPCell projectsTitle = createSectionCell("Projects");
		
		PdfPTable project1Header = createSubsectionTable("Coloring Simulator", "August 2018 - July 2022", "Roblox video game with ~450k plays");
		
		PdfPCell colSimBullet1 = createBulletCell("Designed a framework that allowed for other programmers to expand upon");
		
		PdfPCell colSimBullet2 = createBulletCell("Learned to design achievable deadlines through precise planning and coordination with several developers");
		
		
		PdfPTable project2Header = createSubsectionTable("Minesweeper", "January 2022 - April 2023", "Unpublished Roblox video game");
		
		PdfPCell msweepBullet1 = createBulletCell("Created a three dimensional version of the classic game Minesweeper using complex garbage collection techniques");
	
		PdfPCell msweepBullet2 = createBulletCell("Utilized access modifiers to control access to class data, promoting data integrity");
		
		
		PdfPTable project3Header = createSubsectionTable("Chess", "August 2022 - October 2022", "Java based school project");

		PdfPCell chessBullet1 = createBulletCell("Reduced code duplication by employing inheritance to create a hierarchy of game elements");
		
		PdfPCell chessBullet2 = createBulletCell("Implemented an AI opponent that prioritizes piece protection and strategic piece captures");
		
		
		PdfPTable project4Header = createSubsectionTable("Mutation Testing Plugin", "Jaunary 2023 - April 2023", "Roblox testing tool");
		
		
		PdfPCell mtBullet1 = createBulletCell("Assessed effectiveness of mutation testing in game development, gauging community opinion on its utility and impact");
		
		PdfPCell mtBullet2 = createBulletCell("Developed a code injection framework for consistent reference handling amid script hierarchical changes");
		
		PdfPCell mtBullet3 = createBulletCell("Employed string manipulation in Lua to simulate code mutations, optimizing bug detection and debugging");

		
		colSimBullet2.setPaddingBottom(subsectionPadding);
		msweepBullet2.setPaddingBottom(subsectionPadding);
		chessBullet2.setPaddingBottom(subsectionPadding);
		
		projects.addCell(projectsTitle);
		
		projects.addCell(project1Header);
		projects.addCell(colSimBullet1);
		projects.addCell(colSimBullet2);
		
		projects.addCell(project2Header);
		projects.addCell(msweepBullet1);
		projects.addCell(msweepBullet2);
		
		projects.addCell(project3Header);
		projects.addCell(chessBullet1);
		projects.addCell(chessBullet2);
		
		projects.addCell(project4Header);
		projects.addCell(mtBullet1);
		projects.addCell(mtBullet2);
		projects.addCell(mtBullet3);
	
		return projects;
	}
	
	private PdfPTable createSkills() {
		Font skillsFont = bulletFont; //A reference. Controls the font used within this section
		
		PdfPTable skills = new PdfPTable(1);
		skills.setWidthPercentage(100f);
		skills.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		skills.setSpacingAfter(sectionPadding);
		
		PdfPCell skillsTitle = createSectionCell("Skills");
		skillsTitle.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		skillsTitle.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		skillsTitle.setBorder(PdfPCell.NO_BORDER);
		skillsTitle.setPaddingLeft(0);
		skillsTitle.setUseAscender(true);
		
		
		String[] languagesToDisplay = {"Java","C++", "C", "Python", "Lua", "JavaScript", "SQL"};
		String[] skillsToDisplay = {"Functional Programming", "Object Oriented Programming", "Software Testing", "Never Nesting"};
		
		Phrase programmingLanguages = new Phrase("Programming Languages: ", skillsFont);
		
		for(int i = 0; i < languagesToDisplay.length; i++) {
			String language = languagesToDisplay[i];
			
			Chunk languageChunk;
			if(i + 1 < languagesToDisplay.length) {
				languageChunk = new Chunk(language + ", ", skillsFont);
			} else {
				languageChunk = new Chunk(language, skillsFont);
			}
			
			programmingLanguages.add(languageChunk);
		}
		
		Phrase programmingSkills = new Phrase("Programming Skills: ", skillsFont);
		
		for(int i = 0; i < skillsToDisplay.length; i++) {
			String skill = skillsToDisplay[i];
			
			Chunk skillChunk;
			if(i + 1 < skillsToDisplay.length) {
				skillChunk = new Chunk(skill + ", ", skillsFont);
			} else {
				skillChunk = new Chunk(skill, skillsFont);
			}
			
			programmingSkills.add(skillChunk);
		}
		
		PdfPCell languagesCell = new PdfPCell(programmingLanguages);
		languagesCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		languagesCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		languagesCell.setBorder(PdfPCell.NO_BORDER);
		languagesCell.setPaddingLeft(0);
		languagesCell.setPaddingBottom(subsectionPadding / 1.5f);
		languagesCell.setUseAscender(true);
		
		PdfPCell skillsCell = new PdfPCell(programmingSkills);
		skillsCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		skillsCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		skillsCell.setBorder(PdfPCell.NO_BORDER);
		skillsCell.setPaddingLeft(0);
		skillsCell.setUseAscender(true);
		
		skills.addCell(skillsTitle);
		
		skills.addCell(languagesCell);
		skills.addCell(skillsCell);
		
		return skills;
	}
	
	private PdfPTable createVolunteerOpportunities() {
		PdfPTable volunteerOpportunities = new PdfPTable(1);
		volunteerOpportunities.setWidthPercentage(100f);
		volunteerOpportunities.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		volunteerOpportunities.setSpacingAfter(sectionPadding);
		
		PdfPCell volunteerOpportunitiesTitle = createSectionCell("VOLUNTEER OPPORTUNITIES");
		volunteerOpportunitiesTitle.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		volunteerOpportunitiesTitle.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		volunteerOpportunitiesTitle.setBorder(PdfPCell.NO_BORDER);
		volunteerOpportunitiesTitle.setPaddingLeft(0);
		volunteerOpportunitiesTitle.setUseAscender(true);
		
		PdfPCell ejSolarSpark = new PdfPCell(new Phrase("Solar Spark", subsectionFont));
		ejSolarSpark.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		ejSolarSpark.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		ejSolarSpark.setBorder(PdfPCell.NO_BORDER);
		ejSolarSpark.setPaddingLeft(0);
		ejSolarSpark.setUseAscender(true);
		
		PdfPCell ssBullet1 = createBulletCell("Mentored 30 Middle School students to achieve their goal of raising $70,000 for solar panels");
		
		PdfPCell ssBullet2 = createBulletCell("Established and sustained a revered, annual tradition");
		
		PdfPCell ssBullet3 = createBulletCell("Produced a promotional video to aid in the marketing of the project");
	
		volunteerOpportunities.addCell(volunteerOpportunitiesTitle);
		
		volunteerOpportunities.addCell(ejSolarSpark);
		volunteerOpportunities.addCell(ssBullet1);
		volunteerOpportunities.addCell(ssBullet2);
		volunteerOpportunities.addCell(ssBullet3);
		
		return volunteerOpportunities;
	}
	
	public ByteArrayInputStream createResume() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		createFonts();

		Document doc = new Document(PageSize.A4, 34, 34, 38, 38);
		PdfWriter writer = PdfWriter.getInstance(doc, baos);

		doc.open();
		
		PdfPTable header = createHeader();

		doc.add(header);
				
		//I think when it adds it to the doc, it calculates the height, so it is no longer the first time
		float headerHeight = header.calculateHeights(false);

		PdfContentByte cb = writer.getDirectContent();		
		LineSeparator horzLine = new LineSeparator();
		horzLine.drawLine(cb, 
				doc.left(),  
				doc.right(), 
				doc.top() - headerHeight);
		
		doc.add(createEducationSection());

		doc.add(createExperienceSection());
		
		doc.add(createProjects());
		
		doc.add(createSkills());
		
		doc.add(createVolunteerOpportunities());
		
		doc.close();
		
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
